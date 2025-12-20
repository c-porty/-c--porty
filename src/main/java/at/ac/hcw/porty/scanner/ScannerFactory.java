package at.ac.hcw.porty.scanner;

import at.ac.hcw.porty.types.PortStatus;
import at.ac.hcw.porty.types.ScanStrategy;
import at.ac.hcw.porty.types.interfaces.PortScanner;

import java.util.Map;

public final class ScannerFactory {
    public static PortScanner create(ScanStrategy strategy) {
        return switch (strategy) {
            case MOCK -> new MockPortScanner(Map.of(22, PortStatus.OPEN, 80, PortStatus.CLOSED));
            case NMAP -> throw new IllegalArgumentException("Not yet implemented");
        };
    }
}
