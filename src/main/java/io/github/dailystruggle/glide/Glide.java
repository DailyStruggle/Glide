package io.github.dailystruggle.glide;

import io.github.dailystruggle.glide.Commands.*;
import io.github.dailystruggle.glide.Listeners.*;
import io.github.dailystruggle.glide.configuration.Configs;
import io.github.dailystruggle.glide.customEventListeners.GlideEffects;
import io.github.dailystruggle.glide.customEventListeners.OnPlayerGlide;
import io.github.dailystruggle.glide.customEventListeners.OnPlayerLand;
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.FireworkEffect;
import org.bukkit.Instrument;
import org.bukkit.Particle;
import org.bukkit.command.CommandExecutor;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.permissions.Permission;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;

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

    private static final SubCommand subCommands = new SubCommand("glide.use",null);

    @Override
    public void onEnable() {
        plugin = this;
        configs = new Configs(this);

        int pluginId = 12846;
        Metrics metrics = new Metrics(this, pluginId);

        // Plugin startup logic
        initDefaultCommands();
        Objects.requireNonNull(getCommand("glide")).setExecutor(new GlideCmd(subCommands));
        Objects.requireNonNull(getCommand("glide")).setTabCompleter(new TabComplete(subCommands));

        getServer().getPluginManager().registerEvents(new OnDamage(),this);
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
        invulnerablePlayers.clear();
        configs = null;
    }

    public static Glide getPlugin() {
        return plugin;
    }

    public static Configs getConfigs() {
        return configs;
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

    private static void initDefaultCommands() {
        subCommands.setSubCommand("help", new SubCommand("glide.use",new Help()));
        subCommands.setSubCommand("reload", new SubCommand("glide.reload",new Reload()));

        subCommands.setSubParam("player", "glide.use.other", SubCommand.ParamType.PLAYER);

        //adding every sound takes too long at startup
        Bukkit.getPluginManager().addPermission(new Permission("glide.effect.glide.sound"));
        Bukkit.getPluginManager().addPermission(new Permission("glide.effect.gliding.sound"));
        Bukkit.getPluginManager().addPermission(new Permission("glide.effect.land.sound"));

        for(Particle particle : Particle.values()) {
            Bukkit.getPluginManager().addPermission(new Permission("glide.effect.glide.particle." + particle.name()));
            Bukkit.getPluginManager().addPermission(new Permission("glide.effect.gliding.particle." + particle.name()));
            Bukkit.getPluginManager().addPermission(new Permission("glide.effect.land.particle." + particle.name()));
        }

        for(PotionEffectType effect : PotionEffectType.values()) {
            Bukkit.getPluginManager().addPermission(new Permission("glide.effect.glide.potion." + effect.getName()));
            Bukkit.getPluginManager().addPermission(new Permission("glide.effect.gliding.potion." + effect.getName()));
            Bukkit.getPluginManager().addPermission(new Permission("glide.effect.land.potion." + effect.getName()));
        }

        for(FireworkEffect.Type type : FireworkEffect.Type.values()) {
            Bukkit.getPluginManager().addPermission(new Permission("glide.effect.glide.firework." + type.name()));
            Bukkit.getPluginManager().addPermission(new Permission("glide.effect.gliding.firework." + type.name()));
            Bukkit.getPluginManager().addPermission(new Permission("glide.effect.land.firework." + type.name()));
        }

        for(Instrument instrument : Instrument.values()) {
            Bukkit.getPluginManager().addPermission(new Permission("glide.effect.glide.note." + instrument.name()));
            Bukkit.getPluginManager().addPermission(new Permission("glide.effect.gliding.note." + instrument.name()));
            Bukkit.getPluginManager().addPermission(new Permission("glide.effect.land.note." + instrument.name()));
        }
    }

    public static void setSubCommand(@NotNull String name, @NotNull SubCommand subCommand) {
        subCommands.setSubCommand(name,subCommand);
    }

    public static void setSubCommand(@NotNull String name, @NotNull String permission, @NotNull CommandExecutor commandExecutor) {
        setSubCommand(name,new SubCommand(permission,commandExecutor));
    }
}
