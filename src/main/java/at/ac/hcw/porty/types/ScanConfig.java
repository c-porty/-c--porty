package at.ac.hcw.porty.types;

import java.time.Duration;

public record ScanConfig(Host host, PortRange range, Duration timeoutPerPort, double statsTime) { }
