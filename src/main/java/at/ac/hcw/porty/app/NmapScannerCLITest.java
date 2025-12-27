package at.ac.hcw.porty.app;

import at.ac.hcw.porty.listeners.PortScanCLIListener;
import at.ac.hcw.porty.scanner.Scanner;
import at.ac.hcw.porty.scanner.ScannerFactory;
import at.ac.hcw.porty.types.Host;
import at.ac.hcw.porty.types.PortRange;
import at.ac.hcw.porty.types.ScanConfig;
import at.ac.hcw.porty.types.ScanStrategy;
import at.ac.hcw.porty.types.interfaces.ScanHandle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;

public class NmapScannerCLITest {
    private static final Logger logger =
            LoggerFactory.getLogger(NmapScannerCLITest.class);

    public static void main(String[] args) {
        // possible hosts for tests (that are not "illegal" to use: scanme.nmap.org, webxio.at (my own domain)
        ScanConfig config = new ScanConfig(new Host("scanme.nmap.org"), new PortRange(-1, -1), Duration.ofMillis(100), 0.5);
        Scanner scanner = new Scanner(ScannerFactory.create(ScanStrategy.NMAP));

        ScanHandle handle = scanner.scan(config, new PortScanCLIListener());

        handle.summary().join();
        logger.debug("Done with {}.", scanner.getScanner().name());
    }
}
