package at.ac.hcw.porty.scanner;

import at.ac.hcw.porty.types.records.ScanConfig;
import at.ac.hcw.porty.types.interfaces.PortScanListener;
import at.ac.hcw.porty.types.interfaces.IPortScanner;
import at.ac.hcw.porty.types.interfaces.ScanHandle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class Scanner {
    private static final Logger logger =
            LoggerFactory.getLogger(Scanner.class);

    private IPortScanner scanner;

    public Scanner(IPortScanner scanner) {
        this.scanner = scanner;
    }

    public void setScanner(IPortScanner scanner) {
        this.scanner = scanner;
    }

    public IPortScanner getScanner() {
        return this.scanner;
    }

    public ScanHandle scan(ScanConfig config, PortScanListener[] listeners) {
        if (scanner == null) throw new IllegalStateException("No scanner is set, aborting!");
        logger.debug("Executing scan with: {}", scanner.name());
        return scanner.scan(config, listeners);
    }
}
