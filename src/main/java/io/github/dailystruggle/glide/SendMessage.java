package io.github.dailystruggle.glide;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.*;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SendMessage {
    private static UUID serverId = new UUID(0,0);

    public static void sendMessage(CommandSender sender, Player player, String message) {
        if(message.equals("")) return;
        sendMessage(player,message);
        if(sender instanceof Player) {
            if(!sender.getName().equals(player.getName())) {
                sendMessage((Player) sender, message);
            }
        }
        else {
            message = format(player,message);
            sender.sendMessage(message);
        }

    }

    public static void sendMessage(CommandSender sender, String message) {
        if(message.equals("")) return;
        if(sender instanceof Player) sendMessage((Player) sender,message);
        else {
            message = format(Bukkit.getOfflinePlayer(serverId).getPlayer(),message);
            sender.sendMessage(message);
        }
    }

    public static void sendMessage(Player player, String message) {
        if(message.equals("")) return;
        message = format(player,message);
        BaseComponent[] components = TextComponent.fromLegacyText(message);
        player.spigot().sendMessage(components);
    }

    public static void sendMessage(CommandSender sender, String message, String hover, String click) {
        if(message.equals("")) return;

        Player player;
        if(sender instanceof Player) player = (Player) sender;
        else player = Bukkit.getOfflinePlayer(serverId).getPlayer();

        message = format(player,message);
        BaseComponent[] textComponents = TextComponent.fromLegacyText(message);

        if(!hover.equals("")) {
            BaseComponent[] hoverComponents = TextComponent.fromLegacyText(format(player,hover));
            HoverEvent hoverEvent = new HoverEvent(HoverEvent.Action.SHOW_TEXT, hoverComponents);
            for(BaseComponent component : textComponents) {
                component.setHoverEvent(hoverEvent);
            }
        }

        if(!click.equals("") ) {
            ClickEvent clickEvent = new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, click);
            for(BaseComponent component : textComponents) {
                component.setClickEvent(clickEvent);
            }
        }

        player.spigot().sendMessage(textComponents);
    }

    public static String format(Player player,String text) {
        text = PAPIChecker.fillPlaceholders(player,text);
        text = ChatColor.translateAlternateColorCodes('&',text);
        text = Hex2Color(text);
        return text;
    }

    private static String Hex2Color(String text) {
        Pattern pattern = Pattern.compile("(&?#[0-9a-fA-F]{6})");
        Matcher matcher = pattern.matcher(text);
        while (matcher.find()) {
            String hexColor = text.substring(matcher.start(), matcher.end());
            hexColor = hexColor.replace("&", "");
            StringBuilder bukkitColorCode = new StringBuilder('\u00A7' + "x");
            for (int i = 1; i < hexColor.length(); i++) {
                bukkitColorCode.append(String.valueOf('\u00A7') + hexColor.charAt(i));
            }
            String bukkitColor = bukkitColorCode.toString().toLowerCase();
            text = text.replaceAll(hexColor, bukkitColor);
            matcher.reset(text);
        }
        return text;
    }
}
