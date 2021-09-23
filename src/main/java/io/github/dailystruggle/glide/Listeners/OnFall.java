package io.github.dailystruggle.glide.Listeners;

import io.github.dailystruggle.glide.Glide;
import io.github.dailystruggle.glide.customEvents.PlayerGlideEvent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.permissions.PermissionAttachmentInfo;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class OnFall implements Listener {
    @EventHandler(priority = EventPriority.LOW)
    public void onFall(PlayerMoveEvent event) {
        ConcurrentHashMap<UUID,Double> playerFallDistances = Glide.getPlayerFallDistances();
        Player player = event.getPlayer();
        Location to = event.getTo();
        if(to == null) return;
        if(player.isFlying() || player.isGliding() || player.isOnGround() || player.isSwimming()
                || !to.getBlock().isEmpty() || !player.hasPermission("glide.fall")) {
            Glide.getPlayerFallDistances().put(player.getUniqueId(),0D);
            return;
        }
        UUID playerId = player.getUniqueId();
        if(System.currentTimeMillis()- Glide.getLastLandTime(playerId) < 500) return;
        to = to.clone();
        to.subtract(event.getFrom());
        if(to.getY()>0D) {
            try {
                playerFallDistances.computeIfPresent(playerId,(uuid, aLong) -> aLong=0D);
            }
            catch (NullPointerException e) {
                return;
            }
            return;
        }

        if(to.getY()<0) {
            try {
                playerFallDistances.putIfAbsent(playerId,0D);
            }
            catch (NullPointerException e) {
                return;
            }
            Location finalDiff = to;
            playerFallDistances.compute(playerId,(uuid, aDouble) -> aDouble-= finalDiff.getY());
        }

        double fallDistance = 3D;
        Set<PermissionAttachmentInfo> perms = player.getEffectivePermissions();
        for (PermissionAttachmentInfo perm : perms) {
            if(!perm.getValue()) continue;
            String node = perm.getPermission();
            if (node.startsWith("glide.fall.")) {
                String[] val = node.split("\\.");
                if (val.length < 3 || val[2] == null || val[2].equals("")) continue;
                int number;
                try {
                    number = Integer.parseInt(val[2]);
                } catch (NumberFormatException exception) {
                    Bukkit.getLogger().warning("[rtp] invalid permission: " + node);
                    continue;
                }
                fallDistance = number;
                break;
            }
        }

        boolean farEnough;
        try {
            farEnough = playerFallDistances.get(playerId) > fallDistance;
        }
        catch (NullPointerException e) {
            farEnough = false;
        }
        if(farEnough) {
            Bukkit.getPluginManager().callEvent(new PlayerGlideEvent(player));
        }
    }
}
