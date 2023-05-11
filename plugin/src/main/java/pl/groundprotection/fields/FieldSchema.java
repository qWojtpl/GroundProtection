package pl.groundprotection.fields;

import lombok.Getter;
import org.bukkit.Material;

import java.util.List;

@Getter
public class FieldSchema {

    private final String name;
    private final int size;
    private final Material item;
    private final String permission;
    private final List<FieldFlag> flags;
    private final List<String> disabledWorlds;
    private final List<String> limits;
    private final int daysToRemove;

    public FieldSchema(String name, int size, Material item, String permission, List<FieldFlag> flags,
                       List<String> disabledWorlds, List<String> limits, int daysToRemove) {
        this.name = name;
        this.size = size;
        this.item = item;
        this.permission = permission;
        this.flags = flags;
        this.disabledWorlds = disabledWorlds;
        this.limits = limits;
        this.daysToRemove = daysToRemove;
    }

}
