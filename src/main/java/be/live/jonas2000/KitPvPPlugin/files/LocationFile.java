package be.live.jonas2000.KitPvPPlugin.files;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;

public class LocationFile {

    private static File file;
    private static FileConfiguration LocationFile;

    public static void create() {
        file = new File(Bukkit.getServer().getPluginManager().getPlugin("KitPvPPlugin").getDataFolder(), "Locations.yml");

        try {
            if (!file.exists()) {
                file.createNewFile();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        LocationFile = YamlConfiguration.loadConfiguration(file);
    }

    public static FileConfiguration getLocationFile() {
        return LocationFile;
    }

    public static File getFile() {
        return file;
    }

    public static void save() {
        try {
            LocationFile.save(file);
        } catch (IOException e) {
            System.out.println("Opslaan is niet gelukt");
        }
    }
}
