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
import pl.groundprotection.permissions.PermissionManager;
import pl.groundprotection.util.LocationUtil;
import pl.groundprotection.util.PlayerUtil;

import java.text.MessageFormat;
import java.util.List;

public class Commands implements CommandExecutor {

    private final GroundProtection plugin = GroundProtection.getInstance();
    private final Messages messages = plugin.getMessages();
    private final FieldsManager fieldsManager = plugin.getFieldsManager();
    private final PermissionManager permissionManager = plugin.getPermissionManager();

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if(args.length > 0) {
            if(args[0].equalsIgnoreCase("reload")) {
                reload(sender);
            } else if(args[0].equalsIgnoreCase("locations")) {
                if(args.length > 1) {
                    if(sender.hasPermission(permissionManager.getPermission("getOtherPlayerFieldLocations"))) {
                        getLocations(sender, args[1]);
                    } else {
                        getLocations(sender, sender.getName());
                    }
                } else {
                    getLocations(sender, sender.getName());
                }
            } else if(args[0].equalsIgnoreCase("counts")) {
                if(args.length > 1) {
                    if(sender.hasPermission(permissionManager.getPermission("countOtherPlayerFields"))) {
                        countPlayerFields(sender, args[1]);
                    } else {
                        countPlayerFields(sender, sender.getName());
                    }
                } else {
                    countPlayerFields(sender, sender.getName());
                }
            } else if(sender instanceof Player) {
                if(args[0].equalsIgnoreCase("info")) {
                    fieldInfo((Player) sender);
                } else if(args[0].equalsIgnoreCase("visualize")) {
                    visualizeField((Player) sender);
                } else if(args[0].equalsIgnoreCase("allow")) {
                    if(args.length < 2) {
                        sender.sendMessage(messages.getMessage("mustProvidePlayer"));
                    } else {
                        allowPlayer((Player) sender, args[1]);
                    }
                } else if(args[0].equalsIgnoreCase("remove")) {
                    if(args.length < 2) {
                        sender.sendMessage(messages.getMessage("mustProvidePlayer"));
                    } else {
                        removePlayer((Player) sender, args[1]);
                    }
                } else if(args[0].equalsIgnoreCase("get")) {
                    if(args.length < 2) {
                        sender.sendMessage(messages.getMessage("mustProvideField"));
                    } else {
                        giveField((Player) sender, args[1]);
                    }
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

    private void fieldInfo(Player sender) {
        if(!permissionManager.checkPermission(sender, "getFieldInfo")) {
            return;
        }
        List<Field> fields = fieldsManager.getFields((sender).getLocation());
        if(fields.size() == 0) {
            sender.sendMessage(messages.getMessage("noFieldFound"));
        } else {
            for(Field field : fields) {
                String contributors = "§4-";
                if(field.getFieldContributors().size() > 0) {
                    contributors = field.getFieldContributors().get(0);
                    int i = 0;
                    for(String contributor : field.getFieldContributors()) {
                        if(i == 0) {
                            i++;
                            continue;
                        }
                        contributors += ", " + contributor;
                    }
                }
                String location = LocationUtil.locationBuilder(field.getFieldLocation());
                if(field.getFieldOwner().equals(sender.getName()) ||
                        field.getFieldContributors().contains(sender.getName()) ||
                        sender.hasPermission(permissionManager.getPermission("alwaysGetAccurateFieldInfo"))) {
                    String message = messages.getMessage("fieldInfoContributor");
                    String[] split = message.split("%nl%");
                    for(String msg : split) {
                        sender.sendMessage(MessageFormat.format(msg,
                                field.getFieldOwner(),
                                field.getSchema().getName(),
                                contributors,
                                location));
                    }
                } else {
                    sender.sendMessage(MessageFormat.format(messages.getMessage("fieldInfo"),
                            field.getFieldOwner(),
                            field.getSchema().getName(),
                            contributors,
                            location));
                    return;
                }
            }
        }
    }

    private void reload(CommandSender sender) {
        if(sender instanceof Player) {
            if(!permissionManager.checkPermission((Player) sender, "reloadConfiguration")) {
                return;
            }
        }
        plugin.getDataHandler().save();
        plugin.getDataHandler().loadConfig();;
        sender.sendMessage(messages.getMessage("reloaded"));
    }

    private void visualizeField(Player sender) {
        if(!permissionManager.checkPermission(sender, "visualizeField")) {
            return;
        }
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
                    field.getFieldContributors().contains(sender.getName()) ||
                    sender.hasPermission(permissionManager.getPermission("alwaysVisualizeField"))) {
                i++;
                new FieldVisualizer(field, sender, materials[j], 16 + (2 * i));
                j++;
                if(j >= materials.length) {
                    j = 0;
                }
            }
        }
        if(i == 0) {
            sender.sendMessage(messages.getMessage("noFieldFound"));
        } else {
            sender.sendMessage(messages.getMessage("visualizing"));
        }
    }

    private void countPlayerFields(CommandSender sender, String player) {
        if(sender instanceof Player) {
            if(!permissionManager.checkPermission((Player) sender, "countFields")) {
                return;
            }
        } else {
            if(player.equals("CONSOLE")) {
                sender.sendMessage(messages.getMessage("mustProvidePlayer"));
                return;
            }
        }
        if(PlayerUtil.getPlayer(player) == null) {
            sender.sendMessage(messages.getMessage("playerNotOnline"));
            return;
        }
        sender.sendMessage("§5{========== §b" + player + "'s fields §5==========}");
        sender.sendMessage(" ");
        for(String name : fieldsManager.getSchemas().keySet()) {
            FieldSchema schema = fieldsManager.getFieldSchema(name);
            if(schema == null) {
                continue;
            }
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
        if(sender instanceof Player) {
            if(!permissionManager.checkPermission((Player) sender, "getFieldLocations")) {
                return;
            }
        } else {
            if(player.equals("CONSOLE")) {
                sender.sendMessage(messages.getMessage("mustProvidePlayer"));
                return;
            }
        }
        sender.sendMessage("§5{========== §b" + player + "'s fields §5==========}");
        sender.sendMessage(" ");
        List<Field> fields = fieldsManager.getPlayerFields(player);
        for(String schemaKey : fieldsManager.getSchemas().keySet()) {
            FieldSchema schema = fieldsManager.getFieldSchema(schemaKey);
            if(schema == null) {
                continue;
            }
            for(Field field : fields) {
                if(!field.getSchema().equals(schema)) {
                    continue;
                }
                Location loc = field.getFieldLocation();
                sender.sendMessage("§e" + field.getSchema().getName() + "§5: " + LocationUtil.locationBuilder(loc));
            }
        }
        sender.sendMessage(" ");
    }

    private void allowPlayer(Player sender, String nickname) {
        if(!permissionManager.checkPermission(sender, "allowPlayer")) {
            return;
        }
        if(sender.getName().equals(nickname)) {
            sender.sendMessage(messages.getMessage("cantAllowYourself"));
            return;
        }
        if(PlayerUtil.getPlayerFor(nickname, sender) == null) {
            sender.sendMessage(messages.getMessage("playerNotOnline"));
            return;
        }
        List<Field> fields = fieldsManager.getFields(sender.getLocation());
        int i = 0;
        int j = 0;
        for(Field field : fields) {
            if(!field.getFieldOwner().equals(sender.getName())) {
                continue;
            }
            if(field.getFieldContributors().contains(nickname)) {
                j++;
                continue;
            }
            fieldsManager.addContributor(field, nickname);
            i++;
        }
        if(i == 0) {
            if(j != 0) {
                sender.sendMessage(messages.getMessage("playerIsContributor"));
            } else {
                sender.sendMessage(messages.getMessage("noFieldFound"));
            }
        } else {
            sender.sendMessage(MessageFormat.format(messages.getMessage("allowedPlayer"), nickname, i));
        }
    }

    private void removePlayer(Player sender, String nickname) {
        if(!permissionManager.checkPermission(sender, "removePlayer")) {
            return;
        }
        if(sender.getName().equals(nickname)) {
            sender.sendMessage(messages.getMessage("cantRemoveYourself"));
            return;
        }
        List<Field> fields = fieldsManager.getFields(sender.getLocation());
        int i = 0;
        int j = 0;
        for(Field field : fields) {
            if(!field.getFieldOwner().equals(sender.getName())) {
                continue;
            }
            if(!field.getFieldContributors().contains(nickname)) {
                j++;
                continue;
            }
            fieldsManager.removeContributor(field, nickname);
            i++;
        }
        if(i == 0) {
            if(j != 0) {
                sender.sendMessage(messages.getMessage("playerIsNotContributor"));
            } else {
                sender.sendMessage(messages.getMessage("noFieldFound"));
            }
        } else {
            sender.sendMessage(MessageFormat.format(messages.getMessage("removedPlayer"), nickname, i));
        }
    }

    private void giveField(Player sender, String fieldName) {
        if(!permissionManager.checkPermission(sender, "getFieldItem")) {
            return;
        }
        FieldSchema schema = fieldsManager.getFieldSchema(fieldName);
        if(schema == null) {
            sender.sendMessage(messages.getMessage("notFoundField"));
            return;
        }
        sender.getInventory().addItem(schema.getItem().getItemStack());
        sender.sendMessage(messages.getMessage("fieldGiven"));
    }

    private void showHelp(CommandSender sender) {
        sender.sendMessage("§5{========== §bGroundProtection §5==========}");
        sender.sendMessage(" ");
        sender.sendMessage("§5/§egp info §5- §bCheck owner of field that you're standing on");
        sender.sendMessage("§5/§egp visualize §5- §bVisualize field. You must be a contributor or owner");
        sender.sendMessage("§5/§egp locations §5- §bGet locations of your owned fields");
        sender.sendMessage("§5/§egp allow <nick> §5- §bAdd player as a contributor on fields that you're standing on");
        sender.sendMessage("§5/§egp remove <nick> §5- §bRemove contributor from fields that you're standing on");
        if(sender.hasPermission(permissionManager.getPermission("reloadConfiguration"))) {
            sender.sendMessage("§5/§egp reload §5- §bReload configuration");
        }
        sender.sendMessage(" ");
    }

}
