package at.ac.hcw.porty.types.interfaces;

import at.ac.hcw.porty.types.records.TechnicalReference;

import java.util.Optional;

public interface IPortDocumentationProvider {
    Optional<TechnicalReference> getDocumentation(int port, String service);
}
