package pl.groundprotection.fields;

public enum FieldFlag {

    PREVENT_DESTROY(""),
    PREVENT_PLACE(""),
    PROTECT_DOORS(""),
    PROTECT_CHESTS(""),
    PROTECT_INTERACTABLE_BLOCKS(""),
    PROTECT_OTHER_BLOCKS(""),
    PROTECT_ANIMALS(""),
    PROTECT_HOSTILES(""),
    PROTECT_OTHER_ENTITIES(""),
    PREVENT_PVP(""),
    PREVENT_POTIONS(""),
    PREVENT_FISHING_ROD(""),
    PREVENT_BOW(""),
    PREVENT_SPREAD(""),
    PREVENT_SPAWN_EGGS(""),
    PREVENT_NAME_TAGS(""),
    PREVENT_EXPLOSIONS("");

    private final String description;

    FieldFlag(String description) {
        this.description = description;
    }

    public String getDescription() {
        return this.description;
    }

}