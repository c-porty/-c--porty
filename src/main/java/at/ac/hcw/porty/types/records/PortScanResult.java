package at.ac.hcw.porty.types.records;

import at.ac.hcw.porty.documentation.PortDocumentationProvider;
import at.ac.hcw.porty.types.enums.PortStatus;

import java.io.Serializable;
import java.util.Optional;

public record PortScanResult(
        Host host,
        int port,
        PortStatus status,
        String service,
        String os,
        TechnicalReference technicalReference
) implements Serializable {
    public PortScanResult {
        PortDocumentationProvider documentationProvider = new PortDocumentationProvider();
        Optional<TechnicalReference> ref = documentationProvider.getDocumentation(port, service);

        if (ref.isPresent()) {
            technicalReference = ref.get();
        }
    }

    public PortScanResult(Host host, int port, PortStatus status, String service, String os) {
        this(host, port, status, service, os, null);
    }

    @Override
    public String toString() {
        return String.format("%s:%d -> %s (%s)", host.address(), port, status, service);
    }
}
