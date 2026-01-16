package at.ac.hcw.porty.controller;

import at.ac.hcw.porty.types.records.PortScanResult;
import at.ac.hcw.porty.types.records.ScanSummary;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.lang.ref.Reference;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class ResultsController {
    @FXML
    private Label dateTimeLabel;
    @FXML
    private GridPane resultGrid;
    @FXML
    private Region redBar;
    @FXML
    private Region greenBar;

    private MainController mainController;
    private ScanSummary scanSummary;

    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }

    public void setScanSummary(ScanSummary scanSummary) {
        this.scanSummary = scanSummary;

        Instant start = scanSummary.startedAt();
        ZonedDateTime zdt = start.atZone(ZoneId.systemDefault()); // in lokale Zeit konvertieren
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy, HH:mm");

        dateTimeLabel.setText(zdt.format(formatter));

        displayScanSummary();

        // Risiko-Balken aktualisieren, nachdem Layout fertig ist
        Platform.runLater(() -> updateRiskBar(scanSummary.severity()));
    }

    @FXML
    private void navigateBack() {
        mainController.traceBackNavigation();
    }

    public void displayScanSummary() {
        int row = 0;

        addRow(row++, "IP-Address", scanSummary.host().address());
        addRow(row++, "Time taken", scanSummary.finishedAt().getEpochSecond() - scanSummary.startedAt().getEpochSecond() + "s");
        if(!scanSummary.detectedOs().isEmpty()) {
            addRow(row++, "Operating system", scanSummary.detectedOs());
        }
        addRow(row++, "Open ports", String.valueOf(scanSummary.results().size()));

        int i = 1;
        for (PortScanResult port : scanSummary.results()) {
            addRow(row++, "Port #" + i, port.port() + " " + (!port.service().isEmpty() ? "(" + port.service() + ")" : ""));
            i++;
        }

        addRow(row, "Average security risk", getRiskLabel(scanSummary.severity())
        );
    }

    private String getRiskLabel(float severity) {
        if (severity < 0.33f) {
            return "Low";
        } else if (severity < 0.66f) {
            return "Medium";
        } else {
            return "High";
        }
    }

    private void addRow(int row, String leftText, String rightText) {
        Label left = new Label(leftText);
        Label right = new Label(rightText);

        String rowStyle = (row % 2 == 0) ? "porty-results-row-even" : "porty-results-row-odd";
        left.getStyleClass().add(rowStyle);
        right.getStyleClass().add(rowStyle);
        right.getStyleClass().add("porty-result-border");

        if (row == 0) {
            left.setStyle("-fx-background-radius: 5 0 0 0; -fx-border-radius: 5 0 0 0;");
            right.setStyle("-fx-background-radius: 0 5 0 0; -fx-border-radius: 0 5 0 0;");
        }

        int lastRow = 3 + scanSummary.results().size();
        if(!scanSummary.detectedOs().isEmpty()) {lastRow++;}


        if (row == lastRow) {
            left.setStyle(left.getStyle() + "-fx-background-radius: 0 0 0 5; -fx-border-radius: 0 0 0 5;");
            right.setStyle(right.getStyle() + "-fx-background-radius: 0 0 5 0; -fx-border-radius: 0 0 5 0;");
        }

        resultGrid.addRow(row, left, right);
    }

    private void updateRiskBar(float severity) {
        double maxWidth = redBar.getWidth();
        double newWidth = (1 - severity) * maxWidth;

        if (newWidth < 0) newWidth = 0;
        if (newWidth > maxWidth) newWidth = maxWidth;

        greenBar.setPrefWidth(newWidth);
    }
}
