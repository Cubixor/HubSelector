package me.cubixor.hubselector.utils.packets;

import java.io.Serializable;

public class Packet implements Serializable {

    private final PacketType packetType;

    public Packet(PacketType packetType) {
        this.packetType = packetType;
    }

    public PacketType getPacketType() {
        return packetType;
    }

}
