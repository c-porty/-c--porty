package at.ac.hcw.porty.listeners;

import at.ac.hcw.porty.types.records.PortScanResult;
import at.ac.hcw.porty.types.records.ScanConfig;
import at.ac.hcw.porty.types.records.ScanSummary;
import at.ac.hcw.porty.types.interfaces.PortScanListener;
import javafx.application.Platform;
import javafx.collections.ObservableList;

public class PortScanUIListener implements PortScanListener {
    private final ObservableList<String> outputTextList;

    public PortScanUIListener(ObservableList<String> list){
        this.outputTextList = list;
    }

    @Override public void onStarted(ScanConfig config) {
        Platform.runLater(() -> {
            outputTextList.clear();
            outputTextList.add("Started: " + config.host().address());
        });
    }

    @Override public void onResult(PortScanResult result) {
        Platform.runLater(() -> {
            outputTextList.add("Result: " + result.host().address() + ":" + result.port() + " -> " + result.status());
        });
    }

    @Override public void onComplete(ScanSummary summary) {
        Platform.runLater(() -> {
            outputTextList.add("Completed: " + summary.results().size() + " ports");
            outputTextList.add(String.format("Detailed information on host %s: ", summary.host().address()));
            for (PortScanResult result : summary.results()) {
                outputTextList.add(result.toString());
            }
        });
    }

    @Override public void onError(Throwable t) {
        Platform.runLater(() -> {
            outputTextList.add("Error: " + t);
        });
    }

    @Override public void onProgress(String msg) {
        Platform.runLater(() -> {
            outputTextList.add("Progress: " + msg);
        });
    }

    @Override public void onCancel() {
        Platform.runLater(() -> {
            outputTextList.add("Task cancelled");
        });
    }
}
