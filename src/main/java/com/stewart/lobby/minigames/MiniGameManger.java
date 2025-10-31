package com.stewart.lobby.minigames;

import com.stewart.lobby.Lobby;
import com.stewart.lobby.manager.ConfigManager;
import com.stewart.lobby.utils.LobbyUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import java.util.ArrayList;
import java.util.List;

public class MiniGameManger {
    private List<SumoArena> lstSumoArenas = new ArrayList<>();
    YamlConfiguration gameConfig;
    public MiniGameManger(YamlConfiguration gameConfig, Lobby main) {
        this.gameConfig = gameConfig;
        System.out.println("MiniGameManger class initialized");
        loadSumoArenas(main);
    }

    private void loadSumoArenas(Lobby main) {
        for (String s : gameConfig.getConfigurationSection( "sumo-arenas").getKeys(false)) {

            Location spawn1 = new Location(Bukkit.getWorld(gameConfig.getString( "sumo-arenas." + s + ".world")),
                    gameConfig.getDouble( "sumo-arenas." + s + ".spawn1-x"),
                    gameConfig.getDouble("sumo-arenas." + s + ".spawn1-y"),
                    gameConfig.getDouble(  "sumo-arenas." + s + ".spawn1-z"),
                    (float) gameConfig.getDouble( "sumo-arenas." + s + ".spawn1-yaw"),
                    (float) gameConfig.getDouble( "sumo-arenas." + s + ".spawn1-pitch"));

            Location spawn2 = new Location(Bukkit.getWorld(gameConfig.getString( "sumo-arenas." + s + ".world")),
                    gameConfig.getDouble( "sumo-arenas." + s + ".spawn2-x"),
                    gameConfig.getDouble("sumo-arenas." + s + ".spawn2-y"),
                    gameConfig.getDouble(  "sumo-arenas." + s + ".spawn2-z"),
                    (float) gameConfig.getDouble( "sumo-arenas." + s + ".spawn2-yaw"),
                    (float) gameConfig.getDouble( "sumo-arenas." + s + ".spawn2-pitch"));

            SumoArena sumoArena = new SumoArena(s, spawn1, spawn2, gameConfig.getInt( "sumo-arenas." + s + ".y-min"), main);
            lstSumoArenas.add(sumoArena);
          //  System.out.println("Loaded Sumo Arena: " + s);
        }
    }

    public void SumoGameRequested(Player player, int difficulty, Lobby main) {
        player.closeInventory();
        System.out.println("Sumo game requested by player number of arenas: " + lstSumoArenas.size());
        for (SumoArena arena : lstSumoArenas) {
            if (!arena.isInUse()) {
                arena.setInUse(true);
                arena.getSumoMiniGame().setDifficulty(difficulty);
                arena.getSumoMiniGame().addPlayerToGame(player);
                arena.getSumoMiniGame().startCountdown();
                System.out.println("Sumo game started in arena with difficulty " + difficulty);
                LobbyUtils.sendGameJoinMessage(player.getName(), "Sumo bots");

                if (main.getJda() != null) {
                    main.getJda().getGuildById(ConfigManager.getDiscordServer())
                            .getTextChannelById(ConfigManager.getDiscordChannel())
                            .sendMessage("Sending " + player.getName() + " to Sumo-bot").queue();
                }
                return;
            }
        }
        player.sendMessage(ChatColor.RED + "All Sumo arenas are currently in use. Please try again later.");
       // System.out.println("No available Sumo arenas at the moment.");
    }

    public void entityDamageEntityFired(EntityDamageByEntityEvent e) {
        for (SumoArena arena : lstSumoArenas) {
            if (arena.isInUse()) {
                arena.getSumoMiniGame().entityDamageEntityFired(e);
            }
        }
    }




}
