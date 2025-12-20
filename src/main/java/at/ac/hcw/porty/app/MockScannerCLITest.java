package at.ac.hcw.porty.app;

import at.ac.hcw.porty.listeners.PortScanCLIListener;
import at.ac.hcw.porty.scanner.MockPortScanner;
import at.ac.hcw.porty.scanner.Scanner;
import at.ac.hcw.porty.types.*;
import at.ac.hcw.porty.types.interfaces.PortScanListener;
import at.ac.hcw.porty.types.interfaces.ScanHandle;

import java.time.Duration;
import java.util.Map;

public class MockScannerCLITest {
    public static void main(String[] args) {
        MockPortScanner mockScanner = new MockPortScanner(Map.of(22, PortStatus.OPEN, 80, PortStatus.CLOSED));
        ScanConfig config = new ScanConfig(new Host("localhost"), new PortRange(1, 50), Duration.ofMillis(100));
        Scanner scanner = new Scanner(mockScanner);

        ScanHandle handle = scanner.scan(config, new PortScanCLIListener());

        handle.summary().join();
        System.out.println("Done with MockPortScanner.");
    }
}
