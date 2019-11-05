package be.live.jonas2000.KitPvPPlugin.tabcompleter;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class ModTabCompleter implements TabCompleter {
    private final String[] COMMANDS = {"setSpawn","setWarp","warpList","delWarp","kick","ban"};

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args){
        final List<String> tabCompletions = new ArrayList<>();

        StringUtil.copyPartialMatches(args[0], Arrays.asList(COMMANDS), tabCompletions);
        Collections.sort(tabCompletions);
        return tabCompletions;
    }
}
