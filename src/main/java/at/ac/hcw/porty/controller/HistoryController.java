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
import javafx.scene.control.TableCell;
import javafx.scene.control.TableView;
import javafx.scene.control.TableColumn;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

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
            ScanHistoryTableDTO entry = new ScanHistoryTableDTO(scanFile.startedAt(),scanFile.host().address(), scanFile.results().toArray().length, "hier");
            tableEntries.add(entry);
        }

        historyTable.getSortOrder().add(dateCol);
        historyTable.sort();
    }

}
