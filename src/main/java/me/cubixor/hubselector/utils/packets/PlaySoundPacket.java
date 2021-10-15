package me.cubixor.hubselector.utils.packets;

import java.io.Serializable;

public class PlaySoundPacket extends Packet implements Serializable {

    private final String player;
    private final String sound;
    private final float volume;
    private final float pitch;

    public PlaySoundPacket(String player, String sound, float volume, float pitch) {
        super(PacketType.PLAY_SOUND);
        this.player = player;
        this.sound = sound;
        this.volume = volume;
        this.pitch = pitch;
    }

    public String getPlayer() {
        return player;
    }

    public String getSound() {
        return sound;
    }

    public float getVolume() {
        return volume;
    }

    public float getPitch() {
        return pitch;
    }
}
