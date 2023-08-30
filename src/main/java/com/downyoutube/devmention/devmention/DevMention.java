package com.downyoutube.devmention.devmention;

import com.downyoutube.devmention.devmention.commands.MainCommand;
import com.downyoutube.devmention.devmention.commands.MsgCommand;
import com.downyoutube.devmention.devmention.commands.ReplyCommand;
import com.downyoutube.devmention.devmention.events.Chat;
import com.downyoutube.devmention.devmention.task.CheckOnlinePlayers;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;

public final class DevMention extends JavaPlugin {

    public static DevMention main;

    @Override
    public void onEnable() {
        // Plugin startup logic
        main = this;

        //loadResource(this, "config.yml");
        getConfig().options().copyDefaults();
        saveDefaultConfig();

        Objects.requireNonNull(getCommand("msg")).setExecutor(new MsgCommand());
        Objects.requireNonNull(getCommand("mention")).setExecutor(new MainCommand());
        Objects.requireNonNull(getCommand("reply")).setExecutor(new ReplyCommand());
        getServer().getPluginManager().registerEvents(new Chat(), this);

        this.getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");
        this.getServer().getMessenger().registerIncomingPluginChannel(this, "BungeeCord", new CheckOnlinePlayers());
        this.getServer().getMessenger().registerIncomingPluginChannel(this, "BungeeCord", new Chat());
        this.getServer().getMessenger().registerIncomingPluginChannel(this, "BungeeCord", new MsgCommand());

        CheckOnlinePlayers.startCheck();
    }

    @Override
    public void onDisable() {
        this.getServer().getMessenger().unregisterOutgoingPluginChannel(this);
        this.getServer().getMessenger().unregisterIncomingPluginChannel(this);
    }

    /*
    @Override
    public void onPluginMessageReceived(String channel, Player player, byte[] message) {
        if (!channel.equals("bbce:chatsign")) {
            return;
        }

        ByteArrayDataInput in = ByteStreams.newDataInput(message);

        short len = in.readShort();
        byte[] msgbytes = new byte[len];
        in.readFully(msgbytes);

        String chat_message = new String(msgbytes, StandardCharsets.UTF_8);
        player.chat(chat_message);
    }

     */

    /*
    private static File loadResource(Plugin plugin, String resource) {
        File folder = plugin.getDataFolder();
        if (!folder.exists())
            folder.mkdir();
        File resourceFile = new File(folder, resource);
        try {
            //if (!resourceFile.exists()) {
            resourceFile.createNewFile();
            try (InputStream in = plugin.getResource(resource);
                 OutputStream out = new FileOutputStream(resourceFile)) {
                ByteStreams.copy(in, out);
            }
            //}
        } catch (Exception e) {
            e.printStackTrace();
        }
        return resourceFile;
    }

     */
}
