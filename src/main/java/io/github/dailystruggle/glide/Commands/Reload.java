package io.github.dailystruggle.glide.Commands;

import io.github.dailystruggle.glide.Glide;
import io.github.dailystruggle.glide.SendMessage;
import io.github.dailystruggle.glide.configuration.Configs;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class Reload implements CommandExecutor {
    private final Configs configs;

    public Reload() {
        this.configs = Glide.getConfigs();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(!sender.hasPermission("glide.reload")) return false;

        SendMessage.sendMessage(sender,configs.lang.getLog("reloading"));
        configs.refresh();
        SendMessage.sendMessage(sender,configs.lang.getLog("reloaded"));

        return true;
    }
}
