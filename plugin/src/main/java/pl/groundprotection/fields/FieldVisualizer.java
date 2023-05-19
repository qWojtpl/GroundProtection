package pl.groundprotection.fields;

import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;
import pl.groundprotection.GroundProtection;

import java.util.ArrayList;
import java.util.List;

@Getter
public class FieldVisualizer {

    private final GroundProtection plugin = GroundProtection.getInstance();
    private final List<Location> locations = new ArrayList<>();
    private final Field field;
    private final Player player;
    private final Material material;
    private final int spaces;
    private final int maxY;
    private final int startY;
    private int task;
    private int rollbackTask;
    private int iterator = 0;
    private int yIterator;

    public FieldVisualizer(Field field, Player player, Material material, int spaces) {
        this.field = field;
        this.player = player;
        this.material = material;
        this.spaces = spaces;
        this.maxY = (int) player.getLocation().getY() + spaces + 16;
        this.startY = (int) player.getLocation().getY() - spaces - 16;
        visualizeField();
    }

    private void visualizeField() {
        Location loc = field.getFieldLocation();
        Location wall = new Location(loc.getWorld(), loc.getX(), 0, loc.getZ());
        wall.setZ(wall.getZ() + (double) (field.getSchema().getSize() - 1) / 2 + 1);
        int start = (int) wall.getX() - (field.getSchema().getSize() - 1) / 2;
        for(int i = start; i <= start + field.getSchema().getSize(); i++) {
            wall.setX(i);
            Location newLoc = new Location(wall.getWorld(), wall.getX(), 0, wall.getZ());
            locations.add(newLoc);
        }
        wall = new Location(loc.getWorld(), loc.getX(), 0, loc.getZ());
        wall.setZ(wall.getZ() - (double) (field.getSchema().getSize() - 1) / 2 - 1);
        start = (int) wall.getX() - (field.getSchema().getSize() - 1) / 2 - 1;
        for(int i = start; i <= start + field.getSchema().getSize() + 1; i++) {
            wall.setX(i);
            Location newLoc = new Location(wall.getWorld(), wall.getX(), 0, wall.getZ());
            locations.add(newLoc);
        }
        wall = new Location(loc.getWorld(), loc.getX(), 0, loc.getZ());
        wall.setX(wall.getX() + (double) (field.getSchema().getSize() - 1) / 2 + 1);
        start = (int) wall.getZ() - (field.getSchema().getSize() - 1) / 2;
        for(int i = start; i <= start + field.getSchema().getSize(); i++) {
            wall.setZ(i);
            Location newLoc = new Location(wall.getWorld(), wall.getX(), 0, wall.getZ());
            locations.add(newLoc);
        }
        wall = new Location(loc.getWorld(), loc.getX(), 0, loc.getZ());
        wall.setX(wall.getX() - (double) (field.getSchema().getSize() - 1) / 2 - 1);
        start = (int) wall.getZ() - (field.getSchema().getSize() - 1) / 2;
        for(int i = start; i <= start + field.getSchema().getSize(); i++) {
            wall.setZ(i);
            Location newLoc = new Location(wall.getWorld(), wall.getX(), 0, wall.getZ());
            locations.add(newLoc);
        }
        yIterator = startY;
        task = plugin.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, () -> {
            if(!player.isOnline() || iterator >= locations.size()) {
                if(yIterator >= maxY) {
                    cancelTask();
                    return;
                } else {
                    yIterator += spaces;
                    iterator = 0;
                }
            }
            for(int i = 0; i < 16; i++) {
                if(locations.size() - 1 < iterator) break;
                BlockData blockData = material.createBlockData();
                Location blockChangeLoc = locations.get(iterator++);
                blockChangeLoc.setY(yIterator);
                player.sendBlockChange(blockChangeLoc, blockData);
            }
        }, 0L, 1L);
    }

    private void cancelTask() {
        plugin.getServer().getScheduler().cancelTask(task);
        plugin.getServer().getScheduler().runTaskLater(plugin, this::rollback, 20L * 16);
    }

    private void rollback() {
        iterator = 0;
        yIterator = startY;
        rollbackTask = plugin.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, () -> {
            if(!player.isOnline() || iterator >= locations.size()) {
                if(yIterator >= maxY) {
                    cancelRollbackTask();
                    return;
                } else {
                    yIterator += spaces;
                    iterator = 0;
                }
            }
            for(int i = 0; i < 16; i++) {
                if(locations.size() - 1 < iterator) break;
                Location blockChangeLoc = locations.get(iterator++);
                blockChangeLoc.setY(yIterator);
                BlockData blockData = blockChangeLoc.getBlock().getBlockData();
                player.sendBlockChange(blockChangeLoc, blockData);
            }
        }, 0L, 1L);
    }

    private void cancelRollbackTask() {
        plugin.getServer().getScheduler().cancelTask(rollbackTask);
    }

}
