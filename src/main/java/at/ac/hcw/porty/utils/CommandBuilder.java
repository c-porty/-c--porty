package at.ac.hcw.porty.utils;

import at.ac.hcw.porty.types.records.ScanConfig;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class CommandBuilder {
    public static ProcessBuilder buildNmapCommand(ScanConfig config, String path, File tempOutputFile) {
        String host = config.host().address();
        int startPort = config.range().start();
        int endPort = config.range().end();
        String portSpec = String.format("%d-%d", startPort, endPort);

        List<String> nmapCommand = new ArrayList<>();
        nmapCommand.add(path);
        nmapCommand.add("-v");
        nmapCommand.add("--stats-every");
        nmapCommand.add(config.statsTime() + "s");
        nmapCommand.add("--reason");

        // if the user wishes to scan a port range, include it into the command
        if (startPort != -1 && endPort != -1) {
            nmapCommand.add("-p");
            nmapCommand.add(portSpec);
        }

        nmapCommand.add("-oX"); // output as XML
        nmapCommand.add(tempOutputFile.getPath());
        nmapCommand.add(host);

        return new ProcessBuilder(nmapCommand);
    }
}
