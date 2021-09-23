package io.github.dailystruggle.glideeffects;

import io.github.dailystruggle.glide.customEvents.PlayerGlideEvent;
import io.github.dailystruggle.glide.customEvents.PlayerLandEvent;
import org.bukkit.Bukkit;
import org.bukkit.Effect;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionAttachmentInfo;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class GlideEffects extends JavaPlugin {
    @Override
    public void onEnable() {
        // Plugin startup logic
        for(Effect effect : Effect.values()) {
            Bukkit.getPluginManager().addPermission(new Permission("glide.effect.glide." + effect.name()));
            Bukkit.getPluginManager().addPermission(new Permission("glide.effect.land." + effect.name()));
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

            List<Effect> effects = new ArrayList<>();
            List<PotionEffect> potionEffects = new ArrayList<>();
            Set<PermissionAttachmentInfo> perms = event.getPlayer().getEffectivePermissions();
            for (PermissionAttachmentInfo perm : perms) {
                if (!perm.getValue()) continue;
                String node = perm.getPermission();
                if (node.startsWith("glide.effect.glide.")) {
                    String[] val = node.split("\\.");
                    if (val[3] == null || val[3].equals("")) continue;

                    if(val[3].equals("potion")) {
                        if (val.length<5 || val[4] == null || val[4].equals("")) continue;
                        PotionEffectType potionEffectType = PotionEffectType.getByName(val[4]);
                        if(potionEffectType == null) continue;
                        int duration = Integer.MAX_VALUE;
                        int amplifier = 1;
                        boolean ambient = false;
                        boolean particles = false;
                        boolean icon = false;
                        if (val.length > 5 && val[5]!=null) {
                            try {
                                duration = Integer.parseInt(val[5]);
                            } catch (NumberFormatException ignored) {
                                Bukkit.getLogger().warning("[GlideEffects] invalid duration setting: " + val[85]);
                            }
                        }

                        if (val.length > 6 && val[6]!=null) {
                            try {
                                amplifier = Integer.parseInt(val[6]);
                            } catch (NumberFormatException ignored) {
                                Bukkit.getLogger().warning("[GlideEffects] invalid amplifier setting: " + val[8]);
                            }
                        }

                        if (val.length > 7 && val[7]!=null) {
                            try {
                                ambient = Boolean.parseBoolean(val[7]);
                            } catch (NumberFormatException ignored) {
                                Bukkit.getLogger().warning("[GlideEffects] invalid ambient setting: " + val[7]);
                            }
                        }

                        if (val.length > 8 && val[8]!=null) {
                            try {
                                particles = Boolean.parseBoolean(val[8]);
                            } catch (NumberFormatException ignored) {
                                Bukkit.getLogger().warning("[GlideEffects] invalid particles setting: " + val[8]);
                            }
                        }

                        if (val.length > 9 && val[9]!=null) {
                            try {
                                icon = Boolean.parseBoolean(val[9]);
                            } catch (NumberFormatException ignored) {
                                Bukkit.getLogger().warning("[GlideEffects] invalid icon setting: " + val[9]);
                            }
                        }

                        potionEffects.add(new PotionEffect(potionEffectType,duration,amplifier,ambient,particles,icon));
                    }
                    else {
                        Effect effect;
                        try{
                            effect = Effect.valueOf(val[3]);
                        }
                        catch (IllegalArgumentException | NullPointerException exception) {
                            Bukkit.getLogger().warning("[GlideEffects] invalid effect: " + val[3]);
                            continue;
                        }
                        effects.add(effect);
                    }
                }
            }

            for(Effect effect : effects) {
                event.getPlayer().playEffect(event.getPlayer().getLocation(),effect, effect.getData());
            }
            for(PotionEffect effect : potionEffects) {
                event.getPlayer().addPotionEffect(effect,true);
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

            List<Effect> effects = new ArrayList<>();
            List<PotionEffect> potionEffects = new ArrayList<>();
            Set<PermissionAttachmentInfo> perms = event.getPlayer().getEffectivePermissions();
            for (PermissionAttachmentInfo perm : perms) {
                if (!perm.getValue()) continue;
                String node = perm.getPermission();
                if (node.startsWith("glide.effect.land.")) {
                    String[] val = node.split("\\.");
                    if (val[3] == null || val[3].equals("")) continue;

                    if(val[3].equals("potion")) {
                        if (val.length<5 || val[4] == null || val[4].equals("")) continue;
                        PotionEffectType potionEffectType = PotionEffectType.getByName(val[4]);
                        if(potionEffectType == null) continue;
                        int duration = 255;
                        int amplifier = 1;
                        boolean ambient = false;
                        boolean particles = true;
                        boolean icon = false;
                        if (val.length > 5 && val[5]!=null) {
                            try {
                                duration = Integer.parseInt(val[5]);
                            } catch (NumberFormatException ignored) {
                                Bukkit.getLogger().warning("[GlideEffects] invalid duration setting: " + val[85]);
                            }
                        }

                        if (val.length > 6 && val[6]!=null) {
                            try {
                                amplifier = Integer.parseInt(val[6]);
                            } catch (NumberFormatException ignored) {
                                Bukkit.getLogger().warning("[GlideEffects] invalid amplifier setting: " + val[8]);
                            }
                        }

                        if (val.length > 7 && val[7]!=null) {
                            try {
                                ambient = Boolean.parseBoolean(val[7]);
                            } catch (NumberFormatException ignored) {
                                Bukkit.getLogger().warning("[GlideEffects] invalid ambient setting: " + val[7]);
                            }
                        }

                        if (val.length > 8 && val[8]!=null) {
                            try {
                                particles = Boolean.parseBoolean(val[8]);
                            } catch (NumberFormatException ignored) {
                                Bukkit.getLogger().warning("[GlideEffects] invalid particles setting: " + val[8]);
                            }
                        }

                        if (val.length > 9 && val[9]!=null) {
                            try {
                                icon = Boolean.parseBoolean(val[9]);
                            } catch (NumberFormatException ignored) {
                                Bukkit.getLogger().warning("[GlideEffects] invalid icon setting: " + val[9]);
                            }
                        }

                        potionEffects.add(new PotionEffect(potionEffectType,duration,amplifier,ambient,particles,icon));
                    }
                    else {
                        Effect effect;
                        try{
                            effect = Effect.valueOf(val[3]);
                        }
                        catch (IllegalArgumentException | NullPointerException exception) {
                            Bukkit.getLogger().warning("[GlideEffects] invalid effect: " + val[3]);
                            continue;
                        }
                        effects.add(effect);
                    }
                }
            }

            for(Effect effect : effects) {
                event.getPlayer().playEffect(event.getPlayer().getLocation(),effect, effect.getData());
            }
            for(PotionEffect effect : potionEffects) {
                event.getPlayer().addPotionEffect(effect,true);
            }
        }
    }
}
