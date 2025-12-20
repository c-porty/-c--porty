package at.ac.hcw.porty.types.interfaces;

import at.ac.hcw.porty.types.ScanConfig;

public interface PortScanner {
    String name();
    ScanHandle scan(ScanConfig config, PortScanListener listener);
}
