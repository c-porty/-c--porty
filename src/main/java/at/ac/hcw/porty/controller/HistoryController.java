package at.ac.hcw.porty.controller;

import at.ac.hcw.porty.dto.ScanHistoryTableDTO;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableView;
import javafx.scene.control.TableColumn;

import java.util.Collections;
import java.util.Date;

public class HistoryController {
    @FXML
    private TableView<ScanHistoryTableDTO> historyTable;
    @FXML
    private TableColumn<ScanHistoryTableDTO, Date> dateCol;
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
        addressCol.setCellValueFactory(cell ->
                cell.getValue().addressProperty());
        portsCol.setCellValueFactory(cell ->
                cell.getValue().portsProperty().asObject());
        infoCol.setCellValueFactory(cell ->
                cell.getValue().fileProperty());

        historyTable.setItems(tableEntries);

        ScanHistoryTableDTO[] entries = new ScanHistoryTableDTO[3];
        entries[0] = new ScanHistoryTableDTO(new Date(),"Address 1",3,"address1.json");
        entries[1] = new ScanHistoryTableDTO(new Date(),"Address 2",1,"address2.json");
        entries[2] = new ScanHistoryTableDTO(new Date(),"Address 3",2,"address3.json");

        Collections.addAll(tableEntries, entries);
    }

}
