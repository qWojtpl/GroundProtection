package pl.groundprotection.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import pl.groundprotection.GroundProtection;
import pl.groundprotection.fields.Field;
import pl.groundprotection.fields.FieldsManager;

public class Commands implements CommandExecutor {

    private final GroundProtection plugin = GroundProtection.getInstance();
    private final FieldsManager fieldsManager = plugin.getFieldsManager();

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if(args.length > 0) {
            if(sender instanceof Player) {
                if(args[0].equalsIgnoreCase("info")) {
                    Field field = fieldsManager.getField(((Player) sender).getLocation());
                    if(field == null) {
                        sender.sendMessage("No fields found");
                    } else {
                        sender.sendMessage("Field owner: " + field.getFieldOwner());
                    }
                }
            }
        }
        return true;
    }

}
