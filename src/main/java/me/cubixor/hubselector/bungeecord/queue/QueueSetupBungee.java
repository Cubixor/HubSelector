package me.cubixor.hubselector.bungeecord.queue;

import me.cubixor.hubselector.bungeecord.HubSelectorBungee;
import me.cubixor.hubselector.utils.QueueRank;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.Collections;
import java.util.LinkedList;

public class QueueSetupBungee {

    private final HubSelectorBungee plugin;
    private final QueueMainBungee queue;

    public QueueSetupBungee() {
        plugin = HubSelectorBungee.getInstance();
        queue = QueueMainBungee.getInstance();
    }

    public void setup() {
        if (plugin.getQueueServer() != null) {
            queue.setQueueListener(new QueueListener());
            plugin.getProxy().getPluginManager().registerListener(plugin, queue.getQueueListener());

            queue.setQueuePaused(plugin.getHubServers().getBoolean("queue-paused"));
            for (QueueRank queueRank : getQueueRanksFromConfig()) {
                queue.getQueuePlayers().put(queueRank, new LinkedList<>());
            }
            new QueueActionbar();
            new QueueTitle();
            if (plugin.getConfig().getBoolean("queue.bossbar-enabled")) {
                new QueueBossBar();
            }
        }
    }

    private LinkedList<QueueRank> getQueueRanksFromConfig() {
        LinkedList<QueueRank> queueRanks = new LinkedList<>();
        for (String name : plugin.getConfig().getSection("queue.ranks").getKeys()) {
            String permission = plugin.getConfig().getString("queue.ranks." + name + ".permission");
            int priority = plugin.getConfig().getInt("queue.ranks." + name + ".priority");
            String displayName = plugin.getConfig().getString("queue.ranks." + name + ".name");
            QueueRank queueRank = new QueueRank(name, permission, priority, displayName);
            queueRanks.add(queueRank);
        }
        queueRanks.add(new QueueRank("default", "hub.queue.default", Integer.MAX_VALUE, plugin.getMessages().getString("queue.default-rank")));
        Collections.sort(queueRanks);
        return queueRanks;
    }

    public void queueReload() {
        for (ProxiedPlayer player : QueueUtils.getQueuePlayers()) {
            player.disconnect(plugin.getMessage("connect.kick-stopped"));
        }

        queue.getActionBarRunnable().cancel();
        queue.getTitleRunnable().cancel();
        if (queue.getBossBar() != null) {
            queue.setBossBar(null);
            queue.getBossBarRunnable().cancel();
        }

        plugin.getProxy().getPluginManager().unregisterListener(queue.getQueueListener());

        plugin.setQueueServer(null);
    }
}