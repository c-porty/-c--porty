package at.ac.hcw.porty.documentation;

import at.ac.hcw.porty.types.interfaces.IPortDocumentationProvider;
import at.ac.hcw.porty.types.records.PortScanResult;
import at.ac.hcw.porty.types.records.TechnicalReference;
import at.ac.hcw.porty.types.records.TechnicalReferenceJSONEntry;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;

import java.io.InputStream;
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public final class PortDocumentationProvider implements IPortDocumentationProvider {
    private final static Map<Key, TechnicalReference> REFERENCES = loadReferences();

    private static Map<Key, TechnicalReference> loadReferences() {
        Map<Key, TechnicalReference> map = new HashMap<>();
        ObjectMapper mapper = new ObjectMapper();

        try (InputStream is = PortDocumentationProvider.class
                .getResourceAsStream("/at/ac/hcw/porty/documentation/porty-documentation.json")) {
            if (is == null) {
                throw new RuntimeException("porty-documentation.json not found in resources!");
            }

            List<TechnicalReferenceJSONEntry> entries = mapper.readValue(is, new TypeReference<>() {});
            for (TechnicalReferenceJSONEntry entry : entries) {
                map.put(new Key(entry.port(), normalize(entry.service())),
                        new TechnicalReference(URI.create(entry.uri()), entry.title()));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }


        return map;
    }

    @Override
    public Optional<TechnicalReference> getDocumentation(int port, String service) {
        String normalizedService = normalize(service);
        TechnicalReference ref = REFERENCES.get(new Key(port, normalizedService));

        if (ref == null) {
            ref = REFERENCES.get(new Key(port, ""));
        }

        return Optional.ofNullable(ref);
    }


    private static String normalize(String service) {
        return service == null ? "" : service.toLowerCase();
    }

    private record Key(int port, String service) {}
}
