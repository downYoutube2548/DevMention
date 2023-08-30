package com.downyoutube.devmention.devmention.commands;

import com.Zrips.CMI.CMI;
import com.Zrips.CMI.Containers.CMIUser;
import com.downyoutube.devmention.devmention.DevMention;
import com.downyoutube.devmention.devmention.task.CheckOnlinePlayers;
import com.downyoutube.devmention.devmention.utils.ConfigManager;
import com.downyoutube.devmention.devmention.utils.Utils;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ReplyCommand implements CommandExecutor, TabExecutor {

    public static HashMap<String, String> userReply = new HashMap<>();

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (sender instanceof Player player) {
            if (args.length >= 1) {
                if (userReply.containsKey(player.getName()) && CheckOnlinePlayers.getAllOnlinePlayers().contains(player.getName())) {

                    StringBuilder sb = new StringBuilder();
                    for (String s : args) {
                        sb.append(s).append(" ");
                    }
                    String message = sb.substring(0, sb.length() - 1);

                    player.sendMessage(ConfigManager.getPrivateMessageFrom()
                            .replace("%player%", userReply.get(player.getName()))
                            .replace("%message%", Utils.colorize(message))
                    );

                    ByteArrayDataOutput out = ByteStreams.newDataOutput();

                    out.writeUTF("ForwardToPlayer"); // So BungeeCord knows to forward it
                    out.writeUTF(userReply.get(player.getName()));
                    out.writeUTF("MessagePlayer");

                    ByteArrayOutputStream msgbytes = new ByteArrayOutputStream();
                    DataOutputStream msgout = new DataOutputStream(msgbytes);
                    try {
                        msgout.writeUTF(player.getName()+";"+userReply.get(player.getName())+";"+ Utils.colorize(message));
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }

                    out.write(msgbytes.toByteArray());

                    player.sendPluginMessage(DevMention.main, "BungeeCord", out.toByteArray());

                } else {
                    player.sendMessage(ChatColor.RED + "You don't have any message to reply");
                }

            } else {
                player.sendMessage(ChatColor.RED + "Usage: /reply <message>");
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
}
