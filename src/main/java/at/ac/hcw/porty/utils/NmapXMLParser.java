package at.ac.hcw.porty.utils;

import at.ac.hcw.porty.types.enums.PortStatus;
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

            NodeList portNodes = hostEl.getElementsByTagName("port");
            for (int pi = 0; pi < portNodes.getLength(); pi++) {
                Element portEl = (Element) portNodes.item(pi);
                int port = Integer.parseInt(portEl.getAttribute("portid"));

                Element stateEl = (Element) portEl.getElementsByTagName("state").item(0);
                String state = stateEl != null ? stateEl.getAttribute("state") : "unknown";

                Element svcEl = (Element) portEl.getElementsByTagName("service").item(0);
                String serviceName = svcEl != null ? svcEl.getAttribute("name") : "";
                String product = svcEl != null ? svcEl.getAttribute("product") : "";
                String version = svcEl != null ? svcEl.getAttribute("version") : "";
                String extra = svcEl != null ? svcEl.getAttribute("extrainfo") : "";

                PortStatus status = mapState(state);
                StringBuilder note = new StringBuilder();
                if (!serviceName.isBlank()) {
                    note.insert(0, "service: " + serviceName);
                    if (!product.isBlank() || !version.isBlank()) {
                        note.append(" (").append(product);
                        if (!version.isBlank()) note.append(" ").append(version);
                        note.append(")");
                    }
                    if (!extra.isBlank()) note.append(" ").append(extra);
                }

                out.add(new PortScanResult(config.host(), port, status, note.toString()));
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
}