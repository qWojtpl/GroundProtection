package pl.groundprotection.data;

import lombok.Getter;

import java.util.HashMap;

@Getter
public class Messages {

    private final HashMap<String, String> messages = new HashMap<>();

    public void addMessage(String key, String message) {
        messages.put(key, message);
    }

    public String getMessage(String key) {
        return messages.getOrDefault(key, "null").replace("&", "§");
    }

    public String getPrefixedMessage(String key) {
        return messages.getOrDefault("prefix", "> ") +
                messages.getOrDefault(key, "null").replace("&", "§");
    }

    public void clearMessages() {
        messages.clear();
    }

}
