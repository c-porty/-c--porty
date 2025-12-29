package at.ac.hcw.porty.types.records;

import java.time.Duration;

public record ScanConfig(Host host, PortRange range, Duration timeoutPerPort, double statsTime) { }
