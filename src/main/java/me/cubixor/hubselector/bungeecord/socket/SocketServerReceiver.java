package me.cubixor.hubselector.bungeecord.socket;

import me.cubixor.hubselector.bungeecord.HubInventory;
import me.cubixor.hubselector.bungeecord.HubMenuUpdate;
import me.cubixor.hubselector.bungeecord.HubSelectorBungee;
import me.cubixor.hubselector.bungeecord.HubUtils;
import me.cubixor.hubselector.utils.packets.MenuClickPacket;
import me.cubixor.hubselector.utils.packets.MenuClosePacket;
import me.cubixor.hubselector.utils.packets.MenuOpenPacket;
import me.cubixor.hubselector.utils.packets.Packet;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.io.IOException;
import java.io.ObjectInputStream;

public class SocketServerReceiver {
    HubSelectorBungee plugin;

    public SocketServerReceiver() {
        plugin = HubSelectorBungee.getInstance();
    }


    public void serverMessageReader(ObjectInputStream in) throws IOException, ClassNotFoundException {
        while (true) {

            Object object = in.readObject();
            Packet packet = (Packet) object;


            switch (packet.getPacketType()) {
                case MENU_CLICK: {
                    MenuClickPacket menuClickPacket = (MenuClickPacket) object;

                    String server = menuClickPacket.getServer();
                    ProxiedPlayer player = plugin.getProxy().getPlayer(menuClickPacket.getPlayer());

                    new HubInventory().inventoryClickData(player, HubUtils.getHub(server), true);
                    break;
                }
                case MENU_OPEN: {
                    MenuOpenPacket menuOpenPacket = (MenuOpenPacket) object;

                    ProxiedPlayer player = plugin.getProxy().getPlayer(menuOpenPacket.getPlayer());

                    if (!plugin.getConfig().getBoolean("menu-update.update-on-open-only")) {
                        new HubMenuUpdate().updateMenu(player);
                    } else {
                        new SocketServerSender().sendALlHubsInfo(player);
                    }
                    break;
                }
                case MENU_CLOSE: {
                    MenuClosePacket menuClosePacket = (MenuClosePacket) object;

                    ProxiedPlayer player = plugin.getProxy().getPlayer(menuClosePacket.getPlayer());

                    if (plugin.getMenuUpdateTask().get(player) != null) {
                        plugin.getProxy().getScheduler().cancel(plugin.getMenuUpdateTask().get(player));
                        plugin.getMenuUpdateTask().remove(player);
                    }

                    break;
                }
            }
        }

    }
}