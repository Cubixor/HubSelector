package me.cubixor.hubselector.utils;

import java.io.Serializable;

public class Hub implements Serializable {

    private final String server;
    private String name;
    private int slots;
    private boolean vip;
    private boolean active;

    public Hub(Hub hub) {
        this.server = hub.getServer();
        this.name = hub.getName();
        this.slots = hub.getSlots();
        this.vip = hub.isVip();
        this.active = hub.isActive();
    }

    public Hub(String server, String name, int slots, boolean vip, boolean active) {
        this.server = server;
        this.name = name;
        this.slots = slots;
        this.vip = vip;
        this.active = active;
    }

    public String getServer() {
        return server;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getSlots() {
        return slots;
    }

    public void setSlots(int slots) {
        this.slots = slots;
    }

    public boolean isVip() {
        return vip;
    }

    public void setVip(boolean vip) {
        this.vip = vip;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    @Override
    public String toString() {
        return "Hub{" +
                "server='" + server + '\'' +
                ", name='" + name + '\'' +
                ", slots=" + slots +
                ", vip=" + vip +
                ", active=" + active +
                '}';
    }
}
