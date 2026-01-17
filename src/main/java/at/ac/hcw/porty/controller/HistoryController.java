package at.ac.hcw.porty.controller;

import at.ac.hcw.porty.dto.ScanHistoryDTO;
import at.ac.hcw.porty.repositories.ScanResultRepositoryFactory;
import at.ac.hcw.porty.types.enums.ScanResultRepositoryOption;
import at.ac.hcw.porty.types.interfaces.IScanResultRepository;
import at.ac.hcw.porty.types.records.Host;
import at.ac.hcw.porty.types.records.ScanSummary;
import at.ac.hcw.porty.utils.HistoryHandler;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableView;
import javafx.scene.control.TableColumn;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class HistoryController {
    @FXML
    private TableView<ScanHistoryDTO> historyTable;
    @FXML
    private TableColumn<ScanHistoryDTO, Instant> dateCol;
    @FXML
    private TableColumn<ScanHistoryDTO, String> addressCol;
    @FXML
    private TableColumn<ScanHistoryDTO, Integer> portsCol;
    @FXML
    private TableColumn<ScanHistoryDTO, String> infoCol;
    @FXML
    private BarChart<String, Number> historyChart;

    ObservableList<ScanHistoryDTO> tableEntries = FXCollections.observableArrayList();

    IScanResultRepository repositoryJSON = ScanResultRepositoryFactory.create(ScanResultRepositoryOption.JSON);
    IScanResultRepository repositoryBIN = ScanResultRepositoryFactory.create(ScanResultRepositoryOption.BINARY);

    HistoryHandler historyHandler = new HistoryHandler(IScanResultRepository.savePath, List.of(repositoryJSON, repositoryBIN));

    String chartLoadedHost = "";

    @FXML
    public void initialize() {

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

        dateCol.getStyleClass().add("left-aligned");
        addressCol.getStyleClass().add("left-aligned");

        portsCol.getStyleClass().add("center-aligned");
        infoCol.getStyleClass().add("center-aligned");

        historyTable.setItems(tableEntries);

        ArrayList<ScanSummary> scanFiles = historyHandler.loadAll();

        for(ScanSummary scanFile : scanFiles){
            String address = scanFile.host().address();
            Instant timestamp = scanFile.startedAt();
            String filename = address+"-"+timestamp.getEpochSecond();
            ScanHistoryDTO entry = new ScanHistoryDTO(timestamp, address, scanFile.results().size(), filename, scanFile.severity());
            tableEntries.add(entry);
        }

        historyTable.getSortOrder().add(dateCol);
        historyTable.sort();

        historyTable.getSelectionModel()
                .selectedItemProperty()
                .addListener((obs, oldItem, newItem) -> {
                    if (newItem != null) {
                        if(!Objects.equals(chartLoadedHost, newItem.getHost())) {
                            chartLoadedHost = newItem.getHost();
                            setChartData(newItem.getHost());
                        }
                    }
                });

        historyChart.setLegendVisible(false);
    }

    public void setChartData(String host) {
        historyChart.getData().clear();

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName(host);

        ArrayList<Instant> scanDates = new ArrayList<Instant>();
        double portMax = 0;
        double portMin = 0;


        for(ScanHistoryDTO scan : tableEntries) {
            if(Objects.equals(scan.getHost(), host)) {
                portMax = Math.max(portMax, scan.getPorts());
                scanDates.add(scan.getTimestamp());
                series.getData().add(scan.getBar());
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

        setBarColors(host);
    }

    public void setBarColors(String host){
        for(ScanHistoryDTO scan : tableEntries) {
            if(Objects.equals(scan.getHost(), host)) {
                getBarColor(scan);
            }
        }
    }

    public void getBarColor(ScanHistoryDTO entry){
        if(entry.getSeverity()<0.33){
            entry.getBar().getNode().setStyle("-fx-bar-fill: #74E37B;");
            return;
        }
        if(entry.getSeverity()<0.66){
            entry.getBar().getNode().setStyle("-fx-bar-fill: orange;");
            return;
        }
        entry.getBar().getNode().setStyle("-fx-bar-fill: red;");
    }
}
