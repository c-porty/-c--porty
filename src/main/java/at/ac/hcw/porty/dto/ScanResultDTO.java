package at.ac.hcw.porty.dto;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.time.Instant;

public class ScanResultDTO {
    private final StringProperty property = new SimpleStringProperty();
    private final StringProperty entry = new SimpleStringProperty();

    public ScanResultDTO(){};

    public ScanResultDTO(String property, String entry){
        this.property.set(property);
        this.entry.set(entry);
    };

    public String getEntry() {
        return entry.get();
    }

    public StringProperty entryProperty() {
        return entry;
    }

    public String getProperty() {
        return property.get();
    }

    public StringProperty propertyProperty() {
        return property;
    }

    public void setProperty(String property){
        this.property.set(property);
    }

    public void setEntry(String entry){
        this.entry.set(entry);
    }

}
