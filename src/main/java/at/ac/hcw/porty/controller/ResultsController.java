package at.ac.hcw.porty.controller;

import at.ac.hcw.porty.dto.ScanResultDTO;
import at.ac.hcw.porty.types.interfaces.MainAwareController;
import at.ac.hcw.porty.types.records.PortScanResult;
import at.ac.hcw.porty.types.records.ScanSummary;
import at.ac.hcw.porty.types.records.TechnicalReference;
import at.ac.hcw.porty.utils.AlertManager;
import at.ac.hcw.porty.utils.I18n;
import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.*;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javafx.scene.control.Button;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.stage.FileChooser;
import javafx.util.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.kordamp.ikonli.javafx.FontIcon;

import static at.ac.hcw.porty.utils.FileExportToPDF.exportToPdf;

public class ResultsController implements MainAwareController {
    private static final Logger logger =
            LoggerFactory.getLogger(ResultsController.class);
    private static final ExecutorService BROWSER_THREAD = Executors.newCachedThreadPool();
    private static final double SNAPSHOT_SCALE = 2.0;

    @FXML private Label dateTimeLabel;
    @FXML private GridPane resultGrid;
    @FXML private Region redBar;
    @FXML private Region greenBar;
    @FXML private Label resultTitle;
    @FXML private VBox root;
    @FXML private Button exportButton;
    @FXML private Button closeButton;
    @FXML private Tooltip exportToPDFTooltip;
    @FXML private Tooltip resultCloseTooltip;

    private MainController mainController;
    private ScanSummary scanSummary;

    int row = 0;

    @Override
    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }

    public void setScanSummary(ScanSummary scanSummary) {
        this.scanSummary = scanSummary;

        Instant start = scanSummary.startedAt();
        ZonedDateTime zdt = start.atZone(ZoneId.systemDefault()); // in lokale Zeit konvertieren
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy, HH:mm");

        dateTimeLabel.setText(zdt.format(formatter));
        exportToPDFTooltip.setShowDelay(Duration.millis(100));
        resultCloseTooltip.setShowDelay(Duration.millis(100));
        displayScanSummary();

        Platform.runLater(() -> updateRiskBar(scanSummary.severity()));
    }

    @FXML
    private void navigateBack() {
        mainController.traceBackNavigation();
    }

    @FXML
    private void exportPdf() {
        boolean darkMode = isDarkModeActive();
        closeButton.setVisible(false);
        exportButton.setVisible(false);

        root.applyCss();
        root.layout();

        FileChooser chooser = new FileChooser();
        chooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("PDF Files", "*.pdf")
        );
        chooser.setInitialFileName(getFileNameForPDF());
        File file = chooser.showSaveDialog(root.getScene().getWindow());

        BufferedImage image = snapshotView(darkMode);
        if (file != null) {
            boolean success = exportToPdf(file, darkMode, image);

            Alert alert = null;
            if (success) {
                alert = AlertManager.createGridAlert(
                    Alert.AlertType.INFORMATION,
                    I18n.bind("export.success").get(),
                    I18n.bind("export.success.body").get(),
                    new ArrayList<>(0),
                    400,
                    100
                );
            } else {
                alert = AlertManager.createGridAlert(
                    Alert.AlertType.ERROR,
                    I18n.bind("export.failed").get(),
                    I18n.bind("export.failed.body").get(),
                    new ArrayList<>(0),
                    400,
                    100
                );
            }

            alert.show();
        }

        closeButton.setVisible(true);
        exportButton.setVisible(true);
    }

    public void displayScanSummary() {
        setupLanguageTexts();

        ArrayList<ScanResultDTO> scanOverview = new ArrayList<>();
        scanOverview.add(new ScanResultDTO(I18n.bind("history.scanned-address"), scanSummary.host().address()));
        scanOverview.add(new ScanResultDTO(I18n.bind(
                "result.time-taken"),
                scanSummary.finishedAt().getEpochSecond() - scanSummary.startedAt().getEpochSecond() + "s")
        );
        if(!scanSummary.detectedOs().isEmpty()) {
            scanOverview.add(new ScanResultDTO(I18n.bind("result.os"), scanSummary.detectedOs()));
        }
        scanOverview.add(new ScanResultDTO(
                I18n.bind("history.open-ports"),
                String.valueOf(scanSummary.results().size())
        ));
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
        scanOverview.add(new ScanResultDTO(I18n.bind("result.avg-risk"), getRiskLabel(scanSummary.severity())));

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
            return I18n.bind("result.risk.low").get();
        } else if (severity < 0.66f) {
            return I18n.bind("result.risk.medium").get();
        } else {
            return I18n.bind("result.risk.high").get();
        }
    }

    private void addBlock(ArrayList<ScanResultDTO> entries){
        for(int i=0;i<entries.size();i++){
            if(entries.get(i).getAdditionalInfo()) {
                addPortRow(entries.get(i).getProperty(), entries.get(i).getEntry(), entries.get(i).getTechnicalReference(),i == 0, i == entries.size() - 1, (i+1) % 2 == 0);
            } else{
                addRow(entries.get(i).getProperty(), entries.get(i).getEntry(), i == 0, i == entries.size() - 1, (i+1) % 2 == 0);
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

    private void setupLanguageTexts() {
        resultTitle.textProperty().bind(I18n.bind("result.title"));
        exportToPDFTooltip.textProperty().bind(I18n.bind("tooltip.export-to-pdf"));
        resultCloseTooltip.textProperty().bind(I18n.bind("button.close"));
    }

    private void addPortRow(String leftText, String rightText, TechnicalReference technicalReference, boolean firstRow, boolean lastRow, boolean even) {
        String rowStyle = even ? "porty-results-row-even" : "porty-results-row-odd";

        Label left = new Label(leftText);
        left.setMaxWidth(Double.MAX_VALUE);
        left.getStyleClass().add(rowStyle);

        Label portLabel = new Label(rightText);
        portLabel.getStyleClass().add(rowStyle);

        Button infoButton = new Button();
        FontIcon icon = new FontIcon("mdi2i-information-outline");
        icon.getStyleClass().add("porty-result-info-icon");
        infoButton.setGraphic(icon);
        infoButton.getStyleClass().add("porty-port-info-button");

        Tooltip tooltip = new Tooltip();
        tooltip.setShowDelay(Duration.millis(100));
        infoButton.setTooltip(tooltip);

        if (technicalReference != null) {
            infoButton.setOnAction(e -> openTechnicalReference(technicalReference));
            tooltip.textProperty().bind(I18n.bind("tooltip.result.information", technicalReference.title()));
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
        severity = Math.max(0, Math.min(1, severity));

        redBar.prefWidthProperty().bind(root.widthProperty().multiply(severity));
        greenBar.prefWidthProperty().bind(root.widthProperty().multiply(1 - severity));
    }

    private void openTechnicalReference(TechnicalReference ref) {
        if (ref == null || ref.uri() == null) return;
        if (!Desktop.isDesktopSupported()) {
            logger.warn("Desktop browsing not supported on this platform");
            Alert warning = AlertManager.createGridAlert(
                Alert.AlertType.WARNING,
                    I18n.bind("result.open-failed.title").get(),
                    I18n.bind("result.open-failed.body").get(),
                new ArrayList<>(0),
                    400,
                    100
            );
            warning.show();
            return;
        }

        BROWSER_THREAD.submit(() -> {
            try {
                Desktop.getDesktop().browse(ref.uri());
            } catch (Exception e) {
                logger.error("Failed to open technical reference: {}", ref.uri(), e);
            }
        });
    }

    private boolean isDarkModeActive() {
        if (mainController != null) {
            return mainController.darkModeIsActive();
        }
        return true;
    }

    private BufferedImage snapshotView(boolean darkMode) {
        SnapshotParameters params = new SnapshotParameters();
        params.setFill(darkMode ? Paint.valueOf("#141218") : Color.WHITE);
        params.setTransform(javafx.scene.transform.Transform.scale(SNAPSHOT_SCALE, SNAPSHOT_SCALE));
        WritableImage fxImage = root.snapshot(params, null);

        return SwingFXUtils.fromFXImage(fxImage, null);
    }

    private String getFileNameForPDF() {
        Instant start = scanSummary.startedAt();
        ZonedDateTime zdt = start.atZone(ZoneId.systemDefault());
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy-HH_mm");
        String formattedDate = zdt.format(formatter);

        String safeHost = scanSummary.host().address()
                .replace(":", "-")   // replace colons (IPv6) with dash
                .replace("/", "-")   // replace slashes if any
                .replace("\\", "-"); // replace backslashes if any

        return String.format("Scan-%s-%s.pdf", safeHost, formattedDate);
    }
}
