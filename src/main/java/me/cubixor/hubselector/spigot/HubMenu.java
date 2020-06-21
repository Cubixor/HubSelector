package me.cubixor.hubselector.spigot;

import com.google.common.collect.Iterables;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class HubMenu implements Listener {

    private final HubSelector plugin;

    public HubMenu(HubSelector hs) {
        plugin = hs;
    }


    public void updateMenuData(HashMap<String, HashMap<InetSocketAddress, List<String>>> servers, Player player) {
        if (Bukkit.getOnlinePlayers().size() == 0) {
            return;
        }

        if (!plugin.setup) {
            new BukkitRunnable() {
                @Override
                public void run() {
                    if (plugin.setup) {
                        menuDataSet(servers, player);
                        this.cancel();
                    }
                }
            }.runTaskTimer(plugin, 0, 1);
        } else {
            menuDataSet(servers, player);
        }

    }

    private void menuDataSet(HashMap<String, HashMap<InetSocketAddress, List<String>>> servers, Player player) {

        int slot = -1;
        int menu = 1;
        for (String server : servers.keySet()) {
            slot++;

            ItemStack serverItem = null;

            InetSocketAddress ip = Iterables.getFirst(servers.get(server).keySet(), null);

            boolean online = checkOnline(ip.getHostName(), ip.getPort());

            List<String> playerList = new ArrayList<>(servers.get(server).get(Iterables.getFirst(servers.get(server).keySet(), null)));
            int playerCountInt = playerList.size();

            int slots = plugin.getConfiguration().getInt("hub-servers." + server + ".slots");

            boolean vip = plugin.getConfiguration().getBoolean("hub-servers." + server + ".vip");


            if (!online) {
                serverItem = setItem("offline", server, "offline-hub-item-lore", playerCountInt, slots);
            }


            if (playerCountInt >= slots && serverItem == null) {
                serverItem = setItem("full", server, "full-hub-item-lore", playerCountInt, slots);
            }

            if (serverItem == null) {
                serverItem = setItem("regular", server, "hub-item-lore", playerCountInt, slots);
            }


            boolean current = playerList.contains(player.getName());
            if (vip) {
                if (!online) {
                    serverItem = setItem("vip", server, "offline-hub-item-lore", playerCountInt, slots);
                }
                if (current) {
                    serverItem = setItem("vip", server, "vip-hub-item-lore-current", playerCountInt, slots);
                }
                if (online && !current) {
                    serverItem = setItem("vip", server, "vip-hub-item-lore", playerCountInt, slots);
                }
            }

            if (current && !vip) {
                serverItem = setItem("current", server, "current-hub-item-lore", playerCountInt, slots);
            }


            plugin.hubInventory.get(player).get(menu - 1).setItem(slot, serverItem);

            HashMap<Integer, String> serverPos = new HashMap<>();
            if (plugin.serverSlot.get(plugin.hubInventory.get(player).get(menu - 1)) != null) {
                serverPos.putAll(plugin.serverSlot.get(plugin.hubInventory.get(player).get(menu - 1)));
            }
            serverPos.put(slot, server);
            plugin.serverSlot.put(plugin.hubInventory.get(player).get(menu - 1), serverPos);


            if (plugin.hubInventory.get(player).get(menu - 1).getSize() - 10 == slot) {
                menu++;
                slot = -1;
            }
        }
    }

    private boolean checkOnline(String ip, int port) {
        try {
            Socket s = new Socket();
            s.connect(new InetSocketAddress(ip, port), 20);
            s.close();
            return true;
        } catch (Exception e) {
            return false;
        }
    }


    private ItemStack setItem(String hubItemMaterial, String hubItemName, String hubItemLore, Integer players, Integer slots) {
        ItemStack item = new ItemStack(Material.getMaterial(plugin.getConfiguration().getString("menu-items." + hubItemMaterial)), 1);
        ItemMeta itemMeta = item.getItemMeta();
        List<String> itemLore = new ArrayList<>(plugin.getMessageList("menu." + hubItemLore));
        List<String> itemLoreFinal = new ArrayList<>();

        itemMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', plugin.getConfiguration().getString("hub-servers." + hubItemName + ".name")));
        for (String s : itemLore) {
            itemLoreFinal.add(s.replace("%players%", players.toString()).replace("%max%", slots.toString()));
        }
        itemMeta.setLore(itemLoreFinal);
        item.setItemMeta(itemMeta);

        return item;
    }


    @EventHandler
    public void onInventoryClick(InventoryClickEvent evt) {
        if (evt.getCurrentItem() != null && plugin.hubInventory.get(evt.getWhoClicked()).contains(evt.getClickedInventory())) {

            evt.setCancelled(true);
            evt.getWhoClicked().closeInventory();

            if (plugin.serverSlot.get(evt.getInventory()).get(evt.getSlot()) != null) {
                Player player = (Player) evt.getWhoClicked();
                String server = plugin.serverSlot.get(evt.getInventory()).get(evt.getSlot());

                new ConfigurationBungee(plugin).getHubInfo(player, server);
                return;
            }
            int invSlots = plugin.hubInventory.get(evt.getWhoClicked()).getFirst().getSize();
            int inventoryNumber = 0;

            for (int i = 0; i <= plugin.hubInventory.get(evt.getWhoClicked()).size() - 1; i++) {
                if (plugin.hubInventory.get(evt.getWhoClicked()).get(i).equals(evt.getClickedInventory())) {
                    inventoryNumber = i;
                }
            }


            if (evt.getSlot() == invSlots - 4) {
                evt.getWhoClicked().openInventory(plugin.hubInventory.get(evt.getWhoClicked()).get(inventoryNumber + 1));
                return;
            }

            if (evt.getSlot() == invSlots - 6) {
                evt.getWhoClicked().openInventory(plugin.hubInventory.get(evt.getWhoClicked()).get(inventoryNumber - 1));
            }

        }
    }

}
