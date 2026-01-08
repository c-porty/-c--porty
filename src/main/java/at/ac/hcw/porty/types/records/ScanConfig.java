package at.ac.hcw.porty.types.records;

import java.io.Serializable;

public record ScanConfig(Host host, PortRange range, NmapOptions options) implements Serializable { }
