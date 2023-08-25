package com.downyoutube.devmention.devmention.commands;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.reflect.StructureModifier;
import com.comphenix.protocol.wrappers.*;
import com.downyoutube.devmention.devmention.DevMention;
import org.bukkit.*;
import org.bukkit.command.BlockCommandSender;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.UUID;

public class MainCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (args[0].equals("reload")) {
            DevMention.main.reloadConfig();
        }

        else if (args[0].equals("0")) {
            Location location;

            if (!(sender instanceof Player || sender instanceof BlockCommandSender)) {
                sender.sendMessage("This command can only be used by players.");
                return true;
            } else {
                if ((sender instanceof Player player)) {
                    location = player.getLocation();
                } else {
                    BlockCommandSender commandBlock = (BlockCommandSender) sender;
                    location = commandBlock.getBlock().getLocation().clone().add(0.5, 0, 0.5);
                }
            }

            if (args.length < 6) {
                sender.sendMessage("Usage: /particleoutline <width> <height> <depth> <distance> <speed> <offset>");
                return true;
            }

            double width = Double.parseDouble(args[1]);
            double height = Double.parseDouble(args[2]);
            double depth = Double.parseDouble(args[3]);
            double distance = Double.parseDouble(args[4]);
            double speed = Double.parseDouble(args[5]);
            double offset = Double.parseDouble(args[6]);

            Bukkit.getScheduler().runTaskAsynchronously(DevMention.main, () -> {
                for (double x = 0; x <= width; x += distance) {
                    for (double y = 0; y <= height; y += distance) {
                        for (double z = 0; z <= depth; z += distance) {
                            if (((x == 0 || x == width) && (y == 0 || y == height)) || ((x == 0 || x == width) && (z == 0 || z == depth)) || ((y == 0 || y == height) && (z == 0 || z == depth))) {
                                double xOffset = x - width / 2.0;
                                double zOffset = z - depth / 2.0;

                                location.getWorld().spawnParticle(
                                        Particle.END_ROD, // particle
                                        location.clone().add(xOffset, y + 2.15, zOffset), // location
                                        0, // count
                                        0, offset, 0, // offset (not needed for outline)
                                        speed, // speed
                                        null,
                                        true // force
                                );
                            }
                        }
                    }
                }
            });
        }

        if (args[0].equals("1")) {

            Location location;

            if (!(sender instanceof Player || sender instanceof BlockCommandSender)) {
                sender.sendMessage("This command can only be used by players.");
                return true;
            } else {
                if ((sender instanceof Player player)) {
                    location = player.getLocation();
                } else {
                    BlockCommandSender commandBlock = (BlockCommandSender) sender;
                    location = commandBlock.getBlock().getLocation().clone().add(0.5, 0, 0.5);
                }
            }

            if (args.length < 2) {
                sender.sendMessage("Usage: /particlecircle <radius> <points>");
                return true;
            }

            double radius = Double.parseDouble(args[1]);
            int points = Integer.parseInt(args[2]);

            double angleIncrement = 360.0 / points;
            for (int i = 0; i < points; i++) {
                double angle = Math.toRadians(i * angleIncrement);
                double x = radius * Math.cos(angle);
                double z = radius * Math.sin(angle);

                location.getWorld().spawnParticle(
                        Particle.END_ROD, // particle
                        location.clone().add(0, 0.15, 0), // location
                        0, // count
                        x, 0, z, // offset
                        0.1, // speed
                        null, // Object: data
                        true // force
                );
            }
        } else if (args[0].equals("test")) {
            PacketContainer packet = DevMention.protocolManager.createPacket(PacketType.Play.Server.PLAYER_INFO);
            //Bukkit.broadcastMessage(packet.getModifier().write(100, null).toString());

            List<PlayerInfoData> playerInfoDataList = new ArrayList<>();

            WrappedGameProfile profile = new WrappedGameProfile(UUID.randomUUID(), "some_thing");
            WrappedChatComponent display_name = WrappedChatComponent.fromLegacyText(profile.getName());
            PlayerInfoData player_info = new PlayerInfoData(profile, 0, EnumWrappers.NativeGameMode.ADVENTURE, display_name, null);

            playerInfoDataList.add(player_info);

            packet.getModifier().write(0, EnumSet.of(EnumWrappers.PlayerInfoAction.ADD_PLAYER));
            packet.getPlayerInfoDataLists().write(1, playerInfoDataList);

            DevMention.protocolManager.sendServerPacket((Player)sender, packet);
        }
        return true;
    }
}
