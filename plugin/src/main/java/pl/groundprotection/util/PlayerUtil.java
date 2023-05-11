package pl.groundprotection.util;

import org.bukkit.entity.Player;
import pl.groundprotection.GroundProtection;

import javax.annotation.Nullable;

public class PlayerUtil {

    @Nullable
    public static Player getPlayer(String nickname) {
        for(Player p : GroundProtection.getInstance().getServer().getOnlinePlayers()) {
            if(p.getName().equals(nickname)) return p;
        }
        return null;
    }

}
