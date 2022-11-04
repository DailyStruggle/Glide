package io.github.dailystruggle.glide.Listeners;

import io.github.dailystruggle.glide.Glide;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;

public class OnDamage implements Listener {
    @EventHandler(priority = EventPriority.NORMAL)
    public void onDamage(EntityDamageEvent event) {
        if(!(event.getEntity() instanceof Player)) return;
        Player player = (Player) event.getEntity();
        if(Glide.getInvulnerablePlayers().contains(player.getUniqueId())) {
            event.setCancelled(true);
        }
    }
}
