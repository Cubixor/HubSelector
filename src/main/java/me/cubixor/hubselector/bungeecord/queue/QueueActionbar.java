package me.cubixor.hubselector.bungeecord.queue;

import me.cubixor.hubselector.bungeecord.HubSelectorBungee;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

public class QueueActionbar {

    private final HubSelectorBungee plugin;
    private final QueueMainBungee queue;

    public QueueActionbar() {
        plugin = HubSelectorBungee.getInstance();
        queue = QueueMainBungee.getInstance();
        actionBarRunnable();
    }


    private void actionBarRunnable() {
        queue.setActionBarRunnable(plugin.getProxy().getScheduler().schedule(plugin, () -> {
            if (plugin.getQueueServer().getPlayers().isEmpty()) {
                return;
            }

            if (queue.isQueuePaused()) {
                for (ProxiedPlayer player : plugin.getQueueServer().getPlayers()) {
                    player.sendMessage(ChatMessageType.ACTION_BAR, plugin.getMessage("queue.waiting-paused"));
                }
                return;
            }

            if (queue.getJoinTimes().size() <= 1) {
                for (ProxiedPlayer player : plugin.getQueueServer().getPlayers()) {
                    player.sendMessage(ChatMessageType.ACTION_BAR, plugin.getMessage("queue.waiting-unpredictable"));
                }
                return;
            }


            long time = 0;
            LocalDateTime lastTime = queue.getJoinTimes().get(0);
            for (LocalDateTime localDateTime : queue.getJoinTimes()) {
                time += Duration.between(lastTime, localDateTime).getSeconds();
                lastTime = localDateTime;
            }
            long avgTime = time / (queue.getJoinTimes().size() > 1 ? (queue.getJoinTimes().size() - 1) : 1);
            //System.out.println("avgTime " + avgTime);


            for (ProxiedPlayer player : plugin.getQueueServer().getPlayers()) {
                QueuePlayerData queuePlayerData = queue.getTimeJoined().get(player);

                System.out.println(queue.getTimeJoined().toString());

                int multipliedTime = (int) (avgTime * queuePlayerData.getJoinPosition());

                //System.out.println(player.getName() + " multipliedtime " + multipliedTime);

                LocalDateTime timeJoined = queuePlayerData.getTimeJoined();
                LocalDateTime estimatedJoinTime = timeJoined.plusSeconds(multipliedTime);


                int seconds = (int) Duration.between(LocalDateTime.now(), estimatedJoinTime).getSeconds();
                int minutes = seconds / 60;
                int hours = seconds / 3600;

                //System.out.println(player.getName() + " seconds " + seconds);

                String toReplace;
                if (hours > 0) {
                    toReplace = plugin.getMessageString("queue.hours", "%hours%", Integer.toString(hours));
                } else if (minutes > 0) {
                    toReplace = plugin.getMessageString("queue.minutes", "%minutes%", Integer.toString(minutes));
                } else if (seconds > 0) {
                    toReplace = plugin.getMessageString("queue.seconds", "%seconds%", Integer.toString(seconds));
                } else {
                    toReplace = "?";
                }

                player.sendMessage(ChatMessageType.ACTION_BAR, plugin.getMessage("queue.waiting-time", "%time%", toReplace));
            }

        }, 0, 1, TimeUnit.SECONDS));
    }
}
