package com.stewart.lobby.manager;

import com.stewart.lobby.Lobby;
import com.stewart.lobby.utils.NewPlayerGameInventory;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.trait.SkinTrait;
import net.minecraft.server.v1_8_R3.EnumParticle;
import net.minecraft.server.v1_8_R3.PacketPlayOutWorldParticles;
import org.bukkit.*;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import static org.bukkit.Bukkit.getScheduler;

public class LobbyManager {


    private final Lobby main;
    private final int feetBlockY;
    private int gameSeconds;
    // keeps a list of all people currently spawn protected and the game time it started
    private final HashMap<UUID, Integer> playerSpawnProtect = new HashMap<>();
    private final HashMap<UUID, Integer> playerPortalling = new HashMap<>();
    private  int particleIterator = 19; // Task will run 10 times.
    private BukkitTask particleTask = null;
    private final List<UUID> lstNoPvp;
    private final Location noPvpTopCorner;
    private final Location noPvpBottomCorner;

    public LobbyManager(Lobby lobby) {
        this.main = lobby;
        feetBlockY = main.getConfig().getInt("feet-block-y");
        startClock();
        lstNoPvp = new ArrayList<>();
        noPvpTopCorner = new Location(Bukkit.getWorld("world"), 24, 58, -10);
        noPvpBottomCorner = new Location(Bukkit.getWorld("world"), 1, 48, -33 );
        spawnVoteMaster();
        spawnDiscordNPC();
    }

    private void spawnVoteMaster() {
        Bukkit.getScheduler().scheduleSyncDelayedTask(main, new Runnable() {
            @Override
            public void run() {
                String npcName = ChatColor.GOLD + "Votemaster";

                net.citizensnpcs.api.npc.NPC npc = CitizensAPI.getNPCRegistry().createNPC(EntityType.PLAYER, npcName);
                SkinTrait skin = npc.getOrAddTrait(SkinTrait.class);
                skin.setSkinPersistent("vote", ConfigManager.getVotesSkinSignature(), ConfigManager.getVotesSkinTexture());
                npc.spawn(new Location(Bukkit.getWorld("world"), 9.5, 51, -10.5, 180, -17));

                System.out.println("spawning npc for votes");
            }
        }, 200L);

    }

    private void spawnDiscordNPC() {
        Bukkit.getScheduler().scheduleSyncDelayedTask(main, new Runnable() {
            @Override
            public void run() {
                String npcName = ChatColor.BLUE + "Discord";

                net.citizensnpcs.api.npc.NPC npc = CitizensAPI.getNPCRegistry().createNPC(EntityType.PLAYER, npcName);
                SkinTrait skin = npc.getOrAddTrait(SkinTrait.class);
                skin.setSkinPersistent("discord", ConfigManager.getDiscordSkinSignature(), ConfigManager.getDiscordSkinTexture());
                npc.spawn(new Location(Bukkit.getWorld("world"), 1.5, 51, -18.5, -90, -10));

                System.out.println("spawning npc for discord");
            }
        }, 200L);

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

        checkPlayerZone();

        // removes players from the spawn protect list who has been there 5 seconds or more
        playerSpawnProtect.entrySet().removeIf(e -> e.getValue() + 4 < gameSeconds );
        // removes players from the portallingt list who has been there 4 seconds or more
        playerPortalling.entrySet().removeIf(e -> e.getValue() + 3 < gameSeconds );

        gameSeconds += 1;
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


        player.getInventory().clear();
        player.setGameMode(GameMode.SURVIVAL);
        Location location = ConfigManager.getLobbySpawn();
        player.teleport(location);
        // remove armour
        player.getInventory().setHelmet(null);
        player.getInventory().setChestplate(null);
        player.getInventory().setLeggings(null);
        player.getInventory().setBoots(null);
        // give them compass for teleport to parkour
        ItemStack compass = new ItemStack(Material.NETHER_STAR);
        ItemMeta ism = compass.getItemMeta();
        ism.setDisplayName(ChatColor.BLUE+ "Go to lobby parkour");
        compass.setItemMeta(ism);
        player.getInventory().setItem(8,compass);
        // give them netherStar to open game join inventory
        ItemStack netherStar = new ItemStack(Material.COMPASS);
        ItemMeta netherStarMeta = netherStar.getItemMeta();
        netherStarMeta.setDisplayName(ChatColor.BLUE+ "Choose a game type");
        netherStar.setItemMeta(netherStarMeta);
        player.getInventory().setItem(0,netherStar);
        // set active hotbar slot to middle
        player.getInventory().setHeldItemSlot(4);
        showJoinMessages(player);

        // after 2 seconds check & adjust their y coordinate
        Bukkit.getScheduler().scheduleSyncDelayedTask(main, () -> {
            checkPlayerFeetLevel(player);
        }, 40L);

       /* Bukkit.getScheduler().scheduleSyncDelayedTask(main, new Runnable() {
            @Override
            public void run() {
                NewPlayerGameInventory newPlayerGameInventory = new NewPlayerGameInventory(main);
                player.openInventory(newPlayerGameInventory.getGameInventory(player));
                main.getGameManager().AddPlayerToAutoJoin(player.getUniqueId());
            }
        }, 80L);*/
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
          //  System.out.println("Checking " + player.getName() + " is in list = " + (lstNoPvp.contains(uuid) ? "true" : "false"));

            if (isInRegion(player.getLocation(), noPvpBottomCorner, noPvpTopCorner)) {
                if (!lstNoPvp.contains(uuid)){
                    lstNoPvp.add(uuid);
                    player.sendMessage("Entering no PVP area");
                }
            } else {
                if (lstNoPvp.contains(uuid)) {
                    lstNoPvp.remove(uuid);
                    player.sendMessage("Leaving no PVP area");
                }
            }
        }
    }

    public static boolean isInRegion(Location playerLocation, Location lowestPos, Location highestPos){

        double x = playerLocation.getX();
        double y = playerLocation.getY();
        double z = playerLocation.getZ();

        double lowx = lowestPos.getX();
        double lowy = lowestPos.getY();
        double lowz = lowestPos.getZ();

        double highx = highestPos.getX();
        double highy = highestPos.getY();
        double highz = highestPos.getZ();

        return (x <= highx && x >= lowx) && (y <= highy && y >= lowy) && (z <= highz && z >= lowz);
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

}
