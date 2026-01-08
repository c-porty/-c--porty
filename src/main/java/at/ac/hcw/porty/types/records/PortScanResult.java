package at.ac.hcw.porty.types.records;

import at.ac.hcw.porty.types.enums.PortStatus;

import java.io.Serializable;

public record PortScanResult(Host host, int port, PortStatus status, String note) implements Serializable {
    @Override
    public String toString() {
        return String.format("Port %d -> %s (%s)", port, status, note);
    }
}
