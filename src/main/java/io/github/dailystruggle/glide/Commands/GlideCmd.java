package io.github.dailystruggle.glide.Commands;

import io.github.dailystruggle.glide.Glide;
import io.github.dailystruggle.glide.SendMessage;
import io.github.dailystruggle.glide.Tasks.SetupGlide;
import io.github.dailystruggle.glide.configuration.Configs;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class GlideCmd implements CommandExecutor {
    private final Glide plugin;
    private final Configs configs;

    private final SubCommand glideCommands;

    public GlideCmd(SubCommand command) {
        this.plugin = Glide.getPlugin();
        this.configs = Glide.getConfigs();

        glideCommands = command;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        if(!command.getName().equals("glide")) return true;

        if(args.length > 0 && glideCommands.getSubCommands().containsKey(args[0])) {
            if(!sender.hasPermission(glideCommands.getSubCommands().get(args[0]).getPerm())) {
                String msg = configs.lang.getLog("noPerms");
                SendMessage.sendMessage(sender,msg);
            }
            else {
                return Objects.requireNonNull(glideCommands.getSubCommands().get(args[0]).getCommandExecutor())
                        .onCommand(sender,command,label,Arrays.copyOfRange(args, 1, args.length));
            }
            return true;
        }

        if(!sender.hasPermission("glide.use")) {
            SendMessage.sendMessage(sender,configs.lang.getLog("noPerms"));
            return true;
        }

        Map<String,String> glideArgs = new HashMap<>();
        for (String s : args) {
            int idx = s.indexOf(':');
            String arg = idx > 0 ? s.substring(0, idx) : s;
            Map<String,String> subParams = glideCommands.getAllSubParams();
            if (subParams.containsKey(arg) && sender.hasPermission(subParams.get(arg)) && idx < s.length() - 1) {
                glideArgs.putIfAbsent(arg, s.substring(idx + 1)); //only use first instance
            }
        }

        String playerName;
        if(!glideArgs.containsKey("player")) {
            if(!(sender instanceof Player)) {
                SendMessage.sendMessage(sender,configs.lang.getLog("consoleCmdNotAllowed"));
                return true;
            }
            else playerName = sender.getName();
        }
        else playerName = glideArgs.get("player");
        Player player = Bukkit.getPlayer(playerName);
        if(player == null) {
            SendMessage.sendMessage(sender,configs.lang.getLog("badArg","player:"+playerName));
            return true;
        }

        new SetupGlide(player.getUniqueId(), configs).runTask(plugin);

        return true;
    }
}
