package io.github.dailystruggle.glide.Listeners;

import io.github.dailystruggle.glide.Glide;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

public class OnFireworkUse implements Listener {
    @EventHandler(priority = EventPriority.HIGH)
    public void onFireworkUse(PlayerInteractEvent event) {
        if(!Glide.isGliding(event.getPlayer())) return;
        boolean rightClick = event.getAction().equals(Action.RIGHT_CLICK_AIR) || event.getAction().equals(Action.RIGHT_CLICK_BLOCK);
        if(!rightClick) return;
        if( event.getPlayer().getInventory().getChestplate() != null
            && event.getPlayer().getInventory().getChestplate().getType().equals(Material.ELYTRA))
            return;
        if(!Material.FIREWORK_ROCKET.equals(event.getMaterial())) return;
        if(event.getPlayer().hasPermission("glide.firework")) return;
        event.setCancelled(true);
    }
}
