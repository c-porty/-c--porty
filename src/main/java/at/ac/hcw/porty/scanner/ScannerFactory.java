package at.ac.hcw.porty.scanner;

import at.ac.hcw.porty.scanner.implementations.MockPortScanner;
import at.ac.hcw.porty.scanner.implementations.NmapPortScanner;
import at.ac.hcw.porty.types.enums.PortStatus;
import at.ac.hcw.porty.types.enums.ScanStrategy;
import at.ac.hcw.porty.types.interfaces.IPortScanner;

import java.util.Map;

public final class ScannerFactory {
    public static IPortScanner create(ScanStrategy strategy) {
        return switch (strategy) {
            case MOCK -> new MockPortScanner(Map.of(22, PortStatus.OPEN, 80, PortStatus.CLOSED));
            case NMAP -> new NmapPortScanner("nmap");
            default -> throw new IllegalArgumentException("Unknown Scan Strategy");
        };
    }
}
