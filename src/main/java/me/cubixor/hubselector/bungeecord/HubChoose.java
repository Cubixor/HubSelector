package me.cubixor.hubselector.bungeecord;

import me.cubixor.hubselector.bungeecord.queue.QueueMainBungee;
import me.cubixor.hubselector.bungeecord.queue.QueueUtils;
import me.cubixor.hubselector.utils.Hub;
import me.cubixor.hubselector.utils.PermissionUtils;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.time.LocalDateTime;
import java.util.LinkedList;
import java.util.List;

public class HubChoose {

    HubSelectorBungee plugin;

    public HubChoose() {
        plugin = HubSelectorBungee.getInstance();
    }

    public static String chooseServer(ProxiedPlayer player, boolean kickIfFull, boolean kick, boolean bypass) {
        HubSelectorBungee plugin = HubSelectorBungee.getInstance();

        if (QueueUtils.queueAvailable() && (QueueMainBungee.getInstance().isQueuePaused() && !bypass)) {
            return plugin.getHubServers().getString("queue-server");
        }

        LinkedList<String> availableServers = new LinkedList<>();
        LinkedList<String> availableVipServers = new LinkedList<>();

        for (Hub hub : plugin.getHubs()) {
            //System.out.println(hub.toString());

            if (!bypass) {
                if (!HubUtils.checkIfAvailable(hub, player, true)) {
                    continue;
                }
            } else {
                if (!HubUtils.checkIfOnline(hub)) {
                    continue;
                }
            }

            if (hub.isVip()) {
                availableVipServers.add(hub.getServer());
            } else {
                availableServers.add(hub.getServer());
            }
        }

        boolean vipPlayer = PermissionUtils.hasPermission(player, "hub.vip") || bypass;


        if ((availableServers.isEmpty() && !vipPlayer) || (availableVipServers.isEmpty() && vipPlayer && availableServers.isEmpty())) {
            if (HubUtils.canBypassFull(player) || HubUtils.canBypassInactive(player)) {
                for (Hub hub : plugin.getHubs()) {
                    if (HubUtils.checkIfAvailable(hub, player)) {
                        if (hub.isVip()) {
                            availableVipServers.add(hub.getServer());
                        } else {
                            availableServers.add(hub.getServer());
                        }
                    }
                }
                if (!((availableServers.isEmpty() && !vipPlayer) || (availableVipServers.isEmpty() && vipPlayer && availableServers.isEmpty()))) {
                    return getMethod(vipPlayer, availableServers, availableVipServers);
                }
            }
            if (QueueUtils.queueAvailable()) {
/*
                if (!QueueUtils.isInQueue(player)) {
                    new QueueUtils().putInQueue(player);
                }
*/
                return plugin.getHubServers().getString("queue-server");
            } else {
                if (!kick) {
                    if (kickIfFull) {
                        player.disconnect(plugin.getMessage("connect.kick-full"));
                    } else {
                        player.sendMessage(plugin.getMessage("connect.full"));
                    }
                }
                return null;
            }

        }
        return getMethod(vipPlayer, availableServers, availableVipServers);
    }

    public static String getMethod(boolean vip, List<String> availableServers, List<String> availableVipServers) {
        HubSelectorBungee plugin = HubSelectorBungee.getInstance();
        if (vip) {
            return plugin.getVipJoinMethodsInstance().method(availableServers, availableVipServers);
        } else {
            return plugin.getJoinMethodsInstance().method(availableServers);
        }

    }


    public void connectToSpecifiedHub(ProxiedPlayer player, String targetPlayerName, String server) {
        ProxiedPlayer targetPlayer = plugin.getProxy().getPlayer(targetPlayerName);
        Hub hub = HubUtils.getHub(server);

        if (hub == null && !server.equalsIgnoreCase("*")) {
            player.sendMessage(plugin.getMessage("command.invalid-hub"));
            return;
        }

        if (targetPlayer == null) {
            player.sendMessage(plugin.getMessage("command.invalid-player"));
            return;
        }

        if (player.getName().equals(targetPlayerName)) {
            if (HubUtils.checkIfCooldown(player)) {
                return;
            }
        }

        if (server.equalsIgnoreCase("*")) {
            if (targetPlayer.equals(player)) {
                connectToHub(targetPlayer, true, false);
            } else {
                if (connectToHub(targetPlayer, false, false)) {
                    player.sendMessage(plugin.getMessage("connect.success-player", new String[]{"%hub%", "%player%"}, new String[]{"*", targetPlayerName}));
                } else {
                    player.sendMessage(plugin.getMessage("connect.failure-player", "%player%", targetPlayerName));
                }
            }
            return;
        }

        new HubInventory().inventoryClickData(player, targetPlayer, hub, false);

    }

    public boolean connectToHub(ProxiedPlayer player, boolean msg, boolean bypass) {
        if (HubUtils.isInHub(player, msg)) {
            return false;
        }
        String server = chooseServer(player, plugin.getConfig().getBoolean("kick-if-full"), false, bypass);

        if (server != null) {
            Hub hub = HubUtils.getHub(server);
            if (hub != null) {
                if (QueueUtils.isInQueue(player)) {
                    QueueMainBungee queue = QueueMainBungee.getInstance();
                    if (plugin.getQueueServer().getPlayers().size() > 1) {
                        queue.getJoinTimes().add(LocalDateTime.now());
                    } else {
                        queue.getJoinTimes().clear();
                    }
                }
                sendToServer(player, HubUtils.getServerInfo(hub));
                return true;
            } else if (QueueUtils.queueAvailable() && plugin.getQueueServer().getName().equals(server) && !player.getServer().getInfo().equals(plugin.getQueueServer())) {
                player.connect(plugin.getQueueServer());
            }
        }
        return false;
    }

    public void sendToServer(ProxiedPlayer player, ServerInfo serverInfo) {
        player.connect(serverInfo);
        plugin.getServerSlots().get(serverInfo.getName()).add(player.getName());
        System.out.println("sendToServer " + plugin.getServerSlots());
        player.sendMessage(plugin.getMessage("connect.success", "%hub%", HubUtils.getHub(serverInfo.getName()).getName()));
    }
}