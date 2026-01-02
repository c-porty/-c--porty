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
    public NmapOptions {
        // never allow both options to be true
        if (synScan) tcpConnectScan = false;
    }

    public NmapOptions() {
        // default no timeout
        this(true, false, true, false, Duration.ofSeconds(-1), 2);
    }

    public NmapOptions(boolean serviceDetection, boolean osDetection) {
        this(serviceDetection, osDetection, true, false, Duration.ofSeconds(-1), 2);
    }

    public NmapOptions(boolean serviceDetection, boolean osDetection, boolean tcpConnectScan, boolean synScan) {
        this(serviceDetection, osDetection, tcpConnectScan, synScan, Duration.ofSeconds(-1), 2);
    }
}
