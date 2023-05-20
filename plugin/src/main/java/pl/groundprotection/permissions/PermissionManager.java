package pl.groundprotection.permissions;

import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;
import pl.groundprotection.GroundProtection;

import java.util.HashMap;

public class PermissionManager {

    private final GroundProtection plugin = GroundProtection.getInstance();
    private final HashMap<String, Permission> permissions = new HashMap<>();

    public Permission registerPermission(String id, String name, String description) {
        if(id == null || name == null) return null;
        Permission check = plugin.getServer().getPluginManager().getPermission(name);
        if(check != null) {
            plugin.getServer().getPluginManager().removePermission(check);
        }
        Permission perm = new Permission(name, description);
        permissions.put(id, perm);
        plugin.getServer().getPluginManager().addPermission(perm);
        return perm;
    }

    public Permission getPermission(String id) {
        return permissions.getOrDefault(id, new Permission(""));
    }

    public boolean hasPermission(Player player, String id) {
        return player.hasPermission(getPermission(id));
    }

    public boolean checkPermission(Player player, String id) {
        if(!hasPermission(player, id)) {
            player.sendMessage(plugin.getMessages().getMessage("noPermission"));
            return false;
        }
        return true;
    }

    public void clearPermissions() {
        for(String id : permissions.keySet()) {
            plugin.getServer().getPluginManager().removePermission(permissions.get(id));
        }
        permissions.clear();
    }

}
