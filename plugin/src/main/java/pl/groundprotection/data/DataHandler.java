package pl.groundprotection.data;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import pl.groundprotection.GroundProtection;
import pl.groundprotection.fields.*;
import pl.groundprotection.util.DateManager;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Getter
public class DataHandler {

    private final GroundProtection plugin = GroundProtection.getInstance();
    private final FieldsManager fieldsManager = plugin.getFieldsManager();
    private boolean fieldOverlap;
    private List<String> doorBlocks = new ArrayList<>();
    private List<String> chestBlocks = new ArrayList<>();
    private List<String> otherBlocks = new ArrayList<>();
    private List<String> animals = new ArrayList<>();
    private List<String> hostiles = new ArrayList<>();
    private List<String> otherEntities = new ArrayList<>();
    private YamlConfiguration data = new YamlConfiguration();
    @Setter
    private int lastFieldID = -1;

    public void loadConfig() {
        data = new YamlConfiguration();
        plugin.getFieldsManager().getFields().clear();
        plugin.getFieldsManager().getSchemas().clear();
        plugin.getMessages().clearMessages();
        plugin.getPermissionManager().clearPermissions();
        doorBlocks.clear();
        File configFile = getConfigFile();
        YamlConfiguration yml = YamlConfiguration.loadConfiguration(configFile);
        fieldOverlap = yml.getBoolean("config.fieldOverlap");
        doorBlocks = yml.getStringList("protectList.door_blocks");
        chestBlocks = yml.getStringList("protectList.chest_blocks");
        otherBlocks = yml.getStringList("protectList.other_blocks");
        animals = yml.getStringList("protectList.animals");
        hostiles = yml.getStringList("protectList.hostiles");
        otherEntities = yml.getStringList("protectList.other_entities");
        loadFields();
        restoreFields();
        loadMessages();
        loadPermissions();
    }

    public void loadFields() {
        File configFile = getConfigFile();
        YamlConfiguration yml = YamlConfiguration.loadConfiguration(configFile);
        ConfigurationSection section = yml.getConfigurationSection("fields");
        if(section == null) {
            return;
        }
        for(String fieldName : section.getKeys(false)) {
            String path = "fields." + fieldName + ".";
            int size = yml.getInt(path + "size");
            if(size % 2 == 0) {
                plugin.getLogger().warning("Size in " + fieldName + " field must be an odd number, adding 1...");
                size++;
            }
            int daysToRemove = yml.getInt(path + "removeAfterDays");
            String materialStr = yml.getString(path + "item.material");
            if(materialStr == null) {
                plugin.getLogger().severe("Cannot load " + fieldName + " field, because item is null");
                continue;
            }
            Material material = Material.getMaterial(materialStr);
            if(material == null) {
                plugin.getLogger().severe("Cannot compare " + materialStr + " with a correct material!");
                plugin.getLogger().severe("Cannot load " + fieldName + " field");
                continue;
            }
            FieldItem item = new FieldItem(
                    material,
                    yml.getString(path + "item.name", ""),
                    yml.getStringList(path + "item.lore"));
            String permission = yml.getString(path + "permission");
            List<String> flagsStr = yml.getStringList(path + "flags");
            List<FieldFlag> flags = new ArrayList<>();
            for(String f : flagsStr) {
                FieldFlag flag = plugin.getFieldsManager().getFlag(f);
                if(flag == null) {
                    plugin.getLogger().warning("Cannot compare " + f + " with a correct flag! This flag is now skipped.");
                    continue;
                }
                flags.add(flag);
            }
            List<String> disabledWorlds = yml.getStringList(path + "disabledWorlds");
            List<String> limits = yml.getStringList(path + "limits");
            FieldSchema schema = new FieldSchema(
                    fieldName,
                    size,
                    item,
                    permission,
                    flags,
                    disabledWorlds,
                    limits,
                    daysToRemove
            );
            fieldsManager.addFieldSchema(schema);
        }
    }

    public void restoreFields() {
        File dataFile = getDataFile();
        YamlConfiguration yml = YamlConfiguration.loadConfiguration(dataFile);
        data = yml;
        ConfigurationSection section = yml.getConfigurationSection("fields");
        if(section == null) {
            return;
        }
        for(String key : section.getKeys(false)) {
            int id = 0;
            try {
                id = Integer.parseInt(key);
            } catch(NumberFormatException e) {
                plugin.getLogger().severe("Cannot compare " + key + " with a correct id-number!");
            }
            String path = "fields." + key + ".";
            FieldSchema schema = fieldsManager.getFieldSchema(yml.getString(path + "type"));
            if(schema == null) continue;
            if(schema.getDaysToRemove() != 0) {
                if(schema.getDaysToRemove() < DateManager.calculateDays(
                        yml.getString(path + "lastOwnerLogin"), DateManager.getDate("/"), "/")) {
                    removeField(key);
                    continue;
                }
            }
            String owner = yml.getString(path + "owner");
            List<String> contributors = yml.getStringList(path + "contributors");
            List<Integer> cords = yml.getIntegerList(path + "location");
            String worldStr = yml.getString(path + "world");
            if(worldStr == null) continue;
            World w = plugin.getServer().getWorld(worldStr);
            if(w == null) {
                plugin.getLogger().info("Cannot find world: " + worldStr);
                continue;
            }
            Location loc = new Location(w, cords.get(0), cords.get(1), cords.get(2));
            Field field = new Field(
                    Integer.parseInt(key),
                    schema,
                    loc,
                    owner,
                    contributors);
            fieldsManager.getFields().add(field);
            if(id > lastFieldID) {
                lastFieldID = id;
            }
        }
    }

    public void loadMessages() {
        File messagesFile = getMessagesFile();
        YamlConfiguration yml = YamlConfiguration.loadConfiguration(messagesFile);
        ConfigurationSection section = yml.getConfigurationSection("messages");
        if(section == null) {
            return;
        }
        Messages messages = plugin.getMessages();
        for(String key : section.getKeys(false)) {
            messages.addMessage(key, yml.getString("messages." + key));
        }
    }

    public void loadPermissions() {
        File configFile = getConfigFile();
        YamlConfiguration yml = YamlConfiguration.loadConfiguration(configFile);
        ConfigurationSection section = yml.getConfigurationSection("permissions");
        if(section == null) {
            return;
        }
        for(String key : section.getKeys(false)) {
            String name = yml.getString("permissions." + key);
            if(name != null) {
                String[] descriptionArray = name.split("(?<=.)(?=\\p{Lu})");
                String description = descriptionArray[0];
                description = description.substring(0, 1).toUpperCase() + description.substring(1);
                for(int i = 1; i < descriptionArray.length; i++) {
                    description += " " + descriptionArray[i].toLowerCase();
                }
                description += " in GroundProtection plugin";
                plugin.getPermissionManager().registerPermission(key, name, description);
            }
        }
    }

    public void save() {
        try {
            data.save(getDataFile());
        } catch(IOException e) {
            plugin.getLogger().severe("Cannot save data.yml: " + e.getMessage());
        }
    }

    public void saveField(Field field) {
        String path = "fields." + field.getID() + ".";
        data.set(path + "type", field.getSchema().getName());
        data.set(path + "owner", field.getFieldOwner());
        data.set(path + "contributors", field.getFieldContributors());
        List<Integer> location = new ArrayList<>();
        location.add((int) field.getFieldLocation().getX());
        location.add((int) field.getFieldLocation().getY());
        location.add((int) field.getFieldLocation().getZ());
        data.set(path + "location", location);
        if(field.getFieldLocation().getWorld() != null) {
            data.set(path + "world", field.getFieldLocation().getWorld().getName());
        }
        saveLogin(field.getFieldOwner());
    }

    public void saveFieldContributors(Field field) {
        String path = "fields." + field.getID() + ".";
        data.set(path + "contributors", field.getFieldContributors());
    }

    public void removeField(String id) {
        data.set("fields." + id, null);
    }

    public void saveLogin(String player) {
        for(Field field : fieldsManager.getPlayerFields(player)) {
            saveLogin(field);
        }
    }

    public void saveLogin(Field field) {
        data.set("fields." + field.getID() + ".lastOwnerLogin", DateManager.getDate("/"));
    }

    public File getConfigFile() {
        File configFile = new File(plugin.getDataFolder(), "config.yml");
        if(!configFile.exists()) {
            plugin.saveResource("config.yml", false);
        }
        return configFile;
    }

    public File getDataFile() {
        File dataFile = new File(plugin.getDataFolder(), "data.yml");
        if(!dataFile.exists()) {
            plugin.saveResource("data.yml", false);
        }
        return dataFile;
    }

    public File getMessagesFile() {
        File messagesFile = new File(plugin.getDataFolder(), "messages.yml");
        if(!messagesFile.exists()) {
            plugin.saveResource("messages.yml", false);
        }
        return messagesFile;
    }

}
