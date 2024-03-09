package pl.groundprotection;

import lombok.Getter;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;
import pl.groundprotection.api.GroundProtectionAPI;
import pl.groundprotection.commands.CommandHelper;
import pl.groundprotection.commands.Commands;
import pl.groundprotection.data.DataHandler;
import pl.groundprotection.data.Messages;
import pl.groundprotection.events.FieldBlockEvents;
import pl.groundprotection.events.FieldProtectionEvents;
import pl.groundprotection.fields.FieldsManager;
import pl.groundprotection.permissions.PermissionManager;

@Getter
public final class GroundProtection extends JavaPlugin {

    private static GroundProtection main;
    private FieldsManager fieldsManager;
    private PermissionManager permissionManager;
    private DataHandler dataHandler;
    private Messages messages;

    @Override
    public void onEnable() {
        main = this;
        this.messages = new Messages();
        this.permissionManager = new PermissionManager();
        this.fieldsManager = new FieldsManager();
        this.dataHandler = new DataHandler();
        dataHandler.loadConfig();
        dataHandler.registerSaveTask();
        getServer().getPluginManager().registerEvents(new FieldBlockEvents(), this);
        getServer().getPluginManager().registerEvents(new FieldProtectionEvents(), this);
        PluginCommand command = getCommand("groundprotection");
        if(command != null) {
            command.setExecutor(new Commands());
            command.setTabCompleter(new CommandHelper());
        }
        getLogger().info("Loaded.");
    }

    @Override
    public void onDisable() {
        dataHandler.save();
        getLogger().info("Disabled.");
    }

    public static GroundProtection getInstance() {
        return main;
    }

    public static GroundProtectionAPI getAPI() {
        return new GroundProtectionAPI();
    }

}
