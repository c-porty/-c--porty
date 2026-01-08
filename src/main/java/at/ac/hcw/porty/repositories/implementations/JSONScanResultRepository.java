package at.ac.hcw.porty.repositories.implementations;

import at.ac.hcw.porty.types.ScanResultRepository;
import at.ac.hcw.porty.types.records.Host;
import at.ac.hcw.porty.types.records.ScanSummary;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.Optional;

public class JSONScanResultRepository extends ScanResultRepository {
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
            // ensure the dir is there
            Path dir = Paths.get(this.savePath);
            Files.createDirectories(dir);

            String fileName = String.format("%s-%d.json",
                    summary.host().address(),
                    summary.startedAt().getEpochSecond());

            Path file = dir.resolve(fileName);
            objectMapper.writeValue(file.toFile(), summary);
            return true;
        } catch (Exception ignored) {}
        return false;
    }

    @Override
    public Optional<ScanSummary> load(Host host, Instant startedAt) {
        String fileName = String.format("%s-%d.json",
                host.address(),
                startedAt.getEpochSecond());
        Path path = Paths.get(this.savePath).resolve(fileName);

        try {
            ScanSummary summary =
                    objectMapper.readValue(path.toFile(), ScanSummary.class);
            return Optional.of(summary);
        } catch (Exception ignored) {}

        return Optional.empty();
    }
}
