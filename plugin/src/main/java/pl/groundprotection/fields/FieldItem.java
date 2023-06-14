package pl.groundprotection.fields;

import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

@Getter
public class FieldItem {

    private final Material material;
    private final String name;
    private final List<String> lore;

    public FieldItem(Material material, String name, List<String> lore) {
        this.material = material;
        this.name = name;
        this.lore = lore;
    }

    public ItemStack getItemStack() {
        ItemStack is = new ItemStack(material);
        ItemMeta im = is.getItemMeta();
        if(im != null) {
            im.setDisplayName(name);
            if(lore.size() > 0) {
                im.setLore(lore);
            }
            is.setItemMeta(im);
        }
        return is;
    }

}
