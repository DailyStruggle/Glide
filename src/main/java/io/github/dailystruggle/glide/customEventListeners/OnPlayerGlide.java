package io.github.dailystruggle.glide.customEventListeners;

import io.github.dailystruggle.glide.Glide;
import io.github.dailystruggle.glide.customEvents.PlayerGlideEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.permissions.PermissionAttachmentInfo;
import org.bukkit.potion.PotionEffectType;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentSkipListSet;

public class OnPlayerGlide implements Listener {
    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerGlide(PlayerGlideEvent event) {
        Player player = event.getPlayer();
        if(player.hasPotionEffect(PotionEffectType.SLOW_FALLING)) return;
        UUID playerId = player.getUniqueId();
        Glide.getGlidingPlayers().add(playerId);
        player.setFlying(false);
        player.setGliding(true);

        int invulnerabilityTime = 0;
        Set<PermissionAttachmentInfo> perms = player.getEffectivePermissions();
        for (PermissionAttachmentInfo perm : perms) {
            if(!perm.getValue()) continue;
            String node = perm.getPermission();
            if (node.startsWith("glide.invulnerable.")) {
                String[] val = node.split("\\.");
                if (val.length < 3 || val[2] == null || val[2].equals("")) continue;
                int number;
                try {
                    number = Integer.parseInt(val[2]);
                } catch (NumberFormatException exception) {
                    Bukkit.getLogger().warning("[rtp] invalid permission: " + node);
                    continue;
                }
                invulnerabilityTime = number;
                break;
            }
        }


        if(player.hasPermission("glide.invulnerable") && !player.isInvulnerable()) {
            player.setInvulnerable(true);
            Glide.getInvulnerablePlayers().add(playerId);
            if(invulnerabilityTime>0) {
                Bukkit.getScheduler().runTaskLater(Glide.getPlugin(),()->{
                    ConcurrentSkipListSet<UUID> invulnerablePlayers = Glide.getInvulnerablePlayers();
                    if(invulnerablePlayers.contains(playerId)) {
                        player.setInvulnerable(false);
                        invulnerablePlayers.remove(playerId);
                    }
                }, 20L * invulnerabilityTime);
            }
        }
    }
}
