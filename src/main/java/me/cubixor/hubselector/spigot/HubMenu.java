package me.cubixor.hubselector.spigot;

import com.cryptomorin.xseries.XMaterial;
import com.cryptomorin.xseries.XSound;
import me.cubixor.hubselector.spigot.socket.SocketClientSender;
import me.cubixor.hubselector.utils.HubData;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class HubMenu implements Listener {

    private final HubSelector plugin;

    public HubMenu() {
        plugin = HubSelector.getInstance();
    }

    public static void openMenu(Player player) {
        HubSelector plugin = HubSelector.getInstance();
        player.openInventory(plugin.getHubInventory().get(player).getFirst());
        if (plugin.getConfig().getBoolean("sounds.menu-open.enabled")) {
            player.playSound(player.getLocation(),
                    XSound.matchXSound(plugin.getConfiguration().getString("sounds.menu-open.sound")).get().parseSound(),
                    (float) plugin.getConfiguration().getDouble("sounds.menu-open.volume"),
                    (float) plugin.getConfiguration().getDouble("sounds.menu-open.pitch"));
        }
    }

    public void menuDataSet(LinkedList<HubData> hubDataLinkedList, Player player) {
        if (!plugin.getHubInventory().containsKey(player)) {
            new SocketClientSender().menuCloseMessage(player);
            return;
        }
        int slot = 0;
        int menu = 0;
        for (HubData hubData : hubDataLinkedList) {

            ItemStack hubItem = matchServerItem(hubData);

            plugin.getHubInventory().get(player).get(menu).setItem(slot, hubItem);
            plugin.getSlot().put(new HubMenuPos(menu, slot), hubData.getServer());

            if (plugin.getEmptyHubInventory().size() > 1 && plugin.getHubInventory().get(player).get(menu).getSize() - 10 == slot) {
                menu++;
                slot = 0;
            } else {
                slot++;
            }
        }
        player.updateInventory();
    }

    private ItemStack matchServerItem(HubData hubData) {
        ItemStack serverItem;

        if (hubData.isVip()) {
            if (!hubData.isOnline()) {
                serverItem = setItem("offline", "vip-offline-hub-lore", hubData);
                return serverItem;
            }
            if (hubData.isCurrent()) {
                serverItem = setItem("vip", "vip-current-hub-lore", hubData);
                return serverItem;
            }
            if (!hubData.isActive()) {
                serverItem = setItem("inactive", "vip-inactive-hub-lore", hubData);
                return serverItem;
            }
            if (!hubData.isPlayerVip()) {
                serverItem = setItem("vip", "vip-no-permission-hub-lore", hubData);
                return serverItem;
            }
            if (hubData.isFull()) {
                serverItem = setItem("vip", "vip-full-hub-lore", hubData);
                return serverItem;
            }
            serverItem = setItem("vip", "vip-hub-lore", hubData);
        } else {
            if (!hubData.isOnline()) {
                serverItem = setItem("offline", "offline-hub-lore", hubData);
                return serverItem;
            }
            if (hubData.isCurrent()) {
                serverItem = setItem("current", "current-hub-lore", hubData);
                return serverItem;
            }
            if (!hubData.isActive()) {
                serverItem = setItem("inactive", "inactive-hub-lore", hubData);
                return serverItem;
            }
            if (hubData.isFull()) {
                serverItem = setItem("full", "full-hub-lore", hubData);
                return serverItem;
            }

            serverItem = setItem("regular", "regular-hub-lore", hubData);
        }

        return serverItem;
    }


    private ItemStack setItem(String hubItemMaterial, String hubItemLore, HubData hubData) {
        int players = hubData.getPlayers().size();
        int slots = hubData.getSlots();

        ItemStack item = new ItemStack(
                XMaterial.matchXMaterial(plugin.getConfiguration().getString("menu-items." + hubItemMaterial)).get().parseItem());
        ItemMeta itemMeta = item.getItemMeta();
        List<String> itemLore = new ArrayList<>(plugin.getMessageList("menu." + hubItemLore));
        List<String> itemLoreFinal = new ArrayList<>();

        itemMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', hubData.getName()));
        for (String s : itemLore) {
            itemLoreFinal.add(s.replace("%players%", Integer.toString(players)).replace("%max%", Integer.toString(slots)));
        }
        itemMeta.setLore(itemLoreFinal);
        item.setItemMeta(itemMeta);

        return item;
    }


    @EventHandler
    public void onInventoryClick(InventoryClickEvent evt) {
        if (evt.getCurrentItem() != null && evt.getClickedInventory() != null &&
                plugin.getHubInventory().get((Player) evt.getWhoClicked()).contains(evt.getClickedInventory())) {

            Player player = (Player) evt.getWhoClicked();

            evt.setCancelled(true);

            int inventorySlot = evt.getSlot();
            int inventoryId = getInventoryId(player, evt.getClickedInventory());
            String server = plugin.getSlot().get(new HubMenuPos(inventoryId, inventorySlot));
            if (server != null) {
                new SocketClientSender().getHubInfo(player, server);
                player.closeInventory();
                return;
            }


            int invSlots = plugin.getEmptyHubInventory().getFirst().getSize();

            if (plugin.getHubInventory().get(player).size() > 1 && evt.getSlot() == invSlots - 4) {
                evt.getWhoClicked().openInventory(plugin.getHubInventory().get(player).get(inventoryId + 1));
                if (plugin.getConfiguration().getBoolean("sounds.menu-change-page.enabled")) {
                    player.playSound(player.getLocation(),
                            XSound.matchXSound(plugin.getConfiguration().getString("sounds.menu-change-page.sound")).get().parseSound(),
                            (float) plugin.getConfiguration().getDouble("sounds.menu-change-page.volume"),
                            (float) plugin.getConfiguration().getDouble("sounds.menu-change-page.pitch"));
                }
            } else if (plugin.getHubInventory().get(player).size() > 1 && evt.getSlot() == invSlots - 6) {
                evt.getWhoClicked().openInventory(plugin.getHubInventory().get(player).get(inventoryId - 1));
                if (plugin.getConfig().getBoolean("sounds.menu-change-page.enabled")) {
                    player.playSound(player.getLocation(),
                            XSound.matchXSound(plugin.getConfiguration().getString("sounds.menu-change-page.sound")).get().parseSound(),
                            (float) plugin.getConfiguration().getDouble("sounds.menu-change-page.volume"),
                            (float) plugin.getConfiguration().getDouble("sounds.menu-change-page.pitch"));
                }
            } else {
                player.closeInventory();
            }
        }
    }

    private int getInventoryId(Player player, Inventory currentInventory) {
        for (int i = 0; i <= plugin.getHubInventory().get(player).size() - 1; i++) {
            if (plugin.getHubInventory().get(player).get(i).equals(currentInventory)) {
                return i;
            }
        }
        return -1;
    }

}