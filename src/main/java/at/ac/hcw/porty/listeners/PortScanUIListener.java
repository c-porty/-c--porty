package at.ac.hcw.porty.listeners;

import at.ac.hcw.porty.controller.DashboardController;
import at.ac.hcw.porty.controller.MainController;
import at.ac.hcw.porty.types.records.PortScanResult;
import at.ac.hcw.porty.types.records.ScanConfig;
import at.ac.hcw.porty.types.records.ScanSummary;
import at.ac.hcw.porty.types.interfaces.PortScanListener;
import at.ac.hcw.porty.utils.AlertManager;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;

import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PortScanUIListener implements PortScanListener {
    private final ObservableList<String> outputTextList;
    private final MainController mainController;
    private final DashboardController dashboardController;

    public PortScanUIListener(ObservableList<String> list, MainController mainController, DashboardController dashboardController) {
        this.outputTextList = list;
        this.mainController = mainController;
        this.dashboardController = dashboardController;
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
            Alert alert = AlertManager.createAlert(
                    Alert.AlertType.CONFIRMATION,
                    "Scan successful",
                    "Short summary",
                    List.of(
                        "Host:",
                        summary.host().address(),
                        "Open ports:",
                        String.valueOf(summary.results().size()),
                        "Time taken:",
                        String.format("%d s", summary.finishedAt().getEpochSecond()
                            - summary.startedAt().getEpochSecond())
                    )
            );
            ButtonType moreButton = new ButtonType("More", ButtonBar.ButtonData.OK_DONE);
            ButtonType closeButton = new ButtonType("Close", ButtonBar.ButtonData.CANCEL_CLOSE);

            alert.getButtonTypes().setAll(moreButton, closeButton);
            Optional<ButtonType> result = alert.showAndWait();

            if (result.isPresent() && result.get() == moreButton) {
                mainController.navigateToResults(summary);
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

            Pattern pattern = Pattern.compile("(\\d+(?:\\.\\d+)?)%");
            Matcher matcher = pattern.matcher(msg);

            if ((msg.contains("Connect Scan Timing") || msg.contains("SYN Stealth Scan Timing"))&& matcher.find()) {
                double percent = Double.parseDouble(matcher.group(1));
                dashboardController.setProgress(percent);
            }

            if(msg.contains("Nmap done")){
                dashboardController.setProgress(100.0);
            }
        });
    }

    @Override public void onCancel() {
        Platform.runLater(() -> {
            outputTextList.add("Task cancelled");
        });
    }
}
