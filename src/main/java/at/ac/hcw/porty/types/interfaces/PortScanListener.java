package at.ac.hcw.porty.types.interfaces;

import at.ac.hcw.porty.types.records.PortScanResult;
import at.ac.hcw.porty.types.records.ScanConfig;
import at.ac.hcw.porty.types.records.ScanSummary;

public interface PortScanListener {
    void onStarted(ScanConfig config);
    void onResult(PortScanResult result);
    void onComplete(ScanSummary summary);
    void onError(Throwable t);
    void onCancel();

    default void onProgress(String message) {}
}
