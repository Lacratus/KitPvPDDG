package be.live.jonas2000.KitPvPPlugin.objects;

public class KitPvPPlayer  {



    private String UUID;
    private int kills;
    private int deaths;
    private int coins;

    public KitPvPPlayer(String UUID){
        this.UUID = UUID;
        this.kills = 0;
        this.deaths = 0;
        this.coins = 0;
    }

    public KitPvPPlayer(String UUID, int kills, int deaths, int coins) {
        this.UUID = UUID;
        this.kills = kills;
        this.deaths = deaths;
        this.coins = coins;
    }

    public int getCoins() {
        return coins;
    }

    public void setCoins(int coins) {
        this.coins = coins;
    }


    public int getKills(){
        return kills;
    }

    public void setKills(int kills){
        this.kills = kills;
    }

    public int getDeaths(){
        return deaths;
    }

    public void setDeaths(int deaths){
        this.deaths = deaths;
    }


}
