package at.ac.hcw.porty.scanner;

import at.ac.hcw.porty.listeners.PortScanCLIListener;
import at.ac.hcw.porty.types.ScanConfig;
import at.ac.hcw.porty.types.interfaces.PortScanListener;
import at.ac.hcw.porty.types.interfaces.PortScanner;
import at.ac.hcw.porty.types.interfaces.ScanHandle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class Scanner {
    private static final Logger logger =
            LoggerFactory.getLogger(Scanner.class);

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
        logger.debug("Executing scan with: {}", scanner.name());
        return scanner.scan(config, listener);
    }
}
