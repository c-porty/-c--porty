package at.ac.hcw.porty.listeners;

import at.ac.hcw.porty.controller.MainController;
import at.ac.hcw.porty.types.records.PortScanResult;
import at.ac.hcw.porty.types.records.ScanConfig;
import at.ac.hcw.porty.types.records.ScanSummary;
import at.ac.hcw.porty.types.interfaces.PortScanListener;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;

import java.util.Optional;

public class PortScanUIListener implements PortScanListener {
    private final ObservableList<String> outputTextList;
    private final MainController mainController;

    public PortScanUIListener(ObservableList<String> list, MainController mainController) {
        this.outputTextList = list;
        this.mainController = mainController;
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
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Scan Erfolgreich");
            alert.setHeaderText("Kurzzusammenfassung");

            GridPane grid = new GridPane();
            grid.setHgap(10);
            grid.setVgap(6);
            grid.setPadding(new Insets(10));

            Label hostLabel = new Label("Host:");
            hostLabel.getStyleClass().add("alert-title");

            Label hostText = new Label(summary.host().address());
            hostText.getStyleClass().add("alert-text");

            Label portsLabel = new Label("Open ports:");
            portsLabel.getStyleClass().add("alert-title");

            Label portsText = new Label(String.valueOf(summary.results().size()));
            portsText.getStyleClass().add("alert-text");

            Label timeLabel = new Label("Time taken:");
            timeLabel.getStyleClass().add("alert-title");

            Label timeText = new Label(
                    (summary.finishedAt().getEpochSecond() - summary.startedAt().getEpochSecond()) + " s"
            );
            timeText.getStyleClass().add("alert-text");

            grid.addRow(0, hostLabel, hostText);
            grid.addRow(1, portsLabel, portsText);
            grid.addRow(2, timeLabel, timeText);

            alert.getDialogPane().setContent(grid);
            alert.getDialogPane().setPrefSize(500, 250);
            alert.getDialogPane().getStylesheets()
                    .add(getClass().getResource("/at/ac/hcw/porty/styles/styles.css").toExternalForm());

            ButtonType moreButton = new ButtonType("More", ButtonBar.ButtonData.NEXT_FORWARD);
            ButtonType closeButton = new ButtonType("Close", ButtonBar.ButtonData.CANCEL_CLOSE);

            alert.getButtonTypes().setAll(moreButton, closeButton);

            Optional<ButtonType> result = alert.showAndWait();

            if (result.isPresent() && result.get() == moreButton) {
                mainController.navigateToResults();
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
