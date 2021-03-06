package io.github.dailystruggle.glide.Tasks;

import io.github.dailystruggle.glide.configuration.Configs;
import io.github.dailystruggle.glide.customEvents.PlayerGlideEvent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

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
        Bukkit.getPluginManager().callEvent(new PlayerGlideEvent(player));
    }
}
