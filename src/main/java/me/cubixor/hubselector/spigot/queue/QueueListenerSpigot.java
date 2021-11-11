package me.cubixor.hubselector.spigot.queue;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class QueueListenerSpigot implements Listener {

    @EventHandler
    public void onSpawn(CreatureSpawnEvent evt) {
        if (evt.getEntityType().equals(EntityType.ENDER_DRAGON)) {
            evt.setCancelled(true);
        }
    }

    @EventHandler
    public void onMove(PlayerMoveEvent evt) {
        if (evt.getFrom().getX() != evt.getTo().getX() || evt.getFrom().getY() != evt.getTo().getY() || evt.getFrom().getZ() != evt.getTo().getZ()) {
            evt.setCancelled(true);
            evt.getPlayer().setFlySpeed(0);
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent evt) {
        World world = Bukkit.getWorld("queue");
        Player player = evt.getPlayer();

        player.setGameMode(GameMode.SPECTATOR);
        player.teleport(new Location(world, 1000, 0, 1000));
        evt.setJoinMessage(null);
        player.setFlySpeed(0);
        player.setFlying(true);


        for (Player p : Bukkit.getOnlinePlayers()) {
            if (!p.equals(player)) {
                p.hidePlayer(player);
                player.hidePlayer(p);
            }
        }
    }


    @EventHandler
    public void onLeave(PlayerQuitEvent evt) {
        evt.setQuitMessage(null);
        World world = Bukkit.getWorld("queue");
        evt.getPlayer().teleport(new Location(world, 0, 0, 0));
    }
}
