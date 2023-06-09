package pl.groundprotection.fields;

import lombok.Getter;
import org.bukkit.Location;

import java.util.List;

@Getter
public class Field {

    private final int ID;
    private final FieldSchema schema;
    private final Location fieldLocation;
    private final String fieldOwner;
    private final List<String> fieldContributors;

    public Field(int ID, FieldSchema schema, Location fieldLocation, String owner, List<String> contributors) {
        this.ID = ID;
        this.schema = schema;
        this.fieldLocation = fieldLocation;
        this.fieldOwner = owner;
        this.fieldContributors = contributors;
    }

}
