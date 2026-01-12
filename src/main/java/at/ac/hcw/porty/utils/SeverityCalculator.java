package at.ac.hcw.porty.utils;

import at.ac.hcw.porty.types.enums.PortStatus;
import at.ac.hcw.porty.types.records.PortScanResult;
import at.ac.hcw.porty.types.records.ScanSummary;

import java.util.List;
import java.util.Locale;
import java.util.Map;

public final class SeverityCalculator {
    private static final Map<String, Double> SERVICE_PORT_RISK = Map.ofEntries(
            Map.entry("23", 0.9),   // telnet port
            Map.entry("445", 0.9),  // smb port
            Map.entry("3389", 0.9), // rdp port
            Map.entry("5900", 0.8), // vnc port
            Map.entry("6379", 0.8), // redis port
            Map.entry("27017", 0.8),// mongodb port
            Map.entry("9200", 0.8), // elasticsearch port
            Map.entry("139", 0.7),  // netbios port
            Map.entry("21", 0.6),   // ftp port
            Map.entry("3306", 0.6), // mysql port
            Map.entry("5432", 0.6), // postgres port
            Map.entry("25", 0.4),   // smtp port
            Map.entry("110", 0.35), // pop3 port
            Map.entry("143", 0.35), // imap port
            Map.entry("80", 0.1),   // http port
            Map.entry("53", 0.05),  // dns port
            Map.entry("443", 0.02), // https port
            Map.entry("22", 0.02),  // ssh port

            Map.entry("telnet", 0.9),
            Map.entry("rdp", 0.9),
            Map.entry("smb", 0.9),
            Map.entry("cifs", 0.9),
            Map.entry("vnc", 0.8),
            Map.entry("redis", 0.7),
            Map.entry("mongo", 0.7),
            Map.entry("elastic", 0.7),
            Map.entry("ftp", 0.4),
            Map.entry("smtp", 0.35),
            Map.entry("pop3", 0.35),
            Map.entry("imap", 0.35),
            Map.entry("http", 0.1),
            Map.entry("https", 0.02),
            Map.entry("ssh", 0.02),
            Map.entry("dns", 0.05),
            Map.entry("snmp", 0.3)
    );

    public static float calculateSeverity(ScanSummary summary) {
        List<PortScanResult> results = summary.results();
        if (results == null || results.isEmpty()) return 0.0f;

        double S = 0.0;
        int openCount = 0;
        int filteredCount = 0;

        String os = summary.detectedOs();
        String osNorm = os == null ? "" : os.toLowerCase(Locale.ROOT);

        for (PortScanResult r : results) {
            if (r == null) continue;

            if (r.status() == PortStatus.OPEN) {
                openCount++;
                S += scoreOpenPort(r, osNorm);
            } else if (r.status() == PortStatus.FILTERED) {
                filteredCount++;
            }
        }

        S += 0.05 * filteredCount;  // many filtered ports penalty

        // Growth for broad exposure: +0.05 if > 3 open ports
        if (openCount > 3) {
            S += 0.05 * (openCount - 3);
        }

        double severity = 1.0 - Math.exp(-0.5 * S);

        return (float) Math.min(Math.max(severity, 0.0), 1.0);
    }

    private static double scoreOpenPort(PortScanResult r, String osNorm) {
        String svc = r.service() == null ? "" : r.service().toLowerCase(Locale.ROOT);
        String portKey = String.valueOf(r.port());

        // Lookup unified risk by service name or port number
        double risk = SERVICE_PORT_RISK.getOrDefault(svc, SERVICE_PORT_RISK.getOrDefault(portKey, 0.2));

        if (!osNorm.isEmpty()) {
            String os = osNorm.toLowerCase();
            if (os.contains("win")) {
                if (r.port() == 445 || r.port() == 3389 || svc.contains("smb") || svc.contains("rdp")) {
                    risk += 0.1;
                }
            } else if (os.contains("linux") || os.contains("unix")) {
                if (r.port() == 23 || svc.contains("telnet") || r.port() == 21 || svc.contains("ftp")) {
                    risk += 0.1;
                }
            }
        }

        return Math.min(risk, 1.0);
    }
}