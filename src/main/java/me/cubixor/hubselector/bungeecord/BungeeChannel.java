package me.cubixor.hubselector.bungeecord;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PluginMessageEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.scheduler.ScheduledTask;
import net.md_5.bungee.event.EventHandler;
import org.apache.commons.lang3.SerializationUtils;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

public class BungeeChannel implements Listener {

    HubSelectorBungee plugin;

    public BungeeChannel(HubSelectorBungee hsb) {
        plugin = hsb;
    }

    ScheduledTask sd;

    public void getConfiguration(ProxiedPlayer player) {
        Collection<ProxiedPlayer> networkPlayers = ProxyServer.getInstance().getPlayers();
        if (networkPlayers == null || networkPlayers.isEmpty()) {
            return;
        }
        ByteArrayDataOutput out = ByteStreams.newDataOutput();


        StringBuilder configContentBuilder = new StringBuilder();
        try (Stream<String> stream = Files.lines(Paths.get(plugin.configFile.getPath()), StandardCharsets.UTF_8)) {
            stream.forEach(s -> configContentBuilder.append(s).append("\n"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        StringBuilder messagesContentBuilder = new StringBuilder();
        try (Stream<String> stream = Files.lines(Paths.get(plugin.messagesFile.getPath()), StandardCharsets.UTF_8)) {
            stream.forEach(s -> messagesContentBuilder.append(s).append("\n"));
        } catch (IOException e) {
            e.printStackTrace();
        }


        out.writeUTF(configContentBuilder.toString());
        out.writeUTF(messagesContentBuilder.toString());

        sd = plugin.getProxy().getScheduler().schedule(plugin, new Runnable() {
            @Override
            public void run() {
                try {
                    player.getServer().sendData("bungee:config", out.toByteArray());
                    sd.cancel();
                } catch (NullPointerException ignored) {
                }
            }
        }, 0, 1, TimeUnit.MILLISECONDS);
    }


    @EventHandler
    public void onMessageReceive(PluginMessageEvent evt) {
        if (!evt.getTag().equalsIgnoreCase("bungee:config") && !evt.getTag().equalsIgnoreCase("bungee:hub")) {
            return;
        }

        ByteArrayDataInput in = ByteStreams.newDataInput(evt.getData());
        String subChannel = in.readUTF();
        ProxiedPlayer player = (ProxiedPlayer) evt.getReceiver();

        if (subChannel.equalsIgnoreCase("GetInfo")) {
            String server = in.readUTF();

            new HubInventory(plugin).inventoryClickData(player, server);
        } else if (subChannel.equalsIgnoreCase("MenuOpen")) {
            if (!plugin.getConfig().getBoolean("menu-update.update-on-open-only")) {
                new HubMenuUpdate(plugin).updateMenu(player);
            }
            getHubInfo(player);
        } else if (subChannel.equalsIgnoreCase("MenuClose")) {
            try {
                plugin.getProxy().getScheduler().cancel(plugin.taskId.get(player));
                plugin.taskId.remove(player);
            } catch (Exception ignored) {
            }
        }
    }

    @SuppressWarnings("deprecation")
    public void getHubInfo(ProxiedPlayer player) {
        LinkedHashMap<String, HashMap<InetSocketAddress, List<String>>> servers = new LinkedHashMap<>();
        for (String server : plugin.getConfig().getSection("hub-servers").getKeys()) {
            ServerInfo serverInfo = plugin.getProxy().getServerInfo(server);

            List<String> playerNames = new ArrayList<>();
            if (serverInfo.getPlayers() != null) {
                for (ProxiedPlayer p : serverInfo.getPlayers()) {
                    playerNames.add(p.getName());
                }
            }

            HashMap<InetSocketAddress, List<String>> info = new HashMap<>();
            info.put(serverInfo.getAddress(), playerNames);

            servers.put(server, info);
        }


        ByteArrayDataOutput out = ByteStreams.newDataOutput();

        out.write(SerializationUtils.serialize(servers));

        player.getServer().sendData("bungee:hub", out.toByteArray());
    }
}
