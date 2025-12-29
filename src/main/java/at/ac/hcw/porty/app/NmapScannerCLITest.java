package at.ac.hcw.porty.app;

import at.ac.hcw.porty.listeners.PortScanCLIListener;
import at.ac.hcw.porty.scanner.Scanner;
import at.ac.hcw.porty.scanner.ScannerFactory;
import at.ac.hcw.porty.types.records.Host;
import at.ac.hcw.porty.types.records.PortRange;
import at.ac.hcw.porty.types.records.ScanConfig;
import at.ac.hcw.porty.types.enums.ScanStrategy;
import at.ac.hcw.porty.types.interfaces.PortScanListener;
import at.ac.hcw.porty.types.interfaces.ScanHandle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;

public class NmapScannerCLITest {
    private static final Logger logger =
            LoggerFactory.getLogger(NmapScannerCLITest.class);

    public static void main(String[] args) {
        // possible hosts for tests (that are not "illegal" to use: scanme.nmap.org, webxio.at (my own domain)
        ScanConfig config = new ScanConfig(new Host("scanme.nmap.org"), new PortRange(10, 500), Duration.ofMillis(100), 0.5);
        Scanner scanner = new Scanner(ScannerFactory.create(ScanStrategy.NMAP));

        PortScanListener[] listeners = { new PortScanCLIListener() };
        ScanHandle handle = scanner.scan(config, listeners);

        handle.summary().join();
        logger.debug("Done with {}.", scanner.getScanner().name());
    }
}
