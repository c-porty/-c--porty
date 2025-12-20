package at.ac.hcw.porty.types;

public record Host(String address) {
    public Host {
        if (address == null || address.isBlank()) {
            throw new IllegalArgumentException("Host address is required!");
        }
    }
}
