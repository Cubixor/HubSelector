package me.cubixor.hubselector.spigot.socket;

import me.cubixor.hubselector.spigot.HubSelector;
import me.cubixor.hubselector.utils.SocketConnection;
import org.bukkit.ChatColor;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class SocketClient {

    HubSelector plugin;

    public SocketClient() {
        plugin = HubSelector.getInstance();
    }

    public void clientSetup(String host, int port, String server) {
        if (!plugin.isEnabled()) {
            return;
        }
        final boolean msgSent = plugin.getBungeeSocket() != null;
        plugin.setBungeeSocket(null);

        new BukkitRunnable() {
            @Override
            public void run() {
                boolean done = false;
                boolean msg = msgSent;
                while (!done && plugin.isEnabled()) {
                    done = clientConnect(host, port, server);
                    if (!msg && !done) {
                        msg = true;
                        plugin.getLogger().warning(ChatColor.YELLOW + "Couldn't connect to the bungeecord server. Plugin will try to connect until it succeeds.");

                    }
                }
            }
        }.runTaskAsynchronously(plugin);
    }

    private boolean clientConnect(String host, int port, String server) {
        try {
            Socket sock = new Socket(host, port);

            ObjectInputStream objectInputStream = new ObjectInputStream(sock.getInputStream());
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(sock.getOutputStream());
            objectOutputStream.writeUTF(server);
            objectOutputStream.flush();

            SocketConnection socketConnectionSpigot = new SocketConnection(sock, objectInputStream, objectOutputStream);
            plugin.setBungeeSocket(socketConnectionSpigot);

            clientReceive();

            plugin.getLogger().info(ChatColor.GREEN + "Successfully connected to a bungeecord server!");

            return true;
        } catch (IOException ignored) {
            return false;
        }
    }

    private void clientReceive() {
        new BukkitRunnable() {

            @Override
            public void run() {
                new SocketClientReceiver().clientMessageReader(plugin.getBungeeSocket().getInputStream());

            }

        }.runTaskAsynchronously(plugin);

    }
}
