package pl.groundprotection.events;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.*;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.*;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.hanging.HangingPlaceEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.*;
import org.bukkit.event.vehicle.VehicleDamageEvent;
import org.bukkit.event.vehicle.VehicleDestroyEvent;
import pl.groundprotection.GroundProtection;
import pl.groundprotection.data.DataHandler;
import pl.groundprotection.data.Messages;
import pl.groundprotection.fields.Field;
import pl.groundprotection.fields.FieldFlag;
import pl.groundprotection.fields.FieldSchema;
import pl.groundprotection.fields.FieldsManager;
import pl.groundprotection.permissions.PermissionManager;

import java.util.ArrayList;
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
                    if(event.getItem().getType().name().toLowerCase().contains("bucket")
                    || event.getItem().getType().equals(Material.BONE_MEAL)
                    || event.getItem().getType().equals(Material.ARMOR_STAND)
                    || event.getItem().getType().equals(Material.END_CRYSTAL)) {
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
            if(schema.getFlags().contains(FieldFlag.PREVENT_VEHICLE_PLACE)) {
                if(p.getInventory().getItemInMainHand().getType().name().toLowerCase().contains("minecart")
                || p.getInventory().getItemInOffHand().getType().name().toLowerCase().contains("minecart")
                || p.getInventory().getItemInMainHand().getType().name().toLowerCase().contains("_boat")
                || p.getInventory().getItemInOffHand().getType().name().toLowerCase().contains("_boat")) {
                    event.setCancelled(true);
                    event.setUseItemInHand(Event.Result.DENY);
                    break;
                }
            }
            if(schema.getFlags().contains(FieldFlag.PROTECT_INTERACTABLE_BLOCKS)) {
                if(event.getClickedBlock().getType().isInteractable()) {
                    if(!dataHandler.getProtectInteractableBlocksExclude().contains(event.getClickedBlock().getType().name())) {
                        event.setCancelled(true);
                        event.setUseInteractedBlock(Event.Result.DENY);
                        break;
                    }
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
        Entity clicked = event.getRightClicked();
        List<Field> fields = fieldsManager.getFields(event.getRightClicked().getLocation());
        for(Field field : fields) {
            FieldSchema schema = field.getSchema();
            if(fieldsManager.isAllowed(event.getRightClicked().getLocation(), p.getName())) {
                continue;
            }
            if(schema.getFlags().contains(FieldFlag.PREVENT_SPAWN_EGGS)) {
                if(p.getInventory().getItemInMainHand().getType().name().toLowerCase().contains("_spawn_egg")
                || p.getInventory().getItemInOffHand().getType().name().toLowerCase().contains("_spawn_egg")) {
                    event.setCancelled(true);
                    break;
                }
            }
            if(schema.getFlags().contains(FieldFlag.PROTECT_CHESTS)) {
                if(clicked instanceof ChestBoat && p.isSneaking()) {
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

    @EventHandler
    public void onInteractAtEntity(PlayerInteractAtEntityEvent event) {
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
            if(schema.getFlags().contains(FieldFlag.PROTECT_ARMOR_STANDS)) {
                if(event.getRightClicked().getType().equals(EntityType.ARMOR_STAND)) {
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
        Player p = null;
        if(!(event.getDamager() instanceof Player)) {
            if(event.getDamager() instanceof Projectile) {
                if(((Projectile) event.getDamager()).getShooter() instanceof Player) {
                    p = (Player) ((Projectile) event.getDamager()).getShooter();
                }
            }
        } else {
            p = (Player) event.getDamager();
        }
        if(p != null) {
            if(p.hasPermission(permissionManager.getPermission("bypassFieldProtection"))) {
                return;
            }
        }
        Entity victim = event.getEntity();
        List<Field> fields = fieldsManager.getFields(victim.getLocation());
        for(Field field : fields) {
            FieldSchema schema = field.getSchema();
            if(p != null) {
                if(schema.getFlags().contains(FieldFlag.PREVENT_PVP)) {
                    if(victim instanceof Player) {
                        event.setCancelled(true);
                        break;
                    }
                }
                if(fieldsManager.isAllowed(victim.getLocation(), p.getName())) {
                    continue;
                }
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
        if(event.isCancelled() && p != null) {
            p.sendMessage(messages.getMessage("cantDamage"));
        } else {
            if(p != null) {
                List<Field> fieldList = fieldsManager.getFields(event.getDamager().getLocation());
                for(Field field : fieldList) {
                    if(field.getSchema().getFlags().contains(FieldFlag.PREVENT_PVP)) {
                        if(victim instanceof Player) {
                            event.setCancelled(true);
                            p.sendMessage(messages.getMessage("cantDamage"));
                            return;
                        }
                    }
                }
            }
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
        if(event.isCancelled()) {
            p.sendMessage(messages.getMessage("cantUse"));
        }
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
        if(event.isCancelled()) {
            p.sendMessage(messages.getMessage("cantUse"));
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onBlockExplode(BlockExplodeEvent event) {
        if(event.isCancelled()) {
            return;
        }
        for(Block b : new ArrayList<>(event.blockList())) {
            List<Field> fields = fieldsManager.getFields(b.getLocation());
            for(Field field : fields) {
                if(field.getSchema().getFlags().contains(FieldFlag.PREVENT_EXPLOSIONS)) {
                    event.blockList().remove(b);
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onEntityExplode(EntityExplodeEvent event) {
        if(event.isCancelled()) {
            return;
        }
        List<Field> fields = fieldsManager.getFields(event.getEntity().getLocation());
        for(Field field : fields) {
            if(field.getSchema().getFlags().contains(FieldFlag.PREVENT_EXPLOSIONS)) {
                event.setCancelled(true);
                return;
            }
        }
        for(Block b : new ArrayList<>(event.blockList())) {
            List<Field> fieldList = fieldsManager.getFields(b.getLocation());
            for(Field field : fieldList) {
                if(field.getSchema().getFlags().contains(FieldFlag.PREVENT_EXPLOSIONS)) {
                    event.blockList().remove(b);
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onDamageByExplosion(EntityDamageEvent event) {
        if(event.isCancelled()) {
            return;
        }
        if(!event.getCause().equals(EntityDamageEvent.DamageCause.BLOCK_EXPLOSION)
                && !event.getCause().equals(EntityDamageEvent.DamageCause.ENTITY_EXPLOSION)) {
            return;
        }
        List<Field> fields = fieldsManager.getFields(event.getEntity().getLocation());
        for(Field field : fields) {
            if(field.getSchema().getFlags().contains(FieldFlag.PREVENT_EXPLOSIONS)) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onVehicleDestroy(VehicleDestroyEvent event) {
        if(event.isCancelled()) {
            return;
        }
        if(!(event.getAttacker() instanceof Player)) {
            return;
        }
        Player p = (Player) event.getAttacker();
        if(p.hasPermission(permissionManager.getPermission("bypassFieldProtection"))) {
            return;
        }
        List<Field> fields = fieldsManager.getFields(event.getVehicle().getLocation());
        for(Field field : fields) {
            if(fieldsManager.isAllowed(event.getVehicle().getLocation(), p.getName())) {
                continue;
            }
            if(field.getSchema().getFlags().contains(FieldFlag.PROTECT_VEHICLES)) {
                event.setCancelled(true);
                break;
            }
        }
        if(event.isCancelled()) {
            p.sendMessage(messages.getMessage("cantDamage"));
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onVehicleDamage(VehicleDamageEvent event) {
        if(event.isCancelled()) {
            return;
        }
        if(!(event.getAttacker() instanceof Player)) {
            return;
        }
        Player p = (Player) event.getAttacker();
        if(p.hasPermission(permissionManager.getPermission("bypassFieldProtection"))) {
            return;
        }
        List<Field> fields = fieldsManager.getFields(event.getVehicle().getLocation());
        for(Field field : fields) {
            if(fieldsManager.isAllowed(event.getVehicle().getLocation(), p.getName())) {
                continue;
            }
            if(field.getSchema().getFlags().contains(FieldFlag.PROTECT_VEHICLES)) {
                event.setCancelled(true);
                break;
            }
        }
        if(event.isCancelled()) {
            p.sendMessage(messages.getMessage("cantDamage"));
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onBucketEmpty(PlayerBucketEmptyEvent event) {
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
                break;
            }
        }
        if(event.isCancelled()) {
            p.sendMessage(messages.getMessage("cantPlace"));
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onBucketFill(PlayerBucketFillEvent event) {
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
                break;
            }
        }
        if(event.isCancelled()) {
            p.sendMessage(messages.getMessage("cantBreak"));
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onHangEntity(HangingPlaceEvent event) {
        if(event.isCancelled()) {
            return;
        }
        Player p = event.getPlayer();
        if(p == null) {
            return;
        }
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
                break;
            }
        }
        if(event.isCancelled()) {
            p.sendMessage(messages.getMessage("cantPlace"));
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onUnhangEntity(HangingBreakByEntityEvent event) {
        if(event.isCancelled()) {
            return;
        }
        if(!(event.getRemover() instanceof Player)) {
            return;
        }
        Player p = (Player) event.getRemover();
        if(p.hasPermission(permissionManager.getPermission("bypassFieldProtection"))) {
            return;
        }
        List<Field> fields = fieldsManager.getFields(event.getEntity().getLocation());
        for(Field field : fields) {
            if(fieldsManager.isAllowed(event.getEntity().getLocation(), p.getName())) {
                continue;
            }
            if(field.getSchema().getFlags().contains(FieldFlag.PREVENT_DESTROY)) {
                event.setCancelled(true);
                break;
            }
        }
        if(event.isCancelled()) {
            p.sendMessage(messages.getMessage("cantBreak"));
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onOpenChestBoat(InventoryOpenEvent event) {
        if(event.isCancelled()) {
            return;
        }
        if(event.getPlayer().hasPermission(permissionManager.getPermission("bypassFieldProtection"))) {
            return;
        }
        if(event.getPlayer().getVehicle() == null) {
            return;
        }
        if(!(event.getPlayer().getVehicle() instanceof ChestBoat)) {
            return;
        }
        List<Field> fields = fieldsManager.getFields(event.getPlayer().getLocation());
        for(Field field : fields) {
            if(fieldsManager.isAllowed(event.getPlayer().getLocation(), event.getPlayer().getName())) {
                continue;
            }
            if(field.getSchema().getFlags().contains(FieldFlag.PROTECT_CHESTS)) {
                event.setCancelled(true);
                break;
            }
        }
        if(event.isCancelled()) {
            event.getPlayer().sendMessage(messages.getMessage("cantUse"));
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onMoveBlock(BlockPistonExtendEvent event) {
        if(event.isCancelled()) {
            return;
        }
        List<Field> sourceFields = fieldsManager.getFields(event.getBlock().getLocation());
        for(Block b : event.getBlocks()) {
            for(Field field : fieldsManager.getFields(b.getLocation())) {
                if(hasCommon(sourceFields, field)) {
                    continue;
                }
                if(field.getSchema().getFlags().contains(FieldFlag.PREVENT_DESTROY)) {
                    event.setCancelled(true);
                    return;
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onTakeBlock(BlockPistonRetractEvent event) {
        if(event.isCancelled()) {
            return;
        }
        List<Field> sourceFields = fieldsManager.getFields(event.getBlock().getLocation());
        for(Block b : event.getBlocks()) {
            for(Field field : fieldsManager.getFields(b.getLocation())) {
                if(hasCommon(sourceFields, field)) {
                    continue;
                }
                if(field.getSchema().getFlags().contains(FieldFlag.PREVENT_DESTROY)) {
                    event.setCancelled(true);
                    return;
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onFlow(BlockFromToEvent event) {
        if(event.isCancelled()) {
            return;
        }
        Block block = event.getBlock();
        if(!block.getType().equals(Material.WATER)
        && !block.getType().equals(Material.LAVA)) {
            return;
        }
        List<Field> sourceFields = fieldsManager.getFields(block.getLocation());
        List<Field> fields = fieldsManager.getFields(event.getToBlock().getLocation());
        for(Field field : fields) {
            if(hasCommon(sourceFields, field)) {
                continue;
            }
            if(field.getSchema().getFlags().contains(FieldFlag.PREVENT_FLOW_IN)) {
                event.setCancelled(true);
                return;
            }
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onProjectileHit(ProjectileHitEvent event) {
        if(event.isCancelled()) {
            return;
        }
        if(event.getHitBlock() == null) {
            return;
        }
        if(!dataHandler.getProjectileProtectBlocks().contains(event.getHitBlock().getType().name())) {
            return;
        }
        if(!(event.getEntity().getShooter() instanceof Player)) {
            return;
        }
        Player p = (Player) event.getEntity().getShooter();
        if(p.hasPermission(permissionManager.getPermission("bypassFieldProtection"))) {
            return;
        }
        List<Field> fields = fieldsManager.getFields(event.getHitBlock().getLocation());
        for(Field field : fields) {
            if(fieldsManager.isAllowed(event.getHitBlock().getLocation(), p.getName())) {
                continue;
            }
            if(field.getSchema().getFlags().contains(FieldFlag.PREVENT_DESTROY)) {
                event.setCancelled(true);
                p.sendMessage(messages.getMessage("cantBreak"));
                return;
            }
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onCommand(PlayerCommandPreprocessEvent event) {
        if(event.isCancelled()) {
            return;
        }
        Player p = event.getPlayer();
        if(p.hasPermission(permissionManager.getPermission("bypassFieldProtection"))) {
            return;
        }
        List<Field> fields = fieldsManager.getFields(p.getLocation());
        for(Field field : fields) {
            if(fieldsManager.isAllowed(p.getLocation(), p.getName())) {
                continue;
            }
            for(String cmd : field.getSchema().getBlockedCommands()) {
                if(event.getMessage().startsWith(cmd + " ") || event.getMessage().equals(cmd)) {
                    event.setCancelled(true);
                    p.sendMessage(messages.getMessage("cantUseCommand"));
                    return;
                }
            }
        }
    }

    private boolean hasCommon(List<Field> sourceFields, Field field) {
        for(Field sourceField : sourceFields) {
            if(sourceField.getFieldOwner().equals(field.getFieldOwner())
                    || (sourceField.getFieldContributors().contains(field.getFieldOwner())
                    && field.getFieldContributors().contains(sourceField.getFieldOwner()))) {
                return true;
            }
        }
        return false;
    }

}
