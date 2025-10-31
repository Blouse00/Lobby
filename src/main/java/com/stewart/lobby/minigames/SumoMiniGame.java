package com.stewart.lobby.minigames;

import com.stewart.lobby.Lobby;
import com.stewart.lobby.manager.ConfigManager;
import com.stewart.lobby.utils.Countdown;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.util.Vector;

import java.util.ArrayList;

public class SumoMiniGame extends Minigame {

    private String botReach = "3";
    private String botAttackRate = "0.4";
    private String botSpeed = "1";
    private int difficulty = 0; // 0 = noob, 1 = easy, 2 = medium, 3 = hard, 4 = hacker
    private SumoArena sumoArena;
    private NPC sumoNpc;
    int clockTickerId;
    private boolean sentinelRemoved = false; // I need to remove the sentiel trait from the bot to make it
    // fall when it gets stuck in the air this variable is to make sure it only happens once (its a toggle so doing it again would add it back on)

    public SumoMiniGame(Lobby lobby, SumoArena sumoArena) {
        super.setLobbyInstance(lobby);
        this.sumoArena = sumoArena;
        System.out.println("SumoMinigame class initialized");
    }

    public void setDifficulty(int difficulty) {
        if (difficulty < 0 || difficulty > 4) {
            throw new IllegalArgumentException("Difficulty must be 0 (noob), 1 (easy), 2 (medium), or 3 (hard), 4 (hacker).");
        }
        this.difficulty = difficulty;
    }

    public void startCountdown() {
        sentinelRemoved = false;
        // Implement countdown logic here
        if (countdown == null) {
          //  System.out.println("Starting new countdown for Sumo MiniGame.");
            countdown = new Countdown(main, this, 5);
        } else if (countdown.isRunning()) {
            System.out.println("Countdown is already running.");
            countdown.cancel();
            countdown = null;
            countdown = new Countdown(main, this, 5);
        }
        countdown.start();

        // can only be 1 player in bot game
        if (playersInGame.size() == 1) {
            Player player = playersInGame.get(0);
            player.getInventory().clear();
            Location spawnLocation = sumoArena.getSpawn1();
            player.teleport(spawnLocation);
            freezePlayers(true);
            // Add bot to the game
            spawnSumoBot(sumoArena.getSpawn2());

        } else {
            gameEnd(false);
            throw new IllegalStateException("SumoMiniGame can only start with 1 or 2 players.");
        }

       // System.out.println("Countdown started for Sumo MiniGame.");
    }

    private void clockTick() {
        // if the players location y is less than the min y of the arena, they lose
        for (Player player : playersInGame) {
            if (player != null && player.isOnline()) {
                player.setHealth(20); // heal player to full health
               // System.out.println("y min = " + sumoArena.getyMin() + " player y = " + player.getLocation().getY());
                if (player.getLocation().getY() < sumoArena.getyMin()) {
                    System.out.println("Player fell off the arena, player loses.");
                    gameEnd(false);
                    return;
                }
            }
        }
        // if the bots location y is less than the min y of the arena, the player wins
        if (sumoNpc != null && sumoNpc.isSpawned()) {
            if (sumoNpc.getEntity().getLocation().getY() < sumoArena.getyMin()) {
                System.out.println("Bot fell off the arena, player wins.");
                gameEnd(true);
            } else {
                LivingEntity livingEntity = (LivingEntity) sumoNpc.getEntity();
                livingEntity.setHealth(20); // heal bot to full health
                makeFall(livingEntity);
            }
        } else {
            // bot is not spawned, player wins
            System.out.println("Bot is not spawned, player wins.");
            gameEnd(true);
        }
        if (playersInGame.size() == 0) {
            System.out.println("No players in game, ending game.");
            gameEnd(false);
        }
    }

    private void makeFall(LivingEntity bot) {

        // if the 3 blocks below the bot are air make them fall
        Location loc = bot.getLocation();
        if (loc.getBlock().getRelative(0, -1, 0).getType().equals(Material.AIR) &&
                loc.getBlock().getRelative(0, -2, 0).getType().equals(Material.AIR) &&
                loc.getBlock().getRelative(0, -3, 0).getType().equals(Material.AIR) &&
                loc.getBlock().getRelative(0, -4, 0).getType().equals(Material.AIR)) {
            // make the bot fall
            // Grab the player's vector
         //   Vector v = bot.getVelocity();
            System.out.println("Bot is falling, applying force to make it fall fasterdd.");

            gameEnd(true);

            // remove the sentinel trait if not already removed
          /*  if (!sentinelRemoved) {
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "npc select " + sumoNpc.getName());
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "trait sentinel");
                sentinelRemoved = true;
            }*/
            // Apply force in a northern direction, which is negative on the Z-axis in Minecraft

            // If the player is on solid ground, friction will dampen the force significantly.
            // To counter this, also push the player slightly upwards in the air
          //  v.setY(-1);

            // Apply our modified vector to the player
          //  bot.setVelocity(v);
        }
    }



    @Override
    public void start() {

        freezePlayers(false);
        makeBotHostile();
        clockTickerId = Bukkit.getScheduler().scheduleSyncRepeatingTask(main, () -> clockTick(), 20L, 20L);
    }



    @Override
    public void gameEnd(boolean PlayerWon) {
        // telepet all players to lobby spawn
        Location location = ConfigManager.getLobbySpawn();

        for (Player player : playersInGame) {
            if (player != null && player.isOnline()) {
                player.teleport(location);
                player.setFireTicks(0);
                main.getLobbyManager().addLobbyHotbarItems(player);
                if (PlayerWon) {
                    sendTitleSubtitle("You Win!",  "Congratulations!", null, null);
                } else {
                    sendTitleSubtitle("You Lost!",  "Better luck next time!", null, null);
                }
            }
        }
        if (clockTickerId != 0) {
            Bukkit.getScheduler().cancelTask(clockTickerId);
            clockTickerId = 0;
        }
        // destroy the bot
        if (sumoNpc != null && sumoNpc.isSpawned()) {
            sumoNpc.destroy();
            sumoNpc = null;
        }
        if (countdown != null) {
            // stop the countdown if it's still running
            if (countdown.isRunning()) {
                countdown.cancel();
            }
            countdown = null;
        }
        playersInGame = new ArrayList<>();
        System.out.println("Sumo MiniGame ended. Player won: " + PlayerWon);
        sumoArena.setInUse(false);

    }

    private void spawnSumoBot(Location location) {
        if (sumoNpc != null && sumoNpc.isSpawned()) {
            sumoNpc.destroy();
        }

        sumoNpc = CitizensAPI.getNPCRegistry().createNPC(EntityType.PLAYER, "SumoBot" + sumoArena.getArenaId());
        sumoNpc.spawn(location);

        sumoNpc.data().setPersistent(NPC.Metadata.COLLIDABLE, true);
        sumoNpc.data().setPersistent(NPC.Metadata.DEFAULT_PROTECTED, false);
        sumoNpc.spawn(location);

    }

    private void makeBotHostile() {
        // sumoNpc must be not null and spawned
        if (sumoNpc == null || !sumoNpc.isSpawned()) {
            throw new IllegalStateException("Sumo NPC is not spawned.");
        }
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "npc select " + sumoNpc.getName());
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "trait sentinel");
        if (difficulty == 0) { // noob
            botAttackRate = "0.30";
           // botAttackRate = "10";
            botReach = "2.6";
            botSpeed = "0.8";
        } else if (difficulty == 1) { // medium
            botAttackRate = "0.2";
            botReach = "3";
            botSpeed = "1";
         //   sumoNpc.getNavigator().getLocalParameters().attackDelayTicks(4);
        } else if (difficulty == 2) { // medium
            botAttackRate = "0.15";
            botReach = "3.2";
            botSpeed = "1.1";
         //   sumoNpc.getNavigator().getLocalParameters().attackDelayTicks(3);
        } else if (difficulty == 3) { // hard
            botReach = "3.6";
            botAttackRate = "0.05";
            botSpeed = "1.8";
           // sumoNpc.getNavigator().getLocalParameters().attackDelayTicks(2);
        } else if (difficulty == 4) { // hacker
            botReach = "4";
            botAttackRate = "0.05";
            botSpeed = "2.2";
            // sumoNpc.getNavigator().getLocalParameters().attackDelayTicks(2);
        }
        System.out.println("Bot attack rate set to " + botAttackRate + " for difficulty " + difficulty);
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "sentinel attackrate " + botAttackRate);
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "sentinel reach " + botReach);
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "sentinel speed " + botSpeed);
      //  Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "sentinel damage 0");
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "sentinel addtarget players");
   //     sumoNpc.getNavigator().setTarget(playersInGame.get(0), true);
  //      sumoNpc.getNavigator().setTarget(Bukkit.getPlayer("Gibby_law"), true);
    }

    @Override
    public void countDownTick() {
        for (Player player : playersInGame) {
            if (player != null && player.isOnline()) {
                Location location = sumoArena.getSpawn1().clone();
                location.setYaw(player.getLocation().getYaw());
                location.setPitch(player.getLocation().getPitch());
                player.teleport(location);
            }
        }
    }

    public void   entityDamageEntityFired(EntityDamageByEntityEvent e) {
        if (difficulty > 2) { // only apply knockback on hard and hacker difficulties
          //  System.out.println("EntityDamageByEntityEvent triggered.");
            if (e.getEntity() instanceof Player) {
                if (!playersInGame.contains((Player) e.getEntity())) {
                    return;
                }
                Entity p = e.getEntity();
              //  e.setCancelled(true);
                Bukkit.getScheduler().scheduleSyncDelayedTask(main, () -> doKnockback(p, e.getDamager(), false), 1L);
            }/* else if (e.getDamager() instanceof Player) {
            System.out.println("Damager is a player.");
            if (!playersInGame.contains((Player) e.getDamager())) {
                return;
            }
            System.out.println("Bot was hit by player, applying knockback.");
            e.setCancelled(true);
            Bukkit.getScheduler().scheduleSyncDelayedTask(main, () -> doKnockback(e.getDamager(), e.getEntity(), true), 1L);
        }*/
        }
    }

    private void doKnockback(Entity playerEntity, Entity damagerEntity, boolean playerIsDamager) {
          //  System.out.println("Applying knockback to " + (playerIsDamager ? "bot" : "player"));
            double originalLength = playerEntity.getVelocity().length();
            double originalY = playerEntity.getVelocity().getY();
            System.out.println("original length = " + originalLength);
          //  playerEntity.setVelocity( damagerEntity.getLocation().getDirection().multiply(1D));

            Vector direction = playerEntity.getLocation().toVector().subtract(damagerEntity.getLocation().toVector());
            direction.normalize().multiply(originalLength);

            double kbval = 0.3;
            if (difficulty == 4) { // hacker
                kbval = 0.4;
            }

            // if the vector length is less than 0.5, set it to 0.5
            if (direction.length() < kbval) {
                direction.normalize().multiply(kbval);


                // if they wer up dont put them any higher
                if (originalY > 0) {
                    direction.setY(0);
                }
            }
        System.out.println("vector length after kb = " + direction.length());
        playerEntity.setVelocity( direction);
      //  System.out.println("new vector length = " + playerEntity.getVelocity().length());
    }








}
