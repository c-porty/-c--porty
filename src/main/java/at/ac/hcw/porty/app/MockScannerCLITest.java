package at.ac.hcw.porty.app;

import at.ac.hcw.porty.listeners.PortScanCLIListener;
import at.ac.hcw.porty.scanner.Scanner;
import at.ac.hcw.porty.scanner.ScannerFactory;
import at.ac.hcw.porty.types.enums.ScanStrategy;
import at.ac.hcw.porty.types.interfaces.PortScanListener;
import at.ac.hcw.porty.types.interfaces.ScanHandle;
import at.ac.hcw.porty.types.records.Host;
import at.ac.hcw.porty.types.records.NmapOptions;
import at.ac.hcw.porty.types.records.PortRange;
import at.ac.hcw.porty.types.records.ScanConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;

public class MockScannerCLITest {
    private static final Logger logger =
            LoggerFactory.getLogger(MockScannerCLITest.class);

    public static void main(String[] args) {
        NmapOptions options = new NmapOptions();    // has no effect on the mock scanner anyways
        ScanConfig config = new ScanConfig(new Host("localhost"), new PortRange(1, 50), options);
        Scanner scanner = new Scanner(ScannerFactory.create(ScanStrategy.MOCK));

        PortScanListener[] listeners = { new PortScanCLIListener() };
        ScanHandle handle = scanner.scan(config, listeners);

        // handle.cancel();

        handle.summary().join();
        logger.debug("Done with {}.", scanner.getScanner().name());
    }
}
