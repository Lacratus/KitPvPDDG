package be.live.jonas2000.KitPvPPlugin.commands;

import be.live.jonas2000.KitPvPPlugin.files.LocationFile;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SetWarpCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args){
        if(sender instanceof  Player) {
            Player player = (Player) sender;
            String warpName = args[0];

            createLocation(player,warpName);


        }


        return false;
    }

    public static void createLocation(Player player,String locatieNaam) {
        Location loc = player.getLocation();
        double x = loc.getX();
        double y = loc.getY();
        double z = loc.getZ();
        double yaw = loc.getYaw();
        double pitch = loc.getPitch();
        String worldName = loc.getWorld().getName();

        if (!LocationFile.getLocationFile().contains("Locations." + locatieNaam)) {
            zetLocatieInConfig(locatieNaam, x, y, z, yaw, pitch, worldName);

            player.sendMessage(locatieNaam +" set!");
        } else{
            zetLocatieInConfig(locatieNaam, x, y, z, yaw, pitch, worldName);
        }


    }

    public static void zetLocatieInConfig(String locatieNaam, double x, double y, double z, double yaw, double pitch, String worldName) {
        LocationFile.getLocationFile().set("Locations." + locatieNaam +".X", x);
        LocationFile.getLocationFile().set("Locations." + locatieNaam +".Y", y);
        LocationFile.getLocationFile().set("Locations." + locatieNaam +".Z", z);
        LocationFile.getLocationFile().set("Locations." + locatieNaam +".Yaw", yaw);
        LocationFile.getLocationFile().set("Locations." + locatieNaam +".Pitch", pitch);
        LocationFile.getLocationFile().set("Locations." + locatieNaam +".WorldName", worldName);
        LocationFile.save();
    }
}
