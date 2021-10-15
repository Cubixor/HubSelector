package me.cubixor.hubselector.bungeecord.socket;

import me.cubixor.hubselector.bungeecord.HubSelectorBungee;
import me.cubixor.hubselector.bungeecord.queue.QueueUtils;
import me.cubixor.hubselector.utils.SocketConnection;
import net.md_5.bungee.api.ChatColor;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class SocketServer {

    HubSelectorBungee plugin;

    public SocketServer() {
        plugin = HubSelectorBungee.getInstance();
    }


    public void serverSetup(int port) {
        plugin.getProxy().getScheduler().runAsync(plugin, () -> {
            try {
                ServerSocket socketServer = new ServerSocket(port);

                plugin.getProxy().getLogger().info("[HubSelector]" + ChatColor.GREEN + " Successfully started a socket server!");

                while (true) {
                    Socket socket = socketServer.accept();

                    ObjectOutputStream objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
                    ObjectInputStream objectInputStream = new ObjectInputStream(socket.getInputStream());
                    String server = objectInputStream.readUTF();

                    SocketConnection socketConnection = new SocketConnection(socket, objectInputStream, objectOutputStream);
                    plugin.getSpigotSocket().put(server, socketConnection);

                    plugin.getProxy().getLogger().info("[HubSelector]" + ChatColor.GREEN + " Successfully connected to " + server + " server!");

                    new SocketServerSender().getConfiguration(server);
                    new QueueUtils().putInHub();

                    serverReceive(server);

                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }


    private void serverReceive(String server) {
        plugin.getProxy().getScheduler().runAsync(plugin, () -> {
            try {

                new SocketServerReceiver().serverMessageReader(plugin.getSpigotSocket().get(server).getInputStream());

            } catch (IOException e) {
                plugin.getProxy().getLogger().info("[HubSelector]" + ChatColor.YELLOW + " Disconnected from " + server + " server!");
                plugin.getSpigotSocket().remove(server);
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        });
    }

}
