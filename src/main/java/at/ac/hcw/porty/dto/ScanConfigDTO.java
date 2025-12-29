package at.ac.hcw.porty.dto;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class ScanConfigDTO {
    private final StringProperty host = new SimpleStringProperty();

    public ScanConfigDTO() {}

    public ScanConfigDTO(String host) {
        this.host.set(host);
    }

    public String getHost() {
        return host.get();
    }

    public StringProperty HostProperty() {
        return host;
    }

    public void setHost(String host){
        this.host.set(host);
    }
}
