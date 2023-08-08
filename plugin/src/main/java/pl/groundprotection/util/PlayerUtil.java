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
        for(Player p : GroundProtection.getInstance().getServer().getOnlinePlayers()) {
            if(p.getName().equals(nickname)) {
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

}
