package at.ac.hcw.porty.listeners;

import at.ac.hcw.porty.types.PortScanResult;
import at.ac.hcw.porty.types.ScanConfig;
import at.ac.hcw.porty.types.ScanSummary;
import at.ac.hcw.porty.types.interfaces.PortScanListener;

/**
 * This is an extremely simple listener to the events that the scanner emits
 * by using this listener we see in the console what is happening
 */
public class PortScanCLIListener implements PortScanListener {
    @Override public void onStarted(ScanConfig config) { System.out.println("Started: " + config.host().address()); }
    @Override public void onResult(PortScanResult result) { System.out.println("Result: " + result.port() + " -> " + result.status()); }
    @Override public void onComplete(ScanSummary summary) { System.out.println("Completed: " + summary.results().size() + " ports"); }
    @Override public void onError(Throwable t) { System.out.println("Error: " + t); }
    // @Override public void onProgress(String msg) { System.out.println("Progress: " + msg); }
}
