package io.github.dailystruggle.glide.Tasks;

import io.github.dailystruggle.glide.Glide;
import io.github.dailystruggle.glide.configuration.Configs;
import io.github.dailystruggle.glide.customEvents.PlayerGlideEvent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionAttachmentInfo;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentSkipListSet;

public class SetupGlide extends BukkitRunnable {
    private final Player player;
    private final Configs configs;

    public SetupGlide(Player player, Configs configs) {
        this.player = player;
        this.configs = configs;
    }

    @Override
    public void run() {
        Location location = player.getLocation();
        int relative = (int) configs.worlds.getWorldSetting(player.getWorld().getName(),"relative",75);
        int max = (int) configs.worlds.getWorldSetting(player.getWorld().getName(),"max",320);
        int toY = location.getBlockY() + relative;
        if(toY > max) toY = max;
        location.setY(toY);
        player.teleport(location);
        Glide.getGlidingPlayers().add(player.getUniqueId());
        player.setFlying(false);
        player.setGliding(true);
        if(player.hasPermission("glide.invulnerable") && !player.isInvulnerable()) {
            player.setInvulnerable(true);
            Glide.getInvulnerablePlayers().add(player.getUniqueId());
        }

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
        if(invulnerabilityTime>0) {
            Bukkit.getScheduler().runTaskLater(Glide.getPlugin(),()->{
                ConcurrentSkipListSet<UUID> invulnerablePlayers = Glide.getInvulnerablePlayers();
                if(invulnerablePlayers.contains(player.getUniqueId())) {
                    player.setInvulnerable(false);
                    invulnerablePlayers.remove(player.getUniqueId());
                }
            }, 20L * invulnerabilityTime);
        }

        Bukkit.getPluginManager().callEvent(new PlayerGlideEvent(player));
    }
}
