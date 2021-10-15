package me.cubixor.hubselector.utils.packets;

import java.io.Serializable;

public class ConfigPacket extends Packet implements Serializable {

    private final String config, messages;
    private final int serverCount;

    public ConfigPacket(String config, String messages, int serverCount) {
        super(PacketType.CONFIG);
        this.config = config;
        this.messages = messages;
        this.serverCount = serverCount;
    }

    public String getConfig() {
        return config;
    }

    public String getMessages() {
        return messages;
    }

    public int getServerCount() {
        return serverCount;
    }
}
