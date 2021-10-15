package me.cubixor.hubselector.bungeecord.queue;

import me.cubixor.hubselector.bungeecord.HubSelectorBungee;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.Title;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class QueueTitle {

    HubSelectorBungee plugin;
    QueueMainBungee queue;

    public QueueTitle() {
        plugin = HubSelectorBungee.getInstance();
        queue = QueueMainBungee.getInstance();
        titleRunnable();
    }

    private void titleRunnable() {
        queue.setTitleRunnable(plugin.getProxy().getScheduler().schedule(plugin, () -> {
            List<ProxiedPlayer> players = new ArrayList<>(QueueUtils.getQueuePlayers());
            for (ProxiedPlayer player : players) {
                int pos = players.indexOf(player) + 1;

                Title title = ProxyServer.getInstance().createTitle();
                title = title.fadeIn(0);
                title = title.fadeOut(0);
                title = title.stay(40);
                title = title.title(plugin.getMessage("queue.title",
                        new String[]{"%pos%", "%max%"},
                        new String[]{Integer.toString(pos), Integer.toString(players.size())}));
                title = title.subTitle(plugin.getMessage("queue.subtitle", "%priority%", QueueUtils.getQueueRank(player).getDisplayName()));
                title.send(player);
            }

        }, 0, 1, TimeUnit.SECONDS));
    }

}
