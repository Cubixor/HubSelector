package me.cubixor.hubselector.bungeecord.queue;

import me.cubixor.hubselector.bungeecord.HubSelectorBungee;
import me.cubixor.hubselector.utils.QueueRank;
import me.theminecoder.bungeecord.bossbar.BossBar;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.scheduler.ScheduledTask;

import java.io.IOException;
import java.net.Socket;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class QueueMainBungee {

    private static QueueMainBungee instance;

    private final LinkedHashMap<QueueRank, LinkedList<ProxiedPlayer>> queuePlayers = new LinkedHashMap<>();
    private final List<LocalDateTime> joinTimes = new ArrayList<>();
    private final ConcurrentHashMap<ProxiedPlayer, QueuePlayerData> timeJoined = new ConcurrentHashMap<>();
    private ScheduledTask actionBarRunnable;
    private ScheduledTask titleRunnable;
    private ScheduledTask bossBarRunnable;
    private BossBar bossBar;
    private boolean queueOnline = false;
    private boolean queuePaused = false;
    private QueueListener queueListener;

    public static QueueMainBungee getInstance() {
        return instance;
    }

    public void queueOnlineChecker(ServerInfo serverInfo) {
        ProxyServer.getInstance().getScheduler().runAsync(HubSelectorBungee.getInstance(), () -> {
            while (true) {
                try {
                    Socket socket = new Socket();
                    socket.connect(serverInfo.getAddress(), 20);
                    queueOnline = true;
                    try {
                        socket.getInputStream().read();
                    } catch (IOException e) {
                        queueOnline = false;
                    }
                } catch (IOException ignored) {
                }
            }
        });
    }

    public void setInstance() {
        instance = this;
    }

    public synchronized LinkedHashMap<QueueRank, LinkedList<ProxiedPlayer>> getQueuePlayers() {
        return queuePlayers;
    }

    public ScheduledTask getActionBarRunnable() {
        return actionBarRunnable;
    }

    public void setActionBarRunnable(ScheduledTask actionBarRunnable) {
        this.actionBarRunnable = actionBarRunnable;
    }

    public ScheduledTask getTitleRunnable() {
        return titleRunnable;
    }

    public void setTitleRunnable(ScheduledTask titleRunnable) {
        this.titleRunnable = titleRunnable;
    }

    public ScheduledTask getBossBarRunnable() {
        return bossBarRunnable;
    }

    public void setBossBarRunnable(ScheduledTask bossBarRunnable) {
        this.bossBarRunnable = bossBarRunnable;
    }

    public BossBar getBossBar() {
        return bossBar;
    }

    public void setBossBar(BossBar bossBar) {
        this.bossBar = bossBar;
    }

    public boolean isQueueOnline() {
        return queueOnline;
    }

    public synchronized List<LocalDateTime> getJoinTimes() {
        return joinTimes;
    }

    public synchronized ConcurrentHashMap<ProxiedPlayer, QueuePlayerData> getTimeJoined() {
        return timeJoined;
    }

    public boolean isQueuePaused() {
        return queuePaused;
    }

    public void setQueuePaused(boolean queuePaused) {
        this.queuePaused = queuePaused;
    }

    public QueueListener getQueueListener() {
        return queueListener;
    }

    public void setQueueListener(QueueListener queueListener) {
        this.queueListener = queueListener;
    }

}
