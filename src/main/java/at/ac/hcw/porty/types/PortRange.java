package at.ac.hcw.porty.types;

import java.util.stream.IntStream;

public record PortRange(int start, int end) {
    public PortRange {
        if (start < 1 || end > 65535) {
            throw new IllegalArgumentException("Port range is illegal.");
        }
    }

    public IntStream stream() {
        return IntStream.rangeClosed(start, end);
    }
}
