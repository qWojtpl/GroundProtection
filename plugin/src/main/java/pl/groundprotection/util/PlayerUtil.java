package pl.groundprotection.util;

import org.bukkit.entity.Player;
import org.bukkit.metadata.MetadataValue;
import pl.groundprotection.GroundProtection;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class PlayerUtil {

    @Nullable
    public static Player getPlayer(String nickname) {
        if(nickname == null) {
            return null;
        }
        for(Player p : GroundProtection.getInstance().getServer().getOnlinePlayers()) {
            if(p.getName().equals(nickname) || p.getUniqueId().toString().equals(nickname)) {
                return p;
            }
        }
        return null;
    }

    @Nullable
    public static Player getPlayerFor(String nickname, Player player) {
        boolean sourcePlayerVanished = isVanished(player);
        for(Player p : GroundProtection.getInstance().getServer().getOnlinePlayers()) {
            if(!sourcePlayerVanished) {
                if(isVanished(p)) {
                    continue;
                }
            }
            if(p.getName().equals(nickname)) {
                return p;
            }
        }
        return null;
    }

    public static boolean isVanished(Player player) {
        for(MetadataValue meta : player.getMetadata("vanished")) {
            if(meta.asBoolean()) {
                return true;
            }
        }
        return false;
    }

    public static List<Player> getPlayers(Player forPlayer) {
        boolean sourcePlayerVanished = isVanished(forPlayer);
        List<Player> playerList = new ArrayList<>();
        for(Player p : GroundProtection.getInstance().getServer().getOnlinePlayers()) {
            if(!sourcePlayerVanished) {
                if(isVanished(p)) {
                    continue;
                }
            }
            playerList.add(p);
        }
        return playerList;
    }

    public static String parseNickname(String player) {
        GroundProtection plugin = GroundProtection.getInstance();
        if(!plugin.getDataHandler().isUuidMode()) {
            return player;
        }
        return plugin.getServer().getOfflinePlayer(player).getUniqueId().toString();
    }

}
