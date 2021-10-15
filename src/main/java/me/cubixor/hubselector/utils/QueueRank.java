package me.cubixor.hubselector.utils;

import java.io.Serializable;
import java.util.Objects;

public class QueueRank implements Comparable<QueueRank>, Serializable {

    private final String name;
    private final String permission;
    private final Integer priority;
    private final String displayName;

    public QueueRank(String name, String permission, int priority, String displayName) {
        this.name = name;
        this.permission = permission;
        this.priority = priority;
        this.displayName = displayName;
    }

    public String getName() {
        return name;
    }

    public String getPermission() {
        return permission;
    }

    public Integer getPriority() {
        return priority;
    }

    public String getDisplayName() {
        return displayName;
    }

    @Override
    public int compareTo(QueueRank queueRank) {
        return getPriority().compareTo(queueRank.getPriority());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        QueueRank queueRank = (QueueRank) o;
        return name.equals(queueRank.name) && permission.equals(queueRank.permission) && priority.equals(queueRank.priority);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, permission, priority);
    }

    @Override
    public String toString() {
        return "QueueRank{" +
                "name='" + name + '\'' +
                ", permission='" + permission + '\'' +
                ", priority=" + priority +
                ", displayName='" + displayName + '\'' +
                '}';
    }
}
