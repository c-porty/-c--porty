package at.ac.hcw.porty.repositories.implementations;

import at.ac.hcw.porty.types.ScanResultRepository;
import at.ac.hcw.porty.types.records.Host;
import at.ac.hcw.porty.types.records.ScanSummary;

import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.Optional;

public class BinaryScanResultRepository extends ScanResultRepository {
    @Override
    public boolean save(ScanSummary summary) {
        try {
            // ensure the dir is there
            Path dir = Paths.get(this.savePath);
            Files.createDirectories(dir);

            FileOutputStream fileOutputStream =
                    new FileOutputStream(String.format("%s/%s-%d.bin",
                            this.savePath,
                            summary.host().address(),
                            summary.startedAt().getEpochSecond()
                    )
            );
            try (ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream)) {
                objectOutputStream.writeObject(summary);
            }
            return true;
        } catch (Exception ignored) {}
        return false;
    }

    @Override
    public Optional<ScanSummary> load(Host host, Instant startedAt) {
        String filePath = String.format("%s/%s-%d.bin", this.savePath, host.address(), startedAt.getEpochSecond());
        Path path = Paths.get(filePath);

        try (ObjectInputStream in = new ObjectInputStream(Files.newInputStream(path))) {
            ScanSummary summary = (ScanSummary) in.readObject();
            return Optional.of(summary);
        } catch (Exception ignored) {}

        return Optional.empty();
    }
}
