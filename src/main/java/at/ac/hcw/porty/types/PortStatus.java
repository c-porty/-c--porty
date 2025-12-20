package at.ac.hcw.porty.types;

public enum PortStatus {
    OPEN,
    CLOSED,
    FILTERED,
    ERROR;

    public enum ScanStrategy {
        MOCK,
        NMAP
    }
}
