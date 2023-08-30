package com.downyoutube.devmention.devmention.task;

import com.Zrips.CMI.CMI;
import com.Zrips.CMI.Containers.CMIUser;
import com.downyoutube.devmention.devmention.DevMention;
import com.downyoutube.devmention.devmention.utils.ConfigManager;
import com.google.common.collect.Iterables;
import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import com.mojang.authlib.GameProfile;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoRemovePacket;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.craftbukkit.v1_20_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.messaging.PluginMessageListener;

import java.util.*;

public class CheckOnlinePlayers implements PluginMessageListener {

    public static HashMap<String,Set<String>> onlinePlayers = new HashMap<>();

    public static Set<String> getAllOnlinePlayers() {
        Set<String> onlinePlayer = new HashSet<>();
        for (Set<String> onlines : onlinePlayers.values()) {
            for (String online : onlines) {
                onlinePlayer.add(online.split(";")[0]);
            }
        }
        return onlinePlayer;
    }

    public static Set<String> getAllOnlinePlayersWithUUID() {
        Set<String> onlinePlayer = new HashSet<>();
        try {
            for (Set<String> online : onlinePlayers.values()) {
                onlinePlayer.addAll(online);
            }
        } catch (ConcurrentModificationException ignored) {}
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
                Set<String> players = new HashSet<>();
                for (String player_name : in.readUTF().split(", ")) {
                    if (player_name.equals("") || player_name.equals(" ")) continue;
                    Player bukkitPlayer = Bukkit.getPlayer(player_name);
                    players.add(bukkitPlayer == null ? player_name+";"+UUID.randomUUID() : player_name+";"+bukkitPlayer.getUniqueId() );
                }

                Set<String> before = new HashSet<>(getAllOnlinePlayersWithUUID());
                //System.out.println(server+": "+players);

                if (!ConfigManager.getServers().contains(server)) return;
                onlinePlayers.put(server, players);
                Set<String> after = new HashSet<>(getAllOnlinePlayersWithUUID());

                if (!before.equals(after)) {

                    for (Player p : Bukkit.getOnlinePlayers()) {

                        Set<String> logged_in_after = new HashSet<>(after);
                        Set<String> logged_out_before = new HashSet<>(before);
                        logged_in_after.removeAll(before);
                        logged_out_before.removeAll(after);
                        List<ClientboundPlayerInfoUpdatePacket.Action> actions = new ArrayList<>();

                        CraftPlayer craftPlayer = (CraftPlayer) p;
                        ServerPlayer serverPlayer = craftPlayer.getHandle();

                        List<ServerPlayer> npcs = new ArrayList<>();
                        for (String logged_in : logged_in_after) {
                            String logged_in_name = logged_in.split(";")[0];
                            UUID logged_in_uuid = UUID.fromString(logged_in.split(";")[1]);

                            if (Bukkit.getServer().getPluginManager().isPluginEnabled("CMI")) {
                                CMIUser user = CMI.getInstance().getPlayerManager().getUser(logged_in_name);
                                if (user != null && user.isVanished() && !p.hasPermission("cmi.seevanished")) {
                                    continue;
                                }
                            }

                            GameProfile profile = new GameProfile(logged_in_uuid, logged_in_name);

                            if (serverPlayer.getServer() == null) continue;
                            try {
                                ServerPlayer npc = new ServerPlayer(serverPlayer.getServer(), serverPlayer.serverLevel(), profile);
                                actions.add(ClientboundPlayerInfoUpdatePacket.Action.ADD_PLAYER);
                                npcs.add(npc);
                            } catch (Exception ignored) {}
                        }

                        ServerGamePacketListenerImpl connection = serverPlayer.connection;

                        if (!actions.isEmpty() && !npcs.isEmpty()) {
                            connection.send(new ClientboundPlayerInfoUpdatePacket(EnumSet.copyOf(actions), npcs));
                        }

                        List<UUID> logged_out_uuid = new ArrayList<>();
                        for (String logout : logged_out_before) {
                            logged_out_uuid.add(UUID.fromString(logout.split(";")[1]));
                        }

                        connection.send(new ClientboundPlayerInfoRemovePacket(logged_out_uuid));
                    }
                }
            }
        });
    }
}
