package me.cubixor.hubselector.bungeecord;

import me.cubixor.hubselector.bungeecord.queue.QueueUtils;
import me.cubixor.hubselector.utils.Hub;
import me.cubixor.hubselector.utils.PermissionUtils;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.event.ServerConnectEvent;
import net.md_5.bungee.api.event.ServerKickEvent;
import net.md_5.bungee.api.event.ServerSwitchEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

public class ServerJoin implements Listener {

    HubSelectorBungee plugin;

    public ServerJoin() {
        plugin = HubSelectorBungee.getInstance();
    }


    @EventHandler
    public void onJoin(ServerConnectEvent evt) {
        if (!evt.getReason().equals(ServerConnectEvent.Reason.JOIN_PROXY)) {
            return;
        }

        String server = HubChoose.chooseServer(evt.getPlayer(), true, false, false);

        if (server != null) {
            ServerInfo serverInfo = plugin.getProxy().getServerInfo(server);
            evt.setTarget(serverInfo);
            if (plugin.getServerSlots().containsKey(serverInfo.getName())) {
                plugin.getServerSlots().get(serverInfo.getName()).add(evt.getPlayer().getName());
            }
        } else {
            evt.setCancelled(true);
        }
    }


    @EventHandler
    public void onSwitch(ServerSwitchEvent evt) {
        if (HubUtils.getHub(evt.getPlayer().getServer().getInfo().getName()) != null) {
            sendJoinLeaveMessage(evt.getPlayer(), evt.getPlayer().getServer().getInfo(), "join");
        }

        for (Hub hub : plugin.getHubs()) {
            if (evt.getFrom() != null && hub.getServer().equals(evt.getFrom().getName())) {
                plugin.getServerSlots().get(evt.getFrom().getName()).remove(evt.getPlayer().getName());
                new QueueUtils().putInHub();
            } else if (hub.getServer().equals(evt.getPlayer().getServer().getInfo().getName())) {
                if (!plugin.getServerSlots().get(evt.getPlayer().getServer().getInfo().getName()).contains(evt.getPlayer().getName())) {
                    plugin.getServerSlots().get(evt.getPlayer().getServer().getInfo().getName()).add(evt.getPlayer().getName());
                }
            }
        }
    }

    @EventHandler
    public void onLeave(PlayerDisconnectEvent evt) {
        for (String server : plugin.getServerSlots().keySet()) {
            plugin.getServerSlots().get(server).remove(evt.getPlayer().getName());
        }


        if (evt.getPlayer().getServer() == null) {
            return;
        }

        ServerInfo serverInfo = evt.getPlayer().getServer().getInfo();
        Hub hub = HubUtils.getHub(serverInfo.getName());
        if (hub != null) {
            sendJoinLeaveMessage(evt.getPlayer(), evt.getPlayer().getServer().getInfo(), "leave");
            plugin.getServerSlots().get(serverInfo.getName()).remove(evt.getPlayer().getName());

            new QueueUtils().putInHub();
        }
    }


    public void sendJoinLeaveMessage(ProxiedPlayer player, ServerInfo serverInfo, String joinLeave) {
        BaseComponent[] message = null;
        if (PermissionUtils.hasPermission(player, "hub.vip" + joinLeave + "msg")) {
            message = plugin.getMessage("connect." + joinLeave + "-vip", "%player%", player.getDisplayName());
        } else if (PermissionUtils.hasPermission(player, "hub." + joinLeave + "msg")) {
            message = plugin.getMessage("connect." + joinLeave, "%player%", player.getDisplayName());
        }
        if (message != null) {
            for (ProxiedPlayer msgReceiver : serverInfo.getPlayers()) {
                msgReceiver.sendMessage(message);
            }
        }
    }


    @EventHandler
    public void onKick(ServerKickEvent evt) {
        if (!HubUtils.isInHub(evt.getPlayer(), false)) {
            return;
        }

        if (HubUtils.checkIfOnline(evt.getKickedFrom())) {
            return;
        }

        if (HubUtils.isInHub(evt.getPlayer(), false)) {
            String hubString = HubChoose.chooseServer(evt.getPlayer(), true, true, false);

            if (hubString != null) {
                Hub hub = HubUtils.getHub(hubString);
                if (hub != null) {
                    ServerInfo serverInfo = HubUtils.getServerInfo(hub);
                    evt.setCancelServer(serverInfo);
                    plugin.getServerSlots().get(serverInfo.getName()).add(evt.getPlayer().getName());
                } else {
                    new QueueUtils().putInQueue(evt.getPlayer());
                    evt.setCancelServer(plugin.getProxy().getServerInfo(plugin.getHubServers().getString("queue-server")));
                }
                evt.setCancelled(true);

            } else {
                evt.setKickReasonComponent(plugin.getMessage("connect.kick-stopped"));
            }
        } else if (QueueUtils.queueAvailable()) {
            evt.setKickReasonComponent(plugin.getMessage("connect.kick-stopped"));
        }
    }

}
