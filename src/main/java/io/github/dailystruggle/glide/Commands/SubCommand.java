package io.github.dailystruggle.glide.Commands;

import org.bukkit.command.CommandExecutor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class SubCommand {
    public enum ParamType {
        BOOLEAN,
        PLAYER,
        COORDINATE,
        WORLD,
        BIOME,
        NONE
    }

    private final String perm;

    private final CommandExecutor commandExecutor;

    //parameter name,perm, for
    private final Map<String,String> subParams = new HashMap<>();

    //what type of param this is, for tabcompletion
    private final Map<String,ParamType> subParamTypes = new HashMap<>();

    //perm, list of params
    private final Map<String, ArrayList<String>> subParamsPermList = new HashMap<>();

    //command name, commands
    private final Map<String, SubCommand> commands = new HashMap<>();

    public SubCommand(String perm, CommandExecutor commandExecutor) {
        this.perm = perm;
        this.commandExecutor = commandExecutor;
    }

    public @NotNull String getPerm() {
        return perm;
    }

    public CommandExecutor getCommandExecutor() {
        return this.commandExecutor;
    }

    public void setSubCommand(@NotNull String name, @NotNull SubCommand subCommand) {
        commands.put(name, subCommand);
    }

    public SubCommand getSubCommand(@NotNull String name) {
        return commands.get(name);
    }

    public @NotNull Map<String, SubCommand> getSubCommands() {
        return commands;
    }

    public void setSubParam(@NotNull String name, @NotNull String perm, @Nullable ParamType type) {
        if(type == null) type = ParamType.NONE;
        subParams.put(name,perm);
        subParamTypes.put(name,type);
        subParamsPermList.putIfAbsent(perm,new ArrayList<>());
        subParamsPermList.get(perm).add(name);
    }

    @Nullable
    public String getSubParamPerm(@NotNull String name) {
        return subParams.get(name);
    }

    @Nullable
    public ParamType getSubParamType(@NotNull String name) {
        return subParamTypes.get(name);
    }

    public @Nullable Set<Map.Entry<String, ArrayList<String>>> getSubParams() {
        return subParamsPermList.entrySet();
    }

    public @NotNull Map<String, String> getAllSubParams() {
        return subParams;
    }
}
