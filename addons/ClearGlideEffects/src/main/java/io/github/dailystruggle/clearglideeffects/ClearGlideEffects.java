package io.github.dailystruggle.clearglideeffects;

import io.github.dailystruggle.glide.Commands.SubCommand;
import io.github.dailystruggle.glide.Glide;
import io.github.dailystruggle.glide.SendMessage;
import io.github.dailystruggle.glide.configuration.Configs;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionAttachmentInfo;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;
import java.util.stream.Collectors;

public final class ClearGlideEffects extends JavaPlugin {
    public static SubCommand subCommand;

    private static class ClearEffectsCmd implements CommandExecutor{
        public Set<String> subParams = null;

        @Override
        public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
            if(subParams == null) {
                subParams = new HashSet<>(Objects.requireNonNull(subCommand.getSubParams("glide.clearEffects")));
            }

            Configs configs = Glide.getConfigs();
            RegisteredServiceProvider<Permission> rsp = Bukkit.getServer().getServicesManager().getRegistration(Permission.class);
            if(rsp == null) {
                System.out.println("failed to get rsp");
                return true;
            }

            Permission vaultPerms = rsp.getProvider();

            Map<String,String> clearEffectsArgs = new HashMap<>();
            for(int i = 0; i < args.length; i++) {
                int idx = args[i].indexOf(':');
                String arg = idx>0 ? args[i].substring(0,idx) : args[i];
                if(subParams.contains(arg) && idx < args[i].length()-1) {
                    clearEffectsArgs.putIfAbsent(arg,args[i].substring(idx+1)); //only use first instance
                }
            }

            if(!(sender instanceof Player) && !clearEffectsArgs.containsKey("player")){
                SendMessage.sendMessage(sender,configs.lang.getLog("consoleCmdNotAllowed"));
                return true;
            }

            String playerName;
            if(!clearEffectsArgs.containsKey("player"))
                playerName = sender.getName();
            else playerName = clearEffectsArgs.get("player");
            Player player = Bukkit.getPlayer(playerName);
            if(player != null) {
                Set<PermissionAttachmentInfo> perms = player.getEffectivePermissions();
                ArrayList<String> effects = new ArrayList<>(perms.size());

                for (PermissionAttachmentInfo perm : perms) {
                    if (!perm.getValue()) continue;
                    String node = perm.getPermission();
                    if (!node.startsWith("glide.effect.")) continue;
                    effects.add(node);
                }

                for(String effect : effects) {
                    vaultPerms.playerRemove(null,player,effect);
                    for(World world : Bukkit.getWorlds()) {
                        vaultPerms.playerRemove(world.getName(),player,effect);
                    }
                }
            }
            return true;
        }
    }


    @Override
    public void onEnable() {
        // Plugin startup logic
        subCommand = new SubCommand("glide.clearEffects",new ClearEffectsCmd());
        subCommand.setSubParam("player","glide.clearEffects", SubCommand.ParamType.PLAYER);
        Glide.setSubCommand("clearEffects",subCommand);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
