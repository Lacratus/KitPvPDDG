package be.live.jonas2000.KitPvPPlugin.listeners;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Random;

import be.live.jonas2000.KitPvPPlugin.Main;
import be.live.jonas2000.KitPvPPlugin.files.LocationFile;
import be.live.jonas2000.KitPvPPlugin.files.TempStatisticsFile;
import org.bukkit.*;
import org.bukkit.block.Sign;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

public class PlayerListener implements Listener {

    public PlayerListener() {
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) throws IllegalArgumentException, IllegalStateException, SQLException {
        Player player = e.getPlayer();
        String UUID = player.getUniqueId().toString();
        try {
            ResultSet rs = Main.prepareStatement("SELECT COUNT(UUID) FROM player_info WHERE UUID = '" + player.getUniqueId().toString() + "';").executeQuery();
            rs.next();
            if (rs.getInt(1) == 0) {
                Main.prepareStatement("INSERT INTO player_info(UUID, IP, KILLS, DEATHS, COINS, JOIN_DATE) VALUES ('"
                        + player.getUniqueId().toString() + "','" + player.getAddress() + "', DEFAULT, DEFAULT, DEFAULT, DEFAULT);").executeUpdate();
            } else {
                ResultSet rs2 = Main.prepareStatement("SELECT * FROM player_info WHERE UUID = '" + player.getUniqueId() + "';").executeQuery();
                rs2.next();
                player.getInventory().setItem(4, new ItemStack(Material.COMPASS));
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        if (!TempStatisticsFile.getTempStatisticsFile().contains("Players." + UUID)) {
            TempStatisticsFile.getTempStatisticsFile().set("Players." + UUID + ".Kills", 0);
            TempStatisticsFile.getTempStatisticsFile().set("Players." + UUID + ".Deaths", 0);
            TempStatisticsFile.getTempStatisticsFile().set("Players." + UUID + ".Coins", 0);
            TempStatisticsFile.save();
        }
        Main.buildSidebar(player);
    }


    @EventHandler
    public void onDisconnect(PlayerQuitEvent e) {
        Player player = e.getPlayer();
        player.getInventory().clear();
        player.teleport(new Location(player.getWorld(), 100.0D, 100.0D, 100.0D));
        player.getInventory().setItem(4, new ItemStack(Material.COMPASS));
        String UUID = player.getUniqueId().toString();
        try {
            Main.prepareStatement("UPDATE player_info SET DEATHS = DEATHS + "
                    + TempStatisticsFile.getTempStatisticsFile().get("Players." + UUID + ".Deaths")
                    + ",KILLS = KILLS + "
                    + TempStatisticsFile.getTempStatisticsFile().get("Players." + UUID + ".Kills")
                    + ",COINS = COINS + "
                    + TempStatisticsFile.getTempStatisticsFile().get("Players." + UUID + ".Coins")
                    + " WHERE UUID = '" + UUID + "';").executeUpdate();
            TempStatisticsFile.getTempStatisticsFile().set("Players." + UUID + ".Kills", 0);
            TempStatisticsFile.getTempStatisticsFile().set("Players." + UUID + ".Deaths", 0);
            TempStatisticsFile.getTempStatisticsFile().set("Players." + UUID + ".Coins", 0);
            Main.updateSidebar(player, "Disconnect");
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent e) throws SQLException {
        Player player = e.getEntity();
        Player killer = e.getEntity().getKiller();
        if (killer instanceof Player && killer != null) {
            String pUUID = player.getUniqueId().toString();
            String kUUID = killer.getUniqueId().toString();

            int deathsPlayer = TempStatisticsFile.getTempStatisticsFile().getInt("Players." + pUUID + ".Deaths");
            int killsKiller = TempStatisticsFile.getTempStatisticsFile().getInt("Players." + kUUID + ".Kills");
            int coinsKiller = TempStatisticsFile.getTempStatisticsFile().getInt("Players." + kUUID + ".Coins");

            TempStatisticsFile.getTempStatisticsFile().set("Players." + pUUID + ".Deaths", deathsPlayer + 1);
            TempStatisticsFile.getTempStatisticsFile().set("Players." + kUUID + ".Kills", killsKiller + 1);
            TempStatisticsFile.getTempStatisticsFile().set("Players." + kUUID + ".Coins", coinsKiller + 10);
            TempStatisticsFile.save();


            killer.sendMessage("You've earned 10 gold");
            player.sendMessage("You have been killed by " + player.getKiller());
            Main.updateSidebar(player, "Death");
            Main.updateSidebar(killer, "Kill");
        }

    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent e) {
        final World kitPvP = Bukkit.getWorld(LocationFile.getLocationFile().getString("Locations.Spawn.WorldName"));
        final Player player = e.getPlayer();
        player.getInventory().setItem(4, new ItemStack(Material.COMPASS));
        Bukkit.getServer().getScheduler().scheduleAsyncDelayedTask(Main.getPlugin(), new Runnable() {
            public void run() {
                player.teleport(new Location(kitPvP
                        , LocationFile.getLocationFile().getDouble("Locations.Spawn.X")
                        , LocationFile.getLocationFile().getDouble("Locations.Spawn.Y")
                        , LocationFile.getLocationFile().getDouble("Locations.Spawn.Z")
                        , (float) LocationFile.getLocationFile().getDouble("Locations.Spawn.Yaw")
                        , (float) LocationFile.getLocationFile().getDouble("Locations.Spawn.Pitch")));
            }
        }, 10L);
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent e) {
        Player player = e.getPlayer();
        if (player.getItemInHand().getType().equals(Material.COMPASS)) {
            Inventory gui = Bukkit.createInventory((InventoryHolder) null, 9, ChatColor.DARK_GREEN + "Choose a kit!");
            ItemStack Warrior = new ItemStack(Material.DIAMOND_SWORD);
            ItemStack Archer = new ItemStack(Material.BOW);
            ItemStack Tank = new ItemStack(Material.DIAMOND_CHESTPLATE);
            gui.setItem(3, Warrior);
            gui.setItem(4, Archer);
            gui.setItem(5, Tank);
            player.openInventory(gui);
        }
        if (e.getAction() == Action.RIGHT_CLICK_BLOCK && e.getClickedBlock().getState() instanceof Sign) {
            Sign teleportSign = (Sign) e.getClickedBlock().getState();
            String line = teleportSign.getLine(0);
            ConfigurationSection sec = LocationFile.getLocationFile().getConfigurationSection("Locations");
            for (String locatieNaam : sec.getKeys(false)) {
                if (line.equals(ChatColor.BLUE + "[" + locatieNaam + "]")) {
                    World kitPvP = Bukkit.getWorld((LocationFile.getLocationFile().getString("Locations." + locatieNaam + ".WorldName")));
                    player.teleport(new Location(kitPvP
                            , LocationFile.getLocationFile().getDouble("Locations." + locatieNaam + ".X")
                            , LocationFile.getLocationFile().getDouble("Locations." + locatieNaam + ".Y")
                            , LocationFile.getLocationFile().getDouble("Locations." + locatieNaam + ".Z")
                            , (float) LocationFile.getLocationFile().getDouble("Locations." + locatieNaam + ".Yaw")
                            , (float) LocationFile.getLocationFile().getDouble("Locations." + locatieNaam + ".Pitch")
                    ));
                }
            }
        }


    }

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        Player player = (Player) e.getWhoClicked();
        if (ChatColor.translateAlternateColorCodes('&', e.getClickedInventory().getTitle()).equals(ChatColor.DARK_GREEN + "Choose a kit!") && e.getCurrentItem() != null) {
            e.setCancelled(true);
            switch (e.getCurrentItem().getType()) {
                case BOW:
                    player.getInventory().clear();
                    player.getInventory().setHelmet(new ItemStack(Material.LEATHER_HELMET));
                    player.getInventory().setChestplate(new ItemStack(Material.LEATHER_CHESTPLATE));
                    player.getInventory().setLeggings(new ItemStack(Material.LEATHER_LEGGINGS));
                    player.getInventory().setBoots(new ItemStack(Material.LEATHER_BOOTS));
                    ItemStack bow = new ItemStack(Material.BOW);
                    bow.addEnchantment(Enchantment.ARROW_DAMAGE, 1);
                    player.getInventory().setItemInOffHand(bow);
                    player.getInventory().setItem(0, new ItemStack(Material.STONE_SWORD));
                    player.getInventory().setHeldItemSlot(0);
                    this.teleportToRandomLocation(player);
                    break;
                case DIAMOND_SWORD:
                    player.getInventory().clear();
                    player.getInventory().setHelmet(new ItemStack(Material.IRON_HELMET));
                    player.getInventory().setChestplate(new ItemStack(Material.IRON_CHESTPLATE));
                    player.getInventory().setLeggings(new ItemStack(Material.IRON_LEGGINGS));
                    player.getInventory().setBoots(new ItemStack(Material.IRON_BOOTS));
                    player.getInventory().setItemInOffHand(new ItemStack(Material.SHIELD));
                    player.getInventory().setItem(0, new ItemStack(Material.DIAMOND_SWORD));
                    player.getInventory().setHeldItemSlot(0);
                    this.teleportToRandomLocation(player);
                    break;
                case DIAMOND_AXE:
                    player.getInventory().clear();
                    player.getInventory().setHelmet(new ItemStack(Material.DIAMOND_HELMET));
                    player.getInventory().setChestplate(new ItemStack(Material.DIAMOND_CHESTPLATE));
                    player.getInventory().setLeggings(new ItemStack(Material.DIAMOND_LEGGINGS));
                    player.getInventory().setBoots(new ItemStack(Material.DIAMOND_BOOTS));
                    player.getInventory().setItemInOffHand(new ItemStack(Material.SHIELD));
                    player.getInventory().setItem(0, new ItemStack(Material.IRON_SWORD));
                    player.getInventory().setHeldItemSlot(0);
                    this.teleportToRandomLocation(player);
                    break;
                default:
                    return;
            }
        }

    }

    @EventHandler
    public void onSignChance(SignChangeEvent e) {

        Player player = e.getPlayer();
        ConfigurationSection sec = LocationFile.getLocationFile().getConfigurationSection("Locations");
        for (String locatieNaam : sec.getKeys(false)) {
            if (e.getLine(0).equals("[" + locatieNaam + "]")) {
                e.setLine(0, ChatColor.BLUE + "[" + locatieNaam + "]");
            }
        }
    }

    private void teleportToRandomLocation(Player player) {
        ArrayList<Location> locaties = new ArrayList();
        locaties.add(new Location(player.getWorld(), 1.0D, 63.0D, 1.0D));
        locaties.add(new Location(player.getWorld(), 1.0D, 63.0D, 0.0D));
        locaties.add(new Location(player.getWorld(), 0.0D, 63.0D, 1.0D));
        locaties.add(new Location(player.getWorld(), 0.0D, 63.0D, 0.0D));
        Random randomIndex = new Random();
        int index = randomIndex.nextInt(locaties.size());
        player.teleport(locaties.get(index));
    }
}
