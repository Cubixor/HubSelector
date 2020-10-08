package me.cubixor.hubselector.bungeecord;

import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.ServerConnectEvent;
import net.md_5.bungee.api.event.ServerSwitchEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

public class ServerJoin implements Listener {

    HubSelectorBungee plugin;

    public ServerJoin(HubSelectorBungee hsb) {
        plugin = hsb;
    }


    @EventHandler
    public void onJoin(ServerConnectEvent evt) {
        if (evt.getReason().equals(ServerConnectEvent.Reason.JOIN_PROXY)) {
            HubChoose hubChoose = new HubChoose(plugin);
            String server = hubChoose.connectToHub(evt.getPlayer(), evt.getPlayer().hasPermission("hub.vip"), false);

            if (server != null) {
                evt.setTarget(ProxyServer.getInstance().getServerInfo(server));
            }
        }
    }

    @EventHandler
    public void onSwitch(ServerSwitchEvent evt) {
        sendConfig(evt.getPlayer(), evt.getPlayer().getServer().getInfo().getName());
    }

    private void sendConfig(ProxiedPlayer player, String server) {
        if (plugin.serversToReload.contains(server)) {
            new BungeeChannel(plugin).getConfiguration(player);
            plugin.serversToReload.remove(server);
        }
    }
}
