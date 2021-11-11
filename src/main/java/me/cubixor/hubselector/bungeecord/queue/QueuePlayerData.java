package me.cubixor.hubselector.bungeecord.queue;

public class QueuePlayerData {

    private final long timeJoined;
    private final int joinPosition;


    public QueuePlayerData(long timeJoined, int joinPosition) {
        this.timeJoined = timeJoined;
        this.joinPosition = joinPosition;
    }

    public long getTimeJoined() {
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
