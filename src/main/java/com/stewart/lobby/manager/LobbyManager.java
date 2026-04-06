package com.stewart.lobby.manager;

import com.gmail.tracebachi.SockExchange.Spigot.SockExchangeApi;
import com.gmail.tracebachi.SockExchange.SpigotServerInfo;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import com.stewart.lobby.Lobby;
import com.stewart.lobby.instances.PlayerServerInfo;
import com.stewart.lobby.utils.LobbyUtils;
import net.citizensnpcs.api.CitizensAPI;
import net.minecraft.server.v1_8_R3.EnumParticle;
import net.minecraft.server.v1_8_R3.PacketPlayOutWorldParticles;
import org.bukkit.*;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.material.Chest;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scheduler.BukkitTask;
import redis.clients.jedis.JedisPooled;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;

import static org.bukkit.Bukkit.getScheduler;

public class LobbyManager {


    private final Lobby main;
    private final int feetBlockY;
    private int gameSeconds;
    // track the last date we ran the daily wipe at 3am so it only runs once per day
    private LocalDate lastWipeDate = null;
    // keeps a list of all people currently spawn protected and the game time it started
    private final HashMap<UUID, Integer> playerSpawnProtect = new HashMap<>();
    private final HashMap<UUID, Integer> playerPortalling = new HashMap<>();
    private final HashMap<UUID, Integer> playerCooldownSpeed = new HashMap<>();
    private final HashMap<UUID, Integer> playerCooldownInvis = new HashMap<>();
    private final HashMap<UUID, Integer> playerCooldownJump = new HashMap<>();
    private  int particleIterator = 19; // Task will run 10 times.
    private BukkitTask particleTask = null;
    private final List<UUID> lstNoPvp;
    private int gamesCompletedForKey = 10;
    private int[][] pvpCoords;
    //-55 40 -10

    // -11 50 10

    public LobbyManager(Lobby lobby) {
        this.main = lobby;
        feetBlockY = main.getConfig().getInt("feet-block-y");
        startClock();
        lstNoPvp = new ArrayList<>();
        Bukkit.getScheduler().scheduleSyncDelayedTask(main, this::spawnNonGameNPCS, 200L);
        getPVPCoords();
    }

    private void getPVPCoords() {
        int count = 0;
        for (String s : main.getConfig().getConfigurationSection("pvp-areas.").getKeys(false)) {
            count ++;
            System.out.println("found pvp area " + s);
        }
        pvpCoords = new int[count][6];
        for (String s : main.getConfig().getConfigurationSection("pvp-areas.").getKeys(false)) {
            int i = Integer.parseInt(s);
            pvpCoords[i][0] = main.getConfig().getInt("pvp-areas." + s + ".xMin");

            pvpCoords[i][1] = main.getConfig().getInt("pvp-areas." + s + ".xMax");
            pvpCoords[i][2] = main.getConfig().getInt("pvp-areas." + s + ".yMin");
            pvpCoords[i][3] = main.getConfig().getInt("pvp-areas." + s + ".yMax");
            pvpCoords[i][4] = main.getConfig().getInt("pvp-areas." + s + ".zMin");
            pvpCoords[i][5] = main.getConfig().getInt("pvp-areas." + s + ".zMax");

            System.out.println(pvpCoords[i][0] + " " + pvpCoords[i][1] + " " + pvpCoords[i][2] + " " + pvpCoords[i][3] + " " + pvpCoords[i][4] + " " + pvpCoords[i][5]);
            System.out.println("found pvp area sssssssssssssssssssssssssssssssssssssssssssss" );
        }
    }

    private void spawnNonGameNPCS() {
        if (ConfigManager.isVotesEnabled()) {
            spawnVoteMaster();
        }
        spawnDiscordNPC();
        if (ConfigManager.isShopEnabled()) {
            spawnShopNPC();
        }
        spawnSumoNPC();
        spawnKitPVPNPC();
    }

    private void spawnVoteMaster() {
        String texture = ConfigManager.getVotesSkinTexture();
        String signature = ConfigManager.getVotesSkinSignature();
        String nameColour = ChatColor.GOLD + "";
        // get the locations and spawn the NPCs for fiend fight
        for (String s : main.getConfig().getConfigurationSection("votes.spawn.").getKeys(false)) {
            Location location = new Location(Bukkit.getWorld("world"),
                    main.getConfig().getDouble("votes.spawn." + s + ".x"),
                    main.getConfig().getDouble("votes.spawn." + s + ".y"),
                    main.getConfig().getDouble("votes.spawn." + s + ".z"),
                    (float) main.getConfig().getDouble("votes.spawn." + s + ".yaw"),
                    (float) main.getConfig().getDouble("votes.spawn." + s + ".pitch"));

            main.getGameManager().spawnNPC(location, texture, signature, nameColour, "Votemaster");
        }

    }

    private void spawnDiscordNPC() {


        String texture = ConfigManager.getDiscordSkinTexture();
        String signature = ConfigManager.getDiscordSkinSignature();
        String nameColour = ChatColor.BLUE + "";


        // get the locations and spawn the NPCs for fiend fight
        for (String s : main.getConfig().getConfigurationSection("discord.spawn.").getKeys(false)) {
            Location location = new Location(Bukkit.getWorld("world"),
                    main.getConfig().getDouble("discord.spawn." + s + ".x"),
                    main.getConfig().getDouble("discord.spawn." + s + ".y"),
                    main.getConfig().getDouble("discord.spawn." + s + ".z"),
                    (float) main.getConfig().getDouble("discord.spawn." + s + ".yaw"),
                    (float) main.getConfig().getDouble("discord.spawn." + s + ".pitch"));

            main.getGameManager().spawnNPC(location, texture, signature, nameColour, "Discord");
        }


    }

    private void spawnShopNPC() {


        String texture = ConfigManager.getShopSkinTexture();
        String signature = ConfigManager.getShopSkinSignature();
        String nameColour = ChatColor.YELLOW + "";


        // get the locations and spawn the NPCs for fiend fight
        for (String s : main.getConfig().getConfigurationSection("shop.spawn.").getKeys(false)) {
            Location location = new Location(Bukkit.getWorld("world"),
                    main.getConfig().getDouble("shop.spawn." + s + ".x"),
                    main.getConfig().getDouble("shop.spawn." + s + ".y"),
                    main.getConfig().getDouble("shop.spawn." + s + ".z"),
                    (float) main.getConfig().getDouble("shop.spawn." + s + ".yaw"),
                    (float) main.getConfig().getDouble("shop.spawn." + s + ".pitch"));


            String npcName = nameColour + "Visit our Shop!";
            /*net.citizensnpcs.api.npc.NPC npc =CitizensAPI.getNPCRegistry().createNPC(EntityType.VILLAGER, npcName);
            npc.spawn(location);*/

            main.getGameManager().spawnNPC(location, texture, signature, nameColour, npcName);
        }


    }

    private void spawnSumoNPC() {
        String texture = ConfigManager.getSumoSkinTexture();
        String signature = ConfigManager.getSumoSkinSignature();
        String nameColour = ChatColor.GOLD + "";
        // get the locations and spawn the NPCs for fiend fight
        for (String s : main.getConfig().getConfigurationSection("sumo-npc-spawn.").getKeys(false)) {
            Location location = new Location(Bukkit.getWorld("world"),
                    main.getConfig().getDouble("sumo-npc-spawn." + s + ".x"),
                    main.getConfig().getDouble("sumo-npc-spawn." + s + ".y"),
                    main.getConfig().getDouble("sumo-npc-spawn." + s + ".z"),
                    (float) main.getConfig().getDouble("sumo-npc-spawn." + s + ".yaw"),
                    (float) main.getConfig().getDouble("sumo-npc-spawn." + s + ".pitch"));

            main.getGameManager().spawnNPC(location, texture, signature, nameColour, "Sumo bots");
        }
    }

    private void spawnKitPVPNPC() {
        String texture = ConfigManager.getKitPVPSkinTexture();
        String signature = ConfigManager.getKitPVPSkinSignature();
        String nameColour = ChatColor.GOLD + "";
        // get the locations and spawn the NPCs for fiend fight
        for (String s : main.getConfig().getConfigurationSection("kitpvp-npc-spawn.").getKeys(false)) {
            Location location = new Location(Bukkit.getWorld("world"),
                    main.getConfig().getDouble("kitpvp-npc-spawn." + s + ".x"),
                    main.getConfig().getDouble("kitpvp-npc-spawn." + s + ".y"),
                    main.getConfig().getDouble("kitpvp-npc-spawn." + s + ".z"),
                    (float) main.getConfig().getDouble("kitpvp-npc-spawn." + s + ".yaw"),
                    (float) main.getConfig().getDouble("kitpvp-npc-spawn." + s + ".pitch"));

            main.getGameManager().spawnNPC(location, texture, signature, nameColour, "Kit PVP");
        }
    }

    private void startClock() {
        gameSeconds = 0;
        BukkitScheduler scheduler = Bukkit.getServer().getScheduler();
        // Run Whatever code you might have
        scheduler.scheduleSyncRepeatingTask(main, this::doClockTick, 0L, 20L);
    }

    private void doClockTick() {




        if (!playerSpawnProtect.isEmpty()) {
            // remove any players from spawn protect list that may have left the game
            playerSpawnProtect.entrySet().removeIf(e->  Bukkit.getPlayer(e.getKey()) == null );
            // show particles around spawn protected players
            particleIterator = 19;
            particleTask = null;
            showPlayerParticles();
        }

        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player != null) {
                player.getPlayer().setFoodLevel(20);
                if (!main.isPlayerInKitPvP(player) && !main.getGameManager().getMiniGameManger().isPlayerInSumoGame(player)) {
                    removeBadItems(player);
                }
            }
        }

        if (main.getLobbyLeaderBoardManager() != null)  {
            main.getLobbyLeaderBoardManager().updateNextLeaderboard();
        }


        checkPlayerZone();
        // removes players from the spawn protect list who has been there 5 seconds or more
        playerSpawnProtect.entrySet().removeIf(e -> e.getValue() + 4 < gameSeconds );
        // removes players from the cooldown for the speed command
        playerCooldownSpeed.entrySet().removeIf(e -> e.getValue() + 29 < gameSeconds );
        // removes players from the cooldown for the invis command
        playerCooldownInvis.entrySet().removeIf(e -> e.getValue() + 29 < gameSeconds );
        // removes players from the cooldown for the jump command
        playerCooldownJump.entrySet().removeIf(e -> e.getValue() + 29 < gameSeconds );

        // removes players from the portallingt list who has been there 4 seconds or more
        playerPortalling.entrySet().removeIf(e -> e.getValue() + 3 < gameSeconds );
        gameSeconds += 1;
        // every minute remove players from the sent to server list who have been there 60 minutes or more
        if (gameSeconds%60 == 0) {
            main.getGameManager().removePlayerServerInfoOverMinutes(60);
        }
        if (gameSeconds%10 == 0) {
            updateCrateKeyChest();
        }
        doDailyRedisWipe();

    }

    private void doDailyRedisWipe() {
        // Once per day at 03:00 (server local time) run wipeGamesPlayedForCratesFromRedis
        try {
            LocalDate today = LocalDate.now(ZoneId.systemDefault());
            LocalTime now = LocalTime.now(ZoneId.systemDefault());
            LocalTime target = LocalTime.of(3, 0);
            LocalTime windowEnd = target.plusMinutes(10); // run only between 03:00 and 03:09 inclusive
             // trigger once during the 03:00 minute (first tick in that minute) or if we missed the 03:00 minute
            // (for example a server restart that came up after 03:00) but only allow attempts within the first 10 minutes
            // after 03:00 to avoid wiping on random restarts later in the day.
            if (( !now.isBefore(target) && now.isBefore(windowEnd) ) && (lastWipeDate == null || !lastWipeDate.equals(today))) {
                 // Use Redis as a persistent guard so restarts or multiple servers don't repeatedly flush DB.
                 JedisPooled jedis = null;
                 try {
                    // use a different Redis DB index for the guard key so flushing DB 1 doesn't delete our marker
                    jedis = new JedisPooled("redis://localhost:6379/2");
                    String key = "bashy:last_daily_wipe";
                    // setnx returns 1 if the key was set (i.e., we are the first to set it today)
                    Long set = jedis.setnx(key, today.toString());
                    boolean flushSucceeded = false;
                    if (set == 1L) {
                        // ensure the key expires in 25 hours so it won't persist indefinitely
                        jedis.expire(key, 25 * 60 * 60);
                        // flush the DB (this is the intended daily wipe)
                        // perform the flush on DB 1 specifically to match previous behavior
                        JedisPooled flushJedis = null;
                        try {
                            flushJedis = new JedisPooled("redis://localhost:6379/1");
                            flushJedis.flushDB();
                            flushSucceeded = true;
                            System.out.println("Daily wipeGamesPlayedForCratesFromRedis executed at 03:00 (via Redis guard)");
                        } catch (Exception ex2) {
                            System.out.println("Error flushing DB 1 during daily wipe: " + ex2.getMessage());
                            // remove the guard key so another instance (or a later retry) can attempt the wipe
                            try {
                                jedis.del(key);
                            } catch (Exception delEx) {
                                System.out.println("Failed to delete guard key after flush failure: " + delEx.getMessage());
                            }
                        } finally {
                            if (flushJedis != null) try { flushJedis.close(); } catch (Exception ignored) {}
                        }
                    } else {
                        // someone else already performed the wipe (or the key exists)
                        System.out.println("Daily wipe already performed by another instance or earlier in this minute.");
                    }
                    // Mark lastWipeDate only if the flush succeeded (we did the wipe) or the key already existed (someone else did it)
                    if (set != null && (set == 0L || flushSucceeded)) {
                        lastWipeDate = today;
                    }
                } catch (Exception ex) {
                    System.out.println("Error performing Redis-backed daily wipe: " + ex.getMessage());
                } finally {
                    if (jedis != null) {
                        try { jedis.close(); } catch (Exception ignored) {}
                    }
                }

            }
        } catch (Exception e) {
            System.out.println("Error checking/performing daily wipe: " + e.getMessage());
        }
    }

    private void updateCrateKeyChest() {
        JedisPooled jedisPlayerKeys =  new JedisPooled("redis://localhost:6379/2");
        JedisPooled jedisGamesPlayed =  new JedisPooled("redis://localhost:6379/1");
        String gamesPlayedKey = "daily_games_completed";
        String playerKeysKey = "bbcratekeys";


        for (Player player : Bukkit.getOnlinePlayers()) {
            // dont do this if they are in a minigame as i dont want to add the chest to their inventory.
            if (checkPlayerInMiniGame(player)) continue;
            // get how many games played this player has from jedisGamesPlayed
            Double currentScore = jedisGamesPlayed.zscore(gamesPlayedKey, player.getName());
            if (currentScore== null) {
                currentScore = 0d;
            };
            Double currentKeys = jedisPlayerKeys.zscore(playerKeysKey, player.getName());
            // if the current score is > or equal to 10 they get a key
            if (currentScore >= gamesCompletedForKey) {
                // give them a key by adding to their score in jedisPlayerKeys for the key
                if (currentKeys == null) {
                    jedisPlayerKeys.zadd(playerKeysKey, 1d, player.getName());//ZADD
                    currentKeys = 1d;
                } else {
                    jedisPlayerKeys.zincrby(playerKeysKey, 1, player.getName());//ZADD
                    currentKeys += 1;
                }
                // deduct currentscore by 10 in jedisGamesPlayed so they have to complete another 10 games for the next key
                currentScore = currentScore - gamesCompletedForKey;
                jedisGamesPlayed.zadd(gamesPlayedKey, currentScore, player.getName());//ZADD
              //  double newGamesPlayedFromRedis = jedisPlayerKeys.zscore(playerKeysKey, player.getName());
             //   System.out.println("Player " + player.getName() + " has been awarded a crate key for completing " + gamesCompletedForKey + " games. They now have " + newGamesPlayedFromRedis + " games played today.");
            }
         //   System.out.println("Player " + player.getName() + " has " + (currentKeys == null ? 0 : currentKeys.intValue()) + " crate keys and has completed " + currentScore.intValue() + " games today.");
            setHotbarCrateChest(player, currentKeys == null ? 0 : currentKeys.intValue());

        }

        jedisPlayerKeys.close();
        jedisGamesPlayed.close();

    }

    public void playerClickedHotbarCrate(Player player) {
        // check if the player has any keys
        JedisPooled jedisPlayerKeys =  new JedisPooled("redis://localhost:6379/2");
        String playerKeysKey = "bbcratekeys";
        Double currentKeys = jedisPlayerKeys.zscore(playerKeysKey, player.getName());
        if (currentKeys != null && currentKeys >= 1) {
            // open a crate
            // do a command as the console to open a crate
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "uc reward 1 " + player.getName());
            // reduce the number of keys by 1
            jedisPlayerKeys.zincrby(playerKeysKey, -1, player.getName());//
            currentKeys = currentKeys - 1;
            setHotbarCrateChest(player, currentKeys.intValue());
        } else {
            JedisPooled jedisGamesPlayed =  new JedisPooled("redis://localhost:6379/1");
            String gamesPlayedKey = "daily_games_completed";
            Double currentScore = jedisGamesPlayed.zscore(gamesPlayedKey, player.getName());
            int gamesStillToPlay = gamesCompletedForKey - (currentScore == null ? 0 : currentScore.intValue());
            player.sendMessage(ChatColor.RED + "You have no crate keys to open!");
            // tell them how to get more keys, either play gamesCompletedForKey games in a day or vote on all sites
            player.sendMessage(ChatColor.YELLOW + "You can earn keys by completing " + gamesStillToPlay + " more games today");
            player.sendMessage(ChatColor.YELLOW + "or by voting for the server on all of our voting sites!");
            jedisGamesPlayed.close();
        }
        jedisPlayerKeys.close();
    }

    private void setHotbarCrateChest(Player player, int currentKeys) {
        ItemStack chest;
        if (currentKeys == 0) {
            chest = new ItemStack(Material.CHEST);
            ItemMeta chestMeta = chest.getItemMeta();
            chestMeta.setDisplayName(ChatColor.GOLD + "You have no crate keys");
            chest.setItemMeta(chestMeta);
        } else {
            chest = new ItemStack(Material.ENDER_CHEST);
            ItemMeta chestMeta = chest.getItemMeta();
            chestMeta.setDisplayName(ChatColor.GOLD + "You have " + currentKeys + " crate key(s)");
            chest.setItemMeta(chestMeta);
        }
        player.getInventory().setItem(2,chest);
    }

    private void removeBadItems(Player player) {
        // loop through the players inventory and remove any items that are not allowed in the lobby
        // tnt
        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null) {
                if (item.getType() == Material.TNT) {
                    player.getInventory().remove(item);
                }
            }
        }
    }

    // returns if the passed player is currently spawn protected or not, used in the game listener when a player takes
    // damage from another player
    public boolean playerSpawnProtected(Player player) {
        return playerSpawnProtect.containsKey(player.getUniqueId());
    }
    public boolean isPlayerPortalling(Player player) {
        return playerPortalling.containsKey(player.getUniqueId());
    }

    public void addPlayerPortalling(Player player) {
        playerPortalling.put(player.getUniqueId(), gameSeconds);
    }

    public void playerKilled(Player died) {

        // stop them taking fall damage if teleported after falling
        died.setFallDistance(0F);
        for (PotionEffect effect : died.getActivePotionEffects()) {
            died.removePotionEffect(effect.getType());
        }

        Location location = ConfigManager.getLobbyReSpawn();
        died.teleport(location);
        sendMessage(died.getName() + " died!");
        playerSpawnProtect.put(died.getUniqueId(), gameSeconds);

    }

    public void teleportToParkour(Player player) {
        player.teleport(ConfigManager.getParkourSpawn());
    }

    public void playerJoined(Player player) {

        // check if this player is in the list of players who have been sent to a minigame server
        checkIfServerToRejoin(player.getUniqueId());
        lstNoPvp.add(player.getUniqueId());

        player.getInventory().clear();
        player.setGameMode(GameMode.SURVIVAL);
        Location location = ConfigManager.getLobbySpawn();
        player.teleport(location);
        // remove armour
        player.getInventory().setHelmet(null);
        player.getInventory().setChestplate(null);
        player.getInventory().setLeggings(null);
        player.getInventory().setBoots(null);

        addLobbyHotbarItems(player);
        showJoinMessages(player);
    }

    public void addLobbyHotbarItems(Player player) {
        ItemStack compass = new ItemStack(Material.NETHER_STAR);
        ItemMeta ism = compass.getItemMeta();
        ism.setDisplayName(ChatColor.BLUE+ "Go to lobby parkour");
        compass.setItemMeta(ism);
        player.getInventory().setItem(8,compass);
        //  if (player.isOp() || player.getName().equalsIgnoreCase("monkey_bean") || player.getName().equalsIgnoreCase("blouse00")) {
        //   player.getInventory().setItem(1, LobbyUtils.comsticsMenuItem());
        //   }
        // give them compass for teleport to parkour
        ItemStack shopItem = new ItemStack(Material.BEACON);
        ItemMeta shopMeta = shopItem.getItemMeta();
        shopMeta.setDisplayName(ChatColor.GOLD + "Cosmetics");
        shopItem.setItemMeta(shopMeta);
        player.getInventory().setItem(1,shopItem);

        ItemStack miniGameItem = new ItemStack(Material.BLAZE_ROD);
        ItemMeta miniGameItemItemMeta = miniGameItem.getItemMeta();
        miniGameItemItemMeta.setDisplayName(ChatColor.BLUE + "Sumo Practice");
        miniGameItem.setItemMeta(miniGameItemItemMeta);
        player.getInventory().setItem(3,miniGameItem);

        // give them netherStar to open game join inventory
        ItemStack netherStar = new ItemStack(Material.COMPASS);
        ItemMeta netherStarMeta = netherStar.getItemMeta();
        netherStarMeta.setDisplayName(ChatColor.BLUE+ "Choose a game type");
        netherStar.setItemMeta(netherStarMeta);
        player.getInventory().setItem(0,netherStar);
        // set active hotbar slot to middle
        player.getInventory().setHeldItemSlot(4);
    }

    private void checkIfServerToRejoin(UUID uuid) {
        System.out.println("LobbyManager checking if joining player needs to rejoin a server");
        // if they are check with that server to see if they were partway though a game and if so return them to that game
        PlayerServerInfo playerServerInfo = main.getGameManager().getPlayerServerInfo(uuid);
        if (playerServerInfo != null) {
            System.out.println("- player found in game list");
            // check with the server if the player was there
            if (isServerOnline(playerServerInfo.getSockName())) {
                System.out.println("server is online " + playerServerInfo.getSockName());
                System.out.flush();
                String inputString = "Lobby.player-rejoining." + playerServerInfo.getUuid();
                System.out.println("LobbyManager sending message to game server for rejoining player: " + inputString);
                SockExchangeApi api = SockExchangeApi.instance();
                byte[] byteArray = inputString.getBytes();
                api.sendToServer("LobbyChannel", byteArray, playerServerInfo.getSockName());

                main.getGameManager().addPlayerServerInfo(uuid, playerServerInfo.getSockName());
            } else {
                // server is offline
                System.out.println("server is offline " + playerServerInfo.getSockName());
            }
        }
    }

    private boolean isServerOnline(String serverName) {
        SockExchangeApi sockExchangeApi = main.getSockExchangeApi();
        SpigotServerInfo spigotServerInfo = sockExchangeApi.getServerInfo(serverName);
        return spigotServerInfo != null && spigotServerInfo.isOnline();
    }

    public void PlayerReJoinGameServer(String strUuid) {

        UUID uuid = UUID.fromString(strUuid);
        Player player = Bukkit.getPlayer(uuid);

        if (player != null) {
            System.out.println("Lobby PlayerReJoinGameServer server wanted player back " + player.getName());
            PlayerServerInfo playerServerInfo = main.getGameManager().getPlayerServerInfo(uuid);
            String sockName = playerServerInfo.getSockName();
            if (isServerOnline(playerServerInfo.getSockName())) {
                ByteArrayDataOutput out = ByteStreams.newDataOutput();
                out.writeUTF("Connect");
                out.writeUTF(sockName);
                getScheduler().scheduleSyncDelayedTask(main, () -> player.sendPluginMessage(main, "BungeeCord", out.toByteArray()), 10);
                System.out.println("Lobby PlayerReJoinGameServer server wanted player back " + player.getName() + " sent to " + sockName);
            }
        }
    }

    private void showJoinMessages(Player player) {
        int chatStart = 20;
        int chatSpeed = 80;
        int chatTime = chatStart + chatSpeed;
        getScheduler().scheduleSyncDelayedTask(main, () -> player.sendMessage(ChatColor.BLUE + "================" + ChatColor.GOLD + " Welcome to BashyBashy " + ChatColor.BLUE + "================"), chatStart);
        getScheduler().scheduleSyncDelayedTask(main, () -> player.sendMessage(ChatColor.WHITE + "Our mini-games will start with as few as 2 players!"), chatTime);
        chatTime += chatSpeed;
        getScheduler().scheduleSyncDelayedTask(main, () -> player.sendMessage(ChatColor.WHITE + "Feel free to bring your friends for a game any time."), chatTime);
        chatTime += chatSpeed;
        getScheduler().scheduleSyncDelayedTask(main, () -> player.sendMessage(ChatColor.WHITE + "The more players, the quicker the games will start!"), chatTime);
        chatTime += chatSpeed;
        getScheduler().scheduleSyncDelayedTask(main, () -> player.sendMessage(ChatColor.WHITE + "Currently we are mostly arranging game sessions via Discord."), chatTime);
        chatTime += chatSpeed;
        getScheduler().scheduleSyncDelayedTask(main, () -> player.sendMessage(ChatColor.WHITE + "So join our discord server! " + ChatColor.BLUE + "" + ChatColor.UNDERLINE + "https://discord.gg/Ypx4kTRbHp" ), chatTime);
        chatTime += chatSpeed;
        getScheduler().scheduleSyncDelayedTask(main, () -> player.sendMessage(ChatColor.GOLD + "Either use the " + ChatColor.BLUE + "compass" + ChatColor.GOLD + " in your hotbar to select a game, or click on an" +
                ChatColor.BLUE + " NPC " + ChatColor.GOLD + "to start." ), chatTime);
        chatTime += chatSpeed;
        getScheduler().scheduleSyncDelayedTask(main, () -> player.sendMessage(ChatColor.BLUE + "===================================================="), chatTime);
    }

    private void checkPlayerFeetLevel(Player player) {
        if (player != null) {
            int y = player.getLocation().getBlockY();
            System.out.println("floor level is " + feetBlockY + ", player level is " + y);
            if (y < feetBlockY) {
                System.out.println("Teleporting player up");
                Location playerLocation = player.getLocation();
                player.teleport( new Location(
                        Bukkit.getWorld("world"),
                        playerLocation.getX(),
                        feetBlockY,
                        playerLocation.getZ(),
                        playerLocation.getYaw(),
                        playerLocation.getPitch()));
            }
        }
    }

    private void showPlayerParticles() {
        for (UUID uuid : playerSpawnProtect.keySet()) {
            Player player = Bukkit.getPlayer(uuid);
            particleTask = Bukkit.getScheduler().runTaskTimer(main, () -> {
                if (particleIterator != 0) {

                    if (player != null) {
                        try {
                            //  Block of code to try
                            Location location = player.getLocation();

                            PacketPlayOutWorldParticles packet = new PacketPlayOutWorldParticles(EnumParticle.DRIP_LAVA,
                                    true, (float) location.getX(), (float) (location.getY()), (float) location.getZ(),
                                    (float) 0.5, (float) 0.5, (float) 0.5, 0, 10);
                            for (Player online : Bukkit.getOnlinePlayers()) {
                                ((CraftPlayer) online).getHandle().playerConnection.sendPacket(packet);
                            }
                        }
                        catch(Exception e) {
                            //  Block of code to handle errors
                            System.out.println("Player spawn protect particles error: " + e.getMessage());
                        }
                        particleIterator--;
                    } else {
                        System.out.println("player is null, cancelling particles");
                        particleIterator = 0;
                        if (particleTask != null) {
                            particleTask.cancel();
                        }
                    }
                } else {
                    // If "i" is zero, we cancel the task.
                    if (particleTask != null) {
                        particleTask.cancel();
                    }
                }
            }, 0, 1);
        }
    }

    public void removeSpawnProtect(Player player) {
        if(playerSpawnProtect.containsKey(player.getUniqueId())){
            System.out.println("Player caused damage that cancelled their spawn protection");
            playerSpawnProtect.remove(player.getUniqueId());
        }
    }

    private void checkPlayerZone() {

        for (Player player : Bukkit.getOnlinePlayers()) {

            UUID uuid = player.getUniqueId();


            if (isOutsideKitPvpArea(player.getLocation()) && main.isPlayerInKitPvP(player)) {
                LobbyUtils.leaveKitPVP(player, main);
                player.sendMessage(ChatColor.RED + "You have been returned to the lobby as you left the Kit PVP area.");
                continue;
            }

            if (isInPvpArea(player.getLocation(), pvpCoords)) {
                // player.sendMessage(ChatColor.RED + "Entering PVP area");
                lstNoPvp.remove(uuid);
            } else {
                if (!lstNoPvp.contains(uuid)){
                    lstNoPvp.add(uuid);
                   // player.sendMessage( ChatColor.GREEN + "Leaving PVP area");
                }
            }
        }
    }

    public static boolean isOutsideKitPvpArea(Location playerLocation) {

        double x = playerLocation.getX();
        double y = playerLocation.getY();
        double z = playerLocation.getZ();

        double lowx = -190;
        double highx = -85;
        double lowy = 37;
        double highy = 47;
        double lowz = 46;
        double highz = 152;

        return (!(x <= highx) || !(x >= lowx)) || (!(y <= highy) || !(y >= lowy)) || (!(z <= highz) || !(z >= lowz));
    }

    public static boolean isInPvpArea(Location playerLocation, int[][] pvpCoords) {

        double x = playerLocation.getX();
        double y = playerLocation.getY();
        double z = playerLocation.getZ();

        for (int[] pvpCoord : pvpCoords) {
            double lowx = pvpCoord[0];
            double highx = pvpCoord[1];
            double lowy = pvpCoord[2];
            double highy = pvpCoord[3];
            double lowz = pvpCoord[4];
            double highz = pvpCoord[5];

            if ((x <= highx && x >= lowx) && (y <= highy && y >= lowy) && (z <= highz && z >= lowz)) {
                return true;
            }
        }
        return false;
    }

    public boolean isNoPvp(UUID uuid) {
        return lstNoPvp.contains(uuid);
    }

    public void addToNoPvpList(UUID uuid) {
        lstNoPvp.add(uuid);
    }
    public void sendMessage(String message) {
        Bukkit.broadcastMessage(ChatColor.WHITE + message);
    }

    public void playerUsedSpeedCommand(Player player) {
        if (checkPlayerInMiniGame(player)) {
            player.sendMessage(ChatColor.RED + "You cannot use this command while in a mini-game!");
            return;
        }
        if (playerCooldownSpeed.containsKey(player.getUniqueId())) {
            int secondsLeft = (playerCooldownSpeed.get(player.getUniqueId()) + 30) - gameSeconds;
            player.sendMessage(ChatColor.RED + "You must wait " + secondsLeft + " seconds before using Speed again.");
        } else {
            player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 200, 1));
            playerCooldownSpeed.put(player.getUniqueId(), gameSeconds);
            player.sendMessage(ChatColor.GREEN + "You feel swift for 10 seconds!");
        }
    }

    public void playerUsedInvisCommand(Player player) {
        if (checkPlayerInMiniGame(player)) {
            player.sendMessage(ChatColor.RED + "You cannot use this command while in a mini-game!");
            return;
        }
        if( playerCooldownInvis.containsKey(player.getUniqueId())) {
            int secondsLeft = (playerCooldownInvis.get(player.getUniqueId()) + 30) - gameSeconds;
            player.sendMessage(ChatColor.RED + "You must wait " + secondsLeft + " seconds before using Invisibility again.");
        } else {
            player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 200, 1));
            playerCooldownInvis.put(player.getUniqueId(), gameSeconds);
            player.sendMessage(ChatColor.GREEN + "You feel invisible for 10 seconds!");
        }
    }

    public void playerUsedJumpCommand(Player player) {
        if (checkPlayerInMiniGame(player)) {
            player.sendMessage(ChatColor.RED + "You cannot use this command while in a mini-game!");
            return;
        }
        if( playerCooldownJump.containsKey(player.getUniqueId())) {
            int secondsLeft = (playerCooldownJump.get(player.getUniqueId()) + 30) - gameSeconds;
            player.sendMessage(ChatColor.RED + "You must wait " + secondsLeft + " seconds before using Jump again.");
        } else {
            player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, 200, 1));
            playerCooldownJump.put(player.getUniqueId(), gameSeconds);
            player.sendMessage(ChatColor.GREEN + "You feel bouncy for 10 seconds!");
        }
    }

    private boolean checkPlayerInMiniGame(Player player) {
        return main.getGameManager().getMiniGameManger().isPlayerInSumoGame(player) || main.isPlayerInKitPvP(player);
    }

}

