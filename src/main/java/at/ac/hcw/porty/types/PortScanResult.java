package at.ac.hcw.porty.types;

import java.time.Duration;

public record PortScanResult(Host host, int port, PortStatus status, Duration responseTime, String note) { }
