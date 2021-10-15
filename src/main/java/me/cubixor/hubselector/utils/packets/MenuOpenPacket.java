package me.cubixor.hubselector.utils.packets;

import java.io.Serializable;

public class MenuOpenPacket extends Packet implements Serializable {

    private final String player;

    public MenuOpenPacket(String player) {
        super(PacketType.MENU_OPEN);
        this.player = player;
    }

    public String getPlayer() {
        return player;
    }
}
