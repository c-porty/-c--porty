package at.ac.hcw.porty.scanner.implementations;

import at.ac.hcw.porty.types.records.PortScanResult;
import at.ac.hcw.porty.types.PortScanner;
import at.ac.hcw.porty.types.records.ScanConfig;
import at.ac.hcw.porty.types.records.ScanSummary;
import at.ac.hcw.porty.types.interfaces.PortScanListener;
import at.ac.hcw.porty.types.interfaces.ScanHandle;
import at.ac.hcw.porty.utils.CommandBuilder;
import at.ac.hcw.porty.utils.NmapXMLParser;

import java.io.*;
import java.nio.file.Files;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

public class NmapPortScanner extends PortScanner {
    private final String NMAP_PATH;

    public NmapPortScanner(String path) {
        super();
        this.NMAP_PATH = path;
    }

    @Override
    public String name() {
        return "NmapPortScanner";
    }

    @Override
    public ScanHandle scan(ScanConfig config, PortScanListener[] passedListeners) {
        super.setupScan(config, passedListeners);
        final AtomicBoolean cancelled = new AtomicBoolean(false);

        // try to create the temp file we need for the scan output
        File tempOutputFile;
        try {
            tempOutputFile = File.createTempFile("porty-", "-output");
            tempOutputFile.deleteOnExit();
        } catch (IOException e) {
            for (PortScanListener listener : listeners) {
                listener.onError(e);
            }
            cf.completeExceptionally(e);
            return new ScanHandle() {
                @Override public void cancel() {}
                @Override public CompletableFuture<ScanSummary> summary() { return cf; }
            };
        }

        // the command that the process uses
        ProcessBuilder builder = CommandBuilder.buildNmapCommand(config, this.NMAP_PATH, tempOutputFile);

        // try to start the built command
        final Process process;
        try {
            process = builder.start();
            // we do not send input to Nmap so close it
            process.getOutputStream().close();
        } catch (Exception e) {
            for (PortScanListener listener : listeners) {
                listener.onError(e);
            }
            cf.completeExceptionally(e);
            return new ScanHandle() {
                @Override public void cancel() { }
                @Override public CompletableFuture<ScanSummary> summary() { return cf; }
            };
        }
        // the thread pool for scanning, reading stdout, reading stderr
        ExecutorService threadsExecutor = Executors.newFixedThreadPool(3);

        // handle both stdout for stats and stderr for actual errors
        InputStream stdout = process.getInputStream();
        InputStream stderr = process.getErrorStream();
        threadsExecutor.execute(() -> handleStream(stdout, listeners, true));
        threadsExecutor.execute(() -> handleStream(stderr, listeners, false));

        // this is the actual scanner thread
        threadsExecutor.execute(() -> {
            try {
                int exit = process.waitFor();   // ensure file is written
                if (exit != 0) {
                    Exception ex = new RuntimeException("Nmap exited with code " + exit);
                    for (PortScanListener listener : listeners) {
                        listener.onError(ex);
                    }
                    cf.completeExceptionally(ex);
                    return;
                }

                try (InputStream fileInputStream = Files.newInputStream(tempOutputFile.toPath())) {
                    NmapXMLParser parser = new NmapXMLParser();
                    List<PortScanResult> parsedResults = parser.parse(fileInputStream, config);
                    for (PortScanResult r : parsedResults) {
                        results.add(r);
                        for (PortScanListener listener : listeners) {
                            listener.onResult(r);
                        }
                    }

                    ScanSummary summary = new ScanSummary(config.host(), List.copyOf(results), started, Instant.now());
                    for (PortScanListener listener : listeners) {
                        listener.onComplete(summary);
                    }
                    cf.complete(summary);
                }
            } catch (IOException | CancellationException e) {
                // if cancelled is true we know that the user cancelled
                if (cancelled.get()) {
                    for (PortScanListener listener : listeners) {
                        listener.onCancel();
                    }
                } else {
                    for (PortScanListener listener : listeners) {
                        listener.onError(e);
                    }
                    cf.completeExceptionally(e);
                }
            } catch (Exception e) {
                for (PortScanListener listener : listeners) {
                    listener.onError(e);
                }
                if (!cf.isDone()) cf.completeExceptionally(e);
            } finally {
                try { Files.deleteIfExists(tempOutputFile.toPath()); } catch (IOException ignored) {};  // delete file
                threadsExecutor.shutdown(); // if this thread finishes, all threads must close
            }
        });

        // always shut down all worker threads when cf is finished
        cf.whenComplete((s, t) -> threadsExecutor.shutdown());

        return new ScanHandle() {
            @Override
            public void cancel() {
                cancelled.set(true);
                process.destroy();
                try { Thread.sleep(100); } catch (InterruptedException ignored) {}
                if (process.isAlive()) process.destroyForcibly();
                cf.completeExceptionally(new CancellationException("Nmap scan cancelled"));
                threadsExecutor.shutdownNow();
            }

            @Override public CompletableFuture<ScanSummary> summary() { return cf;}
        };
    }

    private static void handleStream(InputStream stream, List<PortScanListener> listeners, boolean progress) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.isBlank()) {
                    for (PortScanListener listener : listeners) {
                        if (progress) {
                            listener.onProgress(line);
                        } else {
                            listener.onError(new RuntimeException(line));
                        }
                    }
                }
            }
        } catch (Exception ignored) {}
    }
}
