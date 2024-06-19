package pl.groundprotection.fields;

import lombok.Getter;

import java.util.List;

@Getter
public class FieldSchema {

    private final String name;
    private final int size;
    private final FieldItem item;
    private final String permission;
    private final List<FieldFlag> flags;
    private final List<String> disabledWorlds;
    private final List<String> limits;
    private final List<String> blockedCommands;
    private final int daysToRemove;

    public FieldSchema(String name, int size, FieldItem item, String permission, List<FieldFlag> flags,
                       List<String> disabledWorlds, List<String> limits, List<String> blockedCommands, int daysToRemove) {
        this.name = name;
        this.size = size;
        this.item = item;
        this.permission = permission;
        this.flags = flags;
        this.disabledWorlds = disabledWorlds;
        this.limits = limits;
        this.blockedCommands = blockedCommands;
        this.daysToRemove = daysToRemove;
    }

}
