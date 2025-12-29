package at.ac.hcw.porty.types.interfaces;

import at.ac.hcw.porty.types.records.ScanConfig;

public interface IPortScanner {
    String name();
    ScanHandle scan(ScanConfig config, PortScanListener[] listener);
}
