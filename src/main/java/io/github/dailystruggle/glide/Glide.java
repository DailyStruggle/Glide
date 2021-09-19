package io.github.dailystruggle.glide;

import io.github.dailystruggle.glide.Commands.GlideCmd;
import io.github.dailystruggle.glide.Commands.Help;
import io.github.dailystruggle.glide.Commands.Reload;
import io.github.dailystruggle.glide.Commands.TabComplete;
import io.github.dailystruggle.glide.Listeners.OnFireworkUse;
import io.github.dailystruggle.glide.Listeners.OnGlideToggle;
import io.github.dailystruggle.glide.Tasks.SetupGlide;
import io.github.dailystruggle.glide.configuration.Configs;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentSkipListSet;

public final class Glide extends JavaPlugin {
    private static final ConcurrentSkipListSet<UUID> glidingPlayers = new ConcurrentSkipListSet<>();
    private static Configs configs;

    @Override
    public void onEnable() {
        configs = new Configs(this);
        // Plugin startup logic
        GlideCmd glide = new GlideCmd(this, configs);
        Objects.requireNonNull(getCommand("glide")).setExecutor(glide);
        Objects.requireNonNull(getCommand("glide")).setTabCompleter(new TabComplete());

        glide.addCommandHandle("reload","glide.reload",new Reload(configs));
        glide.addCommandHandle("help","glide.use",new Help(configs));

        getServer().getPluginManager().registerEvents(new OnGlideToggle(this),this);
        getServer().getPluginManager().registerEvents(new OnFireworkUse(),this);

        SetupGlide.setPlugin(this);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        glidingPlayers.clear();
        configs = null;
    }

    public ConcurrentSkipListSet<UUID> getGlidingPlayers() {
        return glidingPlayers;
    }

    public static boolean isGliding(UUID uuid) {
        return glidingPlayers.contains(uuid);
    }

    public static boolean isGliding(Entity entity) {
        return glidingPlayers.contains(entity.getUniqueId());
    }
}
