package at.ac.hcw.porty.types.records;

import at.ac.hcw.porty.types.enums.PortStatus;

import java.time.Duration;

public record PortScanResult(Host host, int port, PortStatus status, Duration responseTime, String note) { }
