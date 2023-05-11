package pl.groundprotection;

import lombok.Getter;
import org.bukkit.plugin.java.JavaPlugin;
import pl.groundprotection.commands.CommandHelper;
import pl.groundprotection.commands.Commands;
import pl.groundprotection.data.DataHandler;
import pl.groundprotection.events.Events;
import pl.groundprotection.fields.FieldsManager;
import pl.groundprotection.permissions.PermissionManager;

@Getter
public final class GroundProtection extends JavaPlugin {

    private static GroundProtection main;
    private FieldsManager fieldsManager;
    private PermissionManager permissionManager;
    private DataHandler dataHandler;

    @Override
    public void onEnable() {
        main = this;
        this.permissionManager = new PermissionManager();
        this.fieldsManager = new FieldsManager();
        this.dataHandler = new DataHandler();
        dataHandler.loadConfig();
        getServer().getPluginManager().registerEvents(new Events(), this);
        getCommand("groundprotection").setExecutor(new Commands());
        getCommand("groundprotection").setTabCompleter(new CommandHelper());
        getLogger().info("Loaded.");
    }

    @Override
    public void onDisable() {
        getLogger().info("Disabled.");
    }

    public static GroundProtection getInstance() {
        return main;
    }

}
