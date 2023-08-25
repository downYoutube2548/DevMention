package com.downyoutube.devmention.devmention.task;

import com.downyoutube.devmention.devmention.DevMention;
import com.downyoutube.devmention.devmention.utils.ConfigManager;
import com.google.common.collect.Iterables;
import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.messaging.PluginMessageListener;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CheckOnlinePlayers implements PluginMessageListener {

    public static HashMap<String,Set<String>> onlinePlayers = new HashMap<>();

    public static Set<String> getAllOnlinePlayers() {
        Set<String> onlinePlayer = new HashSet<>();
        for (Set<String> online : onlinePlayers.values()) {
            onlinePlayer.addAll(online);
        }
        return onlinePlayer;
    }

    public static void startCheck() {
        Bukkit.getScheduler().runTaskTimerAsynchronously(DevMention.main, ()->{

            for (String server : ConfigManager.getServers()) {
                ByteArrayDataOutput out = ByteStreams.newDataOutput();

                out.writeUTF("PlayerList"); // So BungeeCord knows to forward it
                out.writeUTF(server);

                Player player = Iterables.getFirst(Bukkit.getOnlinePlayers(), null);

                if (player != null) {
                    player.sendPluginMessage(DevMention.main, "BungeeCord", out.toByteArray());
                }
            }

            Set<String> onlinePlayer = new HashSet<>();
            for (Set<String> online : onlinePlayers.values()) {
                onlinePlayer.addAll(online);
            }
            //Bukkit.broadcastMessage(onlinePlayer.toString());
        }, 1, 20);
    }

    @Override
    public void onPluginMessageReceived(String channel, Player player, byte[] message) {
        Bukkit.getScheduler().runTaskAsynchronously(DevMention.main, ()-> {

            if (!channel.equals("BungeeCord")) {
                return;
            }

            ByteArrayDataInput in = ByteStreams.newDataInput(message);

            if (in.readUTF().equals("PlayerList")) {
                String server = in.readUTF();
                String[] playerList = in.readUTF().split(", ");

                if (List.of(playerList).isEmpty()) return;
                onlinePlayers.put(server, new HashSet<>(List.of(playerList)));
            }
        });
    }
}
