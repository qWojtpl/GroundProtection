package pl.groundprotection.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.material.Door;
import pl.groundprotection.GroundProtection;
import pl.groundprotection.data.DataHandler;
import pl.groundprotection.fields.Field;
import pl.groundprotection.fields.FieldFlag;
import pl.groundprotection.fields.FieldSchema;
import pl.groundprotection.fields.FieldsManager;

import java.util.List;

public class FieldProtectionEvents implements Listener {

    private final GroundProtection plugin = GroundProtection.getInstance();
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
    public void onInteract(PlayerInteractEvent event) {
        if(event.isCancelled()) return;
        if(!event.getAction().equals(Action.RIGHT_CLICK_BLOCK)) return;
        Player p = event.getPlayer();
        List<Field> fields = fieldsManager.getFields(p.getLocation());
        for(Field field : fields) {
            FieldSchema schema = field.getSchema();
            if(field.getFieldOwner().equals(p.getName())) continue;
            if(field.getFieldContributors().contains(p.getName())) continue;
            if(schema.getFlags().contains(FieldFlag.PROTECT_DOORS)) {
                if(dataHandler.getDoorBlocks().contains(event.getClickedBlock().getType().name())) {
                    event.setCancelled(true);
                }
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
            if(field.getFieldOwner().equals(p.getName())) continue;
            if(field.getFieldContributors().contains(p.getName())) continue;
            if(field.getSchema().getFlags().contains(FieldFlag.PROTECT_BLOCKS)) {
                if(event instanceof BlockPlaceEvent) {
                    BlockPlaceEvent ev1 = (BlockPlaceEvent) event;
                    ev1.setCancelled(true);
                }
                if(event instanceof BlockBreakEvent) {
                    BlockBreakEvent ev2 = (BlockBreakEvent) event;
                    ev2.setCancelled(true);
                }
                return;
            }
        }
    }

}
