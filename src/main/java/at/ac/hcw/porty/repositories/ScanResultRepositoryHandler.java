package at.ac.hcw.porty.repositories;

import at.ac.hcw.porty.types.interfaces.IScanResultRepository;
import at.ac.hcw.porty.types.records.Host;
import at.ac.hcw.porty.types.records.ScanSummary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.Optional;

public class ScanResultRepositoryHandler {
    private static final Logger logger =
            LoggerFactory.getLogger(ScanResultRepositoryHandler.class);
    private IScanResultRepository repository;

    public ScanResultRepositoryHandler(IScanResultRepository repository) {
        this.repository = repository;
    }

    public void setRepository(IScanResultRepository repository) {
        this.repository = repository;
    }

    public IScanResultRepository getRepository() {
        return this.repository;
    }

    public boolean save(ScanSummary summary) {
        if (repository == null) throw new IllegalStateException("No repository set.");
        logger.info("Saving scan summary for host: {}", summary.host().address());
        return this.repository.save(summary);
    }

    public Optional<ScanSummary> load(Host host, Instant startedAt) {
        if (repository == null) throw new IllegalStateException("No repository set.");
        logger.info("Trying to load summary for host: {}", host.address());
        return this.repository.load(host, startedAt);
    }
}
