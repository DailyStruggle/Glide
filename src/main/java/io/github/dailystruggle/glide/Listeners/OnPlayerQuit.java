package io.github.dailystruggle.glide.Listeners;

import io.github.dailystruggle.glide.Glide;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.UUID;
import java.util.concurrent.ConcurrentSkipListSet;

public class OnPlayerQuit implements Listener {
    @EventHandler(priority = EventPriority.LOW)
    public void onPlayerQuit(PlayerQuitEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();
        ConcurrentSkipListSet<UUID> invulnerablePlayers = Glide.getInvulnerablePlayers();
        if(invulnerablePlayers.contains(uuid)) {
            event.getPlayer().setInvulnerable(false);
            invulnerablePlayers.remove(uuid);
        }

        Glide.getGlidingPlayers().remove(uuid);
    }
}
