package com.downyoutube.devmention.devmention.commands;

import com.Zrips.CMI.CMI;
import com.Zrips.CMI.Containers.CMIUser;
import com.downyoutube.devmention.devmention.DevMention;
import com.downyoutube.devmention.devmention.task.CheckOnlinePlayers;
import com.downyoutube.devmention.devmention.utils.ConfigManager;
import com.downyoutube.devmention.devmention.utils.Utils;
import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.messaging.PluginMessageListener;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MsgCommand implements CommandExecutor, TabExecutor, PluginMessageListener {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player player) {
            if (args.length >= 2) {
                if (CheckOnlinePlayers.getAllOnlinePlayers().contains(args[0])) {

                    if (Bukkit.getServer().getPluginManager().isPluginEnabled("CMI")) {
                        CMIUser user = CMI.getInstance().getPlayerManager().getUser(args[0]);
                        if (user != null && user.isVanished() && !sender.hasPermission("cmi.seevanished")) {
                            player.sendMessage(ChatColor.RED+"player is not online!");
                            return false;
                        }
                    }

                    StringBuilder sb = new StringBuilder();
                    for (String a : args) {
                        sb.append(a).append(" ");
                    }
                    String message = sb.substring(args[0].length(), sb.length()-1);

                    player.sendMessage(ConfigManager.getPrivateMessageFrom()
                            .replace("%player%", args[0])
                            .replace("%message%", Utils.colorize(message))
                    );

                    ReplyCommand.userReply.put(player.getName(), args[0]);

                    ByteArrayDataOutput out = ByteStreams.newDataOutput();

                    out.writeUTF("ForwardToPlayer"); // So BungeeCord knows to forward it
                    out.writeUTF(args[0]);
                    out.writeUTF("MessagePlayer");

                    ByteArrayOutputStream msgbytes = new ByteArrayOutputStream();
                    DataOutputStream msgout = new DataOutputStream(msgbytes);
                    try {
                        msgout.writeUTF(player.getName()+";"+args[0]+";"+Utils.colorize(message));
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }

                    out.write(msgbytes.toByteArray());

                    player.sendPluginMessage(DevMention.main, "BungeeCord", out.toByteArray());


                } else {
                    player.sendMessage(ChatColor.RED+"player is not online!");
                }
            } else {
                player.sendMessage(ChatColor.RED + "Usage: /msg <player> <message>");
            }
        } else {
            sender.sendMessage(ChatColor.RED+"You must be player to execute this command");
        }
        return false;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String lable, String[] args) {
        List<String> output;
        if (Bukkit.getServer().getPluginManager().isPluginEnabled("CMI")) {
            List<String> online = new ArrayList<>();
            for (String onlinePlayer : CheckOnlinePlayers.getAllOnlinePlayers()) {
                CMIUser user = CMI.getInstance().getPlayerManager().getUser(onlinePlayer);
                if (user != null && user.isVanished() && !sender.hasPermission("cmi.seevanished")) {
                    continue;
                }
                online.add(onlinePlayer);
            }
            output = Utils.tabComplete(args[args.length-1], online);
        } else {
            output = Utils.tabComplete(args[args.length-1], new ArrayList<>(CheckOnlinePlayers.getAllOnlinePlayers()));
        }
        return output;
    }

    @Override
    public void onPluginMessageReceived(String channel, Player p, byte[] m) {
        Bukkit.getScheduler().runTaskAsynchronously(DevMention.main, ()-> {

            if (!channel.equals("BungeeCord")) {
                return;
            }

            ByteArrayDataInput in = ByteStreams.newDataInput(m);

            String s = in.readUTF();
            if (s.equals("MessagePlayer")) {
                String input = in.readUTF();
                String sender = input.split(";")[0];
                Player player = Bukkit.getPlayer(input.split(";")[1]);
                if (player != null) {
                    String message = input.substring(sender.length()+player.getName().length()+2);

                    ReplyCommand.userReply.put(player.getName(), sender);

                    player.sendMessage(ConfigManager.getPrivateMessageTo()
                            .replace("%player%", sender)
                            .replace("%message%", message)
                    );

                    for (String sound : ConfigManager.getPrivateMessageSound()) {
                        String sound_id = sound.split(":")[0];
                        float volume = Float.parseFloat(sound.split(":")[1]);
                        float pitch = Float.parseFloat(sound.split(":")[2]);

                        player.playSound(player.getLocation(), Sound.valueOf(sound_id), volume, pitch);
                    }
                }
            }
        });
    }
}
