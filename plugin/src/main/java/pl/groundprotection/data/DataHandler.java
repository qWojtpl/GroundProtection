package pl.groundprotection.data;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import pl.groundprotection.GroundProtection;

import java.io.File;

public class DataHandler {

    private final GroundProtection plugin = GroundProtection.getInstance();

    public void loadConfig() {
        loadFields();
    }

    public void loadFields() {
        File configFile = getConfigFile();
        YamlConfiguration yml = YamlConfiguration.loadConfiguration(configFile);
        ConfigurationSection section = yml.getConfigurationSection("fields");
        if(section == null) return;
        for(String fieldName : section.getKeys(false)) {

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
