package at.ac.hcw.porty.controller;

import at.ac.hcw.porty.dto.ScanHistoryTableDTO;
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
import javafx.scene.text.TextAlignment;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class HistoryController {
    @FXML
    private TableView<ScanHistoryTableDTO> historyTable;
    @FXML
    private TableColumn<ScanHistoryTableDTO, Instant> dateCol;
    @FXML
    private TableColumn<ScanHistoryTableDTO, String> addressCol;
    @FXML
    private TableColumn<ScanHistoryTableDTO, Integer> portsCol;
    @FXML
    private TableColumn<ScanHistoryTableDTO, String> infoCol;
    @FXML
    private BarChart<String, Number> historyChart;

    ObservableList<ScanHistoryTableDTO> tableEntries = FXCollections.observableArrayList();

    IScanResultRepository repositoryJSON = ScanResultRepositoryFactory.create(ScanResultRepositoryOption.JSON);
    IScanResultRepository repositoryBIN = ScanResultRepositoryFactory.create(ScanResultRepositoryOption.BINARY);

    HistoryHandler historyHandler = new HistoryHandler(IScanResultRepository.savePath, List.of(repositoryJSON, repositoryBIN));

    @FXML
    public void initialize() {

        dateCol.setCellValueFactory(cell ->
                cell.getValue().dateProperty());
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
                cell.getValue().addressProperty());

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
            ScanHistoryTableDTO entry = new ScanHistoryTableDTO(timestamp, address, scanFile.results().size(), filename);
            tableEntries.add(entry);
        }

        historyTable.getSortOrder().add(dateCol);
        historyTable.sort();

        historyTable.getSelectionModel()
                .selectedItemProperty()
                .addListener((obs, oldItem, newItem) -> {
                    if (newItem != null) {
                        setChartData(newItem.getAddress());
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

        DateTimeFormatter formatter =
                DateTimeFormatter.ofPattern("dd.MM.yyyy, HH:mm")
                        .withZone(ZoneId.systemDefault());

        for(ScanHistoryTableDTO scan : tableEntries) {
            if(Objects.equals(scan.getAddress(), host)) {
                Instant instant = scan.getDate();

                scanDates.add(instant);
                portMax = Math.max(portMax, scan.getPorts());

                XYChart.Data<String, Number> data = new XYChart.Data<>(formatter.format(instant), scan.getPorts());

                series.getData().add(data);
            }
        }

        Collections.sort(scanDates);

        ArrayList<String> scanDatesAxis = new ArrayList<>();

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

        setBarColors(series, new Host(host), scanDates);
    }

    public void setBarColors(XYChart.Series<String, Number> series, Host host, ArrayList<Instant> timestamps){
        ObservableList<XYChart.Data<String, Number>> bars = series.getData();

        int i=0;
        for(XYChart.Data<String, Number> bar : bars){
            getBarColor(bar, host, timestamps.get(i));
            i++;
        }
    }

    public void getBarColor(XYChart.Data<String, Number> bar, Host host, Instant timestamp){
        Optional<ScanSummary> optionalSummary = historyHandler.load(host, timestamp);
        if(optionalSummary.isPresent()) {
            ScanSummary summary = optionalSummary.get();
            if(summary.severity()<0.33){
                bar.getNode().setStyle("-fx-bar-fill: #74E37B;");
                return;
            }
            if(summary.severity()<0.66){
                bar.getNode().setStyle("-fx-bar-fill: orange;");
                return;
            }
            bar.getNode().setStyle("-fx-bar-fill: red;");
            return;
        } else {
            System.out.println("COULD NOT FIND: "+host.address()+"-"+timestamp.getEpochSecond());
        }
        bar.getNode().setStyle("-fx-bar-fill: gray;");
    }
}
