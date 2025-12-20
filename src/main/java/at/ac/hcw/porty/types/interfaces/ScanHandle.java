package at.ac.hcw.porty.types.interfaces;

import at.ac.hcw.porty.types.ScanSummary;

import java.util.concurrent.CompletableFuture;

public interface ScanHandle {
    void cancel();
    CompletableFuture<ScanSummary> summary();
}
