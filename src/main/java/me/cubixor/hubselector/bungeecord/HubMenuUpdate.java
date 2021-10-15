package me.cubixor.hubselector.bungeecord;

import me.cubixor.hubselector.bungeecord.socket.SocketServerSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Listener;

import java.util.concurrent.TimeUnit;

public class HubMenuUpdate implements Listener {

    HubSelectorBungee plugin;

    public HubMenuUpdate() {
        plugin = HubSelectorBungee.getInstance();
    }


    public void updateMenu(ProxiedPlayer player) {
        plugin.getMenuUpdateTask().put(player, plugin.getProxy().getScheduler().schedule(plugin, () ->
                new SocketServerSender().sendALlHubsInfo(player), 0, plugin.getConfig().getInt("menu-update.rate"), TimeUnit.SECONDS).getId());
    }


    public void updateCooldown(ProxiedPlayer player) {
        plugin.getChangeCooldown().add(player);

        plugin.getProxy().getScheduler().schedule(plugin, () -> plugin.getChangeCooldown().remove(player), plugin.getConfig().getInt("change-cooldown"), TimeUnit.SECONDS);
    }
}
