package at.ac.hcw.porty.types.records;

import java.io.Serializable;
import java.util.stream.IntStream;

public record PortRange(int start, int end) implements Serializable {
    public PortRange {
        if (start < 1 || end > 65535) {
            if (start != -1 || end != -1) { // -1 is for "Port range not needed"
                throw new IllegalArgumentException("Port range is illegal.");
            }
        }
    }

    public IntStream stream() {
        return IntStream.rangeClosed(start, end);
    }
}
