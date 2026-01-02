package at.ac.hcw.porty.types.records;

import java.time.Duration;

public record NmapOptions(
        boolean serviceDetection,
        boolean osDetection,
        boolean tcpConnectScan,
        boolean synScan,
        Duration hostTimeout,
        double statsEvery
) {
    public NmapOptions() {
        // default no timeout
        // for synScan elevated rights are needed...(root privileges)
        // for osProbing elevated rights are needed
        this(true, false, true, false, Duration.ofSeconds(-1), 2);
    }
}
