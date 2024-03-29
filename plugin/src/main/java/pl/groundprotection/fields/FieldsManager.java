package pl.groundprotection.fields;

import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import pl.beaverlib.util.PlayerUtil;
import pl.groundprotection.GroundProtection;
import pl.groundprotection.data.DataHandler;
import pl.groundprotection.data.Messages;

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
            if(flag.name().equals(name)) {
                return flag;
            }
        }
        return null;
    }

    public boolean canPlaceField(FieldSchema schema, String player, Location location) {
        player = PlayerUtil.parseNickname(player, plugin.getDataHandler().isUuidMode());
        for(Field field : fields) {
            FieldSchema s = field.getSchema();
            if(plugin.getDataHandler().isFieldOverlap()) {
                if(field.getFieldOwner().equals(player)) {
                    continue;
                }
                if(field.getFieldContributors().contains(player)) {
                    continue;
                }
            }
            if(field.getFieldLocation().getWorld() != null) {
                if (!field.getFieldLocation().getWorld().equals(location.getWorld())) {
                    continue;
                }
            }
            Location fieldLocationZero = field.getFieldLocation().clone();
            fieldLocationZero.setY(0);
            Location locationZero = location.clone();
            locationZero.setY(0);
            if(fieldLocationZero.distance(locationZero) > (schema.getSize() + s.getSize()) * 2) {
                continue;
            }
            if(getDistance(field.getFieldLocation(), location) > (schema.getSize()-1)/2 + (s.getSize()-1)/2) {
                continue;
            }
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
                if(!field.getFieldLocation().getWorld().equals(location.getWorld())) {
                    continue;
                }
            }
            Location fieldLocationZero = field.getFieldLocation().clone();
            fieldLocationZero.setY(0);
            Location locationZero = location.clone();
            locationZero.setY(0);
            if(fieldLocationZero.distance(locationZero) > field.getSchema().getSize() * 2) {
                continue;
            }
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
        player = PlayerUtil.parseNickname(player, plugin.getDataHandler().isUuidMode());
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
        player = PlayerUtil.parseNickname(player, plugin.getDataHandler().isUuidMode());
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

    public void createField(FieldSchema schema, String owner, Location location) {
        owner = PlayerUtil.parseNickname(owner, plugin.getDataHandler().isUuidMode());
        DataHandler dataHandler = plugin.getDataHandler();
        dataHandler.setLastFieldID(dataHandler.getLastFieldID() + 1);
        Field field = new Field(dataHandler.getLastFieldID(), schema, location, owner, new ArrayList<>());
        Player ownerPlayer = PlayerUtil.getPlayer(owner);
        if(ownerPlayer != null) {
            ownerPlayer.sendMessage(MessageFormat.format(messages.getMessage("placedField"), schema.getName()));
        }
        fields.add(field);
        dataHandler.saveField(field);
    }

    public void removeField(Field field, String player) {
        Player p = PlayerUtil.getPlayer(player);
        if(p != null) {
            p.sendMessage(MessageFormat.format(messages.getMessage("removedField"), field.getSchema().getName()));
        }
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
        player = PlayerUtil.parseNickname(player, plugin.getDataHandler().isUuidMode());
        for(Field field : plugin.getFieldsManager().getFields(location)) {
            if(field.getFieldOwner().equals(player)) {
                return true;
            }
            if(field.getFieldContributors().contains(player)) {
                return true;
            }
        }
        return false;
    }

    public boolean isReachedLimit(FieldSchema schema, Player player) {
        return isReachedLimit(schema, player.getName());
    }

    public boolean isReachedLimit(FieldSchema schema, String player) {
        player = PlayerUtil.parseNickname(player, plugin.getDataHandler().isUuidMode());
        return getCurrentCount(schema, player) >= getLimit(schema, player);
    }

    public int getCurrentCount(FieldSchema schema, Player player) {
        return getCurrentCount(schema, player.getName());
    }

    public int getCurrentCount(FieldSchema schema, String player) {
        player = PlayerUtil.parseNickname(player, plugin.getDataHandler().isUuidMode());
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
        return getLimit(schema, player.getName());
    }

    public int getLimit(FieldSchema schema, String player) {
        Player p = PlayerUtil.getPlayer(PlayerUtil.parseNickname(player, plugin.getDataHandler().isUuidMode()));
        if(p == null) {
            return 0;
        }
        int returnable = 0;
        for(String limit : schema.getLimits()) {
            String[] split = limit.split(":");
            if(split.length != 2) {
                continue;
            }
            if(p.hasPermission(split[0])) {
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

    public void addContributor(Field field, String player) {
        player = PlayerUtil.parseNickname(player, plugin.getDataHandler().isUuidMode());
        field.getFieldContributors().remove(player);
        field.getFieldContributors().add(player);
        plugin.getDataHandler().saveFieldContributors(field);
    }

    public void removeContributor(Field field, String player) {
        player = PlayerUtil.parseNickname(player, plugin.getDataHandler().isUuidMode());
        field.getFieldContributors().remove(player);
        plugin.getDataHandler().saveFieldContributors(field);
    }

}
