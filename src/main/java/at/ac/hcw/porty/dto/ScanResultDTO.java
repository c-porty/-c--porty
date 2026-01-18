package at.ac.hcw.porty.dto;

import at.ac.hcw.porty.types.records.TechnicalReference;
import javafx.beans.property.*;

import java.time.Instant;

public class ScanResultDTO {
    private final StringProperty property = new SimpleStringProperty();
    private final StringProperty entry = new SimpleStringProperty();
    private final BooleanProperty additionalInfo = new SimpleBooleanProperty();
    private final ObjectProperty<TechnicalReference> technicalReference= new SimpleObjectProperty<>();

    public ScanResultDTO(){};

    public ScanResultDTO(String property, String entry){
        this.property.set(property);
        this.entry.set(entry);
        this.additionalInfo.set(false);
        this.technicalReference.set(null);
    };

    public ScanResultDTO(String property, String entry, boolean additionalInfo, TechnicalReference technicalReference){
        this.property.set(property);
        this.entry.set(entry);
        this.additionalInfo.set(additionalInfo);
        this.technicalReference.set(technicalReference);
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

    public boolean getAdditionalInfo() {
        return additionalInfo.get();
    }

    public BooleanProperty additionalInfoProperty() {
        return additionalInfo;
    }

    public TechnicalReference getTechnicalReference() {
        return technicalReference.get();
    }

    public ObjectProperty<TechnicalReference> technicalReferenceProperty() {
        return technicalReference;
    }

    public void setProperty(String property){
        this.property.set(property);
    }

    public void setEntry(String entry){
        this.entry.set(entry);
    }

    public void setAdditionalInfo(boolean additionalInfo){this.additionalInfo.set(additionalInfo);}

    public void setTechnicalReference(TechnicalReference technicalReference){this.technicalReference.set(technicalReference);}

}
