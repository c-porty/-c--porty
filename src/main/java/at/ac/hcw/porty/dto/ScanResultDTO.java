package at.ac.hcw.porty.dto;

import at.ac.hcw.porty.types.records.TechnicalReference;
import javafx.beans.binding.StringBinding;
import javafx.beans.property.*;

public class ScanResultDTO {
    private final StringBinding propertyBinding;
    private final StringBinding entryBinding;
    private final BooleanProperty additionalInfo = new SimpleBooleanProperty();
    private final ObjectProperty<TechnicalReference> technicalReference = new SimpleObjectProperty<>();

    public ScanResultDTO(StringBinding propertyBinding, String entry) {
        this.propertyBinding = propertyBinding;
        this.entryBinding = new StringBinding() {
            @Override
            protected String computeValue() {
                return entry;
            }
        };
        this.additionalInfo.set(false);
        this.technicalReference.set(null);
    }

    public ScanResultDTO(String property, String entry) {
        this.propertyBinding = new StringBinding() {
            @Override
            protected String computeValue() {
                return property;
            }
        };
        this.entryBinding = new StringBinding() {
            @Override
            protected String computeValue() {
                return entry;
            }
        };
        this.additionalInfo.set(false);
        this.technicalReference.set(null);
    }

    public ScanResultDTO(String left, String right, boolean additionalInfo, TechnicalReference technicalReference) {
        this.propertyBinding = new StringBinding() {
            @Override
            protected String computeValue() {
                return left;
            }
        };
        this.entryBinding = new StringBinding() {
            @Override
            protected String computeValue() {
                return right;
            }
        };
        this.additionalInfo.set(additionalInfo);
        this.technicalReference.set(technicalReference);
    }

    public String getProperty() {
        return propertyBinding.get();
    }

    public StringBinding propertyBinding() {
        return propertyBinding;
    }

    public String getEntry() {
        return entryBinding.get();
    }

    public StringBinding entryBinding() {
        return entryBinding;
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

    public void setAdditionalInfo(boolean additionalInfo) {
        this.additionalInfo.set(additionalInfo);
    }

    public void setTechnicalReference(TechnicalReference technicalReference) {
        this.technicalReference.set(technicalReference);
    }
}
