package me.cubixor.hubselector.spigot.socket;

import me.cubixor.hubselector.spigot.HubSelector;
import me.cubixor.hubselector.utils.packets.MenuClickPacket;
import me.cubixor.hubselector.utils.packets.MenuClosePacket;
import me.cubixor.hubselector.utils.packets.MenuOpenPacket;
import org.bukkit.entity.Player;

import java.io.ObjectOutputStream;

public class SocketClientSender {

    HubSelector plugin;

    public SocketClientSender() {
        plugin = HubSelector.getInstance();
    }

    private ObjectOutputStream getOutputStream() {
        try {
            return plugin.getBungeeSocket().getOutputStream();
        } catch (Exception e) {
            return null;
        }
    }


    public void getHubInfo(Player player, String server) {
        try {
            ObjectOutputStream out = getOutputStream();
            if (out == null) return;

            MenuClickPacket menuClickPacket = new MenuClickPacket(player.getName(), server);

            out.writeObject(menuClickPacket);
            out.flush();

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void menuOpenMessage(Player player) {
        try {
            ObjectOutputStream out = getOutputStream();
            if (out == null) return;

            MenuOpenPacket menuOpenPacket = new MenuOpenPacket(player.getName());

            out.writeObject(menuOpenPacket);
            out.flush();

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void menuCloseMessage(Player player) {
        try {
            ObjectOutputStream out = getOutputStream();
            if (out == null) return;

            MenuClosePacket menuClosePacket = new MenuClosePacket(player.getName());

            out.writeObject(menuClosePacket);
            out.flush();

        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
