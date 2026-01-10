package at.ac.hcw.porty.dto;

import javafx.beans.property.*;

import java.time.Instant;
import java.util.Date;

public class ScanHistoryTableDTO {
    private final ObjectProperty<Instant> date = new SimpleObjectProperty<Instant>();
    private final StringProperty address = new SimpleStringProperty();
    private final IntegerProperty ports = new SimpleIntegerProperty();
    private final StringProperty file = new SimpleStringProperty();

    public ScanHistoryTableDTO(){};

    public ScanHistoryTableDTO(Instant date, String address, int ports, String file){
        this.date.set(date);
        this.address.set(address);
        this.ports.set(ports);
        this.file.set(file);
    };

    public Instant getDate() {
        return date.get();
    }

    public ObjectProperty<Instant> dateProperty() {
        return date;
    }

    public void setDate(Instant date){
        this.date.set(date);
    }

    public String getAddress() {
        return address.get();
    }

    public StringProperty addressProperty() {
        return address;
    }

    public void setAddress(String address){
        this.address.set(address);
    }

    public int getPorts() {
        return ports.get();
    }

    public IntegerProperty portsProperty() {
        return ports;
    }

    public void setPorts(int ports){
        this.ports.set(ports);
    }

    public String getFile() {
        return file.get();
    }

    public StringProperty fileProperty() {
        return file;
    }

    public void setFile(String file){
        this.file.set(file);
    }
}
