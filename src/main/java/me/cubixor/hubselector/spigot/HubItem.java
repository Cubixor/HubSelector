package me.cubixor.hubselector.spigot;

import me.cubixor.hubselector.spigot.socket.SocketClientSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryCreativeEvent;
import org.bukkit.event.player.*;

public class HubItem implements Listener {

    private final HubSelector plugin;

    public HubItem() {
        plugin = HubSelector.getInstance();
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent evt) {
        evt.setJoinMessage(null);

        plugin.getHubInventory().put(evt.getPlayer(), plugin.getEmptyHubInventory());
        if (plugin.getConfiguration().getBoolean("item.show")) {
            evt.getPlayer().getInventory().setItem(plugin.getConfiguration().getInt("item.slot-number"), plugin.getHubItem());
        }
    }


    @EventHandler
    public void onLeave(PlayerQuitEvent evt) {
        evt.setQuitMessage(null);

        plugin.getHubInventory().remove(evt.getPlayer());
        if (evt.getPlayer().getInventory().contains(plugin.getHubItem())) {
            evt.getPlayer().getInventory().remove(plugin.getHubItem());
        }
    }

    @EventHandler
    public void onClick(PlayerInteractEvent evt) {
        if (evt.getItem() != null && evt.getItem().equals(plugin.getHubItem())) {
            new SocketClientSender().menuOpenMessage(evt.getPlayer());
            HubMenu.openMenu(evt.getPlayer());
            evt.setCancelled(true);
        }
    }

    @EventHandler
    public void onClose(InventoryCloseEvent evt) {
        if (plugin.getHubInventory().containsKey((Player) evt.getPlayer()) &&
                plugin.getHubInventory().get((Player) evt.getPlayer()).contains(evt.getInventory()) &&
                !plugin.getConfiguration().getBoolean("menu-update.update-on-open-only")) {

            new SocketClientSender().menuCloseMessage(evt.getPlayer().getName());
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent evt) {
        if ((evt.getCurrentItem() != null && evt.getCurrentItem().equals(plugin.getHubItem())) ||
                (plugin.getHubInventory().containsKey((Player) evt.getWhoClicked()) &&
                        plugin.getHubInventory().get((Player) evt.getWhoClicked()).contains(evt.getClickedInventory()))) {
            evt.setCancelled(true);
        }

    }

    @EventHandler
    public void onInventoryCreative(InventoryCreativeEvent evt) {
        if (evt.getCurrentItem() != null && evt.getCurrentItem().equals(plugin.getHubItem())) {
            evt.setCancelled(true);
        }
    }

    @EventHandler
    public void onDrop(PlayerDropItemEvent evt) {
        if (evt.getItemDrop().getItemStack().equals(plugin.getHubItem())) {
            evt.setCancelled(true);
        }
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent evt) {
        evt.getPlayer().getInventory().setItem(plugin.getConfiguration().getInt("item.slot-number"), plugin.getHubItem());
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent evt) {
        evt.getDrops().remove(plugin.getHubItem());
    }
}