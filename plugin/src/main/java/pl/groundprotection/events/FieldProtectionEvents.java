package pl.groundprotection.events;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.BlockSpreadEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import pl.groundprotection.GroundProtection;
import pl.groundprotection.data.DataHandler;
import pl.groundprotection.data.Messages;
import pl.groundprotection.fields.Field;
import pl.groundprotection.fields.FieldFlag;
import pl.groundprotection.fields.FieldSchema;
import pl.groundprotection.fields.FieldsManager;

import java.util.List;

public class FieldProtectionEvents implements Listener {

    private final GroundProtection plugin = GroundProtection.getInstance();
    private final Messages messages = plugin.getMessages();
    private final FieldsManager fieldsManager = plugin.getFieldsManager();
    private final DataHandler dataHandler = plugin.getDataHandler();

    @EventHandler(priority = EventPriority.LOW)
    public void onDestroy(BlockBreakEvent event) {
        checkBlocks(event);
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onPlace(BlockPlaceEvent event) {
        checkBlocks(event);
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onInteractBlock(PlayerInteractEvent event) {
        if(event.isCancelled()) return;
        if(!event.getAction().equals(Action.RIGHT_CLICK_BLOCK) && !event.getAction().equals(Action.PHYSICAL)) return;
        if(event.getClickedBlock() == null) return;
        Player p = event.getPlayer();
        List<Field> fields = fieldsManager.getFields(p.getLocation());
        for(Field field : fields) {
            FieldSchema schema = field.getSchema();
            if(fieldsManager.isAllowed(p.getLocation(), p.getName())) continue;
            if(field.getFieldOwner().equals(p.getName())) continue;
            if(field.getFieldContributors().contains(p.getName())) continue;
            if(schema.getFlags().contains(FieldFlag.PREVENT_PLACE)) {
                if(event.getItem() != null) {
                    if(event.getItem().getType().name().toLowerCase().contains("_bucket")) {
                        event.setCancelled(true);
                        break;
                    }
                }
            }
            if(schema.getFlags().contains(FieldFlag.PREVENT_SPAWN_EGGS)) {
                if(p.getInventory().getItemInMainHand().getType().name().toLowerCase().contains("_spawn_egg")
                        || p.getInventory().getItemInOffHand().getType().name().toLowerCase().contains("_spawn_egg")) {
                    event.setCancelled(true);
                    break;
                }
            }
            if(schema.getFlags().contains(FieldFlag.PROTECT_DOORS)) {
                if(dataHandler.getDoorBlocks().contains(event.getClickedBlock().getType().name())) {
                    event.setCancelled(true);
                    break;
                }
            }
            if(schema.getFlags().contains(FieldFlag.PROTECT_CHESTS)) {
                if(dataHandler.getChestBlocks().contains(event.getClickedBlock().getType().name())) {
                    event.setCancelled(true);
                    break;
                }
            }
            if(schema.getFlags().contains(FieldFlag.PROTECT_OTHER_BLOCKS)) {
                if(dataHandler.getOtherBlocks().contains(event.getClickedBlock().getType().name())) {
                    event.setCancelled(true);
                    break;
                }
            }
        }
        if(event.isCancelled()) p.sendMessage(messages.getMessage("cantUse"));
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onInteractEntity(PlayerInteractEntityEvent event) {
        if(event.isCancelled()) return;
        Player p = event.getPlayer();
        List<Field> fields = fieldsManager.getFields(p.getLocation());
        for(Field field : fields) {
            FieldSchema schema = field.getSchema();
            if(fieldsManager.isAllowed(p.getLocation(), p.getName())) continue;
            Entity clicked = event.getRightClicked();
            if(schema.getFlags().contains(FieldFlag.PREVENT_SPAWN_EGGS)) {
                if(p.getInventory().getItemInMainHand().getType().name().toLowerCase().contains("_spawn_egg")
                        || p.getInventory().getItemInOffHand().getType().name().toLowerCase().contains("_spawn_egg")) {
                    event.setCancelled(true);
                    break;
                }
            }
            if(schema.getFlags().contains(FieldFlag.PROTECT_ANIMALS)) {
                if(dataHandler.getAnimals().contains(clicked.getType().name())) {
                    event.setCancelled(true);
                    break;
                }
            }
            if(schema.getFlags().contains(FieldFlag.PROTECT_HOSTILES)) {
                if(dataHandler.getHostiles().contains(clicked.getType().name())) {
                    event.setCancelled(true);
                    break;
                }
            }
            if(schema.getFlags().contains(FieldFlag.PROTECT_OTHER_ENTITIES)) {
                if(dataHandler.getOtherEntities().contains(clicked.getType().name())) {
                    event.setCancelled(true);
                    break;
                }
            }
        }
        if(event.isCancelled()) p.sendMessage(messages.getMessage("cantUse"));
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onDamage(EntityDamageByEntityEvent event) {
        if(event.isCancelled()) return;
        if(!(event.getDamager() instanceof Player)) return;
        Player p = (Player) event.getDamager();
        List<Field> fields = fieldsManager.getFields(p.getLocation());
        for(Field field : fields) {
            FieldSchema schema = field.getSchema();
            Entity victim = event.getEntity();
            if(schema.getFlags().contains(FieldFlag.PREVENT_PVP)) {
                if(victim instanceof Player) {
                    event.setCancelled(true);
                    break;
                }
            }
            if(fieldsManager.isAllowed(p.getLocation(), p.getName())) continue;
            if(schema.getFlags().contains(FieldFlag.PROTECT_ANIMALS)) {
                if(dataHandler.getAnimals().contains(victim.getType().name())) {
                    event.setCancelled(true);
                    break;
                }
            }
            if(schema.getFlags().contains(FieldFlag.PROTECT_HOSTILES)) {
                if(dataHandler.getHostiles().contains(victim.getType().name())) {
                    event.setCancelled(true);
                    break;
                }
            }
            if(schema.getFlags().contains(FieldFlag.PROTECT_OTHER_ENTITIES)) {
                if(dataHandler.getOtherEntities().contains(victim.getType().name())) {
                    event.setCancelled(true);
                    break;
                }
            }
        }
        if(event.isCancelled()) p.sendMessage(messages.getMessage("cantDamage"));
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onSpread(BlockSpreadEvent event) {
        if(event.isCancelled()) return;
        List<Field> fields = fieldsManager.getFields(event.getBlock().getLocation());
        for(Field field : fields) {
            if(field.getSchema().getFlags().contains(FieldFlag.PREVENT_SPREAD)) {
                event.setCancelled(true);
            }
        }
    }

    private void checkBlocks(Event event) {
        if(!(event instanceof BlockPlaceEvent) && !(event instanceof BlockBreakEvent)) return;
        boolean canceled = false;
        Player p = null;
        if(event instanceof BlockPlaceEvent) {
            BlockPlaceEvent ev1 = (BlockPlaceEvent) event;
            if(ev1.isCancelled()) canceled = true;
            p = ev1.getPlayer();
        }
        if(event instanceof BlockBreakEvent) {
            BlockBreakEvent ev2 = (BlockBreakEvent) event;
            if(ev2.isCancelled()) canceled = true;
            p = ev2.getPlayer();
        }
        if(canceled) return;
        List<Field> fields = fieldsManager.getFields(p.getLocation());
        for(Field field : fields) {
            if(fieldsManager.isAllowed(p.getLocation(), p.getName())) continue;
            if(field.getSchema().getFlags().contains(FieldFlag.PREVENT_PLACE)) {
                if(event instanceof BlockPlaceEvent) {
                    BlockPlaceEvent ev1 = (BlockPlaceEvent) event;
                    ev1.setCancelled(true);
                    p.sendMessage(messages.getMessage("cantPlace"));
                    return;
                }
            }
            if(field.getSchema().getFlags().contains(FieldFlag.PREVENT_DESTROY)) {
                if(event instanceof BlockBreakEvent) {
                    BlockBreakEvent ev2 = (BlockBreakEvent) event;
                    ev2.setCancelled(true);
                    p.sendMessage(messages.getMessage("cantBreak"));
                }
                return;
            }
        }
    }

}
