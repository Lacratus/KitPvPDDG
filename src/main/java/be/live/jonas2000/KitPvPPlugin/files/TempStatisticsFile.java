package be.live.jonas2000.KitPvPPlugin.files;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;

public class TempStatisticsFile {

    private static File file;
    private static FileConfiguration tempStatisticsFile;

    public static void create() {
        file = new File(Bukkit.getServer().getPluginManager().getPlugin("KitPvPPlugin").getDataFolder(), "TempStatistics.yml");

        try {
            if (!file.exists()) {
                file.createNewFile();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        tempStatisticsFile = YamlConfiguration.loadConfiguration(file);
    }

    public static FileConfiguration getTempStatisticsFile() {
        return tempStatisticsFile;
    }

    public static File getFile() {
        return file;
    }

    public static void save() {
        try {
            tempStatisticsFile.save(file);
        } catch (IOException e) {
            System.out.println("Opslaan is niet gelukt");
        }
    }
}
