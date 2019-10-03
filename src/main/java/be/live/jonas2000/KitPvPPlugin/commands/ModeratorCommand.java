package be.live.jonas2000.KitPvPPlugin.commands;

import be.live.jonas2000.KitPvPPlugin.files.LocationFile;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ModeratorCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

        if (sender instanceof Player) {
            Player player = (Player) sender;

            // Set Spawn Location
            if (args[0].equalsIgnoreCase("setSpawn")) {
                String name = "Spawn";
                createLocation(player, name);

                // Set Warp Locations
            } else if (args[0].equalsIgnoreCase("setWarp")) {
                String input = args[1];
                String warpName = input.substring(0, 1).toUpperCase() + input.substring(1).toLowerCase();
                createLocation(player, warpName);

                // Get Warp Locations
            } else if (args[0].equalsIgnoreCase("warpList")) {


                StringBuilder message = new StringBuilder();

                ConfigurationSection sec = LocationFile.getLocationFile().getConfigurationSection("Locations");
                for (String locationName: sec.getKeys(false)) {
                    message.append(locationName).append(", ");
                }
                player.sendMessage(message.toString());

                // Delete Warp Location
            } else if (args[0].equalsIgnoreCase("delWarp")) {
                String input = args[1];
                String warpName = input.substring(0, 1).toUpperCase() + input.substring(1).toLowerCase();
                LocationFile.getLocationFile().set("Locations." + warpName, null);

                // Kick player
            } else if (args[0].equalsIgnoreCase("kick")) { // KICK COMMAND
                if (args.length == 1) {
                    player.sendMessage("/mod kick <Name> <Reason>");
                } else if (args.length == 2) {
                    String playerName = args[1];
                    if (Bukkit.getPlayer(playerName) != null) {
                        Player kickedPlayer = Bukkit.getPlayer(playerName);
                        kickedPlayer.kickPlayer(ChatColor.RED + "You have been kicked");
                    } else {
                        player.sendMessage("Player is not Online!");
                    }
                } else {
                    String playerName = args[1];
                    if (Bukkit.getPlayer(playerName) != null) {
                        Player kickedPlayer = Bukkit.getPlayer(playerName);
                        StringBuilder message = new StringBuilder();
                        for (int i = 2; i < args.length; i++) {
                            message.append(" ").append(args[i]);
                        }
                        kickedPlayer.kickPlayer(ChatColor.RED + message.toString());

                    } else {
                        player.sendMessage("Player is not Online!");
                    }
                }
                // Ban player
            } else if (args[0].equalsIgnoreCase("ban")) {

            }

        }
        return false;
    }

    private static void createLocation(Player player, String locatieNaam) {
        Location loc = player.getLocation();
        double x = loc.getX();
        double y = loc.getY();
        double z = loc.getZ();
        double yaw = loc.getYaw();
        double pitch = loc.getPitch();
        String worldName = loc.getWorld().getName();

        if (!LocationFile.getLocationFile().contains("Locations." + locatieNaam)) {
            zetLocatieInConfig(locatieNaam, x, y, z, yaw, pitch, worldName);

            player.sendMessage(locatieNaam + " set!");
        } else {
            zetLocatieInConfig(locatieNaam, x, y, z, yaw, pitch, worldName);
            player.sendMessage(locatieNaam + " set!");
        }


    }

    private static void zetLocatieInConfig(String locatieNaam, double x, double y, double z, double yaw,
                                           double pitch, String worldName) {
        LocationFile.getLocationFile().set("Locations." + locatieNaam + ".X", x);
        LocationFile.getLocationFile().set("Locations." + locatieNaam + ".Y", y);
        LocationFile.getLocationFile().set("Locations." + locatieNaam + ".Z", z);
        LocationFile.getLocationFile().set("Locations." + locatieNaam + ".Yaw", yaw);
        LocationFile.getLocationFile().set("Locations." + locatieNaam + ".Pitch", pitch);
        LocationFile.getLocationFile().set("Locations." + locatieNaam + ".WorldName", worldName);
        LocationFile.save();
    }
}
