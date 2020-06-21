package me.cubixor.hubselector.spigot;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

public class HubSelector extends JavaPlugin {

    public ItemStack hubItem;
    public HashMap<Player, LinkedList<Inventory>> hubInventory = new HashMap<>();
    public HashMap<Inventory, HashMap<Integer, String>> serverSlot = new HashMap<>();
    public boolean setup;
    FileConfiguration config;
    FileConfiguration messagesConfig;
    LinkedList<Inventory> emptyHubInventory = new LinkedList<>();

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(new HubItem(this), this);
        getServer().getPluginManager().registerEvents(new HubMenu(this), this);


        ConfigurationBungee configurationBungee = new ConfigurationBungee(this);
        getServer().getMessenger().registerIncomingPluginChannel(this, "bungee:config", configurationBungee);
        getServer().getMessenger().registerIncomingPluginChannel(this, "bungee:hub", configurationBungee);
        getServer().getPluginManager().registerEvents(configurationBungee, this);


        /*new UpdateCheckerSpigot(this, 73688).getVersion(version -> {
            if (!this.getDescription().getVersion().equalsIgnoreCase(version)) {
                getLogger().warning("There is a new update of HubSelector available! Go to spigotmc.org and download it!");
            }
        });*/

    }

    @Override
    public void onDisable() {
        getServer().getMessenger().unregisterIncomingPluginChannel(this, "bungee:config");
        getServer().getMessenger().unregisterOutgoingPluginChannel(this, "bungee:config");

    }

    public String getMessage(String path) {
        String prefix = ChatColor.translateAlternateColorCodes('&', messagesConfig.getString("prefix"));
        String message = messagesConfig.getString(path).replace("%prefix%", prefix);
        return ChatColor.translateAlternateColorCodes('&', message);
    }

    public List<String> getMessageList(String path) {
        List<String> message = new ArrayList<>(messagesConfig.getStringList(path));
        List<String> finalMessage = new ArrayList<>();
        for (String s : message) {
            finalMessage.add(ChatColor.translateAlternateColorCodes('&', s));
        }
        return finalMessage;
    }

    public FileConfiguration getConfiguration() {
        return config;
    }
}
