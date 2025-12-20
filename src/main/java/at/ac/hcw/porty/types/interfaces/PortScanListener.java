package at.ac.hcw.porty.types.interfaces;

import at.ac.hcw.porty.types.PortScanResult;
import at.ac.hcw.porty.types.ScanConfig;
import at.ac.hcw.porty.types.ScanSummary;

public interface PortScanListener {
    void onStarted(ScanConfig config);
    void onResult(PortScanResult result);
    void onComplete(ScanSummary summary);
    void onError(Throwable t);

    default void onProgress(String message) {}
}
