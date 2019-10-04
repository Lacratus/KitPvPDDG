package be.live.jonas2000.KitPvPPlugin;

//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import be.live.jonas2000.KitPvPPlugin.commands.ModeratorCommand;
import be.live.jonas2000.KitPvPPlugin.commands.SpawnCommand;
import be.live.jonas2000.KitPvPPlugin.files.LocationFile;
import be.live.jonas2000.KitPvPPlugin.files.SelectedKitFile;
import be.live.jonas2000.KitPvPPlugin.files.TempStatisticsFile;
import be.live.jonas2000.KitPvPPlugin.listeners.PlayerListener;
import be.live.jonas2000.KitPvPPlugin.nms.HeaderFooter;
import be.live.jonas2000.KitPvPPlugin.tabcompleter.ModTabCompleter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;

public class Main extends JavaPlugin {
    private static Connection connection;
    private String host;
    private String database;
    private String username;
    private String password;
    private int port;
    private static Main plugin;
    private static List<Player> playersInLobby = new ArrayList<>();

    public Main() {
    }

    @Override
    public void onEnable() {
        plugin = this;
        Bukkit.getPluginManager().registerEvents(new PlayerListener(), this);
        Bukkit.getPluginManager().registerEvents(new HeaderFooter(), this);
        getCommand("mod").setExecutor(new ModeratorCommand());
        getCommand("mod").setTabCompleter(new ModTabCompleter());
        getCommand("moderator").setExecutor(new ModeratorCommand());
        getCommand("spawn").setExecutor(new SpawnCommand());

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
        //TempStatisticsFile Creation
        TempStatisticsFile.create();
        TempStatisticsFile.getTempStatisticsFile().options().copyDefaults(true);
        TempStatisticsFile.save();

        //LocationFile Creation
        LocationFile.create();
        LocationFile.getLocationFile().options().copyDefaults(true);
        LocationFile.save();
        //SelectedKitFile Creation
        SelectedKitFile.create();
        SelectedKitFile.getSelectedKitFile().options().copyDefaults(true);
        SelectedKitFile.save();
        //Default Spawn Creation
        if (!LocationFile.getLocationFile().contains("Locations.Spawn")) {
            List<String> worlds = new ArrayList<>(Arrays.asList(Bukkit.getWorlds().toString()));
            LocationFile.getLocationFile().set("Locations.Spawn.X", 0);
            LocationFile.getLocationFile().set("Locations.Spawn.Y", 100);
            LocationFile.getLocationFile().set("Locations.Spawn.Z", 0);
            LocationFile.getLocationFile().set("Locations.Spawn.Yaw", 0);
            LocationFile.getLocationFile().set("Locations.Spawn.Pitch", 0);
            LocationFile.getLocationFile().set("Locations.Spawn.WorldName", worlds.get(0));
            LocationFile.save();
        }

    }

    private void openConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            connection = DriverManager.getConnection("jdbc:mysql://" + this.host + ":" + this.port + "/" + this.database, this.username, this.password);
        }
    }

    public static PreparedStatement prepareStatement(String query) {
        PreparedStatement ps = null;

        try {
            ps = connection.prepareStatement(query);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return ps;
    }

    public static Main getPlugin() {
        return plugin;
    }

    public static List<Player> getPlayersInLobby() {
        return playersInLobby;
    }

    public static void addPlayerInLobby(Player player) {
        playersInLobby.add(player);
    }

    public static void removePlayerInLobby(Player player) {
        playersInLobby.remove(player);
    }


    public static void buildSidebar(Player player) throws IllegalArgumentException, IllegalStateException, SQLException {
        double ratio;
        double ratioRoundUp = 0;
        String UUID = player.getUniqueId().toString();
        Scoreboard board = Bukkit.getScoreboardManager().getNewScoreboard();
        Objective obj = board.registerNewObjective("test", "dummy");
        obj.setDisplayName("KitPvP");
        obj.setDisplaySlot(DisplaySlot.SIDEBAR);
        Score under = obj.getScore(ChatColor.YELLOW + "www.Lacratus.be");
        under.setScore(1);
        ResultSet killSet = prepareStatement("SELECT KILLS FROM player_info WHERE UUID = '" + UUID + "';").executeQuery();
        killSet.next();
        int kill = killSet.getInt("KILLS") + TempStatisticsFile.getTempStatisticsFile().getInt("Players." + UUID + ".KILLS");
        Score kills = obj.getScore("Kills: " + kill);
        kills.setScore(9);
        ResultSet deathSet = prepareStatement("SELECT DEATHS FROM player_info WHERE UUID = '" + UUID + "';").executeQuery();
        deathSet.next();
        int death = deathSet.getInt("DEATHS") + TempStatisticsFile.getTempStatisticsFile().getInt("Players." + UUID + ".DEATHS");
        Score deaths = obj.getScore("Deaths: " + death);
        deaths.setScore(7);
        ResultSet coinSet = prepareStatement("SELECT COINS FROM player_info WHERE UUID = '" + UUID + "';").executeQuery();
        coinSet.next();
        int coin = coinSet.getInt("COINS") + TempStatisticsFile.getTempStatisticsFile().getInt("Players." + UUID + ".COINS");
        Score coins = obj.getScore("Coins: " + coin);
        coins.setScore(5);
        if (death == 0) {
            ratio = kill;
            Score rat = obj.getScore("Ratio: " + ratio);
            rat.setScore(3);
        } else {
            ratio = (double) kill / death;
            ratioRoundUp = (double) Math.round(ratio * 100) / 100;
            Score rat = obj.getScore("Ratio: " + ratioRoundUp);
            rat.setScore(3);
        }
        player.setScoreboard(board);

    }

    public static void updateSidebar(Player player, String killDeathDisconnectRestart) throws SQLException {
        double newRatio;
        double oldRatio;
        double newRatioRoundUp = 0;

        String UUID = player.getUniqueId().toString();
        // Creëeren van Resultsets
        ResultSet killSet = prepareStatement("SELECT KILLS FROM player_info WHERE UUID = '" + player.getUniqueId() + "';").executeQuery();
        killSet.next();
        ResultSet deathSet = prepareStatement("SELECT DEATHS FROM player_info WHERE UUID = '" + player.getUniqueId() + "';").executeQuery();
        deathSet.next();
        ResultSet coinSet = prepareStatement("SELECT COINS FROM player_info WHERE UUID = '" + player.getUniqueId() + "';").executeQuery();
        coinSet.next();
        // Bekijken variabelen
        int kill = killSet.getInt("KILLS") + TempStatisticsFile.getTempStatisticsFile().getInt("Players." + UUID + ".Kills");
        int possibleOldKills = kill - 1;
        int death = deathSet.getInt("DEATHS") + TempStatisticsFile.getTempStatisticsFile().getInt("Players." + UUID + ".Deaths");
        int possibleOldDeaths = death - 1;
        int coins = coinSet.getInt("COINS") + TempStatisticsFile.getTempStatisticsFile().getInt("Players." + UUID + ".Coins");
        if (death == 0) {
            newRatioRoundUp = kill;
        } else {
            newRatio = (double) kill / death;
            newRatioRoundUp = (double) Math.round(newRatio * 100) / 100;
        }
        //reset Ratio
        if (killDeathDisconnectRestart.equals("Kill")) {
            oldRatio = (double) (kill - 1) / death;
            double oldRatioRoundUp = (double) Math.round(oldRatio * 100) / 100;
            player.getScoreboard().resetScores("Ratio: " + oldRatioRoundUp);
        } else if (killDeathDisconnectRestart.equals("Death")) {
            oldRatio = (double) kill / (death - 1);
            double oldRatioRoundUp = (double) Math.round(oldRatio * 100) / 100;
            player.getScoreboard().resetScores("Ratio: " + oldRatioRoundUp);
        }

        //resetten en updaten scoreboard
        player.getScoreboard().resetScores("Kills: " + (possibleOldKills));
        player.getScoreboard().getObjective("test").getScore("Kills: " + kill).setScore(9);
        player.getScoreboard().resetScores("Deaths: " + (possibleOldDeaths));
        player.getScoreboard().getObjective("test").getScore("Deaths: " + death).setScore(7);
        player.getScoreboard().resetScores("Coins: " + (coins - 10));
        player.getScoreboard().getObjective("test").getScore("Coins: " + coins).setScore(5);
        player.getScoreboard().getObjective("test").getScore("Ratio: " + newRatioRoundUp).setScore(3);
    }


    @Override
    public void onDisable() {
        ConfigurationSection sec = TempStatisticsFile.getTempStatisticsFile().getConfigurationSection("Players");
        for (String stringUUID : sec.getKeys(false)) {
            int kills = TempStatisticsFile.getTempStatisticsFile().getInt("Players." + stringUUID + ".Kills");
            int deaths = TempStatisticsFile.getTempStatisticsFile().getInt("Players." + stringUUID + ".Deaths");

            try {
                Main.prepareStatement("UPDATE player_info SET DEATHS = DEATHS + "
                        + TempStatisticsFile.getTempStatisticsFile().get("Players." + stringUUID + ".Deaths")
                        + ",KILLS = KILLS + "
                        + TempStatisticsFile.getTempStatisticsFile().get("Players." + stringUUID + ".Kills")
                        + ",COINS = COINS + "
                        + TempStatisticsFile.getTempStatisticsFile().get("Players." + stringUUID + ".Coins")
                        + " WHERE UUID = '" + stringUUID + "';").executeUpdate();
                Main.updateSidebar(Bukkit.getPlayer(UUID.fromString(stringUUID)), "Restart");
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
        TempStatisticsFile.getFile().delete();

    }
}

