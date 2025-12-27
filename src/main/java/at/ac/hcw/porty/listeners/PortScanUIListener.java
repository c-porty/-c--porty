package at.ac.hcw.porty.listeners;

import at.ac.hcw.porty.types.PortScanResult;
import at.ac.hcw.porty.types.ScanConfig;
import at.ac.hcw.porty.types.ScanSummary;
import at.ac.hcw.porty.types.interfaces.PortScanListener;
import javafx.scene.control.TextArea;

public class PortScanUIListener implements PortScanListener {
    private final TextArea outputTextArea;

    public PortScanUIListener(TextArea output){
        this.outputTextArea = output;
    }

    @Override public void onStarted(ScanConfig config) {
        outputTextArea.clear();
        outputTextArea.appendText("Started: " + config.host().address()+ "\n");
    }
    @Override public void onResult(PortScanResult result) { outputTextArea.appendText("Result: " +result.host().address() +":"+ result.port()+ " -> " +  result.status()+ "\n"); }
    @Override public void onComplete(ScanSummary summary) {
        outputTextArea.appendText("Completed: "+summary.results().size()+" ports\n");
    }
    @Override public void onError(Throwable t) { outputTextArea.appendText("Error: "+ t + "\n"); }
    @Override public void onProgress(String msg) { outputTextArea.appendText("Progress: "+ msg + "\n"); }
}
