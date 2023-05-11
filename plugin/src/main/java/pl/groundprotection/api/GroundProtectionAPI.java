package pl.groundprotection.api;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import pl.groundprotection.GroundProtection;
import pl.groundprotection.fields.Field;

import java.util.List;

public class GroundProtectionAPI {

    private final GroundProtection plugin = GroundProtection.getInstance();

    /**
     * Check if player in this location can do anything (is owner of
     * field or is contributor)
     *
     * @param   location    Location to check player's permissions
     * @param   player      Which player will be checked
     * @return  True if player is allowed in this location or false if not
     */
    public boolean isPlayerAllowed(Location location, String player) {
        return plugin.getFieldsManager().isAllowed(location, player);
    }

    /**
     * Get player's owned fields
     *
     * @param   player  Player to check (nickname)
     * @return Player fields (can be empty list)
     */
    public List<Field> getPlayerFields(String player) {
        return plugin.getFieldsManager().getPlayerFields(player);
    }

    /**
     * Get player's owned fields
     *
     * @param   player  Player to check
     * @return Player fields (can be empty list)
     */
    public List<Field> getPlayerFields(Player player) {
        return plugin.getFieldsManager().getPlayerFields(player);
    }

}
