package io.github.dailystruggle.glide.Listeners;

import io.github.dailystruggle.glide.Glide;
import io.github.dailystruggle.glide.customEvents.PlayerLandEvent;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityToggleGlideEvent;

public class OnGlideToggle implements Listener {
    @EventHandler(priority = EventPriority.LOWEST)
    public void onGlideToggle(EntityToggleGlideEvent event) {
        if(!(event.getEntity() instanceof Player)) return;
        if(!Glide.getGlidingPlayers().contains(event.getEntity().getUniqueId())) return;
        Block block = event.getEntity().getLocation().getBlock().getRelative(BlockFace.DOWN);
        if(block.getType().isSolid() || block.isLiquid() || ((Player) event.getEntity()).isFlying()) {
            Bukkit.getPluginManager().callEvent(new PlayerLandEvent((Player) event.getEntity()));
        }
        else event.setCancelled(true);
    }
}
