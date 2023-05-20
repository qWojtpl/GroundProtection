package pl.groundprotection.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;
import pl.groundprotection.GroundProtection;
import pl.groundprotection.permissions.PermissionManager;
import pl.groundprotection.util.PlayerUtil;

import java.util.ArrayList;
import java.util.List;

public class CommandHelper implements TabCompleter {

    private final GroundProtection plugin = GroundProtection.getInstance();
    private final PermissionManager permissionManager = plugin.getPermissionManager();

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
        if(!(sender instanceof Player)) return null;
        Player player = (Player) sender;
        List<String> completions = new ArrayList<>();
        if(args.length == 1) {
            if(permissionManager.hasPermission(player, "getFieldInfo")) completions.add("info");
            if(permissionManager.hasPermission(player, "visualizeField")) completions.add("visualize");
            if(permissionManager.hasPermission(player, "getFieldLocations")) completions.add("locations");
            if(permissionManager.hasPermission(player, "countFields")) completions.add("counts");
            if(permissionManager.hasPermission(player, "allowPlayer")) completions.add("allow");
            if(permissionManager.hasPermission(player, "removePlayer")) completions.add("remove");
            if(permissionManager.hasPermission(player, "reloadConfiguration")) completions.add("reload");
        } else if(args.length == 2) {
            if(permissionManager.hasPermission(player, "allowPlayer")
                    || permissionManager.hasPermission(player, "removePlayer")) {
                if (args[0].equalsIgnoreCase("allow") || args[0].equalsIgnoreCase("remove")) {
                    for (Player p : PlayerUtil.getPlayers((Player) sender)) {
                        completions.add(p.getName());
                    }
                }
            }
        }
        return StringUtil.copyPartialMatches(args[args.length-1], completions, new ArrayList<>());
    }

}
