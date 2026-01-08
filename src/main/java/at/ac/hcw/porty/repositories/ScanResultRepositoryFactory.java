package at.ac.hcw.porty.repositories;

import at.ac.hcw.porty.repositories.implementations.BinaryScanResultRepository;
import at.ac.hcw.porty.repositories.implementations.JSONScanResultRepository;
import at.ac.hcw.porty.repositories.implementations.XMLScanResultRepository;
import at.ac.hcw.porty.types.ScanResultRepository;
import at.ac.hcw.porty.types.enums.ScanResultRepositoryOption;

public class ScanResultRepositoryFactory {
    public static ScanResultRepository create(ScanResultRepositoryOption option) {
        return switch (option) {
            case BINARY -> new BinaryScanResultRepository();
            case JSON -> new JSONScanResultRepository();
            case XML -> new XMLScanResultRepository();
        };
    }
}
