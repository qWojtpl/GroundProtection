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
            if(p.getName().equals(nickname)) return p;
        }
        return null;
    }

    public static List<Player> getPlayers(Player forPlayer) {
        boolean sourcePlayerVanished = false;
        for(MetadataValue meta : forPlayer.getMetadata("vanished")) {
            if(meta.asBoolean()) {
                sourcePlayerVanished = true;
                break;
            }
        }
        List<Player> playerList = new ArrayList<>();
        for(Player p : GroundProtection.getInstance().getServer().getOnlinePlayers()) {
            if(!sourcePlayerVanished) {
                boolean vanished = false;
                for(MetadataValue meta : p.getMetadata("vanished")) {
                    if(meta.asBoolean()) {
                        vanished = true;
                        break;
                    }
                }
                if(vanished) continue;
            }
            playerList.add(p);
        }
        return playerList;
    }

}
