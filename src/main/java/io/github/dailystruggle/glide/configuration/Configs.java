package io.github.dailystruggle.glide.configuration;

import io.github.dailystruggle.glide.Glide;

//route for all config classes
public class Configs {
    private final Glide plugin;
    public Worlds worlds;
    public Lang lang;
    public String version;

    public Configs(Glide plugin) {
        this.plugin = plugin;
        String name = plugin.getServer().getClass().getPackage().getName();
        version = name.substring(name.indexOf('-')+1);

        lang = new Lang(plugin);
        worlds = new Worlds(plugin, lang);
    }

    public void refresh() {
        String name = plugin.getServer().getClass().getPackage().getName();
        version = name.substring(name.indexOf('-')+1);
        lang = new Lang(plugin);
        worlds = new Worlds(plugin, lang);
    }
}
