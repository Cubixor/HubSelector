package me.cubixor.hubselector.spigot;

import java.util.Objects;

public class HubMenuPos {

    int menu;
    int slot;

    public HubMenuPos(int menu, int slot) {
        this.menu = menu;
        this.slot = slot;
    }

    public int getMenu() {
        return menu;
    }

    public int getSlot() {
        return slot;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        HubMenuPos that = (HubMenuPos) o;
        return menu == that.menu &&
                slot == that.slot;
    }

    @Override
    public int hashCode() {
        return Objects.hash(menu, slot);
    }
}
