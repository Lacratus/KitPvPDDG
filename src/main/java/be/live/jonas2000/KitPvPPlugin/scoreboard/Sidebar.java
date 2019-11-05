package be.live.jonas2000.KitPvPPlugin.scoreboard;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;

public class Sidebar {
    private Player player;
    private int kill;
    private int death;
    private int coin;

    public Sidebar(Player player, int kill, int death, int coin) {
        this.player = player;
        this.kill = kill;
        this.death = death;
        this.coin = coin;
        Build();
    }

    public void Build() {
        double ratio;
        double ratioRoundUp = 0;
        Scoreboard board = Bukkit.getScoreboardManager().getNewScoreboard();
        Objective obj = board.registerNewObjective("test", "dummy");
        obj.setDisplayName("KitPvP");
        obj.setDisplaySlot(DisplaySlot.SIDEBAR);
        Score under = obj.getScore(ChatColor.YELLOW + "www.Lacratus.be");
        under.setScore(1);
        Score kills = obj.getScore("Kills: " + kill);
        kills.setScore(9);
        Score deaths = obj.getScore("Deaths: " + death);
        deaths.setScore(7);
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

    public void update(Player player, boolean killed) {
        double newRatio;
        double oldRatio;
        double newRatioRoundUp = 0;

        if(!killed){
            kill = kill +1;
            coin = coin + 10;
        } else {
            death = death + 1;
        }
        // Bekijken variabelen
        int possibleOldKills = kill - 1;
        int possibleOldDeaths = death - 1;
        if (death == 0) {
            newRatioRoundUp = kill;
        } else {
            newRatio = (double) kill / death;
            newRatioRoundUp = (double) Math.round(newRatio * 100) / 100;
        }
        //reset Ratio
        if (!killed) {
            oldRatio = (double) (kill - 1) / death;
            double oldRatioRoundUp = (double) Math.round(oldRatio * 100) / 100;
            player.getScoreboard().resetScores("Kills: " + (possibleOldKills));
            player.getScoreboard().getObjective("test").getScore("Kills: " + kill).setScore(9);
            player.getScoreboard().resetScores("Coins: " + (coin - 10));
            player.getScoreboard().getObjective("test").getScore("Coins: " + coin).setScore(5);
            player.getScoreboard().resetScores("Ratio: " + oldRatioRoundUp);
            player.getScoreboard().getObjective("test").getScore("Ratio: " + newRatioRoundUp).setScore(3);
        } else {
            oldRatio = (double) kill / (death - 1);
            double oldRatioRoundUp = (double) Math.round(oldRatio * 100) / 100;
            player.getScoreboard().resetScores("Deaths: " + (possibleOldDeaths));
            player.getScoreboard().getObjective("test").getScore("Deaths: " + death).setScore(7);
            player.getScoreboard().resetScores("Ratio: " + oldRatioRoundUp);
            player.getScoreboard().getObjective("test").getScore("Ratio: " + newRatioRoundUp).setScore(3);
        }
    }
}
