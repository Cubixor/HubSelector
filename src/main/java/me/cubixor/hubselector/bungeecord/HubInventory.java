package me.cubixor.hubselector.bungeecord;

import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;

public class HubInventory {

    HubSelectorBungee plugin;

    public HubInventory(HubSelectorBungee hsb) {
        plugin = hsb;
    }

    @SuppressWarnings("deprecation")
    public void inventoryClickData(ProxiedPlayer player, String server) {

        InetSocketAddress serverIp = plugin.getProxy().getServerInfo(server).getAddress();
        List<String> players = new ArrayList<>();
        for (ProxiedPlayer p : plugin.getProxy().getServerInfo(server).getPlayers()) {
            players.add(p.getName());
        }

        boolean online = new HubChoose(plugin).checkIfOnline(serverIp.getHostName(), serverIp.getPort());
        boolean current = players.contains(player.getName());
        boolean full = players.size() >= plugin.getConfig().getInt("hub-servers." + server + ".slots");
        boolean vip = plugin.getConfig().getBoolean("hub-servers." + server + ".vip");


        if (!online) {
            player.sendMessage(plugin.getMessage("connect.hub-offline"));
            return;
        }

        if (current) {
            player.sendMessage(plugin.getMessage("connect.hub-current"));
            return;
        }

        if (full) {
            player.sendMessage(plugin.getMessage("connect.hub-full"));
            return;
        }

        if (vip) {
            if (!player.hasPermission("hub.vip")) {
                player.sendMessage(plugin.getMessage("connect.hub-vip"));
                return;
            }
        }
        player.sendMessage(plugin.getMessage("connect.success", server));

        player.connect(plugin.getProxy().getServerInfo(server));
    }
}
