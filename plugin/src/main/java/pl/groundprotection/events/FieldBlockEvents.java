package pl.groundprotection.events;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import pl.groundprotection.GroundProtection;
import pl.groundprotection.fields.Field;
import pl.groundprotection.fields.FieldSchema;
import pl.groundprotection.fields.FieldsManager;

public class FieldBlockEvents implements Listener {

    private final GroundProtection plugin = GroundProtection.getInstance();
    private final FieldsManager fieldsManager = plugin.getFieldsManager();

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlaceField(BlockPlaceEvent event) {
        if(event.isCancelled()) return;
        Player p = event.getPlayer();
        if(p.isSneaking()) return;
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
            event.setCancelled(true);
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
    public void onRemoveField(BlockBreakEvent event) {
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

    @EventHandler
    public void onMoveField(BlockPistonExtendEvent event) {
        for(Field f : fieldsManager.getFields()) {
            for(Block b : event.getBlocks()) {
                if (f.getFieldLocation().equals(b.getLocation())) {
                    event.setCancelled(true);
                    return;
                }
            }
        }
    }

    @EventHandler
    public void onTakeField(BlockPistonRetractEvent event) {
        for(Field f : fieldsManager.getFields()) {
            for(Block b : event.getBlocks()) {
                if (f.getFieldLocation().equals(b.getLocation())) {
                    event.setCancelled(true);
                    return;
                }
            }
        }
    }

}
