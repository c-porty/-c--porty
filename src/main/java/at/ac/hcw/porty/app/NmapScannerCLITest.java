package at.ac.hcw.porty.app;

import at.ac.hcw.porty.listeners.PortScanCLIListener;
import at.ac.hcw.porty.repositories.ScanResultRepositoryFactory;
import at.ac.hcw.porty.scanner.Scanner;
import at.ac.hcw.porty.scanner.ScannerFactory;
import at.ac.hcw.porty.types.enums.PortStatus;
import at.ac.hcw.porty.types.enums.ScanResultRepositoryOption;
import at.ac.hcw.porty.types.interfaces.IScanResultRepository;
import at.ac.hcw.porty.types.records.*;
import at.ac.hcw.porty.types.enums.ScanStrategy;
import at.ac.hcw.porty.types.interfaces.PortScanListener;
import at.ac.hcw.porty.types.interfaces.ScanHandle;
import at.ac.hcw.porty.utils.HistoryHandler;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sound.sampled.Port;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class NmapScannerCLITest {
    private static final Logger logger =
            LoggerFactory.getLogger(NmapScannerCLITest.class);

    public static void main(String[] args) throws JsonProcessingException {
        // possible hosts for tests (that are not "illegal" to use: scanme.nmap.org, webxio.at (my own domain)
        NmapOptions options = new NmapOptions(
            false,
            true,
            false,
            true,
            Duration.ofSeconds(15),
            2,
            true,
            true
        );
        ScanConfig config = new ScanConfig(new Host("scanme.nmap.org"), new PortRange(-1, -1), options);
        Scanner scanner = new Scanner(ScannerFactory.create(ScanStrategy.NMAP));

        PortScanListener[] listeners = { new PortScanCLIListener() };
        ScanHandle handle = scanner.scan(config, listeners);

        ScanSummary summary = handle.summary().join();
        logger.debug("Done with {}.", scanner.getScanner().name());
        System.out.println(summary.severityPercent());

        IScanResultRepository repositoryJSON = ScanResultRepositoryFactory.create(ScanResultRepositoryOption.JSON);
        IScanResultRepository repositoryBIN = ScanResultRepositoryFactory.create(ScanResultRepositoryOption.BINARY);

        HistoryHandler history = new HistoryHandler(IScanResultRepository.savePath, List.of(repositoryJSON, repositoryBIN));

        ArrayList<ScanSummary> all = history.loadAll();
        System.out.printf("Found %d entries.%n", all.size());
        System.out.println(all.getLast());
    }
}
