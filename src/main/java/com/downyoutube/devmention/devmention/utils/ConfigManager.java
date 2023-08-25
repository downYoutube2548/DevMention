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

    public static String getTitle() {
        return Utils.colorize(DevMention.main.getConfig().getString("mention-title.title"));
    }
    public static String getSubTitle() {
        return Utils.colorize(DevMention.main.getConfig().getString("mention-title.subtitle"));
    }
    public static int getTitleIn() {
        return DevMention.main.getConfig().getInt("mention-title.in");
    }
    public static int getTitleStay() {
        return DevMention.main.getConfig().getInt("mention-title.stay");
    }
    public static int getTitleOut() {
        return DevMention.main.getConfig().getInt("mention-title.out");
    }
}
