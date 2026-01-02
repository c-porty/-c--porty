package at.ac.hcw.porty.utils;

import at.ac.hcw.porty.types.records.NmapOptions;
import at.ac.hcw.porty.types.records.ScanConfig;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class NmapCommandBuilder {
    public static ProcessBuilder buildNmapCommand(ScanConfig config, String path, File tempOutputFile) {
        final NmapOptions options = config.options();
        String host = config.host().address();
        int startPort = config.range().start();
        int endPort = config.range().end();
        String portSpec = String.format("%d-%d", startPort, endPort);

        List<String> nmapCommand = new ArrayList<>();
        nmapCommand.add(path);

        if (options.serviceDetection()) {
            nmapCommand.add("-sV"); // version detection for services detected
        }
        if (options.osDetection()) {
            nmapCommand.add("-O");
        }
        if (options.tcpConnectScan()) {
            nmapCommand.add("-sT");
        }
        if (options.synScan()) {
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
        nmapCommand.add(tempOutputFile.getPath());
        nmapCommand.add(host);

        return new ProcessBuilder(nmapCommand);
    }
}
