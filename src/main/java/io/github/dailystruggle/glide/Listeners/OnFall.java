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
import org.bukkit.potion.PotionEffectType;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;

public class OnFall implements Listener {
    private static final ConcurrentHashMap<UUID,Double> playerFallDistance = new ConcurrentHashMap<>();

    @EventHandler(priority = EventPriority.LOW)
    public void onFall(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        if(player.hasPotionEffect(PotionEffectType.SLOW_FALLING)) return; //because of glitchy appearance with other plugins
        Location to = event.getTo();
        if(to == null) return;
        if(player.isFlying() || player.isGliding() || player.isOnGround() || player.isSwimming()
                || to.getBlock().isLiquid() || !player.hasPermission("glide.fall")) return;
        UUID playerId = player.getUniqueId();
        if(System.currentTimeMillis()- Glide.getLastLandTime(playerId) < 500) return;
        to = to.clone();
        to.subtract(event.getFrom());
        if(to.getY()>0D) {
            try {
                playerFallDistance.computeIfPresent(playerId,(uuid, aLong) -> aLong=0D);
            }
            catch (NullPointerException e) {
                return;
            }
            return;
        }

        if(to.getY()<0) {
            try {
                playerFallDistance.putIfAbsent(playerId,0D);
            }
            catch (NullPointerException e) {
                return;
            }
            Location finalDiff = to;
            playerFallDistance.compute(playerId,(uuid, aDouble) -> aDouble-= finalDiff.getY());
        }

        double fallDistance = 3D;
        int invulnerabilityTime = 0;
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
            else if (node.startsWith("glide.invulnerable.")) {
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

        boolean farEnough;
        try {
            farEnough = playerFallDistance.get(playerId) > fallDistance;
        }
        catch (NullPointerException e) {
            return;
        }

        if(farEnough) {
            Glide.getGlidingPlayers().add(playerId);
            player.setGliding(true);
            Bukkit.getPluginManager().callEvent(new PlayerGlideEvent(player));
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
}
