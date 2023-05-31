package pl.groundprotection.fields;

import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import pl.groundprotection.GroundProtection;
import pl.groundprotection.data.DataHandler;
import pl.groundprotection.data.Messages;
import pl.groundprotection.util.PlayerUtil;

import javax.annotation.Nullable;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Getter
public class FieldsManager {

    private final GroundProtection plugin = GroundProtection.getInstance();
    private final Messages messages = plugin.getMessages();
    private final HashMap<String, FieldSchema> schemas = new HashMap<>();
    private final List<Field> fields = new ArrayList<>();

    public void addFieldSchema(FieldSchema schema) {
        schemas.put(schema.getName(), schema);
    }

    @Nullable
    public FieldSchema getFieldSchema(String name) {
        return schemas.getOrDefault(name, null);
    }

    public void removeFieldSchema(FieldSchema schema) {
        schemas.remove(schema.getName());
    }

    public void removeFieldSchema(String schema) {
        schemas.remove(schema);
    }

    @Nullable
    public FieldFlag getFlag(String name) {
        for(FieldFlag flag : FieldFlag.values()) {
            if(flag.name().equals(name)) return flag;
        }
        return null;
    }

    public boolean canPlaceField(FieldSchema schema, Player player, Location location) {
        for(Field field : fields) {
            FieldSchema s = field.getSchema();
            if(plugin.getDataHandler().isFieldOverlap()) {
                if(field.getFieldOwner().equals(player.getName())) continue;
                if(field.getFieldContributors().contains(player.getName())) continue;
            }
            if(field.getFieldLocation().getWorld() != null) {
                if (!field.getFieldLocation().getWorld().equals(location.getWorld())) continue;
            }
            if(field.getFieldLocation().distance(location) > (schema.getSize() + s.getSize()) * 2) continue;
            if(getDistance(field.getFieldLocation(), location) > (schema.getSize()-1)/2 + (s.getSize()-1)/2) continue;
            return false;
        }
        return true;
    }

    @Nullable
    public Field getField(Location location) {
        List<Field> fields = getFields(location);
        if(fields.size() > 0) {
            return fields.get(0);
        }
        return null;
    }

    public List<Field> getFields(Location location) {
        List<Field> currentFields = new ArrayList<>();
        location = location.clone().subtract(0, 1, 0);
        location = location.getBlock().getLocation();
        for(Field field : fields) {
            if(field.getFieldLocation().getWorld() != null) {
                if(!field.getFieldLocation().getWorld().equals(location.getWorld())) continue;
            }
            if(field.getFieldLocation().distance(location) > field.getSchema().getSize() * 2) continue;
            FieldSchema schema = field.getSchema();
            if(getDistance(field.getFieldLocation(), location) <= (schema.getSize() - 1) / 2) {
                currentFields.add(field);
            }
        }
        return currentFields;
    }

    public List<Field> getPlayerFields(Player player) {
        return getPlayerFields(player.getName());
    }

    public List<Field> getPlayerFields(String player) {
        List<Field> playerFields = new ArrayList<>();
        for(Field field : fields) {
            if(field.getFieldOwner().equals(player)) {
                playerFields.add(field);
            }
        }
        return playerFields;
    }

    public List<Field> getPlayerFieldsBySchema(Player player, FieldSchema schema) {
        return getPlayerFieldsBySchema(player.getName(), schema);
    }

    public List<Field> getPlayerFieldsBySchema(String player, FieldSchema schema) {
        List<Field> playerFields = new ArrayList<>();
        for(Field field : fields) {
            if(field.getFieldOwner().equals(player)) {
                if(field.getSchema().equals(schema)) {
                    playerFields.add(field);
                }
            }
        }
        return playerFields;
    }

    public void createField(FieldSchema schema, Player owner, Location location) {
        DataHandler dataHandler = plugin.getDataHandler();
        dataHandler.setLastFieldID(dataHandler.getLastFieldID() + 1);
        Field field = new Field(dataHandler.getLastFieldID(), schema, location, owner.getName(), new ArrayList<>());
        owner.sendMessage(MessageFormat.format(messages.getMessage("placedField"), schema.getName()));
        fields.add(field);
        dataHandler.saveField(field);
    }

    public void removeField(Field field, Player player) {
        player.sendMessage(MessageFormat.format(messages.getMessage("removedField"), field.getSchema().getName()));
        fields.remove(field);
        plugin.getDataHandler().removeField(String.valueOf(field.getID()));
    }

    public int getDistance(Location fieldLocation, Location requestedLocation) {
        int distance = 0;
        Location loc = fieldLocation.clone();
        Location req = requestedLocation.clone();
        while(loc.getX() != req.getX() || loc.getZ() != req.getZ()) {
            if(loc.getX() > req.getX()) {
                loc.setX(loc.getX() - 1);
            } else if(loc.getX() < req.getX()) {
                loc.setX(loc.getX() + 1);
            }
            if(loc.getZ() > req.getZ()) {
                loc.setZ(loc.getZ() - 1);
            } else if(loc.getZ() < req.getZ()) {
                loc.setZ(loc.getZ() + 1);
            }
            distance++;
        }
        return distance;
    }

    public boolean isAllowed(Location location, String player) {
        for(Field field : plugin.getFieldsManager().getFields(location)) {
            if(field.getFieldOwner().equals(player)) return true;
            if(field.getFieldContributors().contains(player)) return true;
        }
        return false;
    }

    public boolean isReachedLimit(FieldSchema schema, Player player) {
        return isReachedLimit(schema, player.getName());
    }

    public boolean isReachedLimit(FieldSchema schema, String player) {
        return (getCurrentCount(schema, player) >= getLimit(schema, player));
    }

    public int getCurrentCount(FieldSchema schema, Player player) {
        return getCurrentCount(schema, player.getName());
    }

    public int getCurrentCount(FieldSchema schema, String player) {
        int count = 0;
        for(Field field : fields) {
            if(field.getFieldOwner().equals(player)) {
                if(field.getSchema().getName().equals(schema.getName())) {
                    count++;
                }
            }
        }
        return count;
    }

    public int getLimit(FieldSchema schema, Player player) {
        int returnable = 0;
        for(String limit : schema.getLimits()) {
            String[] split = limit.split(":");
            if(split.length != 2) continue;
            if(player.hasPermission(split[0])) {
                try {
                    int a = Integer.parseInt(split[1]);
                    if(a > returnable) {
                        returnable = a;
                    }
                } catch(NumberFormatException e) {
                    plugin.getLogger().severe("Cannot compare " + split[1] + " with a correct number! " +
                            "(schema: " + schema.getName() + ", limit: " + limit + ")");
                }
            }
        }
        return returnable;
    }

    public int getLimit(FieldSchema schema, String player) {
        return getLimit(schema, PlayerUtil.getPlayer(player));
    }

    public void addContributor(Field field, String player) {
        field.getFieldContributors().remove(player);
        field.getFieldContributors().add(player);
        plugin.getDataHandler().saveFieldContributors(field);
    }

    public void removeContributor(Field field, String player) {
        field.getFieldContributors().remove(player);
        plugin.getDataHandler().saveFieldContributors(field);
    }

}
