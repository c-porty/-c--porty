package at.ac.hcw.porty.types.records;

import java.io.Serializable;
import java.time.Instant;
import java.util.List;

public record ScanSummary(Host host, List<PortScanResult> results, ScanConfig config, Instant startedAt, Instant finishedAt) implements Serializable { }
