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

        boolean needsPrivilegedRights = options.synScan() || options.osDetection();
        String os = System.getProperty("os.name").toLowerCase();

        List<String> nmapArgs = new ArrayList<>();
        nmapArgs.add(path);
        nmapArgs.add("-n");

        if (options.serviceDetection()) nmapArgs.add("-sV");
        if (options.tcpConnectScan()) nmapArgs.add("-sT");
        if (options.synScan()) nmapArgs.add("-sS");
        if (options.osDetection()) nmapArgs.add("-O");

        if (options.hostTimeout().getSeconds() != -1) {
            nmapArgs.add("--host-timeout");
            nmapArgs.add(options.hostTimeout().getSeconds() + "s");
        }

        if (options.statsEvery() != -1) {
            nmapArgs.add("-v");
            nmapArgs.add("--stats-every");
            nmapArgs.add(options.statsEvery() + "s");
        }

        if (startPort != -1 && endPort != -1) {
            nmapArgs.add("-p");
            nmapArgs.add(startPort + "-" + endPort);
        }

        nmapArgs.add("-oX");
        nmapArgs.add(tempOutputFile.toString());

        String target = host;
        if (options.includeSubnet() && subnet != null) {
            if (type == AddressType.IPv6) nmapArgs.add("-6");
            target = host + "/" + subnet;
        }
        nmapArgs.add(target);

        if (needsPrivilegedRights && os.contains("mac")) {
            // Build ONE shell command
            StringBuilder cmd = new StringBuilder();
            for (String a : nmapArgs) {
                cmd.append(escapeForShell(a)).append(" ");
            }

            String appleScript =
                    "do shell script " +
                            "\"" + cmd.toString().trim().replace("\\", "\\\\").replace("\"", "\\\"") + "\"" +
                            " with administrator privileges";

            return new ProcessBuilder("osascript", "-e", appleScript);
        }

        // Linux
        if (needsPrivilegedRights && os.contains("linux")) {
            nmapArgs.add(0, "pkexec");
        }

        return new ProcessBuilder(nmapArgs);
    }

    private static String escapeForShell(String s) {
        // Wrap in single quotes; if single quotes present, break/escape them
        if (s.contains("'")) {
            return "'" + s.replace("'", "'\"'\"'") + "'";
        }
        return "'" + s + "'";
    }
}
