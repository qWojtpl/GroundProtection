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
        String message = messages.getOrDefault(key, "null").replace("&", "§");
        if(message.contains("%prefix%")) {
            message = message.replace("%prefix%", messages.getOrDefault("prefix", "> ")
                    .replace("&", "§") );
        }
        return message;
    }

    public void clearMessages() {
        messages.clear();
    }

}
