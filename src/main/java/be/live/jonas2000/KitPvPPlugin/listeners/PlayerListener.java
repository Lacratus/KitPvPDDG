package be.live.jonas2000.KitPvPPlugin.listeners;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import be.live.jonas2000.KitPvPPlugin.Main;
import be.live.jonas2000.KitPvPPlugin.objects.KitPvPPlayer;
import be.live.jonas2000.KitPvPPlugin.files.ConfigFile;
import be.live.jonas2000.KitPvPPlugin.scoreboard.Sidebar;
import org.bukkit.*;
import org.bukkit.block.Sign;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class PlayerListener implements Listener {

    public Map<String,KitPvPPlayer> playerList = new HashMap<>();
    private  Main plugin;
    private ConfigFile locationFile;
    private ConfigFile selectedKitFile;
    private Sidebar sidebar;

    public PlayerListener(Main plugin) {
        this.plugin = plugin;
        locationFile = plugin.getLocationFile();
        selectedKitFile = plugin.getSelectedKitFile();
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) throws IllegalArgumentException, IllegalStateException, SQLException {
        Player player = e.getPlayer();
        String UUID = player.getUniqueId().toString();
        //Players being added to database on first join
        try {
            ResultSet rs = plugin.prepareStatement("SELECT COUNT(UUID) FROM player_info WHERE UUID = '" + player.getUniqueId().toString() + "';").executeQuery();
            rs.next();
            if (rs.getInt(1) == 0) {
                plugin.prepareStatement("INSERT INTO player_info(UUID, IP, KILLS, DEATHS, COINS, JOIN_DATE) VALUES ('"
                        + player.getUniqueId().toString() + "','" + player.getAddress() + "', DEFAULT, DEFAULT, DEFAULT, DEFAULT);").executeUpdate();

            } else {
                ResultSet rs2 = plugin.prepareStatement("SELECT * FROM player_info WHERE UUID = '" + player.getUniqueId() + "';").executeQuery();
                rs2.next();

            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        ResultSet killSet = plugin.prepareStatement("SELECT KILLS,Deaths,Coins FROM player_info WHERE UUID = '" + UUID + "';").executeQuery();
        killSet.next();
        int kill = killSet.getInt("KILLS");
        int death = killSet.getInt("DEATHS");// + TempStatisticsFile.getTempStatisticsFile().getInt("Players." + UUID + ".DEATHS")
        int coin = killSet.getInt("COINS");// + TempStatisticsFile.getTempStatisticsFile().getInt("Players." + UUID + ".COINS");

        //KitPvPPlayer being constructed & added in hashmap
        KitPvPPlayer p = new KitPvPPlayer(UUID,kill,death,coin);
        playerList.put(UUID, p);
        sidebar = new Sidebar(player,kill,death,coin);
        plugin.addPlayerInLobby(player);
        player.getInventory().clear();
        player.getInventory().setItem(4, new ItemStack(Material.COMPASS));

        if (!locationFile.getConfig().contains("Locations.Spawn")) {
            player.teleport(new Location(player.getWorld(), 0, 100, 0));
            player.sendMessage("Plaats een spawn met /mod setspawn");
        } else {
            final World kitPvP = Bukkit.getWorld(locationFile.getConfig().getString("Locations.Spawn.WorldName"));
            player.teleport(new Location(kitPvP
                    , locationFile.getConfig().getInt("Locations.Spawn.X")
                    , locationFile.getConfig().getInt("Locations.Spawn.Y")
                    , locationFile.getConfig().getInt("Locations.Spawn.Z")
                    , (float) locationFile.getConfig().getDouble("Locations.Spawn.Yaw")
                    , (float) locationFile.getConfig().getDouble("Locations.Spawn.Pitch")));
        }
    }


    @EventHandler
    public void onDisconnect(PlayerQuitEvent e) {
        //Update statistics in database and send them to the lobby
        Player player = e.getPlayer();
        player.getInventory().clear();
        player.teleport(new Location(player.getWorld(), 100.0D, 100.0D, 100.0D));
        player.getInventory().setItem(4, new ItemStack(Material.COMPASS));
        String UUID = player.getUniqueId().toString();
        try {
            plugin.prepareStatement("UPDATE player_info SET DEATHS = "
                    + playerList.get(UUID).getDeaths()
                    + ",KILLS = "
                    + playerList.get(UUID).getKills()
                    + ",COINS = "
                    + playerList.get(UUID).getCoins()
                    + " WHERE UUID = '" + UUID + "';").executeUpdate();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        playerList.remove(UUID);
        player.getInventory().clear();
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onDeath(PlayerDeathEvent e) throws SQLException {
        // People being added in YAML File and scoreboard update
        Player player = e.getEntity();
        Player killer = e.getEntity().getKiller();

        if (killer instanceof Player && killer != null) {
            String pUUID = player.getUniqueId().toString();
            String kUUID = killer.getUniqueId().toString();
            KitPvPPlayer player1 = playerList.get(pUUID);
            KitPvPPlayer killer1 = playerList.get(kUUID);
            player1.setDeaths(player1.getDeaths() +1);
            killer1.setKills(killer1.getKills() + 1);
            killer1.setCoins(killer1.getCoins() + 10);


            killer.sendMessage("You've earned 10 gold");
            sidebar.update(killer, false);
            sidebar.update(player, true);
        }
        e.getDrops().clear();

        plugin.addPlayerInLobby(player);

    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent e) {
        //Player teleported to spawn
        final World kitPvP = Bukkit.getWorld(locationFile.getConfig().getString("Locations.Spawn.WorldName"));
        final Player player = e.getPlayer();
        player.getInventory().setItem(4, new ItemStack(Material.COMPASS));
        Bukkit.getServer().getScheduler().scheduleAsyncDelayedTask(plugin.getPlugin(), new Runnable() {
            public void run() {
                player.teleport(new Location(kitPvP
                        , locationFile.getConfig().getDouble("Locations.Spawn.X")
                        , locationFile.getConfig().getDouble("Locations.Spawn.Y")
                        , locationFile.getConfig().getDouble("Locations.Spawn.Z")
                        , (float) locationFile.getConfig().getDouble("Locations.Spawn.Yaw")
                        , (float) locationFile.getConfig().getDouble("Locations.Spawn.Pitch")));
            }
        }, 10L);
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent e) {
        Player player = e.getPlayer();
        // opens kitselection
        if (player.getItemInHand().getType().equals(Material.COMPASS)) {
            openKitSelection(player, getSelectedKit(player));
        }
        //Telµeports you to location(USE ["Location"], First letter needs to be uppercase)
        if(getSelectedKit(player) == null && e.getAction() == Action.RIGHT_CLICK_BLOCK && e.getClickedBlock().getState() instanceof Sign){
            player.sendMessage("Choose a kit before clicking on a sign!");
        }
        else if (e.getAction() == Action.RIGHT_CLICK_BLOCK && e.getClickedBlock().getState() instanceof Sign ) {
            Sign teleportSign = (Sign) e.getClickedBlock().getState();
            String line = teleportSign.getLine(0);
            ConfigurationSection sec = locationFile.getConfig().getConfigurationSection("Locations");
            for (String locatieNaam : sec.getKeys(false)) {
                if (line.equals(ChatColor.BLUE + "[" + locatieNaam + "]")) {
                    World kitPvP = Bukkit.getWorld((locationFile.getConfig().getString("Locations." + locatieNaam + ".WorldName")));
                    player.teleport(new Location(kitPvP
                            , locationFile.getConfig().getDouble("Locations." + locatieNaam + ".X")
                            , locationFile.getConfig().getDouble("Locations." + locatieNaam + ".Y")
                            , locationFile.getConfig().getDouble("Locations." + locatieNaam + ".Z")
                            , (float) locationFile.getConfig().getDouble("Locations." + locatieNaam + ".Yaw")
                            , (float) locationFile.getConfig().getDouble("Locations." + locatieNaam + ".Pitch")
                    ));
                    player.getInventory().clear();
                    giveKit(player, getSelectedKit(player));
                    plugin.removePlayerInLobby(player);
                }

            }
        }


    }

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        Player player = (Player) e.getWhoClicked();
        // Show which kit a player choose
        if(e.getCurrentItem() == null){

        } else if(e.getCurrentItem() == null && ChatColor.translateAlternateColorCodes('&', e.getClickedInventory().getTitle()).equals(ChatColor.DARK_GREEN + "Choose a kit!")){

        } else if (ChatColor.translateAlternateColorCodes('&', e.getClickedInventory().getTitle()).equals(ChatColor.DARK_GREEN + "Choose a kit!")) {
            e.setCancelled(true);
            ItemStack warrior;
            ItemStack archer;
            ItemStack tank;
            Inventory gui = Bukkit.createInventory((InventoryHolder) null, 9, ChatColor.DARK_GREEN + "Choose a kit!");
            switch (e.getCurrentItem().getType()) {
                case DIAMOND_SWORD:
                    warrior = new ItemStack(Material.DIAMOND_SWORD);
                    warrior.addUnsafeEnchantment(Enchantment.LURE, 1);
                    final ItemMeta itemMetaWarrior = warrior.getItemMeta();
                    itemMetaWarrior.addItemFlags(ItemFlag.HIDE_ENCHANTS);
                    warrior.setItemMeta(itemMetaWarrior);
                    archer = new ItemStack(Material.BOW);
                    tank = new ItemStack(Material.DIAMOND_CHESTPLATE);
                    gui.setItem(3, warrior);
                    gui.setItem(4, archer);
                    gui.setItem(5, tank);
                    player.openInventory(gui);
                    setSelectedKit(player, Material.DIAMOND_SWORD);
                    break;
                case BOW:
                    warrior = new ItemStack(Material.DIAMOND_SWORD);
                    archer = new ItemStack(Material.BOW);
                    archer.addUnsafeEnchantment(Enchantment.LURE, 1);
                    final ItemMeta itemMetaArcher = archer.getItemMeta();
                    itemMetaArcher.addItemFlags(ItemFlag.HIDE_ENCHANTS);
                    archer.setItemMeta(itemMetaArcher);
                    tank = new ItemStack(Material.DIAMOND_CHESTPLATE);
                    gui.setItem(3, warrior);
                    gui.setItem(4, archer);
                    gui.setItem(5, tank);
                    player.openInventory(gui);
                    setSelectedKit(player, Material.BOW);
                    break;
                case DIAMOND_CHESTPLATE:
                    warrior = new ItemStack(Material.DIAMOND_SWORD);
                    archer = new ItemStack(Material.BOW);
                    tank = new ItemStack(Material.DIAMOND_CHESTPLATE);
                    tank.addUnsafeEnchantment(Enchantment.LURE, 1);
                    final ItemMeta itemMetaTank = tank.getItemMeta();
                    itemMetaTank.addItemFlags(ItemFlag.HIDE_ENCHANTS);
                    tank.setItemMeta(itemMetaTank);
                    gui.setItem(3, warrior);
                    gui.setItem(4, archer);
                    gui.setItem(5, tank);
                    player.openInventory(gui);
                    setSelectedKit(player, Material.DIAMOND_CHESTPLATE);
                    break;
            }
        }

    }

    @EventHandler
    public void onSignChance(SignChangeEvent e) {
        // Turns first line into blue if it is a location.
        ConfigurationSection sec = locationFile.getConfig().getConfigurationSection("Locations");
        for (String locatieNaam : sec.getKeys(false)) {
            if (e.getLine(0).equals("[" + locatieNaam + "]")) {
                e.setLine(0, ChatColor.BLUE + "[" + locatieNaam + "]");
            }
        }
    }

    // Player drops no items
    @EventHandler
    public void onPlayerDropItem(PlayerDropItemEvent e) {
        e.setCancelled(true);
    }


    // No hunger
    @EventHandler
    public void onFoodLevelChange(FoodLevelChangeEvent e) { e.setCancelled(true); }

    // No damage in lobby
    @EventHandler
    public void onTakingDamage(EntityDamageEvent e) {
        if (e.getEntity() instanceof Player) {
            if (plugin.getPlayersInLobby().contains((Player) e.getEntity())) {
                e.setCancelled(true);
            }
        }
    }

    private void openKitSelection(Player player, String selectedKit) {
        Inventory gui = Bukkit.createInventory(null, 9, ChatColor.DARK_GREEN + "Choose a kit!");
        ItemStack warrior = new ItemStack(Material.DIAMOND_SWORD);
        ItemStack archer = new ItemStack(Material.BOW);
        ItemStack tank = new ItemStack(Material.DIAMOND_CHESTPLATE);
        if (selectedKit == null) {
            gui.setItem(3, warrior);
            gui.setItem(4, archer);
            gui.setItem(5, tank);
            player.openInventory(gui);
        } else {
            switch (selectedKit) {
                case "BOW":
                    archer.addUnsafeEnchantment(Enchantment.LURE, 1);
                    final ItemMeta itemMetaArcher = archer.getItemMeta();
                    itemMetaArcher.addItemFlags(ItemFlag.HIDE_ENCHANTS);
                    archer.setItemMeta(itemMetaArcher);
                    gui.setItem(3, warrior);
                    gui.setItem(4, archer);
                    gui.setItem(5, tank);
                    player.openInventory(gui);
                    break;
                case "DIAMOND_SWORD":
                    warrior.addUnsafeEnchantment(Enchantment.LURE, 1);
                    final ItemMeta itemMetaWarrior = warrior.getItemMeta();
                    itemMetaWarrior.addItemFlags(ItemFlag.HIDE_ENCHANTS);
                    warrior.setItemMeta(itemMetaWarrior);
                    gui.setItem(3, warrior);
                    gui.setItem(4, archer);
                    gui.setItem(5, tank);
                    player.openInventory(gui);
                    break;
                case "DIAMOND_CHESTPLATE":
                    tank.addUnsafeEnchantment(Enchantment.LURE, 1);
                    final ItemMeta itemMetaTank = tank.getItemMeta();
                    itemMetaTank.addItemFlags(ItemFlag.HIDE_ENCHANTS);
                    tank.setItemMeta(itemMetaTank);
                    gui.setItem(3, warrior);
                    gui.setItem(4, archer);
                    gui.setItem(5, tank);
                    player.openInventory(gui);
                    break;
                default: player.sendMessage("Choose a kit first!");
            }
        }
    }

    private void giveKit(Player player, String selectedKit) {
        switch (selectedKit) {
            case "BOW":
                player.getInventory().setHelmet(new ItemStack(Material.LEATHER_HELMET));
                player.getInventory().setChestplate(new ItemStack(Material.LEATHER_CHESTPLATE));
                player.getInventory().setLeggings(new ItemStack(Material.LEATHER_LEGGINGS));
                player.getInventory().setBoots(new ItemStack(Material.LEATHER_BOOTS));
                ItemStack bow = new ItemStack(Material.BOW);
                bow.addEnchantment(Enchantment.ARROW_DAMAGE, 1);
                player.getInventory().setItemInOffHand(bow);
                player.getInventory().setItem(0, new ItemStack(Material.STONE_SWORD));
                player.getInventory().setHeldItemSlot(0);
                break;
            case "DIAMOND_SWORD":
                player.getInventory().setHelmet(new ItemStack(Material.IRON_HELMET));
                player.getInventory().setChestplate(new ItemStack(Material.IRON_CHESTPLATE));
                player.getInventory().setLeggings(new ItemStack(Material.IRON_LEGGINGS));
                player.getInventory().setBoots(new ItemStack(Material.IRON_BOOTS));
                player.getInventory().setItemInOffHand(new ItemStack(Material.SHIELD));
                player.getInventory().setItem(0, new ItemStack(Material.DIAMOND_SWORD));
                player.getInventory().setHeldItemSlot(0);
                break;
            case "DIAMOND_CHESTPLATE":
                player.getInventory().setHelmet(new ItemStack(Material.DIAMOND_HELMET));
                player.getInventory().setChestplate(new ItemStack(Material.DIAMOND_CHESTPLATE));
                player.getInventory().setLeggings(new ItemStack(Material.DIAMOND_LEGGINGS));
                player.getInventory().setBoots(new ItemStack(Material.DIAMOND_BOOTS));
                player.getInventory().setItemInOffHand(new ItemStack(Material.SHIELD));
                player.getInventory().setItem(0, new ItemStack(Material.IRON_SWORD));
                player.getInventory().setHeldItemSlot(0);
                break;
            default:
        }
    }

    private void setSelectedKit(Player player, Material selectedKit) {
        selectedKitFile.getConfig().set("Spelers." + player.getUniqueId().toString(), selectedKit.toString());
        selectedKitFile.saveFile();
    }

    private String getSelectedKit(Player player) {
        return (String) selectedKitFile.getConfig().get("Spelers." + player.getUniqueId().toString());
    }

}
