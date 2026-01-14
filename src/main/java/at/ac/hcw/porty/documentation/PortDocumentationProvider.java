package at.ac.hcw.porty.documentation;

import at.ac.hcw.porty.types.interfaces.IPortDocumentationProvider;
import at.ac.hcw.porty.types.records.TechnicalReference;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public final class PortDocumentationProvider implements IPortDocumentationProvider {
    private static final Map<Key, TechnicalReference> REFERENCES = createReferences();

    private static Map<Key, TechnicalReference> createReferences() {
        Map<Key, TechnicalReference> map = new HashMap<>();

        // SSH
        map.put(new Key(22, "ssh"), new TechnicalReference(
                URI.create("https://en.wikipedia.org/wiki/Secure_Shell"),
                "SSH (Secure Shell)"
        ));
        // Telnet
        map.put(new Key(23, "telnet"), new TechnicalReference(
                URI.create("https://en.wikipedia.org/wiki/Telnet"),
                "Telnet"
        ));
        // FTP
        map.put(new Key(21, "ftp"), new TechnicalReference(
                URI.create("https://en.wikipedia.org/wiki/File_Transfer_Protocol"),
                "FTP (File Transfer Protocol)"
        ));
        // SMB / CIFS
        map.put(new Key(445, "smb"), new TechnicalReference(
                URI.create("https://en.wikipedia.org/wiki/Server_Message_Block"),
                "SMB (Server Message Block)"
        ));
        map.put(new Key(139, "netbios"), new TechnicalReference(
                URI.create("https://en.wikipedia.org/wiki/NetBIOS"),
                "NetBIOS/SMB"
        ));
        // RDP
        map.put(new Key(3389, "rdp"), new TechnicalReference(
                URI.create("https://en.wikipedia.org/wiki/Remote_Desktop_Protocol"),
                "RDP (Remote Desktop Protocol)"
        ));
        // VNC
        map.put(new Key(5900, "vnc"), new TechnicalReference(
                URI.create("https://en.wikipedia.org/wiki/Virtual_Network_Computing"),
                "VNC (Virtual Network Computing)"
        ));
        // Redis
        map.put(new Key(6379, "redis"), new TechnicalReference(
                URI.create("https://en.wikipedia.org/wiki/Redis"),
                "Redis Database"
        ));
        // MongoDB
        map.put(new Key(27017, "mongo"), new TechnicalReference(
                URI.create("https://en.wikipedia.org/wiki/MongoDB"),
                "MongoDB Database"
        ));
        // Elasticsearch
        map.put(new Key(9200, "elastic"), new TechnicalReference(
                URI.create("https://en.wikipedia.org/wiki/Elasticsearch"),
                "Elasticsearch"
        ));
        // MySQL
        map.put(new Key(3306, "mysql"), new TechnicalReference(
                URI.create("https://en.wikipedia.org/wiki/MySQL"),
                "MySQL Database"
        ));
        // PostgreSQL
        map.put(new Key(5432, "postgres"), new TechnicalReference(
                URI.create("https://en.wikipedia.org/wiki/PostgreSQL"),
                "PostgreSQL Database"
        ));
        // SMTP
        map.put(new Key(25, "smtp"), new TechnicalReference(
                URI.create("https://en.wikipedia.org/wiki/Simple_Mail_Transfer_Protocol"),
                "SMTP (Email)"
        ));
        // POP3
        map.put(new Key(110, "pop3"), new TechnicalReference(
                URI.create("https://en.wikipedia.org/wiki/Post_Office_Protocol"),
                "POP3 Email Protocol"
        ));
        // IMAP
        map.put(new Key(143, "imap"), new TechnicalReference(
                URI.create("https://en.wikipedia.org/wiki/Internet_Message_Access_Protocol"),
                "IMAP Email Protocol"
        ));
        // HTTP / HTTPS
        map.put(new Key(80, "http"), new TechnicalReference(
                URI.create("https://en.wikipedia.org/wiki/Hypertext_Transfer_Protocol"),
                "HTTP"
        ));
        map.put(new Key(443, "https"), new TechnicalReference(
                URI.create("https://en.wikipedia.org/wiki/HTTPS"),
                "HTTPS"
        ));
        // DNS
        map.put(new Key(53, "dns"), new TechnicalReference(
                URI.create("https://en.wikipedia.org/wiki/Domain_Name_System"),
                "DNS"
        ));
        // SNMP
        map.put(new Key(161, "snmp"), new TechnicalReference(
                URI.create("https://en.wikipedia.org/wiki/Simple_Network_Management_Protocol"),
                "SNMP"
        ));

        return map;
    }

    @Override
    public Optional<TechnicalReference> getDocumentation(int port, String service) {
        TechnicalReference reference = REFERENCES.get(new Key(port, normalize(service)));
        return Optional.ofNullable(reference); // null if not found
    }

    private static String normalize(String service) {
        return service == null ? "" : service.toLowerCase();
    }

    private record Key(int port, String service) {}
}
