package me.cubixor.hubselector.bungeecord;

import me.cubixor.hubselector.bungeecord.socket.SocketServerSender;
import me.cubixor.hubselector.utils.Hub;
import me.cubixor.hubselector.utils.PermissionUtils;
import net.md_5.bungee.api.connection.ProxiedPlayer;

public class HubInventory {

    HubSelectorBungee plugin;

    public HubInventory() {
        plugin = HubSelectorBungee.getInstance();
    }

    public void inventoryClickData(ProxiedPlayer player, Hub hub, boolean fromInv) {
        inventoryClickData(player, player, hub, fromInv);
    }

    public void inventoryClickData(ProxiedPlayer player, ProxiedPlayer target, Hub hub, boolean fromInv) {
        if (HubUtils.checkIfCooldown(player)) {
            return;
        }

        boolean online = HubUtils.checkIfOnline(hub);
        boolean current = HubUtils.checkIfCurrent(hub, target);
        boolean full = HubUtils.checkIfFull(hub, player, false);
        boolean vip = hub.isVip();
        boolean active = hub.isActive();

        boolean available = HubUtils.checkIfAvailable(hub, target);
        if (fromInv && !available) {
            if (plugin.getConfig().getBoolean("sounds.hub-not-available.enabled")) {
                new SocketServerSender().playSound(player, plugin.getConfig().getString("sounds.hub-not-available.sound"),
                        plugin.getConfig().getFloat("sounds.hub-not-available.volume"),
                        plugin.getConfig().getFloat("sounds.hub-not-available.pitch"));
            }
        }

        if (!online) {
            player.sendMessage(plugin.getMessage("connect.hub-offline"));
            return;
        }

        if (!active && !HubUtils.canBypassInactive(player)) {
            player.sendMessage(plugin.getMessage("connect.hub-inactive"));
            return;
        }

        if (current) {
            player.sendMessage(plugin.getMessage("connect.hub-current"));
            return;
        }

        if (full && !HubUtils.canBypassFull(player)) {
            player.sendMessage(plugin.getMessage("connect.hub-full"));
            return;
        }

        if (vip && !PermissionUtils.hasPermission(player, "hub.vip")) {
            player.sendMessage(plugin.getMessage("connect.hub-vip"));
            return;
        }

        new HubMenuUpdate().updateCooldown(player);
        new HubChoose().sendToServer(target, HubUtils.getServerInfo(hub));

        if (!player.equals(target)) {
            player.sendMessage(plugin.getMessage("connect.success-player", new String[]{"%hub%", "%player%"}, new String[]{hub.getName(), target.getName()}));
        }

        if (fromInv) {
            if (plugin.getConfig().getBoolean("sounds.hub-connect.enabled")) {
                new SocketServerSender().playSound(target
                        , plugin.getConfig().getString("sounds.hub-connect.sound"),
                        plugin.getConfig().getFloat("sounds.hub-connect.volume"),
                        plugin.getConfig().getFloat("sounds.hub-connect.pitch"));
            }
        }
    }
}
