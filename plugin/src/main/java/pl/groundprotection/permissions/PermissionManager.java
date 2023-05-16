package pl.groundprotection.permissions;

import org.bukkit.permissions.Permission;
import pl.groundprotection.GroundProtection;

import java.util.HashMap;

public class PermissionManager {

    private final GroundProtection plugin = GroundProtection.getInstance();
    private final HashMap<String, Permission> permissions = new HashMap<>();

    public Permission registerPermission(String id, String name, String description) {
        if(id == null || name == null) return null;
        if(plugin.getServer().getPluginManager().getPermission(name) != null) {
            plugin.getServer().getPluginManager().removePermission(permissions.get(name));
        }
        Permission perm = new Permission(name, description);
        permissions.put(id, perm);
        plugin.getServer().getPluginManager().addPermission(perm);
        return perm;
    }

    public Permission getPermission(String id) {
        return permissions.getOrDefault(id, null);
    }

    public void clearPermissions() {
        for(String id : permissions.keySet()) {
            plugin.getServer().getPluginManager().removePermission(permissions.get(id));
        }
        permissions.clear();
    }

}
