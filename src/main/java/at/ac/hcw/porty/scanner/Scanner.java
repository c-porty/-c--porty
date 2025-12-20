package at.ac.hcw.porty.scanner;

import at.ac.hcw.porty.types.ScanConfig;
import at.ac.hcw.porty.types.interfaces.PortScanListener;
import at.ac.hcw.porty.types.interfaces.PortScanner;
import at.ac.hcw.porty.types.interfaces.ScanHandle;

public final class Scanner {
    private PortScanner scanner;

    public Scanner(PortScanner scanner) {
        this.scanner = scanner;
    }

    public void setScanner(PortScanner scanner) {
        this.scanner = scanner;
    }

    public PortScanner getScanner() {
        return this.scanner;
    }

    public ScanHandle scan(ScanConfig config, PortScanListener listener) {
        if (scanner == null) throw new IllegalStateException("No scanner is set, aborting!");
        System.out.println("Executing scan with: " + scanner.name());
        return scanner.scan(config, listener);
    }
}
