package io.github.dailystruggle.glide;

import io.github.dailystruggle.glide.Commands.GlideCmd;
import io.github.dailystruggle.glide.Commands.Help;
import io.github.dailystruggle.glide.Commands.Reload;
import io.github.dailystruggle.glide.Commands.TabComplete;
import io.github.dailystruggle.glide.Listeners.OnFall;
import io.github.dailystruggle.glide.Listeners.OnFireworkUse;
import io.github.dailystruggle.glide.Listeners.OnGlideToggle;
import io.github.dailystruggle.glide.configuration.Configs;
import io.github.dailystruggle.glide.customEventListeners.GlideEffects;
import io.github.dailystruggle.glide.customEventListeners.OnPlayerGlide;
import io.github.dailystruggle.glide.customEventListeners.OnPlayerLand;
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.Particle;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffectType;

import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;

public final class Glide extends JavaPlugin {
    private static Glide plugin;
    private static final ConcurrentSkipListSet<UUID> glidingPlayers = new ConcurrentSkipListSet<>();
    private static final ConcurrentSkipListSet<UUID> invulnerablePlayers = new ConcurrentSkipListSet<>();
    private static final ConcurrentHashMap<UUID,Long> playerLandTimes = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<UUID,Double> playerFallDistances = new ConcurrentHashMap<>();
    private static Configs configs;

    @Override
    public void onEnable() {
        plugin = this;
        configs = new Configs(this);

        int pluginId = 12846;
        Metrics metrics = new Metrics(this, pluginId);

        // Plugin startup logic
        GlideCmd glide = new GlideCmd(this, configs);
        Objects.requireNonNull(getCommand("glide")).setExecutor(glide);
        Objects.requireNonNull(getCommand("glide")).setTabCompleter(new TabComplete());

        glide.addCommandHandle("reload","glide.reload",new Reload(configs));
        glide.addCommandHandle("help","glide.use",new Help(configs));

        //adding every sound takes too long at startup
        Bukkit.getPluginManager().addPermission(new Permission("glide.effect.glide.sound"));

        for(Particle particle : Particle.values()) {
            Bukkit.getPluginManager().addPermission(new Permission("glide.effect.glide.particle." + particle.name()));
            Bukkit.getPluginManager().addPermission(new Permission("glide.effect.land.particle." + particle.name()));
        }

        for(PotionEffectType effect : PotionEffectType.values()) {
            Bukkit.getPluginManager().addPermission(new Permission("glide.effect.glide.potion." + effect.getName()));
            Bukkit.getPluginManager().addPermission(new Permission("glide.effect.land.potion." + effect.getName()));
        }

        getServer().getPluginManager().registerEvents(new OnFall(),this);
        getServer().getPluginManager().registerEvents(new OnGlideToggle(),this);
        getServer().getPluginManager().registerEvents(new OnFireworkUse(),this);
        getServer().getPluginManager().registerEvents(new OnPlayerGlide(),this);
        getServer().getPluginManager().registerEvents(new OnPlayerLand(),this);
        getServer().getPluginManager().registerEvents(new GlideEffects(),this);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        glidingPlayers.clear();
        configs = null;

        for(UUID uuid : invulnerablePlayers) {
            Player player = Bukkit.getOfflinePlayer(uuid).getPlayer();
            if(player == null) continue;
            player.setInvulnerable(false);
        }
    }

    public static Glide getPlugin() {
        return plugin;
    }

    public static ConcurrentSkipListSet<UUID> getGlidingPlayers() {
        return glidingPlayers;
    }

    public static ConcurrentSkipListSet<UUID> getInvulnerablePlayers() {
        return invulnerablePlayers;
    }

    public static Long getLastLandTime(UUID uuid) {
        return playerLandTimes.getOrDefault(uuid,0L);
    }

    public static Long setLastLandTime(UUID uuid, Long val) {
        return playerLandTimes.put(uuid,val);
    }

    public static boolean isGliding(UUID uuid) {
        return glidingPlayers.contains(uuid);
    }

    public static boolean isGliding(Entity entity) {
        return glidingPlayers.contains(entity.getUniqueId());
    }

    public static ConcurrentHashMap<UUID,Double> getPlayerFallDistances() {
        return playerFallDistances;
    }
}
