package be.live.jonas2000.KitPvPPlugin;

//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.*;

import be.live.jonas2000.KitPvPPlugin.objects.KitPvPPlayer;
import be.live.jonas2000.KitPvPPlugin.commands.ModeratorCommand;
import be.live.jonas2000.KitPvPPlugin.commands.SpawnCommand;
import be.live.jonas2000.KitPvPPlugin.files.ConfigFile;
import be.live.jonas2000.KitPvPPlugin.listeners.PlayerListener;
import be.live.jonas2000.KitPvPPlugin.nms.HeaderFooter;
import be.live.jonas2000.KitPvPPlugin.tabcompleter.ModTabCompleter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin {
    private Connection connection;
    private String host;
    private String database;
    private String username;
    private String password;
    private int port;
    private Main plugin;
    private List<Player> playersInLobby = new ArrayList<>();
    private PlayerListener listener;
    private ConfigFile locationFile;
    private ConfigFile selectedKitFile;
    // Name of files
    String locationFileName ="Locations.yml";
    String selectedKitFileName ="selectedKits.yml";

    public Main() {
    }

    @Override
    public void onEnable() {
        plugin = this;
        //file creation

            this.locationFile = new ConfigFile(locationFileName);
            this.selectedKitFile = new ConfigFile(selectedKitFileName);


        this.listener = new PlayerListener(this);
        // Listeners and Commands
        Bukkit.getPluginManager().registerEvents(listener, this);
        Bukkit.getPluginManager().registerEvents(new HeaderFooter(), this);
        getCommand("mod").setExecutor(new ModeratorCommand(this));
        getCommand("mod").setTabCompleter(new ModTabCompleter());
        getCommand("moderator").setExecutor(new ModeratorCommand(this));
        getCommand("spawn").setExecutor(new SpawnCommand(this));

        //Database connection
        this.host = "localhost";
        this.port = 3306;
        this.database = "kitpvpddg";
        this.username = "root";
        this.password = "";

        try {
            this.openConnection();
            System.out.println("Works");
        } catch (SQLException e) {
            e.printStackTrace();
        }

        //Default Spawn Creation
        if (!locationFile.getConfig().contains("Locations.Spawn")) {
            locationFile.getConfig().set("Locations.Spawn.X", 0);
            locationFile.getConfig().set("Locations.Spawn.Y", 100);
            locationFile.getConfig().set("Locations.Spawn.Z", 0);
            locationFile.getConfig().set("Locations.Spawn.Yaw", 0);
            locationFile.getConfig().set("Locations.Spawn.Pitch", 0);
            locationFile.getConfig().set("Locations.Spawn.WorldName", "world");
        }
        //Database update every 15 Minutes
        int id = Bukkit.getServer().getScheduler().scheduleAsyncRepeatingTask(this, new Runnable() {
            @Override
            public void run() {
                updateDatabase();
                System.out.println("WORKING SCHEDULER");
                getServer().broadcastMessage("GESLAAGD");
            }
        }, 0, 1200L * 1);

    }


    private void openConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            connection = DriverManager.getConnection("jdbc:mysql://" + this.host + ":" + this.port + "/" + this.database, this.username, this.password);
        }
    }

    public PreparedStatement prepareStatement(String query) {
        PreparedStatement ps = null;

        try {
            ps = connection.prepareStatement(query);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return ps;
    }

    public Main getPlugin() {
        return plugin;
    }

    public List<Player> getPlayersInLobby() {
        return playersInLobby;
    }

    public void addPlayerInLobby(Player player) {
        playersInLobby.add(player);
    }

    public void removePlayerInLobby(Player player) {
        playersInLobby.remove(player);
    }





    @Override
    public void onDisable() {
        // Add statistics to database on restart/stop
        //ConfigurationSection sec = TempStatisticsFile.getTempStatisticsFile().getConfigurationSection("Players");
        updateDatabase();
    }

    public ConfigFile getLocationFile() {
        return locationFile;
    }

    public ConfigFile getSelectedKitFile() {
        return selectedKitFile;
    }

    public void updateDatabase(){
        Iterator iterator = listener.playerList.entrySet().iterator();
        while(iterator.hasNext()){
            Map.Entry MEUUID = (Map.Entry) iterator.next();
            KitPvPPlayer player = listener.playerList.get(MEUUID.getKey());
            int kills = player.getKills();
            int deaths = player.getDeaths();
            int coins = player.getCoins();

            try {
                plugin.prepareStatement("UPDATE player_info SET DEATHS = "
                        + deaths
                        + ",KILLS ="
                        + kills
                        + ",COINS ="
                        + coins
                        + " WHERE UUID = '" + MEUUID.getKey() + "';").executeUpdate();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
    }

}

