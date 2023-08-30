package com.downyoutube.devmention.devmention.events;

import com.Zrips.CMI.CMI;
import com.Zrips.CMI.Containers.CMIUser;
import com.downyoutube.devmention.devmention.DevMention;
import com.downyoutube.devmention.devmention.task.CheckOnlinePlayers;
import com.downyoutube.devmention.devmention.utils.ConfigManager;
import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.plugin.messaging.PluginMessageListener;

import java.io.*;
import java.util.HashSet;
import java.util.Set;

public class Chat implements Listener, PluginMessageListener {

    @EventHandler(priority = EventPriority.HIGH)
    public void onChat(AsyncPlayerChatEvent event) throws IOException {

        if (event.isCancelled()) return;

        String full_message = event.getMessage();
        StringBuilder final_message = new StringBuilder();
        Set<String> mentionedPlayer = new HashSet<>();
        for (String message : full_message.split(" ")) {
            Set<String> players_name = CheckOnlinePlayers.getAllOnlinePlayers();

            if (players_name.contains(ChatColor.stripColor(message))) {

                if (Bukkit.getServer().getPluginManager().isPluginEnabled("CMI")) {
                    CMIUser user = CMI.getInstance().getPlayerManager().getUser(ChatColor.stripColor(message));

                    if (user == null || !user.isVanished() || event.getPlayer().hasPermission("cmi.seevanished")) {
                        final_message.append(ConfigManager.getMentionColor()).append(message).append(ChatColor.RESET);
                        mentionedPlayer.add(ChatColor.stripColor(message));
                    }

                } else {
                    final_message.append(ConfigManager.getMentionColor()).append(message).append(ChatColor.RESET);
                    mentionedPlayer.add(ChatColor.stripColor(message));
                }

            } else {
                final_message.append(message);
            }
            final_message.append(" ");
        }

        if (!mentionedPlayer.isEmpty()) {
            event.setMessage(final_message.toString());

            for (String mention : mentionedPlayer) {
                // forward to mentioned player

                ByteArrayDataOutput out = ByteStreams.newDataOutput();

                out.writeUTF("ForwardToPlayer"); // So BungeeCord knows to forward it
                out.writeUTF(ChatColor.stripColor(mention));
                out.writeUTF("MentionPlayer");

                ByteArrayOutputStream msgbytes = new ByteArrayOutputStream();
                DataOutputStream msgout = new DataOutputStream(msgbytes);
                msgout.writeUTF(event.getPlayer().getName()+";"+ChatColor.stripColor(mention));

                out.write(msgbytes.toByteArray());

                event.getPlayer().sendPluginMessage(DevMention.main, "BungeeCord", out.toByteArray());
            }
        }
    }

    @Override
    public void onPluginMessageReceived(String channel, Player player, byte[] message) {

        Bukkit.getScheduler().runTaskAsynchronously(DevMention.main, ()-> {

            if (!channel.equals("BungeeCord")) {
                return;
            }

            ByteArrayDataInput in = ByteStreams.newDataInput(message);

            String s = in.readUTF();
            if (s.equals("MentionPlayer")) {
                String input = in.readUTF();
                String sender = input.split(";")[0];
                Player mentioned_player = Bukkit.getPlayer(input.split(";")[1]);
                if (mentioned_player != null) {

                    if (!ConfigManager.getMentionMessage().equals(""))
                        mentioned_player.sendMessage(ConfigManager.getMentionMessage().replace("%player%", sender));

                    for (String sound : ConfigManager.getMentionSound()) {
                        String sound_id = sound.split(":")[0];
                        float volume = Float.parseFloat(sound.split(":")[1]);
                        float pitch = Float.parseFloat(sound.split(":")[2]);

                        mentioned_player.playSound(player.getLocation(), Sound.valueOf(sound_id), volume, pitch);
                    }
                    mentioned_player.sendTitle(ConfigManager.getMentionTitle().replace("%player%", sender), ConfigManager.getMentionSubTitle().replace("%player%", sender), ConfigManager.getMentionTitleIn(), ConfigManager.getMentionTitleStay(), ConfigManager.getMentionTitleOut());
                }

            }
        });
    }
}
