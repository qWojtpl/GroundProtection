package pl.groundprotection.events;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.*;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import pl.groundprotection.GroundProtection;
import pl.groundprotection.data.DataHandler;
import pl.groundprotection.data.Messages;
import pl.groundprotection.fields.Field;
import pl.groundprotection.fields.FieldFlag;
import pl.groundprotection.fields.FieldSchema;
import pl.groundprotection.fields.FieldsManager;
import pl.groundprotection.permissions.PermissionManager;

import java.util.List;

public class FieldProtectionEvents implements Listener {

    private final GroundProtection plugin = GroundProtection.getInstance();
    private final Messages messages = plugin.getMessages();
    private final FieldsManager fieldsManager = plugin.getFieldsManager();
    private final DataHandler dataHandler = plugin.getDataHandler();
    private final PermissionManager permissionManager = plugin.getPermissionManager();

    @EventHandler(priority = EventPriority.LOW)
    public void onDestroy(BlockBreakEvent event) {
        if(event.isCancelled()) {
            return;
        }
        Player p = event.getPlayer();
        if(p.hasPermission(permissionManager.getPermission("bypassFieldProtection"))) {
            return;
        }
        List<Field> fields = fieldsManager.getFields(event.getBlock().getLocation());
        for(Field field : fields) {
            if(fieldsManager.isAllowed(event.getBlock().getLocation(), p.getName())) {
                continue;
            }
            if(field.getSchema().getFlags().contains(FieldFlag.PREVENT_DESTROY)) {
                event.setCancelled(true);
                p.sendMessage(messages.getMessage("cantBreak"));
                break;
            }
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onPlace(BlockPlaceEvent event) {
        if(event.isCancelled()) {
            return;
        }
        Player p = event.getPlayer();
        if(p.hasPermission(permissionManager.getPermission("bypassFieldProtection"))) {
            return;
        }
        List<Field> fields = fieldsManager.getFields(event.getBlock().getLocation());
        for(Field field : fields) {
            if(fieldsManager.isAllowed(event.getBlock().getLocation(), p.getName())) {
                continue;
            }
            if(field.getSchema().getFlags().contains(FieldFlag.PREVENT_PLACE)) {
                event.setCancelled(true);
                p.sendMessage(messages.getMessage("cantPlace"));
                break;
            }
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onInteractBlock(PlayerInteractEvent event) {
        if(event.isCancelled()) {
            return;
        }
        if(!event.getAction().equals(Action.RIGHT_CLICK_BLOCK) && !event.getAction().equals(Action.PHYSICAL)) {
            return;
        }
        if(event.getClickedBlock() == null) {
            return;
        }
        Player p = event.getPlayer();
        if(p.hasPermission(permissionManager.getPermission("bypassFieldProtection"))) {
            return;
        }
        List<Field> fields = fieldsManager.getFields(event.getClickedBlock().getLocation());
        for(Field field : fields) {
            FieldSchema schema = field.getSchema();
            if(fieldsManager.isAllowed(event.getClickedBlock().getLocation(), p.getName())) {
                continue;
            }
            if(schema.getFlags().contains(FieldFlag.PREVENT_PLACE)) {
                if(event.getItem() != null) {
                    if(event.getItem().getType().name().toLowerCase().contains("_bucket") ||
                            event.getItem().getType().equals(Material.BONE_MEAL)) {
                        event.setCancelled(true);
                        event.setUseItemInHand(Event.Result.DENY);
                        break;
                    }
                }
            }
            if(schema.getFlags().contains(FieldFlag.PREVENT_SPAWN_EGGS)) {
                if(p.getInventory().getItemInMainHand().getType().name().toLowerCase().contains("_spawn_egg")
                        || p.getInventory().getItemInOffHand().getType().name().toLowerCase().contains("_spawn_egg")) {
                    event.setCancelled(true);
                    event.setUseItemInHand(Event.Result.DENY);
                    break;
                }
            }
            if(schema.getFlags().contains(FieldFlag.PROTECT_INTERACTABLE_BLOCKS)) {
                if(event.getClickedBlock().getType().isInteractable()) {
                    event.setCancelled(true);
                    event.setUseInteractedBlock(Event.Result.DENY);
                    break;
                }
            }
            if(schema.getFlags().contains(FieldFlag.PROTECT_DOORS)) {
                if(dataHandler.getDoorBlocks().contains(event.getClickedBlock().getType().name())) {
                    event.setCancelled(true);
                    event.setUseInteractedBlock(Event.Result.DENY);
                    break;
                }
            }
            if(schema.getFlags().contains(FieldFlag.PROTECT_CHESTS)) {
                if(dataHandler.getChestBlocks().contains(event.getClickedBlock().getType().name())) {
                    event.setCancelled(true);
                    event.setUseInteractedBlock(Event.Result.DENY);
                    break;
                }
            }
            if(schema.getFlags().contains(FieldFlag.PROTECT_OTHER_BLOCKS)) {
                if(dataHandler.getOtherBlocks().contains(event.getClickedBlock().getType().name())) {
                    event.setCancelled(true);
                    event.setUseInteractedBlock(Event.Result.DENY);
                    break;
                }
            }
            if(schema.getFlags().contains(FieldFlag.PROTECT_CROPS)) {
                if(event.getAction().equals(Action.PHYSICAL)) {
                    if(Material.FARMLAND.equals(event.getClickedBlock().getType())) {
                        event.setCancelled(true);
                        break;
                    }
                }
            }
        }
        if(event.isCancelled()) {
            p.sendMessage(messages.getMessage("cantUse"));
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onInteractEntity(PlayerInteractEntityEvent event) {
        if(event.isCancelled()) {
            return;
        }
        Player p = event.getPlayer();
        if(p.hasPermission(permissionManager.getPermission("bypassFieldProtection"))) {
            return;
        }
        List<Field> fields = fieldsManager.getFields(event.getRightClicked().getLocation());
        for(Field field : fields) {
            FieldSchema schema = field.getSchema();
            if(fieldsManager.isAllowed(event.getRightClicked().getLocation(), p.getName())) {
                continue;
            }
            Entity clicked = event.getRightClicked();
            if(schema.getFlags().contains(FieldFlag.PREVENT_SPAWN_EGGS)) {
                if(p.getInventory().getItemInMainHand().getType().name().toLowerCase().contains("_spawn_egg")
                        || p.getInventory().getItemInOffHand().getType().name().toLowerCase().contains("_spawn_egg")) {
                    event.setCancelled(true);
                    break;
                }
            }
            if(schema.getFlags().contains(FieldFlag.PREVENT_NAME_TAGS)) {
                if(p.getInventory().getItemInOffHand().getType().equals(Material.NAME_TAG)
                        || p.getInventory().getItemInMainHand().getType().equals(Material.NAME_TAG)) {
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
        if(event.isCancelled()) {
            p.sendMessage(messages.getMessage("cantUse"));
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onDamage(EntityDamageByEntityEvent event) {
        if(event.isCancelled()) {
            return;
        }
        Player p;
        if(!(event.getDamager() instanceof Player)) {
            if(event.getDamager() instanceof Projectile) {
                if(((Projectile) event.getDamager()).getShooter() instanceof Player) {
                    p = (Player) ((Projectile) event.getDamager()).getShooter();
                } else {
                    return;
                }
            } else {
                return;
            }
        } else {
            p = (Player) event.getDamager();
        }
        if(p.hasPermission(permissionManager.getPermission("bypassFieldProtection"))) {
            return;
        }
        Entity victim = event.getEntity();
        List<Field> fields = fieldsManager.getFields(victim.getLocation());
        for(Field field : fields) {
            FieldSchema schema = field.getSchema();
            if(schema.getFlags().contains(FieldFlag.PREVENT_PVP)) {
                if(victim instanceof Player) {
                    event.setCancelled(true);
                    break;
                }
            }
            if(fieldsManager.isAllowed(victim.getLocation(), p.getName())) {
                continue;
            }
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
        if(event.isCancelled()) {
            p.sendMessage(messages.getMessage("cantDamage"));
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onSpread(BlockSpreadEvent event) {
        if(event.isCancelled()) {
            return;
        }
        List<Field> fields = fieldsManager.getFields(event.getBlock().getLocation());
        for(Field field : fields) {
            if(field.getSchema().getFlags().contains(FieldFlag.PREVENT_SPREAD)) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onShoot(ProjectileLaunchEvent event) {
        if(event.isCancelled()) {
            return;
        }
        if(!(event.getEntity().getShooter() instanceof Player)) {
            return;
        }
        Player p = (Player) event.getEntity().getShooter();
        if(p.hasPermission(permissionManager.getPermission("bypassFieldProtection"))) {
            return;
        }
        List<Field> fields = fieldsManager.getFields(p.getLocation());
        for(Field field : fields) {
            if(fieldsManager.isAllowed(p.getLocation(), p.getName())) {
                continue;
            }
            if(field.getSchema().getFlags().contains(FieldFlag.PREVENT_BOW)) {
                if(event.getEntity() instanceof Arrow) {
                    event.setCancelled(true);
                    break;
                }
            }
            if(field.getSchema().getFlags().contains(FieldFlag.PREVENT_FISHING_ROD)) {
                if(event.getEntity() instanceof FishHook) {
                    event.setCancelled(true);
                    break;
                }
            }
            if(field.getSchema().getFlags().contains(FieldFlag.PREVENT_POTIONS)) {
                if(event.getEntity() instanceof ThrownPotion) {
                    event.setCancelled(true);
                    break;
                }
            }
        }
        if(event.isCancelled()) p.sendMessage(messages.getMessage("cantUse"));
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onFish(PlayerFishEvent event) {
        if(event.isCancelled()) {
            return;
        }
        if(event.getCaught() == null) {
            return;
        }
        Player p = event.getPlayer();
        if(p.hasPermission(permissionManager.getPermission("bypassFieldProtection"))) {
            return;
        }
        List<Field> fields = fieldsManager.getFields(event.getCaught().getLocation());
        for(Field field : fields) {
            if(fieldsManager.isAllowed(event.getCaught().getLocation(), p.getName())) {
                continue;
            }
            if(field.getSchema().getFlags().contains(FieldFlag.PREVENT_FISHING_ROD)) {
                event.setCancelled(true);
                break;
            }
            if(field.getSchema().getFlags().contains(FieldFlag.PREVENT_PVP)) {
                if(event.getCaught() instanceof Player) {
                    event.setCancelled(true);
                    break;
                }
            }
            if(field.getSchema().getFlags().contains(FieldFlag.PROTECT_ANIMALS)) {
                if(dataHandler.getAnimals().contains(event.getCaught().getType().name())) {
                    event.setCancelled(true);
                    break;
                }
            }
            if(field.getSchema().getFlags().contains(FieldFlag.PROTECT_HOSTILES)) {
                if(dataHandler.getHostiles().contains(event.getCaught().getType().name())) {
                    event.setCancelled(true);
                    break;
                }
            }
        }
        if(event.isCancelled()) p.sendMessage(messages.getMessage("cantUse"));
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onBlockExplode(BlockExplodeEvent event) {
        if(event.isCancelled()) {
            return;
        }
        for(Block b : event.blockList()) {
            List<Field> fields = fieldsManager.getFields(b.getLocation());
            for (Field field : fields) {
                if (field.getSchema().getFlags().contains(FieldFlag.PREVENT_EXPLOSIONS)) {
                    event.setCancelled(true);
                    break;
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onEntityExplode(EntityExplodeEvent event) {
        if(event.isCancelled()) {
            return;
        }
        for(Block b : event.blockList()) {
            List<Field> fields = fieldsManager.getFields(b.getLocation());
            for (Field field : fields) {
                if (field.getSchema().getFlags().contains(FieldFlag.PREVENT_EXPLOSIONS)) {
                    event.setCancelled(true);
                    break;
                }
            }
        }
    }

}
