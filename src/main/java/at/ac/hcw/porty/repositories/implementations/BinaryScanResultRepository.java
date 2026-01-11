package at.ac.hcw.porty.repositories.implementations;

import at.ac.hcw.porty.types.interfaces.IScanResultRepository;
import at.ac.hcw.porty.types.records.Host;
import at.ac.hcw.porty.types.records.ScanSummary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.Optional;
import java.util.Set;

public class BinaryScanResultRepository implements IScanResultRepository {
    private static final Logger logger =
            LoggerFactory.getLogger(BinaryScanResultRepository.class);
    private static final String EXT = ".bin";

    @Override
    public boolean save(ScanSummary summary) {
        try {
            Path dir = Paths.get(IScanResultRepository.savePath);
            Files.createDirectories(dir);

            Path file = dir.resolve(String.format("%s-%d%s",
                    summary.host().address(),
                    summary.startedAt().getEpochSecond(),
                    EXT));

            try (ObjectOutputStream out = new ObjectOutputStream(Files.newOutputStream(file))) {
                out.writeObject(summary);
            }
            return true;
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
        return false;
    }

    @Override
    public Optional<ScanSummary> load(Host host, Instant startedAt) {
        Path path = Paths.get(IScanResultRepository.savePath)
                .resolve(String.format("%s-%d%s", host.address(), startedAt.getEpochSecond(), EXT));
        return parse(path);
    }

    @Override
    public Set<String> supportedExtensions() {
        return Set.of(EXT);
    }

    @Override
    public Optional<ScanSummary> parse(Path file) {
        try (ObjectInputStream in = new ObjectInputStream(Files.newInputStream(file))) {
            return Optional.of((ScanSummary) in.readObject());
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
        return Optional.empty();
    }
}