package at.ac.hcw.porty.types.records;

import at.ac.hcw.porty.utils.SeverityCalculator;

import java.io.Serializable;
import java.time.Instant;
import java.util.List;

public record ScanSummary(
        Host host,
        List<PortScanResult> results,
        String detectedOs,
        ScanConfig config,
        Instant startedAt,
        Instant finishedAt,
        float severity
) implements Serializable {
    public ScanSummary {
        if (Float.isNaN(severity)) severity = 0f;
        if (severity < 0f) severity = 0f;
        if (severity > 1f) severity = 1f;
    }

    public ScanSummary(
            Host host,
            List<PortScanResult> results,
            String detectedOs,
            ScanConfig config,
            Instant startedAt,
            Instant finishedAt
    ) {
        this(
            host, results, detectedOs, config, startedAt, finishedAt,
                SeverityCalculator.calculateSeverity(new ScanSummary(
                        host, results, detectedOs, config, startedAt, finishedAt, 0f
                ))
        );
    }

    public String severityPercent() {
        return Math.round(severity * 100) + "%";
    }
}