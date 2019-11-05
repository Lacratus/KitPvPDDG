package be.live.jonas2000.KitPvPPlugin.commands;

import be.live.jonas2000.KitPvPPlugin.Main;
import be.live.jonas2000.KitPvPPlugin.files.ConfigFile;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SpawnCommand implements CommandExecutor {

    private  Main plugin;
    private ConfigFile locationFile;

    public SpawnCommand(Main plugin) {
        this.plugin = plugin;
        locationFile = plugin.getLocationFile();
    }
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Player player = (Player) sender;
        plugin.addPlayerInLobby(player);
        final World kitPvP = Bukkit.getWorld(locationFile.getConfig().getString("Locations.Spawn.WorldName"));
        player.teleport(new Location(kitPvP
                , locationFile.getConfig().getInt("Locations.Spawn.X")
                , locationFile.getConfig().getInt("Locations.Spawn.Y")
                , locationFile.getConfig().getInt("Locations.Spawn.Z")
                , (float) locationFile.getConfig().getDouble("Locations.Spawn.Yaw")
                , (float) locationFile.getConfig().getDouble("Locations.Spawn.Pitch")));


        return false;
    }
}
