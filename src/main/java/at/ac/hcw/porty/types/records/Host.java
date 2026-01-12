package at.ac.hcw.porty.types.records;

import java.io.Serializable;

import at.ac.hcw.porty.types.enums.AddressType;
import java.net.IDN;
import java.util.regex.Pattern;

import com.google.common.net.InetAddresses;

public record Host(String address, Integer subnet, AddressType type) implements Serializable {
    private static final Pattern DOMAIN_PATTERN = Pattern.compile(
            "^(?=.{1,253}$)(?!-)[A-Za-z0-9-]{1,63}(?<!-)" +
                    "(\\.(?!-)[A-Za-z0-9-]{1,63}(?<!-))*$"
    );

    public Host {
        if (address == null || address.isBlank()) {
            throw new IllegalArgumentException("Host address is required!");
        }
        address = address.trim();

        AddressType detectedType = detectType(address);
        if (subnet != null) {
            if (detectedType == AddressType.DOMAIN) {
                subnet = null; // domains don't use CIDR
            } else {
                int max = detectedType == AddressType.IPv6 ? 128 : 32;
                if (subnet < 0 || subnet > max) {
                    throw new IllegalArgumentException("Subnet is not a valid CIDR notation.");
                }
            }
        }

        type = detectedType;
    }

    public Host(String address) {
        this(address, null, null);
    }

    public Host(String address, Integer subnet) {
        this(address, subnet, null);
    }

    private static AddressType detectType(String address) {
        boolean ipAddress = InetAddresses.isInetAddress(address);
        if (ipAddress) {
            return InetAddresses.forString(address).getAddress().length == 4
                    ? AddressType.IPv4
                    : AddressType.IPv6;
        }

        try {
            String asciiDomain = IDN.toASCII(address, IDN.ALLOW_UNASSIGNED);
            if (DOMAIN_PATTERN.matcher(asciiDomain).matches()) {
                return AddressType.DOMAIN;
            }
        } catch (IllegalArgumentException ignored) {}

        throw new IllegalArgumentException("Illegal host argument for address");
    }
}
