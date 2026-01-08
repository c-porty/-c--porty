package at.ac.hcw.porty.types;

import at.ac.hcw.porty.repositories.ScanResultRepositoryFactory;
import at.ac.hcw.porty.repositories.ScanResultRepositoryHandler;
import at.ac.hcw.porty.types.enums.ScanResultRepositoryOption;
import at.ac.hcw.porty.types.interfaces.IPortScanner;
import at.ac.hcw.porty.types.interfaces.PortScanListener;
import at.ac.hcw.porty.types.records.PortScanResult;
import at.ac.hcw.porty.types.records.ScanConfig;
import at.ac.hcw.porty.types.records.ScanSummary;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public abstract class PortScanner implements IPortScanner {
    protected List<PortScanListener> listeners;
    protected Instant started;
    protected List<PortScanResult> results;
    protected CompletableFuture<ScanSummary> cf;
    protected ScanResultRepositoryHandler scanResultRepositoryHandler =
            new ScanResultRepositoryHandler(ScanResultRepositoryFactory.create(ScanResultRepositoryOption.JSON));

    protected void setupScan(ScanConfig config, PortScanListener[] passedListeners) {
        listeners = List.of(passedListeners);
        for (PortScanListener listener : listeners) {
            listener.onStarted(config);     // tells all listeners that the scan now starts
        }
        started = Instant.now();
        results = Collections.synchronizedList(new ArrayList<>());
        cf = new CompletableFuture<>();
    }
}
