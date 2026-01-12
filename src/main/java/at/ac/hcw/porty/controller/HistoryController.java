package at.ac.hcw.porty.controller;

import at.ac.hcw.porty.dto.ScanHistoryTableDTO;
import at.ac.hcw.porty.repositories.ScanResultRepositoryFactory;
import at.ac.hcw.porty.types.enums.ScanResultRepositoryOption;
import at.ac.hcw.porty.types.interfaces.IScanResultRepository;
import at.ac.hcw.porty.types.records.ScanSummary;
import at.ac.hcw.porty.utils.HistoryHandler;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
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
    private BarChart<String, Integer> historyChart;

    ObservableList<ScanHistoryTableDTO> tableEntries = FXCollections.observableArrayList();

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

        historyTable.setItems(tableEntries);

        IScanResultRepository repositoryJSON = ScanResultRepositoryFactory.create(ScanResultRepositoryOption.JSON);
        IScanResultRepository repositoryBIN = ScanResultRepositoryFactory.create(ScanResultRepositoryOption.BINARY);

        HistoryHandler historyHandler = new HistoryHandler(IScanResultRepository.savePath, List.of(repositoryJSON, repositoryBIN));

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
    }

    public void setChartData(String host) {
        historyChart.getData().clear();

        XYChart.Series<String, Integer> series = new XYChart.Series<>();
        series.setName(host);

        ArrayList<String> scanDates = new ArrayList<String>();

        for(ScanHistoryTableDTO scan : tableEntries) {
            if(Objects.equals(scan.getAddress(), host)) {
                Instant instant = scan.getDate();

                DateTimeFormatter formatter =
                        DateTimeFormatter.ofPattern("dd.MM.yyyy, HH:mm")
                                .withZone(ZoneId.systemDefault());

                String formattedDate = formatter.format(instant);

                scanDates.add(formattedDate);
                series.getData().add(new XYChart.Data<>(formattedDate, scan.getPorts()));
            }
        }

        Collections.sort(scanDates);
        scanDates = new ArrayList<>(new LinkedHashSet<>(scanDates));

        CategoryAxis xAxis = (CategoryAxis) historyChart.getXAxis();
        xAxis.getCategories().clear();
        xAxis.setCategories(FXCollections.observableArrayList(scanDates));

        historyChart.getData().add(series);
    }
}
