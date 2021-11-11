package me.cubixor.hubselector.bungeecord.queue;


import me.cubixor.hubselector.bungeecord.HubSelectorBungee;
import me.theminecoder.bungeecord.bossbar.BarColor;
import me.theminecoder.bungeecord.bossbar.BarStyle;
import me.theminecoder.bungeecord.bossbar.BossBar;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class QueueBossBar {

    private final HubSelectorBungee plugin;
    private final QueueMainBungee queue;

    public QueueBossBar() {
        plugin = HubSelectorBungee.getInstance();
        queue = QueueMainBungee.getInstance();
        createBossBar();
    }

    private void createBossBar() {
        BossBar bossBar = new BossBar(plugin.getMessageList("queue.bossbar").get(0), BarColor.PURPLE, BarStyle.SOLID);
        bossBar.setProgress(0);
        queue.setBossBar(bossBar);
        cycleBossBars();
    }

    private void cycleBossBars() {
        List<String> messages = new ArrayList<>(plugin.getMessageList("queue.bossbar"));
        int size = messages.size() - 1;

        queue.setBossBarRunnable(plugin.getProxy().getScheduler().schedule(plugin, new Runnable() {
            int currentBarId = 0;

            @Override
            public void run() {
                synchronized (queue) {
                    queue.getBossBar().setTitle(plugin.getMessageList("queue.bossbar").get(currentBarId));
                }
                if (currentBarId == size) {
                    currentBarId = 0;
                } else {
                    currentBarId++;
                }

            }
        }, 0, plugin.getConfig().getInt("queue.bossbar-change-rate"), TimeUnit.SECONDS));
    }


}
