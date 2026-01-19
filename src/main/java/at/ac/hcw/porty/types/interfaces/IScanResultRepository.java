package at.ac.hcw.porty.types.interfaces;

import at.ac.hcw.porty.types.records.Host;
import at.ac.hcw.porty.types.records.ScanSummary;

import java.nio.file.Path;
import java.time.Instant;
import java.util.Optional;
import java.util.Set;

public interface IScanResultRepository {
    String savePath = "./src/main/saves/history";

    boolean save(ScanSummary summary);
    Optional<ScanSummary> load(Host host, Instant startedAt);
    Set<String> supportedExtensions();
    Optional<ScanSummary> parse(Path file);
}
