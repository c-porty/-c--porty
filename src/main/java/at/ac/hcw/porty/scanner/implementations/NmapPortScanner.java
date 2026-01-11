package at.ac.hcw.porty.scanner.implementations;

import at.ac.hcw.porty.types.records.PortScanResult;
import at.ac.hcw.porty.types.PortScanner;
import at.ac.hcw.porty.types.records.ScanConfig;
import at.ac.hcw.porty.types.records.ScanSummary;
import at.ac.hcw.porty.types.interfaces.PortScanListener;
import at.ac.hcw.porty.types.interfaces.ScanHandle;
import at.ac.hcw.porty.utils.NmapCommandBuilder;
import at.ac.hcw.porty.utils.NmapXMLParser;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.*;
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
        final ExecutorService executor = Executors.newFixedThreadPool(3);

        // Create temporary output paths
        TempFiles tmp;
        try {
            tmp = createTempOutputPaths();
        } catch (IOException e) {
            notifyError(e);
            cf.completeExceptionally(e);
            return emptyHandle();
        }

        // Build and start Nmap process
        final Process process;
        try {
            process = startProcess(config, tmp.outputFile);
        } catch (Exception e) {
            notifyError(e);
            cf.completeExceptionally(e);
            return emptyHandle();
        }

        // Start stream handlers (stdout for progress, stderr for errors)
        startStreamHandlers(process, executor);

        // Worker task: wait for process, parse results, complete future
        executor.execute(() -> {
            try {
                int exit = process.waitFor();
                if (exit != 0) {
                    Exception ex = new RuntimeException("Nmap exited with code " + exit);
                    notifyError(ex);
                    cf.completeExceptionally(ex);
                    return;
                }

                List<PortScanResult> parsed = parseResults(tmp.outputFile, config);
                String detectedOs = detectOs(parsed);

                for (PortScanResult r : parsed) {
                    results.add(r);
                    notifyResult(r);
                }

                ScanSummary summary = new ScanSummary(
                        config.host(),
                        List.copyOf(results),
                        detectedOs,
                        config,
                        started,
                        Instant.now()
                );
                notifyComplete(summary);
                cf.complete(summary);

            } catch (CancellationException e) {
                if (cancelled.get()) {
                    listeners.forEach(PortScanListener::onCancel);
                } else {
                    notifyError(e);
                    cf.completeExceptionally(e);
                }
            } catch (Exception e) {
                notifyError(e);
                if (!cf.isDone()) cf.completeExceptionally(e);
            } finally {
                cleanup(tmp);
                executor.shutdown();
            }
        });

        // Persist summary when completed (and requested)
        cf.whenComplete((summary, throwable) -> {
            try {
                if (summary != null && summary.config().options().saveScan()) {
                    this.scanResultRepositoryHandler.save(summary);
                }
            } finally {
                executor.shutdown();
            }
        });

        return new ScanHandle() {
            @Override
            public void cancel() {
                cancelled.set(true);
                process.destroy();
                try { Thread.sleep(100); } catch (InterruptedException ignored) {}
                if (process.isAlive()) process.destroyForcibly();
                cf.completeExceptionally(new CancellationException("Nmap scan cancelled"));
                executor.shutdownNow();
            }

            @Override
            public CompletableFuture<ScanSummary> summary() {
                return cf;
            }
        };
    }

    private TempFiles createTempOutputPaths() throws IOException {
        Path dir = Files.createTempDirectory("porty-temporary");
        Path file = dir.resolve("porty-nmap-output.xml");
        return new TempFiles(dir, file);
    }

    private Process startProcess(ScanConfig config, Path outputFile) throws IOException {
        ProcessBuilder builder = NmapCommandBuilder.buildNmapCommand(config, this.NMAP_PATH, outputFile);
        Process process = builder.start();
        process.getOutputStream().close(); // We do not send input to Nmap, close stdin
        return process;
    }

    private void startStreamHandlers(Process process, ExecutorService executor) {
        InputStream stdout = process.getInputStream();
        InputStream stderr = process.getErrorStream();
        executor.execute(() -> handleStream(stdout, true));
        executor.execute(() -> handleStream(stderr, false));
    }

    private List<PortScanResult> parseResults(Path outputFile, ScanConfig config) throws Exception {
        try (InputStream in = Files.newInputStream(outputFile)) {
            return new NmapXMLParser().parse(in, config);
        }
    }

    private String detectOs(List<PortScanResult> parsed) {
        return parsed.stream()
                .map(PortScanResult::os)
                .filter(s -> s != null && !s.isEmpty())
                .findFirst()
                .orElse("");
    }

    private void cleanup(TempFiles tmp) {
        try { Files.deleteIfExists(tmp.outputFile); } catch (IOException ignored) {}
        try { Files.deleteIfExists(tmp.dir); } catch (IOException ignored) {}
    }

    private void notifyError(Exception e) {
        for (PortScanListener listener : listeners) { listener.onError(e); }
    }

    private void notifyProgress(String line) {
        for (PortScanListener listener : listeners) { listener.onProgress(line); }
    }

    private void notifyResult(PortScanResult r) {
        for (PortScanListener listener : listeners) { listener.onResult(r); }
    }

    private void notifyComplete(ScanSummary summary) {
        for (PortScanListener listener : listeners) { listener.onComplete(summary); }
    }

    private record TempFiles(Path dir, Path outputFile) { }

    private ScanHandle emptyHandle() {
        return new ScanHandle() {
            @Override public void cancel() {}
            @Override public CompletableFuture<ScanSummary> summary() { return cf; }
        };
    }

    private void handleStream(InputStream stream, boolean progress) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.isBlank()) continue;
                if (progress) {
                    notifyProgress(line);
                } else {
                    notifyError(new RuntimeException(line));
                }
            }
        } catch (Exception e) {
            notifyError(e);
        }
    }
}