package pl.groundprotection.permissions;

import org.bukkit.permissions.Permission;
import pl.groundprotection.GroundProtection;

import java.util.HashMap;

public class PermissionManager {

    private final GroundProtection plugin = GroundProtection.getInstance();
    private final HashMap<String, Permission> permissions = new HashMap<>();

    public Permission registerPermission(String name, String description) {
        plugin.getServer().getPluginManager().removePermission(permissions.get(name));
        Permission perm = new Permission(name, description);
        permissions.put(name, perm);
        plugin.getServer().getPluginManager().addPermission(perm);
        return perm;
    }

    public Permission getPermission(String name) {
        return permissions.getOrDefault(name, null);
    }

    public void clearPermissions() {
        for(String name : permissions.keySet()) {
            plugin.getServer().getPluginManager().removePermission(permissions.get(name));
        }
        permissions.clear();
    }

}
