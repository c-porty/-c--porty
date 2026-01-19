package at.ac.hcw.porty.dto;

import at.ac.hcw.porty.types.records.PortRange;
import javafx.beans.property.*;

import java.time.Duration;

public class ScanConfigDTO {
    private final StringProperty host = new SimpleStringProperty();
    private final BooleanProperty serviceDetection = new SimpleBooleanProperty();
    private final BooleanProperty osDetection = new SimpleBooleanProperty();
    private final BooleanProperty tcpConnectScan = new SimpleBooleanProperty();
    private final BooleanProperty synScan = new SimpleBooleanProperty();
    private final ObjectProperty<Duration> hostTimeout = new SimpleObjectProperty<Duration>();
    private final DoubleProperty statsEvery= new SimpleDoubleProperty();
    private final BooleanProperty includeSubnetMask = new SimpleBooleanProperty();
    private final IntegerProperty subnetMask = new SimpleIntegerProperty();
    private final ObjectProperty<PortRange> portRange = new SimpleObjectProperty<>();
    private final BooleanProperty udpScan = new SimpleBooleanProperty();

    public ScanConfigDTO() {}

    public ScanConfigDTO(
            String host,
            boolean serviceDetection,
            boolean osDetection,
            boolean tcpConnectScan,
            boolean synScan,
            Duration hostTimeout,
            double statsEvery,
            boolean includeSubnetMask,
            int subnetMask,
            PortRange portRange,
            boolean udpScan
    ) {
        this.host.set(host);
        this.serviceDetection.set(serviceDetection);
        this.osDetection.set(osDetection);
        this.tcpConnectScan.set(tcpConnectScan);
        this.synScan.set(synScan);
        this.hostTimeout.set(hostTimeout);
        this.statsEvery.set(statsEvery);
        this.includeSubnetMask.set(includeSubnetMask);
        this.subnetMask.set(subnetMask);
        this.portRange.set(portRange);
        this.udpScan.set(udpScan);
    }

    public boolean getUdpScan() {
        return udpScan.get();
    }

    public BooleanProperty getUdpScanProperty() {
        return udpScan;
    }

    public String getHost() {
        return host.get();
    }

    public StringProperty hostProperty() {
        return host;
    }

    public double getStatsEvery() {
        return statsEvery.get();
    }

    public DoubleProperty statsEveryProperty() {
        return statsEvery;
    }

    public Duration getHostTimeout() {
        return hostTimeout.get();
    }

    public ObjectProperty<Duration> hostTimeoutProperty() {
        return hostTimeout;
    }

    public boolean isSynScan() {
        return synScan.get();
    }

    public BooleanProperty synScanProperty() {
        return synScan;
    }

    public boolean isTcpConnectScan() {
        return tcpConnectScan.get();
    }

    public BooleanProperty tcpConnectScanProperty() {
        return tcpConnectScan;
    }

    public boolean isOsDetection() {
        return osDetection.get();
    }

    public BooleanProperty osDetectionProperty() {
        return osDetection;
    }

    public boolean isServiceDetection() {
        return serviceDetection.get();
    }

    public BooleanProperty serviceDetectionProperty() {
        return serviceDetection;
    }

    public boolean isIncludeSubnetMask() {
        return includeSubnetMask.get();
    }

    public boolean isUdpScan() { return udpScan.get(); }

    public BooleanProperty includeSubnetMaskProperty() {
        return includeSubnetMask;
    }

    public int getSubnetMask() {
        return subnetMask.get();
    }

    public IntegerProperty subnetMaskProperty() {
        return subnetMask;
    }

    public PortRange getPortRange() {
        return portRange.get();
    }

    public ObjectProperty<PortRange> portRangeProperty() {
        return portRange;
    }

    public void setHost(String host){
        this.host.set(host);
    }

    public void setServiceDetection(boolean state){this.serviceDetection.set(state);}
    public void setOsDetection(boolean state){this.osDetection.set(state);}
    public void setTcpConnectScan(boolean state){this.tcpConnectScan.set(state);}
    public void setSynScan(boolean state){this.synScan.set(state);}
    public void setUdpScan(boolean udpScan) { this.udpScan.set(udpScan); }

    public void setStatsEvery(double stats){this.statsEvery.set(stats);}

    public void setHostTimeout(Duration timeout){this.hostTimeout.set(timeout);}
    public void setHostTimeout(long seconds){this.hostTimeout.set(Duration.ofSeconds(seconds));}

    public void setIncludeSubnetMask(boolean includeSubnetMask){this.includeSubnetMask.set(includeSubnetMask);}
    public void setSubnetMask(int subnetMask){this.subnetMask.set(subnetMask);}

    public void setPortRange(PortRange portRange){ this.portRange.set(portRange);}
    public void setPortRange(int start, int end){ this.portRange.set(new PortRange(start, end));}
}
