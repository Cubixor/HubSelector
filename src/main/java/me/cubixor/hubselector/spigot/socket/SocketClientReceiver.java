package me.cubixor.hubselector.spigot.socket;

import com.cryptomorin.xseries.XSound;
import me.cubixor.hubselector.spigot.HubMenu;
import me.cubixor.hubselector.spigot.HubSelector;
import me.cubixor.hubselector.spigot.SetupSpigot;
import me.cubixor.hubselector.utils.HubData;
import me.cubixor.hubselector.utils.packets.*;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Reader;
import java.io.StringReader;
import java.util.LinkedList;

public class SocketClientReceiver {

    HubSelector plugin;

    public SocketClientReceiver() {
        plugin = HubSelector.getInstance();
    }


    public void clientMessageReader(ObjectInputStream in) {


        while (true) {
            Object object;

            try {
                object = in.readObject();
            } catch (IOException e) {
                plugin.getLogger().warning(ChatColor.YELLOW + "Disconnected from the bungeecord server. Plugin will try to reconnect until it succeeds.");
                new SocketClient().clientSetup(plugin.getConfig().getString("host"),
                        plugin.getConfig().getInt("port"),
                        plugin.getConfig().getString("server-name"));
                return;
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
                return;
            }
            Packet packet = (Packet) object;


            switch (packet.getPacketType()) {
                case CONFIG: {
                    ConfigPacket configPacket = (ConfigPacket) object;

                    String configStr = configPacket.getConfig();
                    String messagesStr = configPacket.getMessages();

                    Reader configReader = new StringReader(configStr);
                    Reader messagesReader = new StringReader(messagesStr);

                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            SetupSpigot setupSpigot = new SetupSpigot();

                            if (plugin.isSetup()) {
                                setupSpigot.unload();
                            }

                            plugin.setConfiguration(YamlConfiguration.loadConfiguration(configReader));
                            plugin.setMessagesConfig(YamlConfiguration.loadConfiguration(messagesReader));

                            if (!plugin.isSetup()) {
                                setupSpigot.registerMethods();
                            }

                            setupSpigot.itemSetup();
                            setupSpigot.menuSetup(configPacket.getServerCount());

                            if (plugin.isSetup()) {
                                setupSpigot.reload();
                            }

                            plugin.setSetup(true);
                        }
                    }.runTask(plugin);
                    break;
                }
                case HUB_DATA_ALL: {
                    AllHubsDataPacket allHubsDataPacket = (AllHubsDataPacket) object;

                    Player player = Bukkit.getPlayerExact(allHubsDataPacket.getPlayer());

                    LinkedList<HubData> hubData = new LinkedList<>(allHubsDataPacket.getHubData());

                    new HubMenu().menuDataSet(hubData, player);

                    break;
                }
                case PLAY_SOUND: {
                    PlaySoundPacket playSoundPacket = (PlaySoundPacket) object;

                    Player player = Bukkit.getPlayerExact(playSoundPacket.getPlayer());

                    String sound = playSoundPacket.getSound();
                    float volume = playSoundPacket.getVolume();
                    float pitch = playSoundPacket.getPitch();

                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            player.playSound(player.getLocation(), XSound.matchXSound(sound).get().parseSound(), volume, pitch);
                        }
                    }.runTask(plugin);
                    break;
                }
                case MENU_OPEN: {
                    MenuOpenPacket menuOpenPacket = (MenuOpenPacket) object;

                    Player player = Bukkit.getPlayerExact(menuOpenPacket.getPlayer());

                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            HubMenu.openMenu(player);
                        }
                    }.runTask(plugin);
                    break;
                }
            }

        }
    }
}
