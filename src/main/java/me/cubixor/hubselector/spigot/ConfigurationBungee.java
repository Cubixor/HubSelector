package me.cubixor.hubselector.spigot;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import org.apache.commons.lang3.SerializationUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.Reader;
import java.io.StringReader;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

public class ConfigurationBungee implements org.bukkit.plugin.messaging.PluginMessageListener, Listener {

    private final HubSelector plugin;

    public ConfigurationBungee(HubSelector hs) {
        plugin = hs;
    }

    @Override
    public void onPluginMessageReceived(String channel, Player player, byte[] bytes) {
        if (!channel.equalsIgnoreCase("bungee:config") && !channel.equalsIgnoreCase("bungee:hub")) {
            return;
        }


        ByteArrayDataInput in = ByteStreams.newDataInput(bytes);

        if (channel.equalsIgnoreCase("bungee:config")) {
            if (plugin.setup) {
                reload();
            }

            String configStr = in.readUTF();
            String messagesStr = in.readUTF();

            Reader configReader = new StringReader(configStr);
            Reader messagesReader = new StringReader(messagesStr);

            plugin.config = org.bukkit.configuration.file.YamlConfiguration.loadConfiguration(configReader);

            plugin.messagesConfig = org.bukkit.configuration.file.YamlConfiguration.loadConfiguration(messagesReader);

            setup();

            plugin.setup = true;

        }
        if (channel.equalsIgnoreCase("bungee:hub")) {
            LinkedHashMap<String, HashMap<InetSocketAddress, List<String>>> servers = new LinkedHashMap<>(SerializationUtils.deserialize(bytes));

            new HubMenu(plugin).updateMenuData(servers, player);
        }
    }

    public void getHubInfo(Player player, String server) {
        if (!plugin.getServer().getMessenger().isOutgoingChannelRegistered(plugin, "bungee:hun")) {
            plugin.getServer().getMessenger().registerOutgoingPluginChannel(plugin, "bungee:hub");
        }
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF("GetInfo");
        out.writeUTF(server);
        player.sendPluginMessage(plugin, "bungee:hub", out.toByteArray());

    }

    public void menuOpenMessage(Player player) {
        if (!plugin.getServer().getMessenger().isOutgoingChannelRegistered(plugin, "bungee:hun")) {
            plugin.getServer().getMessenger().registerOutgoingPluginChannel(plugin, "bungee:hub");
        }
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF("MenuOpen");
        player.sendPluginMessage(plugin, "bungee:hub", out.toByteArray());
    }

    public void menuCloseMessage(Player player) {
        if (!plugin.getServer().getMessenger().isOutgoingChannelRegistered(plugin, "bungee:hun")) {
            plugin.getServer().getMessenger().registerOutgoingPluginChannel(plugin, "bungee:hub");
        }
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF("MenuClose");
        player.sendPluginMessage(plugin, "bungee:hub", out.toByteArray());

    }


    public void setup() {
        plugin.hubItem = new ItemStack(Material.matchMaterial(plugin.getConfiguration().getString("item.type")));
        ItemMeta hubItemMeta = plugin.hubItem.getItemMeta();
        hubItemMeta.setDisplayName(plugin.getMessage("item.hub-item-name"));
        List<String> hubItemLore = new ArrayList<>(plugin.getMessageList("item.hub-item-lore"));
        hubItemMeta.setLore(hubItemLore);
        plugin.hubItem.setItemMeta(hubItemMeta);

        HubItem hubItem = new HubItem(plugin);

        hubItem.setupMenu();
        for (Player p : Bukkit.getOnlinePlayers()) {
            hubItem.addMenu(p);
        }
    }

    private void reload() {
        if (plugin.hubItem != null) {
            int slot = plugin.getConfiguration().getInt("item.slot-number");
            for (Player p : Bukkit.getOnlinePlayers()) {
                if (p.getInventory().getItem(slot) != null && p.getInventory().getItem(slot).equals(plugin.hubItem)) {
                    p.getInventory().getItem(slot).setAmount(0);
                }
                p.getOpenInventory();
                if (p.getOpenInventory().getTopInventory().equals(plugin.hubInventory.get(p))) {
                    p.closeInventory();
                }
            }
        }

    }
}
