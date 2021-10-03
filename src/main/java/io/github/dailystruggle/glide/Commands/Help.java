package io.github.dailystruggle.glide.Commands;

import io.github.dailystruggle.glide.Glide;
import io.github.dailystruggle.glide.SendMessage;
import io.github.dailystruggle.glide.configuration.Configs;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.util.HashMap;
import java.util.Map;

public class Help implements CommandExecutor {
    private final Configs configs;
    private final Map<String,String> perms = new HashMap<String,String>();

    public Help() {
        this.configs = Glide.getConfigs();
        this.perms.put("glide","glide.use");
        this.perms.put("help","glide.use");
        this.perms.put("reload","glide.reload");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(!sender.hasPermission("glide.use")) {
            SendMessage.sendMessage(sender,configs.lang.getLog("noPerms"));
            return true;
        }

        for(Map.Entry<String,String> entry : perms.entrySet()) {
            if(sender.hasPermission(entry.getValue())){
                String arg;
                if(entry.getKey().equals("glide")) arg = "";
                else arg = entry.getKey();

                String msg = configs.lang.getLog(entry.getKey());
                String hover = "/glide " + arg;
                String click = "/glide " + arg;

                SendMessage.sendMessage(sender,msg,hover,click);
            }
        }

        return true;
    }
}

