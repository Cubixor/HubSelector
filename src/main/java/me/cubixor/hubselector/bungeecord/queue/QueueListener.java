package me.cubixor.hubselector.bungeecord.queue;

import me.cubixor.hubselector.bungeecord.HubSelectorBungee;
import me.cubixor.hubselector.utils.PermissionUtils;
import me.cubixor.hubselector.utils.QueueRank;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.ChatEvent;
import net.md_5.bungee.api.event.ServerConnectedEvent;
import net.md_5.bungee.api.event.ServerDisconnectEvent;
import net.md_5.bungee.api.event.ServerSwitchEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class QueueListener implements Listener {

    private final HubSelectorBungee plugin;
    private final QueueMainBungee queue;

    public QueueListener() {
        plugin = HubSelectorBungee.getInstance();
        queue = QueueMainBungee.getInstance();
    }

    @EventHandler
    public void onJoin(ServerConnectedEvent evt) {
        if (plugin.getQueueServer().equals(evt.getServer().getInfo())) {
            evt.getPlayer().setTabHeader(plugin.getMessage("queue.tablist-header"), plugin.getMessage("queue.tablist-footer"));
        }
    }

    @EventHandler
    public void onSwitch(ServerSwitchEvent evt) {
        //int version = evt.getPlayer().getPendingConnection().getVersion();
        System.out.println("queue switch " + evt.getPlayer().getServer().getInfo());
        System.out.println("queue " + plugin.getQueueServer());

        if (evt.getPlayer().getServer().getInfo().equals(plugin.getQueueServer())) {
            if (queue.getBossBar() != null) {
                queue.getBossBar().addPlayer(evt.getPlayer());
            }
            new QueueUtils().putInQueue(evt.getPlayer());
            queue.getTimeJoined().put(evt.getPlayer(), new QueuePlayerData(LocalDateTime.now(), QueueUtils.getQueuePlayers().indexOf(evt.getPlayer()) + 1));
            System.out.println("put1 " + queue.getQueuePlayers());
            System.out.println("put2 " + queue.getTimeJoined());


            for (ProxiedPlayer player : queue.getTimeJoined().keySet()) {
                if (player.equals(evt.getPlayer())) {
                    continue;
                }

                int playerPos = QueueUtils.getQueuePlayers().indexOf(player) + 1;
                if (queue.getTimeJoined().get(player).getJoinPosition() != playerPos) {
                    queue.getTimeJoined().replace(player, new QueuePlayerData(LocalDateTime.now(), playerPos));
                }
            }

            return;
        }

        if (evt.getFrom() != null && evt.getFrom().equals(plugin.getQueueServer())) {
            if (queue.getBossBar() != null) {
                queue.getBossBar().removePlayer(evt.getPlayer());
            }
            queue.getTimeJoined().remove(evt.getPlayer());
        }

        removeFromQueue(evt.getFrom(), evt.getPlayer());
    }


    @EventHandler
    public void onLeave(ServerDisconnectEvent evt) {
        if (evt.getTarget().equals(plugin.getQueueServer())) {
            removeFromQueue(evt.getTarget(), evt.getPlayer());
            queue.getTimeJoined().remove(evt.getPlayer());

            for (ProxiedPlayer player : queue.getTimeJoined().keySet()) {
                if (player.equals(evt.getPlayer())) {
                    continue;
                }

                int playerPos = QueueUtils.getQueuePlayers().indexOf(player) + 1;
                if (queue.getTimeJoined().get(player).getJoinPosition() != playerPos) {
                    queue.getTimeJoined().replace(player, new QueuePlayerData(LocalDateTime.now(), playerPos));
                }
            }
        }
    }

    @EventHandler
    public void onCommand(ChatEvent evt) {
        if (!(evt.getSender() instanceof ProxiedPlayer)) {
            return;
        }

        ProxiedPlayer player = (ProxiedPlayer) evt.getSender();

        List<ProxiedPlayer> players = new ArrayList<>();
        for (List<ProxiedPlayer> playerList : queue.getQueuePlayers().values()) {
            players.addAll(playerList);
        }

        if (!players.contains(player)) {
            return;
        }

        if (evt.isCommand()) {
            if (PermissionUtils.hasPermission(player, "hub.bypass.commandblocker")) {
                return;
            }
            for (String s : plugin.getConfig().getStringList("queue.allowed-commands")) {
                if (evt.getMessage().replace("/", "").toLowerCase().startsWith(s.toLowerCase())) {
                    return;
                }
            }
        }
        evt.setCancelled(true);
    }

    private void removeFromQueue(ServerInfo serverInfo, ProxiedPlayer player) {
        ServerInfo queueServer = plugin.getQueueServer();
        if (serverInfo != null && serverInfo.equals(queueServer)) {
            player.resetTabHeader();
            player.sendTitle(plugin.getProxy().createTitle().title(new TextComponent()).subTitle(new TextComponent()));
            player.sendMessage(ChatMessageType.ACTION_BAR, new TextComponent());
            QueueRank queueRank = QueueUtils.getQueueRank(player);
            if (queueRank != null) {
                queue.getQueuePlayers().get(queueRank).remove(player);
            }
        }
    }
}
