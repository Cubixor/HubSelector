package me.cubixor.hubselector.spigot;

import me.cubixor.hubselector.spigot.queue.QueueSetupSpigot;
import me.cubixor.hubselector.spigot.socket.SocketClient;
import me.cubixor.hubselector.utils.SocketConnection;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

public class HubSelector extends JavaPlugin {

    private static HubSelector instance;
    private ItemStack hubItem;
    private HashMap<Player, LinkedList<Inventory>> hubInventory = new HashMap<>();
    private HashMap<HubMenuPos, String> slot = new HashMap<>();
    private LinkedList<Inventory> emptyHubInventory = new LinkedList<>();
    private boolean setup;
    private FileConfiguration config;
    private FileConfiguration messagesConfig;
    private SocketConnection bungeeSocket;

    public static HubSelector getInstance() {
        return instance;
    }

    @Override
    public void onEnable() {
        instance = this;

        /*new UpdateCheckerSpigot(this, 73688).getVersion(version -> {
            if (!this.getDescription().getVersion().equalsIgnoreCase(version)) {
                getLogger().warning("There is a new update of HubSelector available! Go to spigotmc.org and download it!");
            }
        });*/


        SetupSpigot setupSpigot = new SetupSpigot();
        if (getServer().getMaxPlayers() != Integer.MAX_VALUE) {
            setupSpigot.changeSlots();
        }
        setupSpigot.createFile();

        if (!getConfig().getBoolean("queue-server")) {
            new SocketClient().clientSetup(getConfig().getString("host"), getConfig().getInt("port"), getConfig().getString("server-name"));
        } else {
            new QueueSetupSpigot().registerMethods();
        }
    }

    @Override
    public void onDisable() {
        Bukkit.getServer().getScheduler().cancelTasks(this);
        if (getConfig().getBoolean("queue-server")) {
            new QueueSetupSpigot().disable();
        } else {
            try {
                getBungeeSocket().getSocket().close();
            } catch (IOException ignored) {
            }
        }
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

    public void setConfiguration(FileConfiguration config) {
        this.config = config;
    }

    public void setMessagesConfig(FileConfiguration messagesConfig) {
        this.messagesConfig = messagesConfig;
    }

    public ItemStack getHubItem() {
        return hubItem;
    }

    public void setHubItem(ItemStack hubItem) {
        this.hubItem = hubItem;
    }

    public HashMap<Player, LinkedList<Inventory>> getHubInventory() {
        return hubInventory;
    }

    public void setHubInventory(HashMap<Player, LinkedList<Inventory>> hubInventory) {
        this.hubInventory = hubInventory;
    }

    public HashMap<HubMenuPos, String> getSlot() {
        return slot;
    }

    public void setSlot(HashMap<HubMenuPos, String> slot) {
        this.slot = slot;
    }

    public LinkedList<Inventory> getEmptyHubInventory() {
        return emptyHubInventory;
    }

    public void setEmptyHubInventory(LinkedList<Inventory> emptyHubInventory) {
        this.emptyHubInventory = emptyHubInventory;
    }

    public boolean isSetup() {
        return setup;
    }

    public void setSetup(boolean setup) {
        this.setup = setup;
    }

    public SocketConnection getBungeeSocket() {
        return bungeeSocket;
    }

    public void setBungeeSocket(SocketConnection bungeeSocket) {
        this.bungeeSocket = bungeeSocket;
    }
}
