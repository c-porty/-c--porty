package at.ac.hcw.porty.utils;

import java.net.*;
import java.util.Enumeration;

public final class NetUtils {
    public static String getLanIPv4Address() {
        InetAddress best = null;
        int bestScore = -1;

        try {
            Enumeration<NetworkInterface> nics = NetworkInterface.getNetworkInterfaces();
            while (nics.hasMoreElements()) {
                NetworkInterface nic = nics.nextElement();
                if (!nic.isUp() || nic.isVirtual()) continue;

                Enumeration<InetAddress> addrs = nic.getInetAddresses();
                while (addrs.hasMoreElements()) {
                    InetAddress addr = addrs.nextElement();
                    if (!(addr instanceof Inet4Address)) continue;

                    // prefer LAN, fallback to any non-loopback
                    int score = addr.isSiteLocalAddress() ? 3 : !addr.isLoopbackAddress() ? 2 : 1;

                    if (score > bestScore) {
                        bestScore = score;
                        best = addr;
                    }

                    // cannot get better than lan address
                    if (bestScore == 3) {
                        System.out.println();
                        return best.getHostAddress();
                    }
                }
            }

            return best != null ? best.getHostAddress() : InetAddress.getLocalHost().getHostAddress();
        } catch (Exception e) {
            return null;
        }
    }
}