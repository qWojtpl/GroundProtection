package pl.groundprotection.fields;

import lombok.Getter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Getter
public class FieldsManager {

    private final HashMap<String, FieldSchema> schemas = new HashMap<>();
    private final List<Field> fields = new ArrayList<>();

    public void addFieldSchema(FieldSchema schema) {
        schemas.put(schema.getName(), schema);
    }

    public void removeFieldSchema(FieldSchema schema) {
        schemas.remove(schema.getName());
    }

    public void removeFieldSchema(String schema) {
        schemas.remove(schema);
    }

}
