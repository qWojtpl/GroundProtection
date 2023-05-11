package pl.groundprotection.fields;

import lombok.Getter;
import org.bukkit.Material;

import java.util.List;

@Getter
public class FieldSchema {

    private final String name;
    private final List<Integer> size;
    private final Material item;
    private final String permission;
    private final List<FieldFlags> flags;
    private final List<String> disabledWorlds;
    private final List<String> limits;

    public FieldSchema(String name, List<Integer> size, Material item, String permission, List<FieldFlags> flags,
                       List<String> disabledWorlds, List<String> limits) {
        this.name = name;
        this.size = size;
        this.item = item;
        this.permission = permission;
        this.flags = flags;
        this.disabledWorlds = disabledWorlds;
        this.limits = limits;
    }

}
