package at.ac.hcw.porty.types;

import at.ac.hcw.porty.types.interfaces.IScanResultRepository;

public abstract class ScanResultRepository implements IScanResultRepository {
    protected final String savePath = "./src/main/saves";
}
