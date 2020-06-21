package me.cubixor.hubselector.spigot;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

public class HubItem implements Listener {

    private final HubSelector plugin;

    public HubItem(HubSelector hs) {
        plugin = hs;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent evt) {
        if (plugin.setup) {
            addMenu(evt.getPlayer());
        } else {
            new BukkitRunnable() {
                @Override
                public void run() {
                    if (plugin.setup) {
                        addMenu(evt.getPlayer());
                        this.cancel();
                    }
                }
            }.runTaskTimer(plugin, 0, 1);
        }
    }

    public void addMenu(Player player) {
        plugin.hubInventory.put(player, plugin.emptyHubInventory);

        player.getInventory().setItem(plugin.getConfiguration().getInt("item.slot-number"), plugin.hubItem);
    }

    public void setupMenu() {
        plugin.hubInventory = new HashMap<>();
        plugin.serverSlot = new HashMap<>();
        plugin.emptyHubInventory = new LinkedList();

        List<String> hubs = new ArrayList<>(plugin.getConfiguration().getConfigurationSection("hub-servers").getKeys(false));
        int hubCount = hubs.size();
        int rowsCount;

        if (hubCount % 9 == 0) {
            rowsCount = hubCount / 9;
        } else {
            rowsCount = hubCount / 9 + 1;
        }

        int maxRows = plugin.getConfiguration().getInt("max-menu-rows");
        int slotsCount;

        LinkedList<Inventory> hubInventory = new LinkedList<>();

        if (rowsCount <= maxRows) {
            slotsCount = (rowsCount + 1) * 9;
            hubInventory.add(Bukkit.createInventory(null, slotsCount, plugin.getMessage("menu.hub-menu-name")));
        } else {
            slotsCount = (maxRows + 1) * 9;
            int menuCount = rowsCount / maxRows;
            if (rowsCount % maxRows != 0) {
                menuCount += 1;
            }
            for (int i = 0; i < menuCount; i++) {
                hubInventory.add(Bukkit.createInventory(null, slotsCount, plugin.getMessage("menu.hub-menu-name")));
            }

            ItemStack nextItem = new ItemStack(Material.getMaterial(plugin.getConfiguration().getString("menu-items.next-page")), 1);
            ItemMeta nextItemMeta = nextItem.getItemMeta();
            nextItemMeta.setDisplayName(plugin.getMessage("menu.next-page-item-name"));
            nextItemMeta.setLore(plugin.getMessageList("menu.next-page-item-lore"));
            nextItem.setItemMeta(nextItemMeta);

            ItemStack previousItem = new ItemStack(Material.getMaterial(plugin.getConfiguration().getString("menu-items.previous-page")), 1);
            ItemMeta previousItemMeta = previousItem.getItemMeta();
            previousItemMeta.setDisplayName(plugin.getMessage("menu.previous-page-item-name"));
            previousItemMeta.setLore(plugin.getMessageList("menu.previous-page-item-lore"));
            previousItem.setItemMeta(previousItemMeta);


            for (int i = 0; i <= (hubInventory.size() - 1); i++) {
                if (i != 0) {
                    hubInventory.get(i).setItem(slotsCount - 6, previousItem);
                }
                if (i != (hubInventory.size() - 1)) {
                    hubInventory.get(i).setItem(slotsCount - 4, nextItem);
                }
            }
        }


        ItemStack closeItem = new ItemStack(Material.getMaterial(plugin.getConfiguration().getString("menu-items.menu-close")), 1);
        ItemMeta closeItemMeta = closeItem.getItemMeta();
        closeItemMeta.setDisplayName(plugin.getMessage("menu.close-item-name"));
        closeItemMeta.setLore(plugin.getMessageList("menu.close-item-lore"));
        closeItem.setItemMeta(closeItemMeta);

        for (Inventory inv : hubInventory) {
            inv.setItem(slotsCount - 5, closeItem);
        }

        plugin.emptyHubInventory.addAll(hubInventory);
    }

    @EventHandler
    public void onLeave(PlayerQuitEvent evt) {
        plugin.hubInventory.remove(evt.getPlayer());
        if (plugin.hubItem != null && evt.getPlayer().getInventory().contains(plugin.hubItem)) {
            evt.getPlayer().getInventory().remove(plugin.hubItem);
        }
    }

    @EventHandler
    public void onClick(PlayerInteractEvent evt) {
        if (evt.getItem() != null && plugin.hubItem != null && evt.getItem().equals(plugin.hubItem)) {
            new ConfigurationBungee(plugin).menuOpenMessage(evt.getPlayer());
            evt.getPlayer().openInventory(plugin.hubInventory.get(evt.getPlayer()).getFirst());
        }
    }

    @EventHandler
    public void onClose(InventoryCloseEvent evt) {
        if (plugin.getConfiguration() != null && !plugin.getConfiguration().getBoolean("menu-update.update-on-open-only")) {
            new ConfigurationBungee(plugin).menuCloseMessage((Player) evt.getPlayer());
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent evt) {
        if (evt.getCurrentItem() != null && plugin.hubItem != null && evt.getCurrentItem().equals(plugin.hubItem)) {
            evt.setCancelled(true);
        }
    }

    @EventHandler
    public void onDrop(PlayerDropItemEvent evt) {
        if (plugin.hubItem != null && evt.getItemDrop().getItemStack().equals(plugin.hubItem)) {
            evt.setCancelled(true);
        }
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent evt) {
        if (plugin.hubItem != null) {
            evt.getDrops().remove(plugin.hubItem);
        }
    }
}
