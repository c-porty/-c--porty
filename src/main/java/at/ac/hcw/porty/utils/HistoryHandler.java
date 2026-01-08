package at.ac.hcw.porty.utils;

import at.ac.hcw.porty.types.interfaces.IScanResultRepository;
import at.ac.hcw.porty.types.records.Host;
import at.ac.hcw.porty.types.records.ScanSummary;

import java.nio.file.*;
import java.time.Instant;
import java.util.*;
import java.util.stream.Stream;

public class HistoryHandler {
    private final Path dir;
    private final Map<String, IScanResultRepository> parsersByExt;

    public HistoryHandler(String savePath, List<IScanResultRepository> parsers) {
        this.dir = Paths.get(savePath);
        this.parsersByExt = new HashMap<>();
        for (IScanResultRepository p : parsers) {
            for (String ext : p.supportedExtensions()) {
                this.parsersByExt.put(ext.toLowerCase(Locale.ROOT), p);
            }
        }
    }

    public ArrayList<ScanSummary> loadAll() {
        return loadAll(null);
    }

    public ArrayList<ScanSummary> loadAll(Host hostFilter) {
        ArrayList<ScanSummary> results = new ArrayList<>();
        if (!Files.isDirectory(dir)) return results;

        String hostAddr = hostFilter != null ? hostFilter.address() : null;

        try (Stream<Path> paths = Files.list(dir)) {
            paths
                    .filter(Files::isRegularFile)
                    .filter(this::isSupportedByExtension)
                    .filter(p -> hostAddr == null || fileBelongsToHost(p, hostAddr))
                    .sorted(Comparator.comparing(Path::getFileName))
                    .forEach(p -> parseInto(results, p));
        } catch (Exception ignored) {}
        return results;
    }

    public Optional<ScanSummary> load(Host host, Instant startedAt) {
        if (!Files.isDirectory(dir)) return Optional.empty();
        String prefix = host.address() + "-" + startedAt.getEpochSecond();

        try (Stream<Path> paths = Files.list(dir)) {
            return paths
                    .filter(Files::isRegularFile)
                    .filter(this::isSupportedByExtension)
                    .filter(p -> p.getFileName().toString().startsWith(prefix + "."))
                    .sorted()
                    .map(this::parseOne)
                    .flatMap(Optional::stream)
                    .findFirst();
        } catch (Exception ignored) {
            return Optional.empty();
        }
    }

    private boolean isSupportedByExtension(Path p) {
        String fn = p.getFileName().toString();
        int dot = fn.lastIndexOf('.');
        if (dot <= 0) return false;
        String ext = fn.substring(dot).toLowerCase(Locale.ROOT);
        return parsersByExt.containsKey(ext);
    }

    private boolean fileBelongsToHost(Path p, String hostAddress) {
        String fn = p.getFileName().toString();
        int dot = fn.lastIndexOf('.');
        if (dot <= 0) return false;
        String base = fn.substring(0, dot);
        int dash = base.lastIndexOf('-');
        if (dash < 0) return false;
        String hostInName = base.substring(0, dash);
        return hostAddress.equals(hostInName);
    }

    private void parseInto(ArrayList<ScanSummary> out, Path p) {
        parseOne(p).ifPresent(out::add);
    }

    private Optional<ScanSummary> parseOne(Path p) {
        String fn = p.getFileName().toString();
        int dot = fn.lastIndexOf('.');
        if (dot <= 0) return Optional.empty();
        String ext = fn.substring(dot).toLowerCase(Locale.ROOT);
        IScanResultRepository parser = parsersByExt.get(ext);
        if (parser == null) return Optional.empty();
        return parser.parse(p);
    }
}