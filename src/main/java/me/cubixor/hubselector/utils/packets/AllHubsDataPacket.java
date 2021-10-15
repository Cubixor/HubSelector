package me.cubixor.hubselector.utils.packets;

import me.cubixor.hubselector.utils.HubData;

import java.io.Serializable;
import java.util.LinkedList;

public class AllHubsDataPacket extends Packet implements Serializable {

    private final String player;
    private final LinkedList<HubData> hubData;

    public AllHubsDataPacket(String player, LinkedList<HubData> hubDataList) {
        super(PacketType.HUB_DATA_ALL);
        this.player = player;
        this.hubData = hubDataList;
    }

    public String getPlayer() {
        return player;
    }

    public LinkedList<HubData> getHubData() {
        return hubData;
    }
}