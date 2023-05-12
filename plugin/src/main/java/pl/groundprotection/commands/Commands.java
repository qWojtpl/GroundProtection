package pl.groundprotection.commands;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import pl.groundprotection.GroundProtection;
import pl.groundprotection.data.Messages;
import pl.groundprotection.fields.Field;
import pl.groundprotection.fields.FieldSchema;
import pl.groundprotection.fields.FieldVisualizer;
import pl.groundprotection.fields.FieldsManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class Commands implements CommandExecutor {

    private final GroundProtection plugin = GroundProtection.getInstance();
    private final Messages messages = plugin.getMessages();
    private final FieldsManager fieldsManager = plugin.getFieldsManager();

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if(args.length > 0) {
            if(args[0].equalsIgnoreCase("reload")) {
                plugin.getDataHandler().save();
                plugin.getDataHandler().loadConfig();
                return true;
            }
            if(sender instanceof Player) {
                if(args[0].equalsIgnoreCase("info")) {
                    Field field = fieldsManager.getField(((Player) sender).getLocation());
                    if(field == null) {
                        sender.sendMessage(messages.getPrefixedMessage("noFieldFound"));
                    } else {
                        sender.sendMessage("Field owner: " + field.getFieldOwner());
                    }
                } else if(args[0].equalsIgnoreCase("locations")) {
                    getLocations(sender, sender.getName());
                } else if(args[0].equalsIgnoreCase("counts")) {
                    countPlayerFields(sender, sender.getName());
                } else if(args[0].equalsIgnoreCase("visualize")) {
                    visualizeField((Player) sender);
                } else {
                    showHelp(sender);
                }
            } else {
                showHelp(sender);
            }
        } else {
            showHelp(sender);
        }
        return true;
    }

    private void visualizeField(Player sender) {
        List<Field> fields = fieldsManager.getFields((sender).getLocation());
        final Material[] materials = new Material[]{
                Material.RED_STAINED_GLASS,
                Material.GREEN_STAINED_GLASS,
                Material.YELLOW_STAINED_GLASS,
                Material.BLUE_STAINED_GLASS,
                Material.PURPLE_STAINED_GLASS,
                Material.ORANGE_STAINED_GLASS,
                Material.WHITE_STAINED_GLASS
        };
        int i = 0;
        int j = 0;
        for(Field field : fields) {
            if(field.getFieldOwner().equalsIgnoreCase(sender.getName()) ||
                    field.getFieldContributors().contains(sender.getName())) {
                i++;
                new FieldVisualizer(field, sender, materials[j], 16 + (2 * i));
                j++;
                if(j >= materials.length) {
                    j = 0;
                }
            }
        }
        if(i == 0) {
            sender.sendMessage(messages.getPrefixedMessage("noFieldFound"));
        } else {
            sender.sendMessage(messages.getPrefixedMessage("visualizing"));
        }
    }

    private void countPlayerFields(CommandSender sender, String player) {
        sender.sendMessage("§5{========== §b" + player + "'s fields §5==========}");
        sender.sendMessage(" ");
        for(String name : fieldsManager.getSchemas().keySet()) {
            FieldSchema schema = fieldsManager.getFieldSchema(name);
            if(schema == null) continue;
            int count = fieldsManager.getCurrentCount(schema, player);
            int max = fieldsManager.getLimit(schema, player);
            String color = "§b";
            if(count == max) {
                color = "§4";
            }
            sender.sendMessage("§e" + schema.getName() + "§5: " + color + count + "§5/" + color + max);
        }
        sender.sendMessage(" ");
    }

    private void getLocations(CommandSender sender, String player) {
        sender.sendMessage("§5{========== §b" + player + "'s fields §5==========}");
        sender.sendMessage(" ");
        List<Field> fields = fieldsManager.getPlayerFields((Player) sender);
        for(String schemaKey : fieldsManager.getSchemas().keySet()) {
            FieldSchema schema = fieldsManager.getFieldSchema(schemaKey);
            if(schema == null) continue;
            for(Field field : fields) {
                if(!field.getSchema().equals(schema)) continue;
                Location loc = field.getFieldLocation();
                sender.sendMessage("§e" + field.getSchema().getName() + "§5: §b"
                        + (int) loc.getX() + "§5, §b"
                        + (int) loc.getY() + "§5, §b"
                        + (int) loc.getZ()
                        + ((loc.getWorld() != null) ? " §5(§b" + loc.getWorld().getName() + "§5)" : ""));
            }
        }
        sender.sendMessage(" ");
    }

    private void showHelp(CommandSender sender) {
        sender.sendMessage("§5{========== §bGroundProtection §5==========}");
        sender.sendMessage(" ");
        sender.sendMessage("§5/§egp info §5- §bCheck owner of field that you're standing on");
        sender.sendMessage("§5/§egp visualize §5- §bVisualize field. You must be an contributor or owner.");
        sender.sendMessage("§5/§egp locations §5- §bGet locations of your owned fields");
        sender.sendMessage(" ");
    }

}
