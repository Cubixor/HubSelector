package me.cubixor.hubselector.bungeecord;

import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Listener;

import java.util.concurrent.TimeUnit;

public class HubMenuUpdate implements Listener {

    HubSelectorBungee plugin;

    public HubMenuUpdate(HubSelectorBungee hsb) {
        plugin = hsb;
    }


    public void updateMenu(ProxiedPlayer player) {
        plugin.taskId.put(player, plugin.getProxy().getScheduler().schedule(plugin, () -> new BungeeChannel(plugin).getHubInfo(player), 0, plugin.getConfig().getInt("menu-update.rate"), TimeUnit.SECONDS).getId());
    }
}
