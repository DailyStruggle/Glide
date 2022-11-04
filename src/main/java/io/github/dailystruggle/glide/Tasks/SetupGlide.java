package io.github.dailystruggle.glide.Tasks;

import io.github.dailystruggle.glide.configuration.Configs;
import io.github.dailystruggle.glide.customEvents.PlayerGlideEvent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.UUID;

public class SetupGlide extends BukkitRunnable {
    private final UUID playerId;
    private final Configs configs;

    public SetupGlide(UUID playerId, Configs configs) {
        this.playerId = playerId;
        this.configs = configs;
    }

    @Override
    public void run() {
        Player player = Bukkit.getPlayer(playerId);
        if(player == null) return;
        Location location = player.getLocation();
        int relative = (int) configs.worlds.getWorldSetting(player.getWorld().getName(),"relative",75);
        int max = (int) configs.worlds.getWorldSetting(player.getWorld().getName(),"max",320);
        int toY = location.getBlockY() + relative;
        if(toY > max) toY = max;
        location.setY(toY);
        player.teleport(location);
        Bukkit.getPluginManager().callEvent(new PlayerGlideEvent(player));
    }
}
