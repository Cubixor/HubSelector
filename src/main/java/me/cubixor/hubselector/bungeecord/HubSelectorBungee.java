package me.cubixor.hubselector.bungeecord;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
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
import java.util.List;

public final class HubSelectorBungee extends Plugin {

    File configFile;
    File messagesFile;
    Configuration config;
    Configuration messages;

    JoinMethod.JoinMethods joinMethodsInstance;
    JoinMethod.VipJoinMethods vipJoinMethodsInstance;

    HashMap<ProxiedPlayer, Integer> taskId = new HashMap<>();

    @Override
    public void onEnable() {
        getProxy().registerChannel("bungee:config");
        getProxy().registerChannel("bungee:hub");
        getProxy().getPluginManager().registerListener(this, new BungeeChannel(this));
        getProxy().getPluginManager().registerListener(this, new HubMenuUpdate(this));
        getProxy().getPluginManager().registerListener(this, new ServerJoin(this));


        getProxy().getPluginManager().registerCommand(this, new HubCommand(this));
        getProxy().getPluginManager().registerCommand(this, new HubSelectorCommand(this));

        loadConfigs();

        new JoinMethod(this).joinMethodSetup();

        /*new UpdateCheckerBungeeCord(this, 73688).getVersion(version -> {
            if (!this.getDescription().getVersion().equalsIgnoreCase(version)) {
                getLogger().warning("There is a new update of HubSelector available! Go to spigotmc.org and download it!");
            }
        });*/

    }


    @Override
    public void onDisable() {
        getProxy().unregisterChannel("bungee:config");
        getProxy().unregisterChannel("bungee:hub");
    }

    public void loadConfigs() {
        if (!getDataFolder().exists()) {
            getDataFolder().mkdir();
        }
        configFile = new File(getDataFolder(), "config.yml");
        messagesFile = new File(getDataFolder(), "messages.yml");

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

        try {
            config = ConfigurationProvider.getProvider(YamlConfiguration.class).load(new File(getDataFolder(), "config.yml"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            messages = ConfigurationProvider.getProvider(YamlConfiguration.class).load(new File(getDataFolder(), "messages.yml"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Configuration getConfig() {
        return config;
    }

    public BaseComponent[] getMessage(String path) {
        String prefix = ChatColor.translateAlternateColorCodes('&', messages.getString("prefix"));
        String message = messages.getString(path).replace("%prefix%", prefix);
        String colored = ChatColor.translateAlternateColorCodes('&', message);
        return TextComponent.fromLegacyText(colored);
    }

    public List<String> getMessageList(String path) {
        List<String> message = new ArrayList<>(messages.getStringList(path));
        List<String> finalMessage = new ArrayList<>();
        for (String s : message) {
            finalMessage.add(ChatColor.translateAlternateColorCodes('&', s));
        }
        return finalMessage;
    }

    public BaseComponent[] getMessage(String path, String serverReplace) {
        String prefix = ChatColor.translateAlternateColorCodes('&', messages.getString("prefix"));
        String message = messages.getString(path).replace("%prefix%", prefix);
        message = message.replace("%hub%", getConfig().getString("hub-servers." + serverReplace + ".name"));
        String colored = ChatColor.translateAlternateColorCodes('&', message);
        return TextComponent.fromLegacyText(colored);
    }

}
