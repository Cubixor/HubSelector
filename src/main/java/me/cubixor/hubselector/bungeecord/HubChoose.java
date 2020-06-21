package me.cubixor.hubselector.bungeecord;

import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.*;

public class HubChoose {

    HubSelectorBungee plugin;


    public HubChoose(HubSelectorBungee hsb) {
        plugin = hsb;
    }

    public String connectToHub(ProxiedPlayer player, boolean vipPlayer, boolean command) {

        if (isInHub(player)) {
            return null;
        }

        List<String> hubServers = new ArrayList<>(plugin.getConfig().getSection("hub-servers").getKeys());
        LinkedList<String> availableServers = new LinkedList<>();
        LinkedList<String> availableVipServers = new LinkedList<>();


        for (String server : hubServers) {

            if (!checkIfAvailable(server, vipPlayer, player)) {
                continue;
            }

            if (checkIfVip(server)) {
                availableVipServers.add(server);
            } else {
                availableServers.add(server);
            }
        }

        if ((availableServers.size() == 0 && !vipPlayer) || (availableVipServers.size() == 0 && vipPlayer && availableServers.size() == 0)) {
            if (!plugin.getConfig().getBoolean("kick-if-full") && command) {
                player.sendMessage(plugin.getMessage("connect.full"));
            } else {
                player.disconnect(plugin.getMessage("join.kick-full"));
            }
            return null;
        }


        String chosenServer;

        if (vipPlayer) {
            chosenServer = plugin.vipJoinMethodsInstance.method(availableServers, availableVipServers);
        } else {
            chosenServer = plugin.joinMethodsInstance.method(availableServers);
        }


        return chosenServer;
    }

    public void chooseHub(ProxiedPlayer player, String target, String server, boolean vip) {
        if (plugin.getProxy().getPlayer(target) == null) {
            player.sendMessage(plugin.getMessage("command.invalid-player"));
            return;
        }

        if (server.equalsIgnoreCase("*")) {
            if (isInHub(player)) {
                return;
            }
            connectToHub(player, vip, true);
            return;
        }

        boolean available = checkIfAvailable(server, vip, plugin.getProxy().getPlayer(target));

        if (available) {
            player.sendMessage(plugin.getMessage("connect.success", server));
            plugin.getProxy().getPlayer(target).connect(plugin.getProxy().getServerInfo(server));
        } else {
            player.sendMessage(plugin.getMessage("command.invalid-hub"));
        }
    }

    public void hubCheck(ProxiedPlayer player, boolean vip) {
        if (isInHub(player)) {
            return;
        }

        String server = connectToHub(player, vip, true);

        if (server != null) {
            player.sendMessage(plugin.getMessage("connect.success", server));
            player.connect(plugin.getProxy().getServerInfo(server));
        }

    }

    private boolean isInHub(ProxiedPlayer player) {
        List<String> hubServers = new ArrayList<>(plugin.getConfig().getSection("hub-servers").getKeys());

        for (String s : hubServers) {
            if (plugin.getProxy().getServerInfo(s) != null && plugin.getProxy().getServerInfo(s).getPlayers().contains(player)) {
                player.sendMessage(plugin.getMessage("command.already-in-hub"));
                return true;
            }
        }
        return false;
    }

    public boolean checkIfOnline(String ip, int port) {
        try {
            Socket s = new Socket();
            s.connect(new InetSocketAddress(ip, port), 20);
            s.close();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean checkIfFull(String server) {
        return plugin.getProxy().getServerInfo(server).getPlayers().size() >= plugin.getConfig().getInt("hub-servers." + server + ".slots");
    }

    public boolean checkIfVip(String server) {
        return plugin.getConfig().getBoolean("hub-servers." + server + ".vip");
    }

    public LinkedHashMap<String, Integer> sortByValue(HashMap<String, Integer> hm) {
        List<Map.Entry<String, Integer>> list =
                new LinkedList<Map.Entry<String, Integer>>(hm.entrySet());

        Collections.sort(list, new Comparator<Map.Entry<String, Integer>>() {
            public int compare(Map.Entry<String, Integer> o1,
                               Map.Entry<String, Integer> o2) {
                return (o1.getValue()).compareTo(o2.getValue());
            }
        });

        LinkedHashMap<String, Integer> temp = new LinkedHashMap<String, Integer>();
        for (Map.Entry<String, Integer> aa : list) {
            temp.put(aa.getKey(), aa.getValue());
        }
        return temp;
    }

    public boolean checkIfAvailable(String server, boolean vipPlayer, ProxiedPlayer player) {
        InetSocketAddress serverAddress = plugin.getProxy().getServerInfo(server).getAddress();

        boolean online = checkIfOnline(serverAddress.getHostName(), serverAddress.getPort());

        if (!online) {
            return false;
        }

        boolean full = checkIfFull(server);

        if (full) {
            return false;
        }

        boolean current = plugin.getProxy().getServerInfo(server).getPlayers().contains(player);

        if (current) {
            return false;
        }

        boolean vip = checkIfVip(server);

        return vipPlayer || !vip;
    }

}
