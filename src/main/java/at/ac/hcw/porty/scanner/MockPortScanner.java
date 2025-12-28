package at.ac.hcw.porty.scanner;

import at.ac.hcw.porty.types.PortScanResult;
import at.ac.hcw.porty.types.PortStatus;
import at.ac.hcw.porty.types.ScanConfig;
import at.ac.hcw.porty.types.ScanSummary;
import at.ac.hcw.porty.types.interfaces.PortScanListener;
import at.ac.hcw.porty.types.interfaces.PortScanner;
import at.ac.hcw.porty.types.interfaces.ScanHandle;

import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadLocalRandom;

public final class MockPortScanner implements PortScanner {
    private final Map<Integer, PortStatus> predefined;

    public MockPortScanner(Map<Integer, PortStatus> predefined) {
        this.predefined = predefined;
    }

    @Override
    public String name() {
        return "MockPortScanner";
    }

    @Override
    public ScanHandle scan(ScanConfig config, PortScanListener[] listeners) {
        for (PortScanListener listener : listeners) {
            listener.onStarted(config);
        }
        Instant started = Instant.now();
        List<PortScanResult> results = Collections.synchronizedList(new ArrayList<>());
        CompletableFuture<ScanSummary> cf = new CompletableFuture<>();

        // einfach simuliert asynchron damit die UI korrekt implementiert werden kann
        Thread worker = new Thread(() -> {
            try {
                int[] ports = config.range().stream().toArray();

                for (int port : ports) {
                    if (Thread.currentThread().isInterrupted()) throw new CancellationException();

                    // now try to actually simulate the delay
                    long delay = ThreadLocalRandom.current().nextLong(50, 200);
                    try { Thread.sleep(delay); } catch (InterruptedException e) { throw new CancellationException(); }

                    PortStatus status = predefined.getOrDefault(port, switch (port % 4) {
                        case 0 -> PortStatus.OPEN;
                        case 2 -> PortStatus.FILTERED;
                        default -> PortStatus.CLOSED;
                    });

                    String note = status == PortStatus.OPEN ? String.format("service: mock-%d", port) : "";
                    PortScanResult result = new PortScanResult(config.host(), port, status, Duration.ofMillis(delay), note);

                    results.add(result);
                    for (PortScanListener listener : listeners) {
                        listener.onResult(result);
                        listener.onProgress("Scanned port: " + port);
                    }
                }
                results.sort(Comparator.comparingInt(PortScanResult::port));
                ScanSummary summary = new ScanSummary(config.host(), List.copyOf(results), started, Instant.now());
                for (PortScanListener listener : listeners) {
                    listener.onComplete(summary);
                }
                cf.complete(summary);
            } catch (Exception e) {
                for (PortScanListener listener : listeners) {
                    listener.onError(e);
                }
                cf.completeExceptionally(e);
            }
        }, "mock-port-scanner");

        worker.setDaemon(true);
        worker.start();

        return new ScanHandle() {
            @Override
            public void cancel() { worker.interrupt(); }
            @Override
            public CompletableFuture<ScanSummary> summary() { return cf; }
        };
    }
}
