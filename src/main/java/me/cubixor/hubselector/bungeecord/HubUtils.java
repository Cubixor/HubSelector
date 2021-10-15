package me.cubixor.hubselector.bungeecord;

import me.cubixor.hubselector.utils.Hub;
import me.cubixor.hubselector.utils.HubData;
import me.cubixor.hubselector.utils.PermissionUtils;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.ArrayList;
import java.util.List;

public class HubUtils {

    public static boolean isInHub(ProxiedPlayer player, boolean msg) {
        HubSelectorBungee plugin = HubSelectorBungee.getInstance();
        for (Hub hub : plugin.getHubs()) {
            if (checkIfCurrent(hub, player)) {
                if (msg) {
                    player.sendMessage(plugin.getMessage("command.already-in-hub"));
                }
                return true;
            }
        }
        return false;
    }

    public static boolean checkIfOnline(Hub hub) {
        return HubSelectorBungee.getInstance().getSpigotSocket().containsKey(hub.getServer());
    }

    public static boolean checkIfOnline(ServerInfo server) {
        return HubSelectorBungee.getInstance().getSpigotSocket().containsKey(server.getName());
    }

    public static boolean checkIfFull(Hub hub, ProxiedPlayer player, boolean ignoreBypass) {
        boolean full = checkIfFull(hub);
        if (!ignoreBypass) {
            return !(!full || canBypassFull(player));
        } else {
            return full;
        }
    }

    public static boolean checkIfFull(Hub hub) {
        //return HubUtils.getServerInfo(hub).getPlayers().size() >= hub.getSlots();
        return HubSelectorBungee.getInstance().getServerSlots().get(hub.getServer()).size() >= hub.getSlots();
    }

    public static boolean checkIfActive(Hub hub, ProxiedPlayer player, boolean ignoreBypass) {
        boolean active = hub.isActive();
        if (!ignoreBypass) {
            return active || canBypassInactive(player);
        } else {
            return active;
        }
    }

    public static boolean canBypassInactive(ProxiedPlayer player) {
        return PermissionUtils.hasPermission(player, "hub.bypass.inactive");
    }


    public static boolean canBypassFull(ProxiedPlayer player) {
        return PermissionUtils.hasPermission(player, "hub.bypass.full");
    }

    public static boolean checkIfCurrent(Hub hub, ProxiedPlayer player) {
        return HubUtils.getServerInfo(hub).getPlayers().contains(player);
    }

    public static boolean checkIfAvailable(Hub hub, ProxiedPlayer player) {
        return checkIfAvailable(hub, player, false);
    }

    public static boolean checkIfAvailable(Hub hub, ProxiedPlayer player, boolean ignoreBypasses) {
/*
        System.out.println("online" + checkIfOnline(hub));
        System.out.println("current" + !checkIfCurrent(hub, player));
        System.out.println("full" + !checkIfFull(hub, player, ignoreBypasses));
        System.out.println("active" + checkIfActive(hub, player, ignoreBypasses));
        System.out.println("vip" + (player.hasPermission("hub.vip") || !hub.isVip()));
*/
        return checkIfOnline(hub)
                && !checkIfCurrent(hub, player)
                && !checkIfFull(hub, player, ignoreBypasses)
                && checkIfActive(hub, player, ignoreBypasses)
                && (PermissionUtils.hasPermission(player, "hub.vip") || !hub.isVip());
    }

    public static boolean checkIfCooldown(ProxiedPlayer player) {
        HubSelectorBungee plugin = HubSelectorBungee.getInstance();
        if (!PermissionUtils.hasPermission(player, "hub.bypass.cooldown") && plugin.getChangeCooldown().contains(player)) {
            player.sendMessage(plugin.getMessage("connect.cooldown"));
            return true;
        }
        return false;
    }

    public static Hub getHub(String server) {
        HubSelectorBungee plugin = HubSelectorBungee.getInstance();
        for (Hub hub : plugin.getHubs()) {
            if (hub.getServer().equals(server)) {
                return hub;
            }
        }
        return null;
    }


    public static ServerInfo getServerInfo(Hub hub) {
        HubSelectorBungee plugin = HubSelectorBungee.getInstance();
        return plugin.getProxy().getServerInfo(hub.getServer());
    }

    public static HubData setHubData(Hub hub, ProxiedPlayer player) {

        List<String> players = new ArrayList<>();
        HubUtils.getServerInfo(hub).getPlayers().forEach(p -> players.add(p.getName()));
        boolean online = HubUtils.checkIfOnline(hub);
        boolean current = HubUtils.checkIfCurrent(hub, player);
        boolean full = HubUtils.checkIfFull(hub, player, true);
        boolean vipPlayer = PermissionUtils.hasPermission(player, "hub.vip");

        return new HubData(hub, players, online, current, full, vipPlayer);
    }

}
