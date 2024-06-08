package pl.groundprotection.data;

import lombok.Setter;

import java.util.List;

@Setter
public class ListRegistry {

    private List<String> list;

    public ListRegistry() {

    }

    public ListRegistry(List<String> list) {
        this.list = list;
    }

    public boolean contains(String value) {
        return list.contains(value);
    }

}
