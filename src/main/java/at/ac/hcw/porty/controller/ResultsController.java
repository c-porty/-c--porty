package at.ac.hcw.porty.controller;

import at.ac.hcw.porty.dto.ScanResultDTO;
import at.ac.hcw.porty.types.records.PortScanResult;
import at.ac.hcw.porty.types.records.ScanSummary;
import at.ac.hcw.porty.types.records.TechnicalReference;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.*;

import java.awt.*;
import java.lang.ref.Reference;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import javafx.scene.control.Button;

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

    int row = 0;

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

        Platform.runLater(() -> updateRiskBar(scanSummary.severity()));
    }

    @FXML
    private void navigateBack() {
        mainController.traceBackNavigation();
    }

    public void displayScanSummary() {

        ArrayList<ScanResultDTO> scanOverview = new ArrayList<>();
        scanOverview.add(new ScanResultDTO( "Scanned Address", scanSummary.host().address()));
        scanOverview.add(new ScanResultDTO("Time taken", scanSummary.finishedAt().getEpochSecond() - scanSummary.startedAt().getEpochSecond() + "s"));
        if(!scanSummary.detectedOs().isEmpty()) {
            scanOverview.add(new ScanResultDTO( "Operating system", scanSummary.detectedOs()));
        }
        scanOverview.add(new ScanResultDTO( "Open ports", String.valueOf(scanSummary.results().size())));
        if(scanSummary.host().subnet()==null) {
            int i = 1;
            for (PortScanResult port : scanSummary.results()) {
                scanOverview.add(new ScanResultDTO( "Port #" + i,
                        port.port() + (!port.service().isEmpty() ? " (" + port.service() + ")" : ""),
                        true,
                        port.technicalReference()));
                i++;
            }
        }
        scanOverview.add(new ScanResultDTO( "Average security risk", getRiskLabel(scanSummary.severity())));

        addBlock(scanOverview);

        if (!(scanSummary.host().subnet()==null)) {
            addEmptyRow();

            Set<String> hostsInNetwork = new HashSet<>();
            for (PortScanResult port : scanSummary.results()) {
                hostsInNetwork.add(port.host().address());
            }

            int blockRemaining = hostsInNetwork.size();
            for (String host : hostsInNetwork) {
                ArrayList<ScanResultDTO> hostOverview = new ArrayList<>();
                hostOverview.add(new ScanResultDTO("Host", host));
                for (int i = 0; i < scanSummary.results().size(); i++) {
                    if (scanSummary.results().get(i).host().address().equals(host)) {
                        hostOverview.add(new ScanResultDTO(
                                "Port #" + (i + 1),
                                scanSummary.results().get(i).port() +
                                        (!scanSummary.results().get(i).service().isEmpty() ? " " +
                                                "(" + scanSummary.results().get(i).service() + ")" : "")));
                    }
                }
                addBlock(hostOverview);
                blockRemaining--;
                if (blockRemaining > 0) {
                    addEmptyRow();
                }
            }
        }
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

    private void addBlock(ArrayList<ScanResultDTO> entries){
        for(int i=0;i<entries.size();i++){
            if(entries.get(i).getAdditionalInfo()) {
                addPortRow(entries.get(i).getProperty(), entries.get(i).getEntry(), entries.get(i).getTechnicalReference(),i == 0, i == entries.size() - 1, i % 2 == 0);
            } else{
                addRow(entries.get(i).getProperty(), entries.get(i).getEntry(), i == 0, i == entries.size() - 1, i % 2 == 0);
            }
        }
    }

    private void addRow(String leftText, String rightText, boolean firstRow, boolean lastRow, boolean even
    ) {
        Label left = new Label(leftText);
        Label right = new Label(rightText);

        String rowStyle = even ? "porty-results-row-even" : "porty-results-row-odd";

        left.getStyleClass().add(rowStyle);
        right.getStyleClass().addAll(rowStyle, "porty-result-border");

        addRow(left, right, firstRow, lastRow);
    }

    private void addPortRow(String leftText, String rightText, TechnicalReference technicalReference, boolean firstRow, boolean lastRow, boolean even) {
        String rowStyle = even ? "porty-results-row-even" : "porty-results-row-odd";

        Label left = new Label(leftText);
        left.setMaxWidth(Double.MAX_VALUE);
        left.getStyleClass().add(rowStyle);

        Label portLabel = new Label(rightText);
        portLabel.getStyleClass().add(rowStyle);

        Button infoButton = new Button("ℹ");
        infoButton.getStyleClass().add("porty-port-info-button");

        if (technicalReference != null) {
            infoButton.setOnAction(e -> openTechnicalReference(technicalReference));
            infoButton.setTooltip(new Tooltip(technicalReference.title()));
        } else {
            infoButton.setDisable(true);
        }

        HBox right = new HBox(6);
        right.setAlignment(Pos.CENTER_LEFT);
        right.setMaxWidth(Double.MAX_VALUE);
        right.getStyleClass().addAll(rowStyle, "porty-result-border");
        right.setStyle("-fx-padding: 0;");

        right.setMinHeight(24);
        right.setPrefHeight(24);
        right.setMaxHeight(Double.MAX_VALUE);
        VBox.setVgrow(right, Priority.ALWAYS);
        GridPane.setVgrow(right, Priority.ALWAYS);
        portLabel.setAlignment(Pos.CENTER_LEFT);
        infoButton.setAlignment(Pos.CENTER_LEFT);


        right.getChildren().addAll(portLabel, infoButton);

        addRow(left, right, firstRow, lastRow);
    }

    public void addRow(Node left, Node right, boolean firstRow, boolean lastRow){
        if (firstRow) {
            left.setStyle("-fx-background-radius: 5 0 0 0; -fx-border-radius: 5 0 0 0;");
            right.setStyle("-fx-background-radius: 0 5 0 0; -fx-border-radius: 0 5 0 0;");
        }

        if (lastRow) {
            left.setStyle(left.getStyle() + "-fx-background-radius: 0 0 0 5; -fx-border-radius: 0 0 0 5;");
            right.setStyle(right.getStyle() + "-fx-background-radius: 0 0 5 0; -fx-border-radius: 0 0 5 0;");
        }

        resultGrid.addRow(row, left, right);
        row++;
    }

    private void addEmptyRow(){
        Region filler = new Region();
        filler.setPrefHeight(24);
        resultGrid.addRow(row, filler);
        row++;
    }

    private void updateRiskBar(float severity) {
        double maxWidth = redBar.getWidth();
        double newWidth = (1 - severity) * maxWidth;

        if (newWidth < 0) newWidth = 0;
        if (newWidth > maxWidth) newWidth = maxWidth;

        greenBar.setPrefWidth(newWidth);
    }

    private void openTechnicalReference(TechnicalReference ref) {
        if (ref == null || ref.uri() == null) return;

        try {
            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().browse(ref.uri());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
