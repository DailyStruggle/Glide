package io.github.dailystruggle.glide.configuration;

import io.github.dailystruggle.glide.Glide;
import io.github.dailystruggle.glide.SendMessage;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;
import java.util.Scanner;

public class Worlds {
    private final Glide plugin;
    private final Lang lang;

    private FileConfiguration config;

    public Worlds(Glide plugin, Lang lang) {
        this.plugin = plugin;
        this.lang = lang;

        File f = new File(plugin.getDataFolder(), "worlds.yml");
        if(!f.exists())
        {
            plugin.saveResource("worlds.yml", false);
        }
        config = YamlConfiguration.loadConfiguration(f);

        if( 	(config.getDouble("version") < 1.0) ) {
            SendMessage.sendMessage(Bukkit.getConsoleSender(),lang.getLog("oldFile","worlds.yml"));
            FileStuff.renameFiles(plugin,"worlds");
            config = YamlConfiguration.loadConfiguration(f);
        }
        update();
    }

    public void update() {
        ArrayList<String> linesInWorlds = new ArrayList<>();
        int defaultRelative = Objects.requireNonNull(config.getConfigurationSection("default")).getInt("relative",75);
        int defaultMax = Objects.requireNonNull(config.getConfigurationSection("default")).getInt("max",320);

        try {
            Scanner scanner = new Scanner(
                    new File(plugin.getDataFolder().getAbsolutePath() + File.separator + "worlds.yml"));
            //for each line in original messages file
            String currWorldName = "default";
            while (scanner.hasNextLine()) {
                String s = scanner.nextLine();

                //append at first blank line
                if(!(s.matches(".*[a-z].*")||s.matches(".*[A-Z].*"))) {
                    //for each missing world, add some default data
                    for(World w : Bukkit.getWorlds()) {
                        String worldName = w.getName();
                        if(config.contains(worldName)) continue;
                        config.set(worldName, config.getConfigurationSection("default"));

                        if(linesInWorlds.get(linesInWorlds.size()-1).length() < 4)
                            linesInWorlds.set(linesInWorlds.size()-1,"    " + worldName + ":");
                        else linesInWorlds.add(worldName + ":");
                        linesInWorlds.add("    relative: " + defaultRelative);
                        linesInWorlds.add("    max: " + defaultMax);
                    }
                }
                else { //if not a blank line
                    if(s.startsWith("    relative:"))
                        s = "    relative: " + Objects.requireNonNull(config.getConfigurationSection(currWorldName)).getInt("relative", defaultRelative);
                    else if(s.startsWith("    max:"))
                        s = "    max: " + Objects.requireNonNull(config.getConfigurationSection(currWorldName)).getInt("max", defaultMax);
                    else if(!s.startsWith("#") && !s.startsWith("  ") && !s.startsWith("version") && (s.matches(".*[a-z].*") || s.matches(".*[A-Z].*")))
                    {
                        currWorldName = s.replace(":","");
                    }
                }

                //add line
                linesInWorlds.add(s);
            }
            scanner.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        FileWriter fw;
        String[] linesArray = linesInWorlds.toArray(new String[0]);
        try {
            fw = new FileWriter(plugin.getDataFolder().getAbsolutePath() + File.separator + "worlds.yml");
            for (String s : linesArray) {
                fw.write(s + "\n");
            }
            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        //-------------UPDATE INTERNAL VERSION ACCORDINGLY-------------
        config = YamlConfiguration.loadConfiguration(new File(plugin.getDataFolder(), "worlds.yml"));
    }

    public Object getWorldSetting(String worldName, String name, Object def) {
        if(!checkWorldExists(worldName)) return def;
        return this.config.getConfigurationSection(worldName).get(name,def);
    }

    public Boolean checkWorldExists(String worldName) {
        if(worldName == null) return false;
        if( Bukkit.getWorld(worldName)==null ) {
            return false;
        }
        else if( !this.config.contains(worldName) ) {
            SendMessage.sendMessage(Bukkit.getConsoleSender(),lang.getLog("newWorld",worldName));
            SendMessage.sendMessage(Bukkit.getConsoleSender(),lang.getLog("updatingWorlds",worldName));
            update(); //not optimal but it works
            SendMessage.sendMessage(Bukkit.getConsoleSender(),lang.getLog("updatedWorlds",worldName));
        }
        return true;
    }
}
