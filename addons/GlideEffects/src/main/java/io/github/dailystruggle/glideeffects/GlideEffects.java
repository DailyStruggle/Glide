package io.github.dailystruggle.glideeffects;

import io.github.dailystruggle.glide.customEvents.PlayerGlideEvent;
import io.github.dailystruggle.glide.customEvents.PlayerLandEvent;
import org.bukkit.Bukkit;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionAttachmentInfo;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public final class GlideEffects extends JavaPlugin {
    @Override
    public void onEnable() {
        // Plugin startup logic

        Bukkit.getPluginManager().addPermission(new Permission("glide.effect.glide.sound."));

        //this is too slow
//        for(Sound sound : Sound.values()) {
//            Bukkit.getPluginManager().addPermission(new Permission("glide.effect.glide.sound." + sound.name()));
//            Bukkit.getPluginManager().addPermission(new Permission("glide.effect.land.sound." + sound.name()));
//        }

        for(Particle particle : Particle.values()) {
            Bukkit.getPluginManager().addPermission(new Permission("glide.effect.glide.particle." + particle.name()));
            Bukkit.getPluginManager().addPermission(new Permission("glide.effect.land.particle." + particle.name()));
        }

        for(PotionEffectType effect : PotionEffectType.values()) {
            Bukkit.getPluginManager().addPermission(new Permission("glide.effect.glide.potion." + effect.getName()));
            Bukkit.getPluginManager().addPermission(new Permission("glide.effect.land.potion." + effect.getName()));
        }

        getServer().getPluginManager().registerEvents(new GlideListener(),this);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    private static class GlideListener implements Listener {
        private final ConcurrentHashMap<UUID,List<PotionEffect>> playerPotionEffects = new ConcurrentHashMap<>();

        @EventHandler(priority = EventPriority.HIGH)
        public void onGlide(PlayerGlideEvent event) {
            List<PotionEffect> oldPotionEffects = playerPotionEffects.get(event.getPlayer().getUniqueId());
            if(oldPotionEffects!=null) {
                for (PotionEffect effect : oldPotionEffects) {
                    event.getPlayer().removePotionEffect(effect.getType());
                }
            }

            List<PotionEffect> potionEffects = new ArrayList<>();
            Map<Sound,float[]> sounds = new HashMap<>();
            Map<Particle,Integer> particles = new HashMap<>();
            Set<PermissionAttachmentInfo> perms = event.getPlayer().getEffectivePermissions();
            for (PermissionAttachmentInfo perm : perms) {
                if (!perm.getValue()) continue;
                String node = perm.getPermission();
                if (node.startsWith("glide.effect.glide.")) {
                    String[] val = node.split("\\.");
                    if (val[3] == null || val[3].equals("")) continue;

                    switch (val[3]) {
                        case "potion":
                            if (val.length < 5 || val[4] == null || val[4].equals("")) continue;
                            PotionEffectType potionEffectType = PotionEffectType.getByName(val[4]);
                            if (potionEffectType == null) continue;
                            int duration = 255;
                            int amplifier = 1;
                            boolean ambient = false;
                            boolean potionParticles = true;
                            boolean icon = false;
                            if (val.length > 5 && val[5] != null) {
                                try {
                                    duration = Integer.parseInt(val[5]);
                                } catch (NumberFormatException ignored) {
                                    Bukkit.getLogger().warning("[GlideEffects] invalid duration setting: " + val[5]);
                                }
                            }

                            if (val.length > 6 && val[6] != null) {
                                try {
                                    amplifier = Integer.parseInt(val[6]);
                                } catch (NumberFormatException ignored) {
                                    Bukkit.getLogger().warning("[GlideEffects] invalid amplifier setting: " + val[6]);
                                }
                            }

                            if (val.length > 7 && val[7] != null) {
                                try {
                                    ambient = Boolean.parseBoolean(val[7]);
                                } catch (NumberFormatException ignored) {
                                    Bukkit.getLogger().warning("[GlideEffects] invalid ambient setting: " + val[7]);
                                }
                            }

                            if (val.length > 8 && val[8] != null) {
                                try {
                                    potionParticles = Boolean.parseBoolean(val[8]);
                                } catch (NumberFormatException ignored) {
                                    Bukkit.getLogger().warning("[GlideEffects] invalid potion particles setting: " + val[8]);
                                }
                            }

                            if (val.length > 9 && val[9] != null) {
                                try {
                                    icon = Boolean.parseBoolean(val[9]);
                                } catch (NumberFormatException ignored) {
                                    Bukkit.getLogger().warning("[GlideEffects] invalid icon setting: " + val[9]);
                                }
                            }

                            potionEffects.add(new PotionEffect(potionEffectType, duration, amplifier, ambient, potionParticles, icon));
                            break;
                        case "sound":
                            Sound sound;
                            try {
                                sound = Sound.valueOf(val[4].toUpperCase());
                            } catch (IllegalArgumentException | NullPointerException exception) {
                                Bukkit.getLogger().warning("[GlideEffects] invalid sound: " + val[4]);
                                continue;
                            }

                            int volume = 100;
                            int pitch = 100;
                            if (val.length > 5 && val[5] != null) {
                                try {
                                    volume = Integer.parseInt(val[5]);
                                } catch (NumberFormatException ignored) {
                                    Bukkit.getLogger().warning("[GlideEffects] invalid volume setting: " + val[5]);
                                }
                            }

                            if (val.length > 6 && val[6] != null) {
                                try {
                                    pitch = Integer.parseInt(val[6]);
                                } catch (NumberFormatException ignored) {
                                    Bukkit.getLogger().warning("[GlideEffects] invalid amplifier setting: " + val[6]);
                                }
                            }

                            float[] vals = {(((float)volume)/100),(((float)pitch)/100)};
                            sounds.put(sound,vals);
                            break;
                        case "particle":
                            Particle particle;
                            try {
                                particle = Particle.valueOf(val[4].toUpperCase());
                            } catch (IllegalArgumentException | NullPointerException exception) {
                                Bukkit.getLogger().warning("[GlideEffects] invalid particle type: " + val[4]);
                                continue;
                            }

                            int numParticles = 0;
                            if (val.length > 5 && val[5] != null) {
                                try {
                                    numParticles = Integer.parseInt(val[5]);
                                } catch (NumberFormatException ignored) {
                                    Bukkit.getLogger().warning("[GlideEffects] invalid number of particles: " + val[5]);
                                    continue;
                                }
                            }
                            particles.put(particle, numParticles);
                            break;
                    }
                }
            }

            for(Map.Entry<Sound,float[]> entry : sounds.entrySet()) {
                event.getPlayer().playSound(event.getPlayer().getLocation(),entry.getKey(),entry.getValue()[0],entry.getValue()[1]);
            }
            for(PotionEffect effect : potionEffects) {
                event.getPlayer().addPotionEffect(effect,true);
            }
            for(Map.Entry<Particle,Integer> entry : particles.entrySet()) {
                event.getPlayer().spawnParticle(entry.getKey(),event.getPlayer().getLocation(),entry.getValue());
            }

            playerPotionEffects.put(event.getPlayer().getUniqueId(),potionEffects);
        }

        @EventHandler(priority = EventPriority.HIGH)
        public void onLand(PlayerLandEvent event) {
            List<PotionEffect> oldPotionEffects = playerPotionEffects.get(event.getPlayer().getUniqueId());
            if(oldPotionEffects!=null) {
                for (PotionEffect effect : oldPotionEffects) {
                    event.getPlayer().removePotionEffect(effect.getType());
                }
            }

            List<PotionEffect> potionEffects = new ArrayList<>();
            Map<Sound,float[]> sounds = new HashMap<>();
            Map<Particle,Integer> particles = new HashMap<>();
            Set<PermissionAttachmentInfo> perms = event.getPlayer().getEffectivePermissions();
            for (PermissionAttachmentInfo perm : perms) {
                if (!perm.getValue()) continue;
                String node = perm.getPermission();
                if (node.startsWith("glide.effect.land.")) {
                    String[] val = node.split("\\.");
                    if (val[3] == null || val[3].equals("")) continue;

                    switch (val[3]) {
                        case "potion":
                            if (val.length < 5 || val[4] == null || val[4].equals("")) continue;
                            PotionEffectType potionEffectType = PotionEffectType.getByName(val[4]);
                            if (potionEffectType == null) continue;
                            int duration = 255;
                            int amplifier = 1;
                            boolean ambient = false;
                            boolean potionParticles = true;
                            boolean icon = false;
                            if (val.length > 5 && val[5] != null) {
                                try {
                                    duration = Integer.parseInt(val[5]);
                                } catch (NumberFormatException ignored) {
                                    Bukkit.getLogger().warning("[GlideEffects] invalid duration setting: " + val[5]);
                                }
                            }

                            if (val.length > 6 && val[6] != null) {
                                try {
                                    amplifier = Integer.parseInt(val[6]);
                                } catch (NumberFormatException ignored) {
                                    Bukkit.getLogger().warning("[GlideEffects] invalid amplifier setting: " + val[6]);
                                }
                            }

                            if (val.length > 7 && val[7] != null) {
                                try {
                                    ambient = Boolean.parseBoolean(val[7]);
                                } catch (NumberFormatException ignored) {
                                    Bukkit.getLogger().warning("[GlideEffects] invalid ambient setting: " + val[7]);
                                }
                            }

                            if (val.length > 8 && val[8] != null) {
                                try {
                                    potionParticles = Boolean.parseBoolean(val[8]);
                                } catch (NumberFormatException ignored) {
                                    Bukkit.getLogger().warning("[GlideEffects] invalid potion particles setting: " + val[8]);
                                }
                            }

                            if (val.length > 9 && val[9] != null) {
                                try {
                                    icon = Boolean.parseBoolean(val[9]);
                                } catch (NumberFormatException ignored) {
                                    Bukkit.getLogger().warning("[GlideEffects] invalid icon setting: " + val[9]);
                                }
                            }

                            potionEffects.add(new PotionEffect(potionEffectType, duration, amplifier, ambient, potionParticles, icon));
                            break;
                        case "sound":
                            Sound sound;
                            try {
                                sound = Sound.valueOf(val[4].toUpperCase());
                            } catch (IllegalArgumentException | NullPointerException exception) {
                                Bukkit.getLogger().warning("[GlideEffects] invalid sound: " + val[4]);
                                continue;
                            }

                            int volume = 100;
                            int pitch = 100;
                            if (val.length > 5 && val[5] != null) {
                                try {
                                    volume = Integer.parseInt(val[5]);
                                } catch (NumberFormatException ignored) {
                                    Bukkit.getLogger().warning("[GlideEffects] invalid volume setting: " + val[5]);
                                }
                            }

                            if (val.length > 6 && val[6] != null) {
                                try {
                                    pitch = Integer.parseInt(val[6]);
                                } catch (NumberFormatException ignored) {
                                    Bukkit.getLogger().warning("[GlideEffects] invalid amplifier setting: " + val[6]);
                                }
                            }

                            float[] vals = {(((float)volume)/100),(((float)pitch)/100)};
                            sounds.put(sound,vals);
                            break;
                        case "particle":
                            Particle particle;
                            try {
                                particle = Particle.valueOf(val[4].toUpperCase());
                            } catch (IllegalArgumentException | NullPointerException exception) {
                                Bukkit.getLogger().warning("[GlideEffects] invalid particle type: " + val[4]);
                                continue;
                            }

                            int numParticles = 0;
                            if (val.length > 5 && val[5] != null) {
                                try {
                                    numParticles = Integer.parseInt(val[5]);
                                } catch (NumberFormatException ignored) {
                                    Bukkit.getLogger().warning("[GlideEffects] invalid number of particles: " + val[5]);
                                    continue;
                                }
                            }
                            particles.put(particle, numParticles);
                            break;
                    }
                }
            }

            for(Map.Entry<Sound,float[]> entry : sounds.entrySet()) {
                event.getPlayer().playSound(event.getPlayer().getLocation(),entry.getKey(),entry.getValue()[0],entry.getValue()[1]);
            }
            for(PotionEffect effect : potionEffects) {
                event.getPlayer().addPotionEffect(effect,true);
            }
            for(Map.Entry<Particle,Integer> entry : particles.entrySet()) {
                event.getPlayer().spawnParticle(entry.getKey(),event.getPlayer().getLocation(),entry.getValue());
            }
        }
    }
}
