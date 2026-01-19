package at.ac.hcw.porty.types.records;

import java.io.Serializable;
import java.time.Duration;

public record NmapOptions(
        boolean serviceDetection,
        boolean osDetection,
        boolean tcpConnectScan,
        boolean synScan,
        Duration hostTimeout,
        double statsEvery,
        boolean saveScan,
        boolean includeSubnet,
        boolean udpScan
) implements Serializable {
    public NmapOptions {
        // never allow both options to be true
        if (synScan) tcpConnectScan = false;
        if (!synScan && !tcpConnectScan) tcpConnectScan = true;
    }

    public NmapOptions() {
        // default no timeout
        this(true, false, true, false, Duration.ofSeconds(-1), 2, false, false, false);
    }

    public NmapOptions(boolean serviceDetection, boolean osDetection, boolean saveScan) {
        this(serviceDetection, osDetection, true, false, Duration.ofSeconds(-1), 2, saveScan, false, false);
    }

    public NmapOptions(boolean serviceDetection, boolean osDetection, boolean tcpConnectScan, boolean synScan) {
        this(serviceDetection, osDetection, tcpConnectScan, synScan, Duration.ofSeconds(-1), 2, false, false, false);
    }

    public NmapOptions(boolean saveScan){
        this(true, false, true, false, Duration.ofSeconds(-1), 2, saveScan, false, false);
    }
}
