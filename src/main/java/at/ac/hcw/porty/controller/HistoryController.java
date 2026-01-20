package at.ac.hcw.porty.controller;

import at.ac.hcw.porty.app.MockScannerCLITest;
import at.ac.hcw.porty.dto.ScanHistoryDTO;
import at.ac.hcw.porty.repositories.ScanResultRepositoryFactory;
import at.ac.hcw.porty.types.enums.PortStatus;
import at.ac.hcw.porty.types.enums.ScanResultRepositoryOption;
import at.ac.hcw.porty.types.interfaces.IScanResultRepository;
import at.ac.hcw.porty.types.interfaces.MainAwareController;
import at.ac.hcw.porty.types.records.Host;
import at.ac.hcw.porty.types.records.PortScanResult;
import at.ac.hcw.porty.types.records.ScanSummary;
import at.ac.hcw.porty.utils.AlertManager;
import at.ac.hcw.porty.utils.HistoryHandler;
import at.ac.hcw.porty.utils.I18n;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.util.Duration;
import org.kordamp.ikonli.javafx.FontIcon;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class HistoryController implements MainAwareController {
    @FXML private TableView<ScanHistoryDTO> historyTable;
    @FXML private TableColumn<ScanHistoryDTO, Instant> dateCol;
    @FXML private TableColumn<ScanHistoryDTO, String> addressCol;
    @FXML private TableColumn<ScanHistoryDTO, Integer> portsCol;
    @FXML private TableColumn<ScanHistoryDTO, String> infoCol;
    @FXML private BarChart<String, Number> historyChart;
    @FXML private Label historyTitle;
    @FXML private Button deleteEntryButton;

    private MainController mainController;
    private static final Logger logger =
            LoggerFactory.getLogger(HistoryController.class);

    ObservableList<ScanHistoryDTO> tableEntries = FXCollections.observableArrayList();

    IScanResultRepository repositoryJSON = ScanResultRepositoryFactory.create(ScanResultRepositoryOption.JSON);
    IScanResultRepository repositoryBIN = ScanResultRepositoryFactory.create(ScanResultRepositoryOption.BINARY);

    HistoryHandler historyHandler = new HistoryHandler(IScanResultRepository.savePath, List.of(repositoryJSON, repositoryBIN));

    String chartLoadedHost = "";

    @FXML
    public void initialize() {
        setupLanguageTexts();

        //Format columns
        dateCol.setCellValueFactory(cell ->
                cell.getValue().timestampProperty());
        DateTimeFormatter formatter =
                DateTimeFormatter.ofPattern("dd.MM.yyyy, HH:mm")
                        .withZone(ZoneId.systemDefault());
        dateCol.setSortType(TableColumn.SortType.DESCENDING);
        dateCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Instant item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(formatter.format(item));
                }
            }
        });

        addressCol.setCellValueFactory(cell ->
                cell.getValue().hostProperty());

        portsCol.setCellValueFactory(cell ->
                cell.getValue().portsProperty().asObject());

        infoCol.setCellValueFactory(cell ->
                cell.getValue().fileProperty());

        infoCol.setCellFactory(col -> new TableCell<>() {
            private final Button btn = new Button();

            {
                FontIcon icon = new FontIcon("mdi2i-information-outline");
                icon.getStyleClass().add("porty-history-table-info-icon");
                Tooltip tooltip = new Tooltip();
                tooltip.setShowDelay(Duration.millis(100));
                tooltip.textProperty().bind(I18n.bind("tooltip.history.information"));
                btn.setTooltip(tooltip);

                btn.setGraphic(icon);
                btn.getStyleClass().add("porty-history-table-info-button");
                btn.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);

                btn.setOnAction(e -> {
                    String file = getItem();
                    openResultPage(file);
                });
            }

            // Do not set Button if file not existent
            @Override
            protected void updateItem(String file, boolean empty) {
                super.updateItem(file, empty);

                if (empty || file == null) {
                    setGraphic(null);
                } else {
                    setGraphic(btn);
                    setText(null);
                }
            }
        });

        dateCol.getStyleClass().add("left-aligned");
        addressCol.getStyleClass().add("left-aligned");

        portsCol.getStyleClass().add("center-aligned");
        infoCol.getStyleClass().add("center-aligned");

        historyTable.setItems(tableEntries);

        ArrayList<ScanSummary> scanFiles = historyHandler.loadAll();

        for(ScanSummary scanFile : scanFiles){
            String address = scanFile.host().address();
            Instant timestamp = scanFile.startedAt();
            int openPorts = scanFile.results().size();
            String filename = address+"-"+timestamp.getEpochSecond();
            ScanHistoryDTO entry = new ScanHistoryDTO(timestamp, address, openPorts, filename, scanFile.severity());
            tableEntries.add(entry);
        }

        historyTable.getSortOrder().add(dateCol);
        historyTable.sort();

        historyTable.getSelectionModel()
                .selectedItemProperty()
                .addListener((obs, oldItem, newItem) -> {
                    if (newItem != null) {
                            chartLoadedHost = newItem.getHost();
                            setChartData(newItem.getHost());
                    }
                });

        historyChart.setLegendVisible(false);
    }

    @Override
    public void setMainController(MainController mainController){
        this.mainController = mainController;
    }

    /* Open result page from file data */
    public void openResultPage(String file){
        String[] data = file.split("-");
        Host host = new Host(data[0]);
        Instant startedAt = Instant.ofEpochSecond(Long.parseLong(data[1]));
        Optional<ScanSummary> summary = historyHandler.load(host,startedAt);
        if(summary.isPresent()) {
            mainController.navigateToResults(summary.get());
        } else {
            logger.error("Could not find summary: {}", host.address()+"-"+startedAt.getEpochSecond());
        }
    }

    /* Try to delete file linked with selected table entry */
    @FXML
    public void onDropButtonClick(){
        ScanHistoryDTO selected = historyTable.getSelectionModel().getSelectedItem();
        if(selected!=null) {
            Alert alert = AlertManager.createDangerAlert(I18n.bind("history.confirm-delete-text").get(), I18n.bind("history.confirm-delete-button").get());
            Optional<ButtonType> result = alert.showAndWait();

            if (result.isPresent() && result.get().getButtonData() == ButtonBar.ButtonData.OK_DONE) {
                Path path = Paths.get(IScanResultRepository.savePath, selected.getFile()+".json");
                File save = path.toFile();
                if (save.delete()) {
                    logger.info("Deleted the file: {}", save.getName());
                    historyTable.getItems().remove(selected);

                } else {
                    logger.error("Failed to delete the file.");
                }
            }
        }
    }

    public void setChartData(String host) {
        historyChart.getData().clear();

        boolean darkMode = true;
        if (mainController != null) {
            darkMode = mainController.darkModeIsActive();
        }
        updateChartTextColor(darkMode);

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName(host);

        ArrayList<Instant> scanDates = new ArrayList<>();
        double portMax = 0;
        double portMin = 0;

        //Get table entry for each saved history file
        for(ScanHistoryDTO scan : tableEntries) {
            if(Objects.equals(scan.getHost(), host)) {
                portMax = Math.max(portMax, scan.getPorts());
                scanDates.add(scan.getTimestamp());
                DateTimeFormatter formatter =
                        DateTimeFormatter.ofPattern("dd.MM.yyyy, HH:mm")
                                .withZone(ZoneId.systemDefault());
                XYChart.Data<String, Number> data =
                        new XYChart.Data<>(formatter.format(scan.getTimestamp()), scan.getPorts());

                series.getData().add(data);

                data.nodeProperty().addListener((obs, oldNode, newNode) -> {
                    if (newNode != null) {
                        applyBarColor(scan, newNode);
                    }
                });
            }
        }


        Collections.sort(scanDates);

        ArrayList<String> scanDatesAxis = new ArrayList<>();

        DateTimeFormatter formatter =
                DateTimeFormatter.ofPattern("dd.MM.yyyy, HH:mm")
                        .withZone(ZoneId.systemDefault());
        for(Instant scanDate : scanDates){
            scanDatesAxis.add(formatter.format(scanDate));
        }

        //Fix Axis for dynamic load
        scanDatesAxis = new ArrayList<>(new LinkedHashSet<>(scanDatesAxis));

        NumberAxis yAxis = (NumberAxis) historyChart.getYAxis();
        yAxis.setAutoRanging(false);

        yAxis.setLowerBound(portMin);
        yAxis.setUpperBound(portMax + 1);
        yAxis.setTickUnit(Math.max(1, (portMax - portMin) / 10));

        CategoryAxis xAxis = (CategoryAxis) historyChart.getXAxis();
        xAxis.getCategories().clear();
        xAxis.setCategories(FXCollections.observableArrayList(scanDatesAxis));

        historyChart.getData().add(series);
    }

    public void applyBarColor(ScanHistoryDTO entry, Node node){
        if(entry.getSeverity()<0.33){
            node.setStyle("-fx-bar-fill: #74E37B;");
            return;
        }
        if(entry.getSeverity()<0.66){
            node.setStyle("-fx-bar-fill: orange;");
            return;
        }
        node.setStyle("-fx-bar-fill: red;");
    }

    private void setupLanguageTexts() {
        historyTitle.textProperty().bind(I18n.bind("history"));
        dateCol.textProperty().bind(I18n.bind("history.date"));
        addressCol.textProperty().bind(I18n.bind("history.scanned-address"));
        portsCol.textProperty().bind(I18n.bind("history.open-ports"));
        infoCol.textProperty().bind(I18n.bind("history.info"));
        deleteEntryButton.textProperty().bind(I18n.bind("history.delete-entry"));
    }

    private void updateChartTextColor(boolean darkMode) {
        Paint textColor = darkMode ? Color.WHITE : Color.BLACK;

        CategoryAxis xAxis = (CategoryAxis) historyChart.getXAxis();
        xAxis.setTickLabelFill(textColor);

        NumberAxis yAxis = (NumberAxis) historyChart.getYAxis();
        yAxis.setTickLabelFill(textColor);
    }
}
