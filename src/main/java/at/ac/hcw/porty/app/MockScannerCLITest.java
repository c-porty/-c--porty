package at.ac.hcw.porty.app;

import at.ac.hcw.porty.listeners.PortScanCLIListener;
import at.ac.hcw.porty.scanner.Scanner;
import at.ac.hcw.porty.scanner.ScannerFactory;
import at.ac.hcw.porty.types.*;
import at.ac.hcw.porty.types.interfaces.ScanHandle;

import java.time.Duration;

public class MockScannerCLITest {
    public static void main(String[] args) {
        ScanConfig config = new ScanConfig(new Host("localhost"), new PortRange(1, 50), Duration.ofMillis(100));
        Scanner scanner = new Scanner(ScannerFactory.create(PortStatus.ScanStrategy.MOCK));

        ScanHandle handle = scanner.scan(config, new PortScanCLIListener());

        handle.summary().join();
        System.out.println("Done with MockPortScanner.");
    }
}
