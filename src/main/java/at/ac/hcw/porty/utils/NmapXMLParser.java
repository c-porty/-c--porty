package at.ac.hcw.porty.utils;

import at.ac.hcw.porty.types.enums.PortStatus;
import at.ac.hcw.porty.types.records.Host;
import at.ac.hcw.porty.types.records.PortScanResult;
import at.ac.hcw.porty.types.records.ScanConfig;
import org.w3c.dom.*;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public final class NmapXMLParser {
    public List<PortScanResult> parse(InputStream xml, ScanConfig config) throws Exception {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(false);
        dbf.setIgnoringComments(true);
        dbf.setIgnoringElementContentWhitespace(true);
        DocumentBuilder db = dbf.newDocumentBuilder();
        Document doc = db.parse(xml);
        doc.getDocumentElement().normalize();

        List<PortScanResult> out = new ArrayList<>();

        NodeList hostNodes = doc.getElementsByTagName("host");
        for (int hi = 0; hi < hostNodes.getLength(); hi++) {
            Element hostEl = (Element) hostNodes.item(hi);

            // Determine OS (highest-accuracy osmatch) for this host
            String os = extractOs(hostEl);

            // Iterate ports
            NodeList portNodes = hostEl.getElementsByTagName("port");
            for (int pi = 0; pi < portNodes.getLength(); pi++) {
                Element portEl = (Element) portNodes.item(pi);

                int port = Integer.parseInt(portEl.getAttribute("portid"));

                // status
                Element stateEl = (Element) portEl.getElementsByTagName("state").item(0);
                String state = stateEl != null ? stateEl.getAttribute("state") : "unknown";
                PortStatus status = mapState(state);

                // service info (name, product, version, extrainfo)
                Element svcEl = (Element) portEl.getElementsByTagName("service").item(0);
                String service = buildServiceString(svcEl);

                String host = extractHost(hostEl);
                out.add(new PortScanResult(
                    new Host(host, config.host().subnet()),
                    port,
                    status,
                    service,
                    os
                ));
            }
        }

        out.sort(Comparator.comparingInt(PortScanResult::port));
        return out;
    }

    private PortStatus mapState(String state) {
        return switch (state) {
            case "open" -> PortStatus.OPEN;
            case "closed", "unfiltered" -> PortStatus.CLOSED;
            case "filtered", "open|filtered", "closed|filtered" -> PortStatus.FILTERED;
            default -> PortStatus.ERROR;
        };
    }

    private String buildServiceString(Element svcEl) {
        if (svcEl == null) return "";
        String name = attr(svcEl, "name");
        String product = attr(svcEl, "product");
        String version = attr(svcEl, "version");
        String extra = attr(svcEl, "extrainfo");

        StringBuilder sb = new StringBuilder();
        if (!name.isBlank()) {
            sb.append(name);
        }

        // Append product/version like " (OpenSSH 9.6p1)"
        if (!product.isBlank() || !version.isBlank()) {
            sb.append(" (");
            if (!product.isBlank()) sb.append(product);
            if (!version.isBlank()) {
                if (!product.isBlank()) sb.append(" ");
                sb.append(version);
            }
            sb.append(")");
        }

        // Append extra info (e.g., "protocol 2.0")
        if (!extra.isBlank()) {
            if (!sb.isEmpty()) sb.append(" ");
            sb.append(extra);
        }
        return sb.toString();
    }

    private String extractOs(Element hostEl) {
        // Nmap host -> <os> contains <osmatch name="..." accuracy="...">; pick highest accuracy
        Element osEl = (Element) hostEl.getElementsByTagName("os").item(0);
        if (osEl == null) return "";

        NodeList matches = osEl.getElementsByTagName("osmatch");
        String bestName = "";
        int bestAcc = -1;

        for (int i = 0; i < matches.getLength(); i++) {
            Element m = (Element) matches.item(i);
            String name = attr(m, "name");
            int acc = parseIntSafe(attr(m, "accuracy"));
            if (acc > bestAcc && !name.isBlank()) {
                bestAcc = acc;
                bestName = name;
            }
        }

        // Optional: enrich with the first osclass (vendor/family/generation)
        if (bestName.isBlank()) {
            NodeList classes = osEl.getElementsByTagName("osclass");
            if (classes.getLength() > 0) {
                Element c = (Element) classes.item(0);
                String vendor = attr(c, "vendor");
                String family = attr(c, "osfamily");
                String gen = attr(c, "osgen");
                String type = attr(c, "type");
                StringBuilder sb = new StringBuilder();
                if (!vendor.isBlank()) sb.append(vendor).append(" ");
                if (!family.isBlank()) sb.append(family).append(" ");
                if (!gen.isBlank()) sb.append(gen).append(" ");
                if (!type.isBlank()) {
                    if (!sb.isEmpty()) sb.append("; ");
                    sb.append(type);
                }
                bestName = sb.toString().trim();
            }
        }

        return bestName;
    }

    private String extractHost(Element hostEl) {
        // 1. Prefer hostnames (domain / DNS)
        NodeList hostnamesEl = hostEl.getElementsByTagName("hostnames");
        if (hostnamesEl.getLength() > 0) {
            NodeList names = ((Element) hostnamesEl.item(0))
                    .getElementsByTagName("hostname");

            String user = "";
            String ptr  = "";

            for (int i = 0; i < names.getLength(); i++) {
                Element hn = (Element) names.item(i);
                String name = hn.getAttribute("name");
                String type = hn.getAttribute("type");

                if ("user".equals(type) && !name.isBlank()) {
                    return name;
                }
                if (("PTR".equals(type) || "DNS".equals(type)) && ptr.isBlank()) {
                    ptr = name;
                }
            }

            if (!ptr.isBlank()) return ptr;
        }

        // 2. Fallback to addresses
        NodeList addrNodes = hostEl.getElementsByTagName("address");

        String ipv4 = "";
        String ipv6 = "";
        String mac  = "";

        for (int i = 0; i < addrNodes.getLength(); i++) {
            Element addrEl = (Element) addrNodes.item(i);
            String type = addrEl.getAttribute("addrtype");
            String addr = addrEl.getAttribute("addr");

            switch (type) {
                case "ipv4" -> ipv4 = addr;
                case "ipv6" -> ipv6 = addr;
                case "mac"  -> mac = addr;
            }
        }

        if (!ipv4.isBlank()) return ipv4;
        if (!ipv6.isBlank()) return ipv6;
        return mac;
    }

    private String attr(Element el, String name) {
        String v = el.getAttribute(name);
        return v.trim();
    }

    private int parseIntSafe(String s) {
        try {
            return Integer.parseInt(s);
        } catch (Exception e) {
            return -1;
        }
    }
}