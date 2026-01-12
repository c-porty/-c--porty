package at.ac.hcw.porty.utils;

import at.ac.hcw.porty.types.enums.AddressType;
import at.ac.hcw.porty.types.records.NmapOptions;
import at.ac.hcw.porty.types.records.ScanConfig;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class NmapCommandBuilder {
    public static ProcessBuilder buildNmapCommand(ScanConfig config, String path, Path tempOutputFile) {
        final NmapOptions options = config.options();
        String host = config.host().address();
        Integer subnet = config.host().subnet();
        AddressType type = config.host().type();
        int startPort = config.range().start();
        int endPort = config.range().end();
        String portSpec = String.format("%d-%d", startPort, endPort);

        // workaround for root privileges
        boolean needsPrivilegedRights = config.options().synScan() || config.options().osDetection();
        String os = System.getProperty("os.name").toLowerCase();
        boolean canAddDetectionWithPrivileges = false;

        List<String> nmapCommand = new ArrayList<>();

        if (needsPrivilegedRights) {
            if (!os.contains("win") && !os.contains("mac")) {
                // on linux pkexec is available to run nmap as root
                nmapCommand.add("pkexec");
                canAddDetectionWithPrivileges = true;
            }
        }
        nmapCommand.add(path);

        if (options.serviceDetection()) {
            nmapCommand.add("-sV"); // version detection for services detected
        }
        if (options.osDetection() && canAddDetectionWithPrivileges) {
            nmapCommand.add("-O");
        }
        if (options.tcpConnectScan()) {
            nmapCommand.add("-sT");
        }
        if (options.synScan() && canAddDetectionWithPrivileges) {
            nmapCommand.add("-sS"); // stealth mode
        }
        if (options.hostTimeout().getSeconds() != -1) {
            nmapCommand.add("--host-timeout");
            nmapCommand.add(String.format("%ds", options.hostTimeout().getSeconds()));
        }
        if (options.statsEvery() != -1) {
            nmapCommand.add("-v");  // to enable even more verbose stats
            nmapCommand.add("--stats-every");
            nmapCommand.add(options.statsEvery() + "s");
        }
        // if the user wishes to scan a port range, include it into the command
        if (startPort != -1 && endPort != -1) {
            nmapCommand.add("-p");
            nmapCommand.add(portSpec);
        }

        // these options must always be included
        nmapCommand.add("-oX"); // output as XML
        nmapCommand.add(tempOutputFile.toString());

        String target = host;
        if (options.includeSubnet() && subnet != null) {
            if (type == AddressType.IPv6) {
                nmapCommand.add("-6");  // force nmap to use IPv6
            }
            target = String.format("%s/%d", host, subnet);
        }

        nmapCommand.add(target);

        System.out.println(nmapCommand);
        return new ProcessBuilder(nmapCommand);
    }
}
