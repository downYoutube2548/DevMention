package com.downyoutube.devmention.devmention.utils;

import com.downyoutube.devmention.devmention.DevMention;

import java.util.List;

public class ConfigManager {

    public static List<String> getServers() {
        return DevMention.main.getConfig().getStringList("servers");
    }

    public static String getMentionColor() {
        return Utils.colorize(DevMention.main.getConfig().getString("mention-color"));
    }
    public static List<String> getMentionSound() {
        return DevMention.main.getConfig().getStringList("mention-sound");
    }
    public static String getMentionMessage() {
        return Utils.colorize(DevMention.main.getConfig().getString("mention-message"));
    }

    public static String getMentionTitle() {
        return Utils.colorize(DevMention.main.getConfig().getString("mention-title.title"));
    }
    public static String getMentionSubTitle() {
        return Utils.colorize(DevMention.main.getConfig().getString("mention-title.subtitle"));
    }
    public static int getMentionTitleIn() {
        return DevMention.main.getConfig().getInt("mention-title.in");
    }
    public static int getMentionTitleStay() {
        return DevMention.main.getConfig().getInt("mention-title.stay");
    }
    public static int getMentionTitleOut() {
        return DevMention.main.getConfig().getInt("mention-title.out");
    }
    public static String getPrivateMessageFrom() { return Utils.colorize(DevMention.main.getConfig().getString("private-message-from")); }
    public static String getPrivateMessageTo() { return Utils.colorize(DevMention.main.getConfig().getString("private-message-to")); }
    public static List<String> getPrivateMessageSound() {
        return DevMention.main.getConfig().getStringList("private-message-sound");
    }
}
