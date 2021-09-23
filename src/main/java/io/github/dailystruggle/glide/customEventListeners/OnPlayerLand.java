package io.github.dailystruggle.glide.customEventListeners;

import io.github.dailystruggle.glide.Glide;
import io.github.dailystruggle.glide.customEvents.PlayerLandEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import java.util.UUID;

public class OnPlayerLand implements Listener {
    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerLand(PlayerLandEvent event) {
        Player player = event.getPlayer();
        UUID playerID = player.getUniqueId();
        Glide.getGlidingPlayers().remove(playerID);
        if(Glide.getInvulnerablePlayers().contains(playerID)) {
            Bukkit.getScheduler().runTaskLater(Glide.getPlugin(),()->{
                player.setInvulnerable(false);
                Glide.getInvulnerablePlayers().remove(playerID);
            },10);
        }
        Glide.setLastLandTime(playerID,System.currentTimeMillis());
        Glide.getPlayerFallDistances().put(event.getPlayer().getUniqueId(),0D);
    }
}
