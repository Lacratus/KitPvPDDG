package be.live.jonas2000.KitPvPPlugin.commands;

import be.live.jonas2000.KitPvPPlugin.files.LocationFile;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SetSpawnCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args){

        if(sender instanceof Player) {
            Player player = (Player) sender;
            String name = "Spawn";

            SetWarpCommand.createLocation(player,name);

        }


        return false;
    }
}
