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

import java.util.*;

public class GlideCmd implements CommandExecutor {
    private final Glide plugin;
    private final Configs configs;

    private final Map<String, SubCommand> glideCommands;
    private final Map<String,String> glideParams = new HashMap<>();

    public GlideCmd(SubCommand command) {
        this.plugin = Glide.getPlugin();
        this.configs = Glide.getConfigs();

        for(Map.Entry<String, ArrayList<String>> entry : Objects.requireNonNull(command.getSubParams())) {
            for(String param : entry.getValue()) {
                glideParams.put(param,entry.getKey());
            }
        }
        glideCommands = command.getSubCommands();
    }

    public void setSubCommand(String name, SubCommand subCommand) {
        glideCommands.put(name,subCommand);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(!command.getName().equals("glide")) return true;

        if(args.length > 0 && glideCommands.containsKey(args[0])) {
            if(!sender.hasPermission(glideCommands.get(args[0]).getPerm())) {
                String msg = configs.lang.getLog("noPerms");
                SendMessage.sendMessage(sender,msg);
            }
            else {
                return Objects.requireNonNull(glideCommands.get(args[0]).getCommandExecutor())
                        .onCommand(sender,command,label,Arrays.copyOfRange(args, 1, args.length));
            }
            return true;
        }

        if(!sender.hasPermission("glide.use")) {
            SendMessage.sendMessage(sender,configs.lang.getLog("noPerms"));
            return true;
        }

        Map<String,String> glideArgs = new HashMap<>();
        for(int i = 0; i < args.length; i++) {
            int idx = args[i].indexOf(':');
            String arg = idx>0 ? args[i].substring(0,idx) : args[i];
            if(this.glideParams.containsKey(arg) && sender.hasPermission(glideParams.get(arg)) && idx < args[i].length()-1) {
                glideArgs.putIfAbsent(arg,args[i].substring(idx+1)); //only use first instance
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

        new SetupGlide(player, configs).runTask(plugin);

        return true;
    }
}
