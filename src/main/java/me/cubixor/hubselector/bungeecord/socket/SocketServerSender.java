package me.cubixor.hubselector.bungeecord.socket;

import me.cubixor.hubselector.bungeecord.HubSelectorBungee;
import me.cubixor.hubselector.bungeecord.HubUtils;
import me.cubixor.hubselector.utils.Hub;
import me.cubixor.hubselector.utils.HubData;
import me.cubixor.hubselector.utils.packets.AllHubsDataPacket;
import me.cubixor.hubselector.utils.packets.ConfigPacket;
import me.cubixor.hubselector.utils.packets.MenuOpenPacket;
import me.cubixor.hubselector.utils.packets.PlaySoundPacket;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.stream.Stream;

public class SocketServerSender {
    HubSelectorBungee plugin;

    public SocketServerSender() {
        plugin = HubSelectorBungee.getInstance();
    }

    private ObjectOutputStream getOutputStream(String server) {
        try {
            return plugin.getSpigotSocket().get(server).getOutputStream();
        } catch (Exception e) {
            return null;
        }
    }

    public void getConfiguration(String server) {
        try {
            ObjectOutputStream out = getOutputStream(server);
            if (out == null) return;

            StringBuilder configContentBuilder = new StringBuilder();
            try (Stream<String> stream = Files.lines(Paths.get(plugin.getConfigFile().getPath()), StandardCharsets.UTF_8)) {
                stream.forEach(s -> configContentBuilder.append(s).append("\n"));
            } catch (IOException e) {
                e.printStackTrace();
            }
            StringBuilder messagesContentBuilder = new StringBuilder();
            try (Stream<String> stream = Files.lines(Paths.get(plugin.getMessagesFile().getPath()), StandardCharsets.UTF_8)) {
                stream.forEach(s -> messagesContentBuilder.append(s).append("\n"));
            } catch (IOException e) {
                e.printStackTrace();
            }


            ConfigPacket configPacket = new ConfigPacket(
                    configContentBuilder.toString(),
                    messagesContentBuilder.toString(),
                    plugin.getHubs().size());

            out.writeObject(configPacket);
            out.flush();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void sendALlHubsInfo(ProxiedPlayer player) {
        try {
            ObjectOutputStream out = getOutputStream(player.getServer().getInfo().getName());
            if (out == null) return;


            LinkedList<HubData> hubDataList = new LinkedList<>();
            for (Hub hub : plugin.getHubs()) {
                hubDataList.add(HubUtils.setHubData(hub, player));
            }


            AllHubsDataPacket allHubsDataPacket = new AllHubsDataPacket(player.getName(), hubDataList);

            out.writeObject(allHubsDataPacket);
            out.flush();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void playSound(ProxiedPlayer player, String sound, float volume, float pitch) {
        try {
            ObjectOutputStream out = getOutputStream(player.getServer().getInfo().getName());
            if (out == null) return;

            PlaySoundPacket playSoundPacket = new PlaySoundPacket(player.getName(), sound, volume, pitch);

            out.writeObject(playSoundPacket);
            out.flush();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void openMenu(ProxiedPlayer player) {
        try {
            ObjectOutputStream out = getOutputStream(player.getServer().getInfo().getName());
            if (out == null) return;

            MenuOpenPacket menuOpenPacket = new MenuOpenPacket(player.getName());

            out.writeObject(menuOpenPacket);
            out.flush();

        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
