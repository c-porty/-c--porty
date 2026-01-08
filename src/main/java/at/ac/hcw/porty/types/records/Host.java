package at.ac.hcw.porty.types.records;

import java.io.Serializable;

public record Host(String address) implements Serializable {
    public Host {
        if (address == null || address.isBlank()) {
            throw new IllegalArgumentException("Host address is required!");
        }
    }
}
