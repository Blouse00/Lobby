package com.stewart.lobby.manager;

import com.stewart.lobby.Lobby;
import net.minecraft.server.v1_8_R3.EnumParticle;
import net.minecraft.server.v1_8_R3.PacketPlayOutWorldParticles;
import org.bukkit.*;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scheduler.BukkitTask;
import org.stewart.bb_api.Bb_api;

import java.util.HashMap;
import java.util.UUID;

public class LobbyManager {


    private final Lobby main;
    private final int feetBlockY;
    private int gameSeconds;
    // keeps a list of all people currently spawn protected and the game time it started
    private final HashMap<UUID, Integer> playerSpawnProtect = new HashMap<>();
    private  int particleIterator = 19; // Task will run 10 times.
    private BukkitTask particleTask = null;

    public LobbyManager(Lobby lobby) {
        this.main = lobby;
        feetBlockY = main.getConfig().getInt("feet-block-y");
        startClock();
    }

    private void startClock() {
        gameSeconds = 0;
        BukkitScheduler scheduler = Bukkit.getServer().getScheduler();
        // Run Whatever code you might have
        scheduler.scheduleSyncRepeatingTask(main, this::doClockTick, 0L, 20L);
    }

    private void doClockTick() {


        if (playerSpawnProtect.size() > 0) {
            // remove any players from spawn protect list that may have left the game
            playerSpawnProtect.entrySet().removeIf(e->  Bukkit.getPlayer(e.getKey()) == null );
            // show particles around spawn protected players
            particleIterator = 19;
            particleTask = null;
            showPlayerParticles();
        }

        // removes players from the spawn protect list who has been there 5 seconds or more
        playerSpawnProtect.entrySet().removeIf(e -> e.getValue() + 4 < gameSeconds );

        gameSeconds += 1;
    }

    // returns if the passed player is currently spawn protected or not, used in the game listener when a player takes
    // damage from another player
    public boolean playerSpawnProtected(Player player) {
        return playerSpawnProtect.containsKey(player.getUniqueId());
    }

    public void playerKilled(Player died) {

        // stop them taking fall damage if teleported after falling
        died.setFallDistance(0F);
        for (PotionEffect effect : died.getActivePotionEffects()) {
            died.removePotionEffect(effect.getType());
        }

        Location location = ConfigManager.getLobbySpawn();
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
        ItemStack compass = new ItemStack(Material.STONE_SLAB2);
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

        // after 2 seconds check & adjust their y coordinate
        Bukkit.getScheduler().scheduleSyncDelayedTask(main, () -> {
            if (player != null) {
                checkPlayerFeetLevel(player);
            }
        }, 40L);
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

    public void sendMessage(String message) {
        Bukkit.broadcastMessage(ChatColor.WHITE + message);
    }

}
