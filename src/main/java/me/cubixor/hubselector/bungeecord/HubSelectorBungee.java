package me.cubixor.hubselector.bungeecord;

import me.cubixor.hubselector.bungeecord.queue.QueueMainBungee;
import me.cubixor.hubselector.bungeecord.queue.QueueSetupBungee;
import me.cubixor.hubselector.bungeecord.socket.SocketServer;
import me.cubixor.hubselector.bungeecord.socket.SocketServerSender;
import me.cubixor.hubselector.utils.Hub;
import me.cubixor.hubselector.utils.SocketConnection;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

public final class HubSelectorBungee extends Plugin {

    private static HubSelectorBungee instance;
    private final HashMap<ProxiedPlayer, Integer> menuUpdateTask = new HashMap<>();
    private final List<ProxiedPlayer> changeCooldown = new ArrayList<>();
    private final HashMap<String, SocketConnection> spigotSocket = new HashMap<>();
    private final HashMap<String, List<String>> serverSlots = new HashMap<>();
    private File configFile;
    private File hubServersFile;
    private File messagesFile;
    private Configuration config;
    private Configuration hubServers;
    private Configuration messages;
    private JoinMethods joinMethodsInstance;
    private VipJoinMethods vipJoinMethodsInstance;
    private ServerInfo queueServer;
    private LinkedList<Hub> hubs = new LinkedList<>();

    public static HubSelectorBungee getInstance() {
        return instance;
    }

    @Override
    public void onEnable() {
        instance = this;
        new QueueMainBungee().setInstance();

        getProxy().getPluginManager().registerListener(this, new HubMenuUpdate());
        getProxy().getPluginManager().registerListener(this, new ServerJoin());

        getProxy().getPluginManager().registerCommand(this, new HubCommand());
        getProxy().getPluginManager().registerCommand(this, new HubSelectorCommand());

        loadConfigs();

        new JoinMethod().setupJoinMethod();

        new SocketServer().serverSetup(getConfig().getInt("socket-server-port"));


        /*new UpdateCheckerBungeeCord(this, 73688).getVersion(version -> {
            if (!this.getDescription().getVersion().equalsIgnoreCase(version)) {
                getLogger().warning("There is a new update of HubSelector available! Go to spigotmc.org and download it!");
            }
        });*/

    }

    @Override
    public void onDisable() {
        getProxy().getScheduler().cancel(this);
    }

    public void loadConfigs() {
        if (!getDataFolder().exists()) {
            getDataFolder().mkdir();
        }
        configFile = new File(getDataFolder(), "config.yml");
        messagesFile = new File(getDataFolder(), "messages.yml");
        hubServersFile = new File(getDataFolder(), "hub-servers.yml");

        if (!configFile.exists()) {
            try (InputStream in = getResourceAsStream("config.yml")) {
                Files.copy(in, configFile.toPath());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (!messagesFile.exists()) {
            try (InputStream in = getResourceAsStream("messages.yml")) {
                Files.copy(in, messagesFile.toPath());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (!hubServersFile.exists()) {
            try (InputStream in = getResourceAsStream("hub-servers.yml")) {
                Files.copy(in, hubServersFile.toPath());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        try {
            config = ConfigurationProvider.getProvider(YamlConfiguration.class).load(new File(getDataFolder(), "config.yml"));
            messages = ConfigurationProvider.getProvider(YamlConfiguration.class).load(new File(getDataFolder(), "messages.yml"));
            hubServers = ConfigurationProvider.getProvider(YamlConfiguration.class).load(new File(getDataFolder(), "hub-servers.yml"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        setHubs(new LinkedList<>(getHubsFromFile()));

        if (getConfig().getBoolean("queue.use-queue")) {
            queueServer = getProxy().getServerInfo(getHubServers().getString("queue-server"));
            QueueMainBungee.getInstance().queueOnlineChecker(queueServer);
            new QueueSetupBungee().setup();
        }

        for (Hub hub : getHubs()) {
            new SocketServerSender().getConfiguration(HubUtils.getServerInfo(hub).getName());

            List<String> players = new ArrayList<>();
            for (ProxiedPlayer player : getProxy().getServerInfo(hub.getServer()).getPlayers()) {
                players.add(player.getName());
            }
            serverSlots.put(hub.getServer(), players);
        }
    }

    private LinkedList<Hub> getHubsFromFile() {
        LinkedList<Hub> hubs = new LinkedList<>();
        for (String s : hubServers.getSection("hub-servers").getKeys()) {
            Hub hub = new Hub(s,
                    hubServers.getString("hub-servers." + s + ".name"),
                    hubServers.getInt("hub-servers." + s + ".slots"),
                    hubServers.getBoolean("hub-servers." + s + ".vip"),
                    hubServers.getBoolean("hub-servers." + s + ".active"));
            hubs.add(hub);
        }
        return hubs;
    }

    public void saveHubServersFile() {
        try {
            ConfigurationProvider.getProvider(YamlConfiguration.class).save(hubServers, hubServersFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getMessageString(String path, String toReplace, String replacement) {
        String prefix = ChatColor.translateAlternateColorCodes('&', messages.getString("prefix"));
        String message = messages.getString(path).replace("%prefix%", prefix);
        String messageReplaced = message.replace(toReplace, replacement);
        return ChatColor.translateAlternateColorCodes('&', messageReplaced);
    }

    public BaseComponent[] getMessage(String path) {
        String prefix = ChatColor.translateAlternateColorCodes('&', messages.getString("prefix"));
        String message = messages.getString(path).replace("%prefix%", prefix);
        String colored = ChatColor.translateAlternateColorCodes('&', message);
        return TextComponent.fromLegacyText(colored);
    }

    public BaseComponent[] getMessage(String path, String toReplace, String replacement) {
        return getMessage(path, new String[]{toReplace}, new String[]{replacement});
    }

    public BaseComponent[] getMessage(String path, String[] toReplace, String[] replacement) {
        String prefix = ChatColor.translateAlternateColorCodes('&', messages.getString("prefix"));
        String message = messages.getString(path).replace("%prefix%", prefix);
        for (int i = 0; i < toReplace.length; i++) {
            message = message.replace(toReplace[i], replacement[i]);
        }
        String colored = ChatColor.translateAlternateColorCodes('&', message);
        if (colored.isEmpty()) {
            return null;
        } else {
            return TextComponent.fromLegacyText(colored);
        }
    }

    public List<String> getMessageList(String path) {
        List<String> message = new ArrayList<>(messages.getStringList(path));
        List<String> finalMessage = new ArrayList<>();
        for (String s : message) {
            finalMessage.add(ChatColor.translateAlternateColorCodes('&', s));
        }
        return finalMessage;
    }

    /*public  HubSelectorApiInterface getApi(){
        HubSelectorApiInterface hubSelectorApi = new HubSelectorApiInterface() {
            @Override
            public String hubConnect(ProxiedPlayer proxiedPlayer) {
                return "asd";
            }
        };
        return hubSelectorApi;
    }*/

    public LinkedList<Hub> getHubs() {
        return hubs;
    }

    public void setHubs(LinkedList<Hub> hubs) {
        this.hubs = hubs;
    }

    public Configuration getConfig() {
        return config;
    }

    public Configuration getMessages() {
        return messages;
    }

    public Configuration getHubServers() {
        return hubServers;
    }

    public File getConfigFile() {
        return configFile;
    }

    public File getMessagesFile() {
        return messagesFile;
    }

    public JoinMethods getJoinMethodsInstance() {
        return joinMethodsInstance;
    }

    public void setJoinMethodsInstance(JoinMethods joinMethodsInstance) {
        this.joinMethodsInstance = joinMethodsInstance;
    }

    public VipJoinMethods getVipJoinMethodsInstance() {
        return vipJoinMethodsInstance;
    }

    public void setVipJoinMethodsInstance(VipJoinMethods vipJoinMethodsInstance) {
        this.vipJoinMethodsInstance = vipJoinMethodsInstance;
    }

    public HashMap<ProxiedPlayer, Integer> getMenuUpdateTask() {
        return menuUpdateTask;
    }

    public List<ProxiedPlayer> getChangeCooldown() {
        return changeCooldown;
    }

    public HashMap<String, SocketConnection> getSpigotSocket() {
        return spigotSocket;
    }

    public ServerInfo getQueueServer() {
        return queueServer;
    }

    public void setQueueServer(ServerInfo queueServer) {
        this.queueServer = queueServer;
    }

    public HashMap<String, List<String>> getServerSlots() {
        return serverSlots;
    }
}
