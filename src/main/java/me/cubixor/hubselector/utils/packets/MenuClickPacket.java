package me.cubixor.hubselector.utils.packets;

import java.io.Serializable;

public class MenuClickPacket extends Packet implements Serializable {

    private final String player, server;

    public MenuClickPacket(String player, String server) {
        super(PacketType.MENU_CLICK);
        this.player = player;
        this.server = server;
    }

    public String getPlayer() {
        return player;
    }

    public String getServer() {
        return server;
    }
}
