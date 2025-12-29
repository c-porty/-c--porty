package at.ac.hcw.porty.types.records;

import java.time.Instant;
import java.util.List;

public record ScanSummary(Host host, List<PortScanResult> results, Instant startedAt, Instant finishedAt) { }
