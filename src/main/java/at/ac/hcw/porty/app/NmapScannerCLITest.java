package at.ac.hcw.porty.app;

import at.ac.hcw.porty.listeners.PortScanCLIListener;
import at.ac.hcw.porty.scanner.Scanner;
import at.ac.hcw.porty.scanner.ScannerFactory;
import at.ac.hcw.porty.types.records.*;
import at.ac.hcw.porty.types.enums.ScanStrategy;
import at.ac.hcw.porty.types.interfaces.PortScanListener;
import at.ac.hcw.porty.types.interfaces.ScanHandle;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.io.ObjectInputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;

public class NmapScannerCLITest {
    private static final Logger logger =
            LoggerFactory.getLogger(NmapScannerCLITest.class);

    public static void main(String[] args) throws JsonProcessingException {
        // possible hosts for tests (that are not "illegal" to use: scanme.nmap.org, webxio.at (my own domain)
        NmapOptions options = new NmapOptions(true, false, true);
        ScanConfig config = new ScanConfig(new Host("webxio.at"), new PortRange(10, 500), options);
        Scanner scanner = new Scanner(ScannerFactory.create(ScanStrategy.NMAP));

        PortScanListener[] listeners = { new PortScanCLIListener() };
        ScanHandle handle = scanner.scan(config, listeners);

        handle.summary().join();
        logger.debug("Done with {}.", scanner.getScanner().name());
    }
}
