package at.ac.hcw.porty.types.interfaces;

import at.ac.hcw.porty.types.records.Host;
import at.ac.hcw.porty.types.records.ScanSummary;

import java.time.Instant;
import java.util.Optional;

public interface IScanResultRepository {
    boolean save(ScanSummary summary);
    Optional<ScanSummary> load(Host host, Instant startedAt);
}
