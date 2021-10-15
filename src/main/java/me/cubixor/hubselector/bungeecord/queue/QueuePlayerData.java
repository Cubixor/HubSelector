package me.cubixor.hubselector.bungeecord.queue;

import java.time.LocalDateTime;

public class QueuePlayerData {

    private final LocalDateTime timeJoined;
    private final int joinPosition;


    public QueuePlayerData(LocalDateTime timeJoined, int joinPosition) {
        this.timeJoined = timeJoined;
        this.joinPosition = joinPosition;
    }

    public LocalDateTime getTimeJoined() {
        return timeJoined;
    }

    public int getJoinPosition() {
        return joinPosition;
    }

    @Override
    public String toString() {
        return "QueuePlayerData{" +
                "timeJoined=" + timeJoined +
                ", joinPosition=" + joinPosition +
                '}';
    }
}
