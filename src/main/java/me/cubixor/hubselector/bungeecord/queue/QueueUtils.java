package me.cubixor.hubselector.bungeecord.queue;

import me.cubixor.hubselector.bungeecord.HubChoose;
import me.cubixor.hubselector.bungeecord.HubSelectorBungee;
import me.cubixor.hubselector.utils.QueueRank;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class QueueUtils {

    private final QueueMainBungee queue;
    private final HubSelectorBungee plugin;

    public QueueUtils() {
        queue = QueueMainBungee.getInstance();
        plugin = HubSelectorBungee.getInstance();
    }

    public static boolean queueAvailable() {
        return HubSelectorBungee.getInstance().getQueueServer() != null
                && QueueMainBungee.getInstance().isQueueOnline();
    }

    public static boolean isInQueue(ProxiedPlayer player) {
        QueueMainBungee queue = QueueMainBungee.getInstance();

        for (QueueRank queueRank : queue.getQueuePlayers().keySet()) {
            if (queue.getQueuePlayers().get(queueRank).contains(player)) {
                return true;
            }
        }
        return false;
    }

    public static QueueRank getQueueRank(ProxiedPlayer player) {
        QueueMainBungee queue = QueueMainBungee.getInstance();

        for (QueueRank queueRank : queue.getQueuePlayers().keySet()) {
            if (queue.getQueuePlayers().get(queueRank).contains(player)) {
                return queueRank;
            }
        }
        return getDefaultQueueRank();
    }

    public static QueueRank getDefaultQueueRank() {
        QueueMainBungee queue = QueueMainBungee.getInstance();

        return new ArrayList<>(queue.getQueuePlayers().keySet()).get(queue.getQueuePlayers().size() - 1);
    }

    public static List<ProxiedPlayer> getQueuePlayers() {
        QueueMainBungee queue = QueueMainBungee.getInstance();

        List<ProxiedPlayer> players = new LinkedList<>();

        for (QueueRank queueRank : new LinkedList<>(queue.getQueuePlayers().keySet())) {
            for (ProxiedPlayer player : new LinkedList<>(queue.getQueuePlayers().get(queueRank))) {
                if (player == null) {
                    queue.getQueuePlayers().get(queueRank).remove(null);
                } else {
                    players.add(player);
                }
            }
        }
        return players;
    }

    public void putInQueue(ProxiedPlayer player) {
        if (QueueUtils.isInQueue(player)) {
            return;
        }
        QueueRank playerRank = getDefaultQueueRank();
        for (QueueRank queueRank : queue.getQueuePlayers().keySet()) {
            if (player.hasPermission(queueRank.getPermission())) {
                if (queueRank.getPriority() < playerRank.getPriority()) {
                    playerRank = queueRank;
                }
            }
        }

        queue.getQueuePlayers().get(playerRank).add(player);

        player.sendMessage(plugin.getMessage("connect.queue"));
    }

    public void putInHub() {
        if (queue.isQueuePaused()) {
            return;
        }

        synchronized (queue.getQueuePlayers()) {

            for (QueueRank queueRank : new LinkedList<>(queue.getQueuePlayers().keySet())) {

                if (queue.getQueuePlayers().get(queueRank).isEmpty()) {
                    continue;
                }

                for (ProxiedPlayer player : new LinkedList<>(queue.getQueuePlayers().get(queueRank))) {
                    if (player == null || !player.isConnected()) {
                        queue.getQueuePlayers().get(queueRank).remove(player);
                        continue;
                    }

                    if (new HubChoose().connectToHub(player, false, false)) {
                        queue.getQueuePlayers().get(queueRank).remove(player);
                        for (ProxiedPlayer p : new ArrayList<>(queue.getTimeJoined().keySet())) {
                            queue.getTimeJoined().replace(p, new QueuePlayerData(System.currentTimeMillis(), getQueuePlayers().indexOf(p) + 1));
                        }
                    }
                }
            }
        }
    }

}


