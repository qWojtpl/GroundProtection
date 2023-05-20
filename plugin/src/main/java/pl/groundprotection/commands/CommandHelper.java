package pl.groundprotection.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;
import pl.groundprotection.GroundProtection;
import pl.groundprotection.util.PlayerUtil;

import java.util.ArrayList;
import java.util.List;

public class CommandHelper implements TabCompleter {

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
        if(!(sender instanceof Player)) return null;
        List<String> completions = new ArrayList<>();
        if(args.length == 1) {
            completions.add("info");
            completions.add("visualize");
            completions.add("locations");
            completions.add("counts");
            completions.add("allow");
            completions.add("remove");
        } else if(args.length == 2) {
            if(args[0].equalsIgnoreCase("allow") || args[0].equalsIgnoreCase("remove")) {
                for(Player p : PlayerUtil.getPlayers((Player) sender)) {
                    completions.add(p.getName());
                }
            }
        }
        return StringUtil.copyPartialMatches(args[args.length-1], completions, new ArrayList<>());
    }

}
