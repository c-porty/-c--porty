package at.ac.hcw.porty.dto;

import at.ac.hcw.porty.types.records.Host;
import javafx.beans.property.*;
import javafx.scene.chart.XYChart;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class ScanHistoryDTO {
    private final ObjectProperty<Instant> timestamp = new SimpleObjectProperty<Instant>();
    private final StringProperty host = new SimpleStringProperty();
    private final IntegerProperty ports = new SimpleIntegerProperty();
    private final StringProperty file = new SimpleStringProperty();

    private final FloatProperty severity = new SimpleFloatProperty();
    private final ObjectProperty<XYChart.Data<String, Number>> bar = new SimpleObjectProperty<>();

    public ScanHistoryDTO(){};

    public ScanHistoryDTO(Instant timestamp, String host, int ports, String file, float severity){
        this.timestamp.set(timestamp);
        this.host.set(host);
        this.ports.set(ports);
        this.file.set(file);
        this.severity.set(severity);

        DateTimeFormatter formatter =
                DateTimeFormatter.ofPattern("dd.MM.yyyy, HH:mm")
                        .withZone(ZoneId.systemDefault());
        this.bar.set(new XYChart.Data<String, Number>(formatter.format(this.timestamp.get()), ports));
    };

    public Instant getTimestamp() {
        return timestamp.get();
    }

    public ObjectProperty<Instant> timestampProperty() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp){
        this.timestamp.set(timestamp);
    }

    public String getHost() {
        return host.get();
    }

    public StringProperty hostProperty() {
        return host;
    }

    public void setHost(String host){
        this.host.set(host);
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

    public XYChart.Data<String, Number> getBar() {
        return bar.get();
    }

    public ObjectProperty<XYChart.Data<String, Number>> barProperty() {
        return bar;
    }

    public float getSeverity() {
        return severity.get();
    }

    public FloatProperty severityProperty() {
        return severity;
    }

    public void setSeverity(float severity){
        this.severity.set(severity);
    }
}
