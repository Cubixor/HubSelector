package me.cubixor.hubselector.utils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class HubData extends Hub implements Serializable {

    private final List<String> players = new ArrayList<>();
    private final boolean online;
    private final boolean current;
    private final boolean full;
    private final boolean vipPlayer;

    public HubData(Hub hub, List<String> players, boolean online, boolean current, boolean full, boolean vipPlayer) {
        super(hub);
        this.players.addAll(players);
        this.online = online;
        this.current = current;
        this.full = full;
        this.vipPlayer = vipPlayer;
    }

    public List<String> getPlayers() {
        return players;
    }

    public boolean isOnline() {
        return online;
    }

    public boolean isCurrent() {
        return current;
    }

    public boolean isFull() {
        return full;
    }

    public boolean isPlayerVip() {
        return vipPlayer;
    }

}
