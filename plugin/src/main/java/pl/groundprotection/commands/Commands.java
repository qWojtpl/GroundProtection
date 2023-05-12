package pl.groundprotection.commands;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import pl.groundprotection.GroundProtection;
import pl.groundprotection.fields.Field;
import pl.groundprotection.fields.FieldSchema;
import pl.groundprotection.fields.FieldVisualizer;
import pl.groundprotection.fields.FieldsManager;

import java.util.List;

public class Commands implements CommandExecutor {

    private final GroundProtection plugin = GroundProtection.getInstance();
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
                        sender.sendMessage("No fields found");
                    } else {
                        sender.sendMessage("Field owner: " + field.getFieldOwner());
                    }
                } else if(args[0].equalsIgnoreCase("locations")) {
                    List<Field> fields = fieldsManager.getPlayerFields((Player) sender);
                    for(Field field : fields) {
                        Location loc = field.getFieldLocation();
                        sender.sendMessage( field.getSchema().getName() + ": "
                                + (int) loc.getX() + ", "
                                + (int) loc.getY() + ", "
                                + (int) loc.getZ()
                                + " (" + loc.getWorld().getName() + ")");
                    }
                } else if(args[0].equalsIgnoreCase("counts")) {
                    for(String name : fieldsManager.getSchemas().keySet()) {
                        FieldSchema schema = fieldsManager.getFieldSchema(name);
                        if(schema == null) continue;
                        int count = fieldsManager.getCurrentCount(schema, (Player) sender);
                        int max = fieldsManager.getLimit(schema, (Player) sender);
                        sender.sendMessage(schema.getName() + ": " + count + "/" + max);
                    }
                } else if(args[0].equalsIgnoreCase("visualize")) {
                    List<Field> fields = fieldsManager.getFields(((Player) sender).getLocation());
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
                            new FieldVisualizer(field, (Player) sender, materials[j], 16 + (2 * i));
                            j++;
                            if(j >= materials.length) {
                                j = 0;
                            }
                        }
                    }
                    if(i == 0) {
                        sender.sendMessage("No fields found");
                    }
                }
            }
        }
        return true;
    }

}
