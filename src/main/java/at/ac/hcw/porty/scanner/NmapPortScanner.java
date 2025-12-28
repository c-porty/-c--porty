package at.ac.hcw.porty.scanner;

import at.ac.hcw.porty.types.PortScanResult;
import at.ac.hcw.porty.types.ScanConfig;
import at.ac.hcw.porty.types.ScanSummary;
import at.ac.hcw.porty.types.interfaces.PortScanListener;
import at.ac.hcw.porty.types.interfaces.PortScanner;
import at.ac.hcw.porty.types.interfaces.ScanHandle;
import at.ac.hcw.porty.utils.CommandBuilder;
import at.ac.hcw.porty.utils.NmapXMLParser;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;

public class NmapPortScanner implements PortScanner {
    private final String NMAP_PATH;

    public NmapPortScanner(String path) {
        this.NMAP_PATH = path;
    }

    @Override
    public String name() {
        return "NmapPortScanner";
    }

    @Override
    public ScanHandle scan(ScanConfig config, PortScanListener[] listeners) {
        for (PortScanListener listener : listeners) {
            listener.onStarted(config);     // tells all listeners that the scan now starts
        }
        Instant started = Instant.now();
        List<PortScanResult> results = Collections.synchronizedList(new ArrayList<>());
        CompletableFuture<ScanSummary> cf = new CompletableFuture<>();
        ProcessBuilder builder = CommandBuilder.buildNmapCommand(config, this.NMAP_PATH);

        // try to start the built command
        final Process process;
        try {
            process = builder.start();
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
        // the stderr thread to parse the verbose output of nmap without blocking the actual scanning
        Thread progressReader = getListenerThread(listeners, process);
        progressReader.start();

        // this is the actual scanner thread
        Thread worker = new Thread(() -> {
            try (InputStream xmlIn = process.getInputStream()) {
                NmapXMLParser parser = new NmapXMLParser();
                List<PortScanResult> parsedResults = parser.parse(xmlIn, config);
                int exit = process.waitFor();
                if (exit != 0) {
                    Exception ex = new RuntimeException("nmap exited with code " + exit);
                    for (PortScanListener listener : listeners) {
                        listener.onError(ex);
                    }
                    cf.completeExceptionally(ex);
                    return;
                }

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
            } catch (CancellationException ce) {
                for (PortScanListener listener : listeners) {
                    listener.onError(ce);
                }
                cf.completeExceptionally(ce);
            } catch (Exception e) {
                for (PortScanListener listener : listeners) {
                    listener.onError(e);
                }
                cf.completeExceptionally(e);
            }
        }, "nmap-scanner");
        worker.setDaemon(true);
        worker.start();

        return new ScanHandle() {
            @Override
            public void cancel() {
                process.destroy();
                try { Thread.sleep(100); } catch (InterruptedException ignored) {}
                if (process.isAlive()) process.destroyForcibly();
                cf.completeExceptionally(new CancellationException("Nmap scan cancelled"));
            }

            @Override public CompletableFuture<ScanSummary> summary() { return cf;}
        };
    }

    private static Thread getListenerThread(PortScanListener[] listeners, Process process) {
        Thread progressReader = new Thread(() -> {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getErrorStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (!line.isBlank()) {
                        for (PortScanListener listener : listeners) {
                            listener.onProgress(line);
                        }
                    }
                }
            } catch (Exception ignored) {}
        }, "nmap-progress-listener");
        progressReader.setDaemon(true);
        return progressReader;
    }
}
