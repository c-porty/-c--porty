package at.ac.hcw.porty.listeners;

import at.ac.hcw.porty.types.records.PortScanResult;
import at.ac.hcw.porty.types.records.ScanConfig;
import at.ac.hcw.porty.types.records.ScanSummary;
import at.ac.hcw.porty.types.interfaces.PortScanListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is an extremely simple listener to the events that the scanner emits
 * by using this listener we see in the console what is happening
 */
public class PortScanCLIListener implements PortScanListener {
    private static final Logger logger =
            LoggerFactory.getLogger(PortScanCLIListener.class);

    @Override public void onStarted(ScanConfig config) { logger.info("Started: {}", config.host().address()); }
    @Override public void onResult(PortScanResult result) { logger.info("Result: {}:{} -> {}", result.host().address(), result.port(), result.status()); }
    @Override public void onComplete(ScanSummary summary) {
        logger.info("Completed: {} ports", summary.results().size());
        logger.info("Detailed information on host {}: ", summary.host().address());
        for (PortScanResult result : summary.results()) {
            logger.info(result.toString());
        }
    }
    @Override public void onError(Throwable t) { logger.info("Error: {}", String.valueOf(t)); }
    @Override public void onProgress(String msg) { logger.info("Progress: {}", msg); }
    @Override public void onCancel() { logger.info("Task cancelled."); }
}
