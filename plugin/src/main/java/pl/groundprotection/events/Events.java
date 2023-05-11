package pl.groundprotection.events;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import pl.groundprotection.GroundProtection;
import pl.groundprotection.fields.Field;
import pl.groundprotection.fields.FieldSchema;
import pl.groundprotection.fields.FieldsManager;

public class Events implements Listener {

    private final GroundProtection plugin = GroundProtection.getInstance();
    private final FieldsManager fieldsManager = plugin.getFieldsManager();

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlaceCuboid(BlockPlaceEvent event) {
        if(event.isCancelled()) return;
        Player p = event.getPlayer();
        Material itemType = p.getInventory().getItemInMainHand().getType();
        FieldSchema schema = null;
        for(String name : fieldsManager.getSchemas().keySet()) {
            if(fieldsManager.getSchemas().get(name).getItem().equals(itemType)) {
                schema = fieldsManager.getSchemas().get(name);
                break;
            }
        }
        if(schema == null) return;
        if(schema.getDisabledWorlds().contains(p.getWorld().getName())) {
            p.sendMessage("You cannot place field in this world");
            return;
        }
        if(fieldsManager.canPlaceField(schema, p, event.getBlock().getLocation())) {
            fieldsManager.createField(schema, p, event.getBlock().getLocation());
        } else {
            p.sendMessage("You cannot place field here");
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onRemoveCuboid(BlockBreakEvent event) {
        if(event.isCancelled()) return;
        Player p = event.getPlayer();
        Field field = null;
        for(Field f : fieldsManager.getFields()) {
            if(f.getFieldLocation().equals(event.getBlock().getLocation())) {
                field = f;
                break;
            }
        }
        if(field == null) return;
        if(field.getFieldOwner().equals(p.getName())) {
            fieldsManager.removeField(field, p);
        } else {
            p.sendMessage("You cannot destroy another player's field!");
            event.setCancelled(true);
        }
    }

}
