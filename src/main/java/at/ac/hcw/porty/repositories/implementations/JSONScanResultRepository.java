package at.ac.hcw.porty.repositories.implementations;

import at.ac.hcw.porty.types.interfaces.IScanResultRepository;
import at.ac.hcw.porty.types.records.Host;
import at.ac.hcw.porty.types.records.ScanSummary;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.Optional;
import java.util.Set;

public class JSONScanResultRepository implements IScanResultRepository {
    private static final Logger logger =
            LoggerFactory.getLogger(JSONScanResultRepository.class);
    private static final String EXT = ".json";

    private final ObjectMapper objectMapper;

    public JSONScanResultRepository() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
        this.objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        this.objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
    }

    @Override
    public boolean save(ScanSummary summary) {
        try {
            Path dir = Paths.get(IScanResultRepository.savePath);
            Files.createDirectories(dir);
            String fileName = String.format("%s-%d%s",
                    summary.host().address(),
                    summary.startedAt().getEpochSecond(),
                    EXT
            );
            Path file = dir.resolve(fileName);
            objectMapper.writeValue(file.toFile(), summary);
            return true;
        } catch (Exception e) {
            logger.error("Failed to save: {}", e.getMessage(), e);
        }
        return false;
    }

    @Override
    public Optional<ScanSummary> load(Host host, Instant startedAt) {
        String fileName = String.format("%s-%d%s",
                host.address(),
                startedAt.getEpochSecond(),
                EXT
        );
        Path path = Paths.get(IScanResultRepository.savePath).resolve(fileName);
        return parse(path);
    }

    @Override
    public Set<String> supportedExtensions() {
        return Set.of(EXT);
    }

    @Override
    public Optional<ScanSummary> parse(Path file) {
        try {
            ScanSummary summary = objectMapper.readValue(file.toFile(), ScanSummary.class);
            return Optional.of(summary);
        } catch (Exception e) {
            logger.error("Failed to parse: {}", e.getMessage(), e);
        }
        return Optional.empty();
    }
}