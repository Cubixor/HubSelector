package me.cubixor.hubselector.spigot;

import com.cryptomorin.xseries.XMaterial;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.*;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;

public class SetupSpigot {

    HubSelector plugin;

    public SetupSpigot() {
        plugin = HubSelector.getInstance();
    }

    public void registerMethods() {
        plugin.getServer().getPluginManager().registerEvents(new HubItem(), plugin);
        plugin.getServer().getPluginManager().registerEvents(new HubMenu(), plugin);
    }

    public void menuSetup(int hubCount) {
        plugin.setHubInventory(new HashMap<>());
        plugin.setSlot(new HashMap<>());
        plugin.setEmptyHubInventory(new LinkedList<>());

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
            if (plugin.getConfiguration().getBoolean("show-menu-close-item")) {
                slotsCount = (rowsCount + 1) * 9;
            } else {
                slotsCount = rowsCount * 9;
            }
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

            ItemStack nextItem = XMaterial.matchXMaterial(plugin.getConfiguration().getString("menu-items.next-page")).get().parseItem();
            ItemMeta nextItemMeta = nextItem.getItemMeta();
            nextItemMeta.setDisplayName(plugin.getMessage("menu.next-page-item-name"));
            nextItemMeta.setLore(plugin.getMessageList("menu.next-page-item-lore"));
            nextItem.setItemMeta(nextItemMeta);

            ItemStack previousItem = XMaterial.matchXMaterial(plugin.getConfiguration().getString("menu-items.previous-page")).get().parseItem();
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

        if (plugin.getConfiguration().getBoolean("show-menu-close-item")) {
            ItemStack closeItem = XMaterial.matchXMaterial(plugin.getConfiguration().getString("menu-items.menu-close")).get().parseItem();
            ItemMeta closeItemMeta = closeItem.getItemMeta();
            closeItemMeta.setDisplayName(plugin.getMessage("menu.close-item-name"));
            closeItemMeta.setLore(plugin.getMessageList("menu.close-item-lore"));
            closeItem.setItemMeta(closeItemMeta);

            for (Inventory inv : hubInventory) {
                inv.setItem(slotsCount - 5, closeItem);
            }
        }

        plugin.getEmptyHubInventory().addAll(hubInventory);
    }

    public void itemSetup() {
        ItemStack hubItem = XMaterial.matchXMaterial(plugin.getConfiguration().getString("item.type")).get().parseItem();
        ItemMeta hubItemMeta = hubItem.getItemMeta();
        hubItemMeta.setDisplayName(plugin.getMessage("item.hub-item-name"));
        List<String> hubItemLore = new ArrayList<>(plugin.getMessageList("item.hub-item-lore"));
        hubItemMeta.setLore(hubItemLore);
        hubItem.setItemMeta(hubItemMeta);
        plugin.setHubItem(hubItem);
    }

    public void unload() {
        int slot = plugin.getConfiguration().getInt("item.slot-number");
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (p.getInventory().getItem(slot) != null && p.getInventory().getItem(slot).equals(plugin.getHubItem())) {
                p.getInventory().getItem(slot).setAmount(0);
            }

            if (plugin.getHubInventory().containsKey(p) && plugin.getHubInventory().get(p).contains(p.getOpenInventory().getTopInventory())) {
                p.closeInventory();
            }
        }
    }

    public void reload() {
        for (Player p : Bukkit.getOnlinePlayers()) {
            plugin.getHubInventory().put(p, plugin.getEmptyHubInventory());
            if (plugin.getConfiguration().getBoolean("item.show")) {
                p.getInventory().setItem(plugin.getConfiguration().getInt("item.slot-number"), plugin.getHubItem());
            }
        }
    }

    public void createFile() {
        plugin.getConfig().options().copyDefaults(false);
        plugin.getConfig().options().copyHeader(false);
        plugin.saveConfig();

        if (plugin.getConfig().get("host") == null) {
            plugin.getConfig().set("host", "localhost");
        }

        if (plugin.getConfig().get("port") == null) {
            plugin.getConfig().set("port", 3000);
        }

        if (plugin.getConfig().get("server-name") == null) {
            plugin.getConfig().set("server-name", "hub");
        }

        if (plugin.getConfig().get("queue-server") == null) {
            plugin.getConfig().set("queue-server", false);
        }

        plugin.saveConfig();
    }

    public void changeSlots(int slots) {
        try {
            Method serverGetHandle = plugin.getServer().getClass().getDeclaredMethod("getHandle");
            Object playerList = serverGetHandle.invoke(plugin.getServer());

            Field maxPlayersField = getMaxPlayersField(playerList);
            maxPlayersField.setInt(playerList, slots);

            updateServerProperties();
        } catch (ReflectiveOperationException e) {
            e.printStackTrace();
        }
    }

    private Field getMaxPlayersField(Object playerList) throws ReflectiveOperationException {
        Class<?> playerListClass = playerList.getClass().getSuperclass();

        try {
            Field field = playerListClass.getDeclaredField("maxPlayers");
            field.setAccessible(true);
            return field;
        } catch (NoSuchFieldException e) {
            for (Field field : playerListClass.getDeclaredFields()) {
                if (field.getType() != int.class) {
                    continue;
                }

                field.setAccessible(true);

                if (field.getInt(playerList) == Bukkit.getMaxPlayers()) {
                    return field;
                }
            }

            throw new NoSuchFieldException("Unable to find maxPlayers field in " + playerListClass.getName());
        }
    }

    private void updateServerProperties() {
        Properties properties = new Properties();
        File propertiesFile = new File("server.properties");

        try {
            try (InputStream is = new FileInputStream(propertiesFile)) {
                properties.load(is);
            }

            String maxPlayers = Integer.toString(plugin.getServer().getMaxPlayers());

            if (properties.getProperty("max-players").equals(maxPlayers)) {
                return;
            }

            properties.setProperty("max-players", maxPlayers);

            try (OutputStream os = new FileOutputStream(propertiesFile)) {
                properties.store(os, "Minecraft server properties");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
