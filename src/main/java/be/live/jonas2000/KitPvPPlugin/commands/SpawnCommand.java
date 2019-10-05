package be.live.jonas2000.KitPvPPlugin.commands;

import be.live.jonas2000.KitPvPPlugin.Main;
import be.live.jonas2000.KitPvPPlugin.files.LocationFile;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SpawnCommand implements CommandExecutor {

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Player player = (Player) sender;
        Main.addPlayerInLobby(player);
        final World kitPvP = Bukkit.getWorld(LocationFile.getLocationFile().getString("Locations.Spawn.WorldName"));
        player.teleport(new Location(kitPvP
                , LocationFile.getLocationFile().getInt("Locations.Spawn.X")
                , LocationFile.getLocationFile().getInt("Locations.Spawn.Y")
                , LocationFile.getLocationFile().getInt("Locations.Spawn.Z")
                , (float) LocationFile.getLocationFile().getDouble("Locations.Spawn.Yaw")
                , (float) LocationFile.getLocationFile().getDouble("Locations.Spawn.Pitch")));


        return false;
    }
}
