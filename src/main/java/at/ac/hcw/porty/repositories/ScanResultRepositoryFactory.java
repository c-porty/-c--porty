package at.ac.hcw.porty.repositories;

import at.ac.hcw.porty.repositories.implementations.BinaryScanResultRepository;
import at.ac.hcw.porty.repositories.implementations.JSONScanResultRepository;
import at.ac.hcw.porty.types.enums.ScanResultRepositoryOption;
import at.ac.hcw.porty.types.interfaces.IScanResultRepository;

public class ScanResultRepositoryFactory {
    public static IScanResultRepository create(ScanResultRepositoryOption option) {
        return switch (option) {
            case BINARY -> new BinaryScanResultRepository();
            case JSON -> new JSONScanResultRepository();
        };
    }
}
