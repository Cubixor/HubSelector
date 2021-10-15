package me.cubixor.hubselector.utils.packets;

import java.io.Serializable;

public class MenuClosePacket extends Packet implements Serializable {

    private final String player;

    public MenuClosePacket(String player) {
        super(PacketType.MENU_CLOSE);
        this.player = player;
    }

    public String getPlayer() {
        return player;
    }

}
