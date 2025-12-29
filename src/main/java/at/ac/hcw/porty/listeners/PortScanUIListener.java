package at.ac.hcw.porty.listeners;

import at.ac.hcw.porty.types.PortScanResult;
import at.ac.hcw.porty.types.ScanConfig;
import at.ac.hcw.porty.types.ScanSummary;
import at.ac.hcw.porty.types.interfaces.PortScanListener;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.scene.control.TextArea;

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
}
