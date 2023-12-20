package pl.groundprotection.events;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import pl.groundprotection.GroundProtection;
import pl.groundprotection.data.Messages;
import pl.groundprotection.fields.Field;
import pl.groundprotection.fields.FieldItem;
import pl.groundprotection.fields.FieldSchema;
import pl.groundprotection.fields.FieldsManager;
import pl.groundprotection.util.LocationUtil;
import pl.groundprotection.util.PlayerUtil;

import java.text.MessageFormat;
import java.util.List;

public class FieldBlockEvents implements Listener {

    private final GroundProtection plugin = GroundProtection.getInstance();
    private final Messages messages = plugin.getMessages();
    private final FieldsManager fieldsManager = plugin.getFieldsManager();

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player p = event.getPlayer();
        List<Field> playerFields = fieldsManager.getPlayerFields(p);
        for(Field field : playerFields) {
            if(!p.hasPermission(field.getSchema().getPermission())) {
                String location = LocationUtil.locationBuilder(field.getFieldLocation());
                p.sendMessage(MessageFormat.format(
                        messages.getMessage("autoRemovedField"),
                        field.getSchema().getName(),
                        location,
                        field.getSchema().getPermission()));
                fieldsManager.removeField(field, p.getName());
                continue;
            }
            plugin.getDataHandler().saveLogin(field);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlaceField(BlockPlaceEvent event) {
        if(event.isCancelled()) {
            return;
        }
        Player p = event.getPlayer();
        if(p.isSneaking()) {
            return;
        }
        ItemStack item = p.getInventory().getItemInMainHand();
        FieldSchema schema = null;
        for(String name : fieldsManager.getSchemas().keySet()) {
            FieldItem fieldItem = fieldsManager.getSchemas().get(name).getItem();
            if(fieldItem.getMaterial().equals(item.getType())) {
                ItemMeta im = item.getItemMeta();
                if(im != null) {
                    if(!im.getDisplayName().equals(fieldItem.getName())) {
                        continue;
                    }
                    if(fieldItem.getLore().size() > 0) {
                        if(im.getLore() == null) {
                            continue;
                        }
                        if(!im.getLore().equals(fieldItem.getLore())) {
                            continue;
                        }
                    }
                }
                schema = fieldsManager.getSchemas().get(name);
                break;
            }
        }
        if(schema == null) {
            return;
        }
        if(!plugin.getPermissionManager().checkPermission(p, schema.getPermission())) {
            event.setCancelled(true);
            return;
        }
        if(schema.getDisabledWorlds().contains(p.getWorld().getName())) {
            p.sendMessage(messages.getMessage("unavailableWorld"));
            event.setCancelled(true);
            return;
        }
        if(fieldsManager.canPlaceField(schema, p.getName(), event.getBlock().getLocation())) {
            if(!fieldsManager.isReachedLimit(schema, p)) {
                fieldsManager.createField(schema, p.getName(), event.getBlock().getLocation());
            } else {
                p.sendMessage(MessageFormat.format(messages.getMessage("reachedLimit"),
                        fieldsManager.getLimit(schema, p)));
                event.setCancelled(true);
            }
        } else {
            p.sendMessage(messages.getMessage("cannotPlaceField"));
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onRemoveField(BlockBreakEvent event) {
        if(event.isCancelled()) {
            return;
        }
        Player p = event.getPlayer();
        Field field = null;
        for(Field f : fieldsManager.getFields()) {
            if(f.getFieldLocation().equals(event.getBlock().getLocation())) {
                field = f;
                break;
            }
        }
        if(field == null) {
            return;
        }
        event.setCancelled(true);
        if(field.getFieldOwner().equals(PlayerUtil.parseNickname(p.getName()))
                || p.hasPermission(plugin.getPermissionManager().getPermission("removeFieldIfNotOwner"))) {
            fieldsManager.removeField(field, p.getName());
            event.getBlock().setType(Material.AIR);
            event.getPlayer().getWorld().dropItemNaturally(event.getBlock().getLocation(), field.getSchema().getItem().getItemStack());
        } else {
            p.sendMessage(messages.getMessage("destroySomeoneField"));
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
                if(f.getFieldLocation().equals(b.getLocation())) {
                    event.setCancelled(true);
                    return;
                }
            }
        }
    }

    @EventHandler
    public void onExplodeField(BlockExplodeEvent event) {
        for(Field f : fieldsManager.getFields()) {
            if(f.getFieldLocation().equals(event.getBlock().getLocation())) {
                event.setCancelled(true);
                return;
            }
        }
    }

}
