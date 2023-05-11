package pl.groundprotection.data;

import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import pl.groundprotection.GroundProtection;
import pl.groundprotection.fields.FieldFlag;
import pl.groundprotection.fields.FieldSchema;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

@Getter
public class DataHandler {

    private final GroundProtection plugin = GroundProtection.getInstance();
    private boolean fieldOverlap;
    private List<String> doorBlocks;

    public void loadConfig() {
        plugin.getFieldsManager().getFields().clear();
        plugin.getFieldsManager().getSchemas().clear();
        doorBlocks.clear();
        File configFile = getConfigFile();
        YamlConfiguration yml = YamlConfiguration.loadConfiguration(configFile);
        fieldOverlap = yml.getBoolean("config.fieldOverlap");
        doorBlocks = yml.getStringList("config.doorBlocks");
        loadFields();
    }

    public void loadFields() {
        File configFile = getConfigFile();
        YamlConfiguration yml = YamlConfiguration.loadConfiguration(configFile);
        ConfigurationSection section = yml.getConfigurationSection("fields");
        if(section == null) return;
        for(String fieldName : section.getKeys(false)) {
            String path = "fields." + fieldName + ".";
            int size = yml.getInt(path + "size");
            if(size % 2 == 0) {
                plugin.getLogger().warning("Size in " + fieldName + " field must be an odd number, adding 1...");
                size++;
            }
            String materialStr = yml.getString(path + "item");
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
                    material,
                    permission,
                    flags,
                    disabledWorlds,
                    limits
            );
            plugin.getFieldsManager().addFieldSchema(schema);
        }
    }

    public File getConfigFile() {
        File configFile = new File(plugin.getDataFolder(), "config.yml");
        if(!configFile.exists()) {
            plugin.saveResource("config.yml", false);
        }
        return configFile;
    }

}
