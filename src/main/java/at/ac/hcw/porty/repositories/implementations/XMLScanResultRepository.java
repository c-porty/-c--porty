package at.ac.hcw.porty.repositories.implementations;

import at.ac.hcw.porty.types.ScanResultRepository;
import at.ac.hcw.porty.types.records.Host;
import at.ac.hcw.porty.types.records.ScanSummary;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.Optional;

public class XMLScanResultRepository extends ScanResultRepository {
    private final XmlMapper xmlMapper;

    public XMLScanResultRepository() {
        this.xmlMapper = new XmlMapper();
        xmlMapper.registerModule(new JavaTimeModule());
        // write Instants as ISO-8601 strings (e.g., 2026-01-02T03:04:05Z)
        this.xmlMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        this.xmlMapper.enable(SerializationFeature.INDENT_OUTPUT);
    }

    @Override
    public boolean save(ScanSummary summary) {
        try {
            // ensure the dir is there
            Path dir = Paths.get(this.savePath);
            Files.createDirectories(dir);

            String fileName = String.format("%s-%d.xml",
                    summary.host().address(),
                    summary.startedAt().getEpochSecond());
            Path file = dir.resolve(fileName);
            xmlMapper.writeValue(file.toFile(), summary);
            return true;
        } catch (Exception ignored) {}
        return false;
    }

    @Override
    public Optional<ScanSummary> load(Host host, Instant startedAt) {
        String fileName = String.format("%s-%d.xml", host.address(), startedAt.getEpochSecond());
        Path path = Paths.get(this.savePath).resolve(fileName);

        try {
            ScanSummary summary = xmlMapper.readValue(path.toFile(), ScanSummary.class);
            return Optional.of(summary);
        } catch (Exception ignored) {}
        return Optional.empty();
    }
}
