package io.github.dailystruggle.glide.customEventListeners;

import io.github.dailystruggle.glide.Glide;
import io.github.dailystruggle.glide.customEvents.PlayerGlideEvent;
import io.github.dailystruggle.glide.customEvents.PlayerLandEvent;
import org.bukkit.*;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.permissions.PermissionAttachmentInfo;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class GlideEffects implements Listener {
    private static class EffectTask extends BukkitRunnable {
        private final Player player;

        public EffectTask(Player player) {
            this.player = player;
        }

        @Override
        public void run() {
            if(!player.isOnline() || !Glide.isGliding(player)) return;
            applyEffects("glide.effect.gliding.",player);
            new EffectTask(player).runTaskLaterAsynchronously(Glide.getPlugin(),1);
        }
    }

    private static final ConcurrentHashMap<UUID, List<PotionEffect>> playerPotionEffects = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<UUID, EffectTask> playerEffectTasks = new ConcurrentHashMap<>();

    @EventHandler(priority = EventPriority.HIGH)
    public void onGlide(PlayerGlideEvent event) {
        playerEffectTasks.computeIfPresent(event.getPlayer().getUniqueId(),(uuid, effectTask) -> {
            effectTask.cancel();
            return effectTask;
        });
        Bukkit.getScheduler().runTaskAsynchronously(Glide.getPlugin(), ()->applyEffects("glide.effect.glide.",event.getPlayer()));
        EffectTask effectTask = new EffectTask(event.getPlayer());
        effectTask.runTaskLaterAsynchronously(Glide.getPlugin(),1);
        playerEffectTasks.put(event.getPlayer().getUniqueId(),effectTask);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onLand(PlayerLandEvent event) {
        playerEffectTasks.computeIfPresent(event.getPlayer().getUniqueId(),(uuid, effectTask) -> {
            effectTask.cancel();
            return effectTask;
        });
        Bukkit.getScheduler().runTaskAsynchronously(Glide.getPlugin(), ()->applyEffects("glide.effect.land.",event.getPlayer()));
    }

    private static void applyEffects(String permission, Player player) {
        List<PotionEffect> oldPotionEffects = playerPotionEffects.get(player.getUniqueId());
        if(oldPotionEffects!=null) {
            for (PotionEffect effect : oldPotionEffects) {
                player.removePotionEffect(effect.getType());
            }
        }

        List<PotionEffect> potionEffects = new ArrayList<>();
        Map<Sound,float[]> sounds = new HashMap<>();
        List<Object[]> notes = new ArrayList<>();
        List<Object[]> particles = new ArrayList<>();
        List<Object[]> fireworks = new ArrayList<>();
        Set<PermissionAttachmentInfo> perms = player.getEffectivePermissions();

        for (PermissionAttachmentInfo perm : perms) {
            if (!perm.getValue()) continue;
            String node = perm.getPermission();
            if(!node.startsWith(permission)) continue;

            String[] val = node.split("\\.");
            if (val[3] == null || val[3].equals("")) continue;

            switch (val[3].toLowerCase()) {
                case "potion": {
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
                            Bukkit.getLogger().warning("[Glide] invalid duration setting: " + val[5]);
                        }
                    }
                    if (val.length > 6 && val[6] != null) {
                        try {
                            amplifier = Integer.parseInt(val[6]);
                        } catch (NumberFormatException ignored) {
                            Bukkit.getLogger().warning("[Glide] invalid amplifier setting: " + val[6]);
                        }
                    }
                    if (val.length > 7 && val[7] != null) {
                        try {
                            ambient = Boolean.parseBoolean(val[7]);
                        } catch (NumberFormatException ignored) {
                            Bukkit.getLogger().warning("[Glide] invalid ambient setting: " + val[7]);
                        }
                    }
                    if (val.length > 8 && val[8] != null) {
                        try {
                            potionParticles = Boolean.parseBoolean(val[8]);
                        } catch (NumberFormatException ignored) {
                            Bukkit.getLogger().warning("[Glide] invalid potion particles setting: " + val[8]);
                        }
                    }
                    if (val.length > 9 && val[9] != null) {
                        try {
                            icon = Boolean.parseBoolean(val[9]);
                        } catch (NumberFormatException ignored) {
                            Bukkit.getLogger().warning("[Glide] invalid icon setting: " + val[9]);
                        }
                    }
                    potionEffects.add(new PotionEffect(potionEffectType, duration, amplifier, ambient, potionParticles, icon));
                    break;
                }
                case "note": {
                    Instrument instrument = Instrument.PIANO;
                    if (val.length > 4 && val[4] != null) {
                        try {
                            instrument = Instrument.valueOf(val[4].toUpperCase());
                        } catch (IllegalArgumentException exception) {
                            Bukkit.getLogger().warning("[Glide] invalid instrument: " + val[4]);
                            continue;
                        }
                    }

                    Note note;
                    int tone = 0;
                    if (val.length > 5 && val[5] != null) {
                        try {
                            tone = Integer.parseInt(val[5]);
                        } catch (NumberFormatException exception) {
                            Bukkit.getLogger().warning("[Glide] invalid tone setting: " + val[5]);
                            continue;
                        }
                    }

                    try {
                        note = new Note(tone);
                    } catch (IllegalArgumentException | NullPointerException exception) {
                        Bukkit.getLogger().warning("[Glide] invalid tone: " + tone);
                        continue;
                    }

                    notes.add(new Object[]{instrument, note});
                    break;
                }
                case "sound": {
                    Sound sound;
                    try {
                        sound = Sound.valueOf(val[4].toUpperCase());
                    } catch (IllegalArgumentException | NullPointerException exception) {
                        Bukkit.getLogger().warning("[Glide] invalid sound: " + val[4]);
                        continue;
                    }
                    int volume = 100;
                    int pitch = 100;
                    if (val.length > 5 && val[5] != null) {
                        try {
                            volume = Integer.parseInt(val[5]);
                        } catch (NumberFormatException ignored) {
                            Bukkit.getLogger().warning("[Glide] invalid volume setting: " + val[5]);
                        }
                    }
                    if (val.length > 6 && val[6] != null) {
                        try {
                            pitch = Integer.parseInt(val[6]);
                        } catch (NumberFormatException ignored) {
                            Bukkit.getLogger().warning("[Glide] invalid amplifier setting: " + val[6]);
                        }
                    }
                    float[] vals = {(((float) volume) / 100), (((float) pitch) / 100)};
                    sounds.put(sound, vals);
                    break;
                }
                case "particle": {
                    Particle particle;
                    try {
                        particle = Particle.valueOf(val[4].toUpperCase());
                    } catch (IllegalArgumentException | NullPointerException exception) {
                        Bukkit.getLogger().warning("[Glide] invalid particle type: " + val[4]);
                        continue;
                    }
                    int numParticles = 0;
                    if (val.length > 5 && val[5] != null) {
                        try {
                            numParticles = Integer.parseInt(val[5]);
                        } catch (NumberFormatException ignored) {
                            Bukkit.getLogger().warning("[Glide] invalid number of particles: " + val[5]);
                            continue;
                        }
                    }
                    particles.add(new Object[]{particle, numParticles});
                    break;
                }
                case "firework": {
                    FireworkEffect.Type type = FireworkEffect.Type.BALL;
                    if (val.length > 4 && val[4] != null) {
                        try {
                            type = FireworkEffect.Type.valueOf(val[4].toUpperCase());
                        } catch (NumberFormatException ignored) {
                            Bukkit.getLogger().warning("[Glide] invalid firework type: " + val[4]);
                            continue;
                        }
                    }

                    int numFireworks = 1;
                    if (val.length > 5 && val[5] != null) {
                        try {
                            numFireworks = Integer.parseInt(val[5]);
                        } catch (NumberFormatException ignored) {
                            Bukkit.getLogger().warning("[Glide] invalid number of particles: " + val[5]);
                            continue;
                        }
                    }

                    int power = 0;
                    if (val.length > 6 && val[6] != null) {
                        try {
                            power = Integer.parseInt(val[6]);
                        } catch (NumberFormatException ignored) {
                            Bukkit.getLogger().warning("[Glide] invalid power: " + val[6]);
                            continue;
                        }
                    }

                    Color color = Color.WHITE;
                    if (val.length > 7 && val[7] != null) {
                        try {
                            color = Color.fromRGB(Integer.parseInt(val[7], 16));
                        } catch (IllegalArgumentException ignored) {
                            Bukkit.getLogger().warning("[Glide] invalid particle color: " + val[7]);
                            continue;
                        }
                    }

                    Color fade = Color.WHITE;
                    if (val.length > 8 && val[8] != null) {
                        try {
                            fade = Color.fromRGB(Integer.parseInt(val[8], 16));
                        } catch (IllegalArgumentException ignored) {
                            Bukkit.getLogger().warning("[Glide] invalid particle color: " + val[8]);
                            continue;
                        }
                    }

                    boolean flicker = false;
                    if (val.length > 9 && val[9] != null) {
                        try {
                            flicker = Boolean.parseBoolean(val[9]);
                        } catch (NumberFormatException ignored) {
                            Bukkit.getLogger().warning("[Glide] invalid flicker setting: " + val[9]);
                            continue;
                        }
                    }

                    boolean trail = false;
                    if (val.length > 10 && val[10] != null) {
                        try {
                            trail = Boolean.parseBoolean(val[10]);
                        } catch (NumberFormatException ignored) {
                            Bukkit.getLogger().warning("[Glide] invalid trail setting: " + val[10]);
                            continue;
                        }
                    }

                    fireworks.add(new Object[]{type, numFireworks, power, color, fade, flicker, trail});
                    break;
                }
            }
        }

        for(Map.Entry<Sound,float[]> entry : sounds.entrySet()) {
            player.playSound(player.getLocation(),entry.getKey(),entry.getValue()[0],entry.getValue()[1]);
        }

        player.addPotionEffects(potionEffects);
        playerPotionEffects.put(player.getUniqueId(),potionEffects);

        for(Object[] particle : particles) {
            Particle p = (Particle) particle[0];
            player.spawnParticle(p,player.getLocation(), (Integer) particle[1]);
        }
        for(Object[] note : notes) {
            player.playNote(player.getLocation(),(Instrument) note[0],(Note) note[1]);
        }
        if(fireworks.size()>0) {
            Bukkit.getScheduler().runTask(Glide.getPlugin(), () -> {
                for (Object[] firework : fireworks) {
                    FireworkEffect.Type type = (FireworkEffect.Type) firework[0];
                    for (int i = 0; i < (int) firework[1]; i++) {
                        Firework f = (Firework) Objects.requireNonNull(player.getLocation().getWorld())
                                .spawnEntity(player.getLocation().clone().add(0, 1, 0), EntityType.FIREWORK);
                        FireworkMeta fwm = f.getFireworkMeta();

                        FireworkEffect fireworkEffect = FireworkEffect.builder()
                                .with(type)
                                .withColor((Color) firework[3])
                                .withFade((Color) firework[4])
                                .flicker((boolean) firework[5])
                                .trail((boolean) firework[6])
                                .build();
                        fwm.addEffect(fireworkEffect);
                        fwm.setPower((int)firework[2]);
                        f.setFireworkMeta(fwm);
                        if(fwm.getPower() == 0) {
                            if (Glide.getInvulnerablePlayers().contains(player.getUniqueId())) {
                                f.detonate();
                            } else {
                                Glide.getInvulnerablePlayers().add(player.getUniqueId());
                                f.detonate();
                                Bukkit.getScheduler().runTaskLater(Glide.getPlugin(), () -> Glide.getInvulnerablePlayers().remove(player.getUniqueId()), 1);
                            }
                        }
                    }
                }
            });
        }
    }
}
