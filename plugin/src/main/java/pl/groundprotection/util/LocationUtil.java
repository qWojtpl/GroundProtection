package pl.groundprotection.util;

import org.bukkit.Location;

public class LocationUtil {

    public static String locationBuilder(Location location) {
        return "§b"
                + (int) location.getX() + "§5, §b"
                + (int) location.getY() + "§5, §b"
                + (int) location.getZ()
                + ((location.getWorld() != null) ? " §5(§b" + location.getWorld().getName() + "§5)" : "");
    }

}
