package com.stewart.lobby.manager;

import com.gmail.tracebachi.SockExchange.Spigot.SockExchangeApi;
import com.gmail.tracebachi.SockExchange.SpigotServerInfo;
import com.stewart.lobby.Lobby;
import com.stewart.lobby.instances.Game;
import com.stewart.lobby.instances.GameServer;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.event.DespawnReason;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.npc.NPCRegistry;
import net.minecraft.server.v1_8_R3.Scoreboard;
import net.minecraft.server.v1_8_R3.ScoreboardTeam;
import net.minecraft.server.v1_8_R3.ScoreboardTeamBase;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scoreboard.NameTagVisibility;
import org.bukkit.scoreboard.Team;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

// game manager keeps a list of all the game instances for each game listed in the config file
// it also has a list of portalmanagers, these are basically the but could be changed to be villagers etc.
public class GameManager {

    private Lobby main;
    private List<Game> gameList = new ArrayList<>();
    YamlConfiguration gameConfig;

    public GameManager(Lobby lobby) {
        this.main = lobby;
        File file = new File(main.getDataFolder(), "games.yml");



        gameConfig =  YamlConfiguration.loadConfiguration(file);
        // when starting the server for the first time poll all the games in the games.yml file
        // responses are handled separately
        // need a delay here otherwise the sockexchangeApi will be null - needs a moment as the plugin has just started
        Bukkit.getScheduler().scheduleSyncDelayedTask(main, new Runnable() {
            @Override
            public void run() {
                deSpawnNPCS();
                checkGamesList();
            }
        }, 60L);


    }

    // should get rid of any npcs already in the map, before more are added.  not sure about this though
    // there always seems to be 2 npcs when the plugin loads then it adds a1 more for the game
    // this gets rid of the 2 that are loaded before the next one is added.
    public void deSpawnNPCS() {
    Iterable<NPCRegistry> ccc = CitizensAPI.getNPCRegistries();
        for (NPCRegistry x : ccc) {
            if (x.sorted() instanceof Collection) {
                System.out.println("npc count = " + ((Collection<?>) x.sorted()).size());
            }
            for (NPC nnn : x.sorted()) {
                System.out.println("npc " + nnn.getName() + "is spawned = " + nnn.isSpawned());
                nnn.despawn();
                nnn.destroy();
            }
        }
    }

    public void checkGamesList() {


        // first loop through each game type
        for (String s : gameConfig.getConfigurationSection("games").getKeys(false)) {
            // eg 'bedwars_solo'
            String gameName = gameConfig.getString("games" + "." + s + ".name");
            String texture = gameConfig.getString("games" + "." + s + ".npc-skin-texture");
            String signature = gameConfig.getString("games" + "." + s + ".npc-skin-signature");
            String nameColour = gameConfig.getString("games" + "." + s + ".npc-name-colour");
            int gameColour = -1;
            int inventorySocket = -1;
            if (gameConfig.contains("games" + "." + s + ".npc-name-colour")) {
                 gameColour = gameConfig.getInt("games" + "." + s + ".colour-int");
                inventorySocket = gameConfig.getInt("games" + "." + s + ".inventory-slot");
            }

            Location location = new Location(Bukkit.getWorld("world"),
                    gameConfig.getDouble("games" + "." + s + ".npc-x"),
                    gameConfig.getDouble("games" + "." + s + ".npc-y"),
                    gameConfig.getDouble("games" + "." + s + ".npc-z"),
                    (float) gameConfig.getDouble("games" + "." + s + ".npc-yaw"),
                    (float) gameConfig.getDouble("games" + "." + s + ".npc-pitch"));

            Game game   = new Game(main, gameName, location, texture, signature, nameColour, gameColour, inventorySocket);
            game.spawnNPC();
            gameList.add(game);

            // game server data will be filled when the server replies

            // the loop through the servers for that type
            for (String s1 : gameConfig.getConfigurationSection("games" + "." + s + ".servers").getKeys(false)) {
                SockExchangeApi sockExchangeApi = main.getSockExchangeApi();
                String sockName = gameConfig.getString("games" + "." + s + ".servers." + s1 + ".sockName");
                    SpigotServerInfo spigotServerInfo = sockExchangeApi.getServerInfo(sockName);
                    if (spigotServerInfo == null) {
                        System.out.println("server not found " + sockName);
                    } else {
                        if (spigotServerInfo.isOnline()) {
                            // server is online
                            // send a message to the game server requesting it to return its status
                            System.out.flush();
                            // string should be split at _
                            // lobby shows request originator
                            // request-status to let it know what we want
                            // the server sockName, to be returned with any reply so this class knows which server to update.

                            String inputString = "Lobby.request-status." + sockName;
                            System.out.println("Requesting game server status: " + sockName + ", msg:" + inputString);
                            SockExchangeApi api = SockExchangeApi.instance();
                            byte[] byteArray = inputString.getBytes();
                            api.sendToServer("LobbyChannel", byteArray, sockName);
                        } else {
                            // server is offline
                            System.out.println("server is offline " + sockName);
                        }
                    }
                }
            }
        }

        public void updateGameServer(String sockName, String gameName, String status, int currentPlayers, int maxPlayers,
                                     int teamSize) {
            // find the game to be updated
            System.out.println("get game by name: " + gameName);
            Game game = getGameByName(gameName);
            if (game != null) {
                game.setMaxPlayers(maxPlayers);
                // now get the game server
                GameServer gameServer = game.getServerBySockName(sockName);
                if (gameServer != null) {
                    System.out.println("Game server object already exists");
                   gameServer.updateDetails(status, currentPlayers, teamSize);
                } else {
                    System.out.println("Game server not found in list, adding it now");
                    gameServer = new GameServer(sockName, status, currentPlayers, teamSize);
                    game.addServerToList(gameServer);
                }
                game.setIsBlocked(false);
                game.checkQueue();
            } else {
                System.out.println("game is not in game list - null");
            }
        }

        // remove player from all game queues except the one passed
        public void removePlayerFromQueues(UUID uuid, Game gameKeep) {
        for (Game game :gameList) {
            System.out.println("remove from queue start, size: " + game.getQueueSize());
            if (gameKeep == null) {
                game.removePlayerFromQueue(uuid);
            } else {
                if (!game.equals(gameKeep)) {
                    game.removePlayerFromQueue(uuid);
                }
            }
            System.out.println("remove from queue finish, size: " + game.getQueueSize());
        }
    }


    public Game getGameByName(String name) {
        if (gameList == null) {
            return null;
        }
        for (Game game : gameList) {
            if (game.getGameName().equalsIgnoreCase(name)) {
                return game;
            }
        }
        return  null;
    }

    public Game getGameByNpcName(String name) {
        // npc name will begin with a 2 character colour code

        if (gameList == null) {
            return null;
        }
        for (Game game : gameList) {
            System.out.println("passed name: " + name + ", game name: " + game.getGameName());
            if (name.contains(game.getGameName())) {
                return game;
            }
        }
        return  null;
    }

    public void addGameHotbarItems(Player player) {
        for (Game game : gameList) {
            if (game.getGameColourInt() > -1) {
                ItemStack wool = new ItemStack(new ItemStack(Material.WOOL, 1, (short) game.getGameColourInt()));
                ItemMeta woolMeta = wool.getItemMeta();
                woolMeta.setDisplayName(ChatColor.GOLD + "Join " + game.getGameName());
                wool.setItemMeta(woolMeta);
                player.getInventory().setItem(game.getInventorySlot(), wool);
            }
        }
    }

    public void hotbarItemClicked(Player player, int slot) {
        for (Game game : gameList) {
            if (game.getInventorySlot() == slot) {
                game.playerJoinRequest(player);
                return;
            }
        }
    }




}

   /* private List<Game> games = new ArrayList<>();
    private List<PortalManager> signs = new ArrayList<>();

    // when the class is constructed get all the games from the config file
    public GameManager(Lobby lobby) {
        FileConfiguration config = lobby.getConfig();
        for (String gameID : config.getConfigurationSection("games").getKeys(false)) {

            // add each game to the list of games
            games.add(new Game(lobby, Integer.parseInt(gameID),
                    config.getString("games." + gameID + ".game"),
                    config.getInt("games." + gameID + ".max-players"),
                    config.getBoolean("games." + gameID + ".has-sub-lobby")
            ));

            // it may be that each game has more than one server.
            // I'll loop through each games 'signs' in the config file to get each server for the game
            // each config sign will have it's coordnates.  There should be an ingame sign at each coordinate

            for (String s : config.getConfigurationSection("games." + gameID + ".signs").getKeys(false)) {
                Location location = new Location(
                        Bukkit.getWorld("World"),
                        config.getDouble("games." + gameID + ".signs." + s + ".x"),
                        config.getDouble("games." + gameID + ".signs." + s +  ".y"),
                        config.getDouble("games." + gameID + ".signs." + s + ".z"));

                PortalManager portalManager = new PortalManager(lobby,Integer.parseInt(s), Integer.parseInt(gameID), location,
                        config.getString("games." + gameID + ".signs." + s + ".server"),
                        config.getString("games." + gameID + ".signs." + s + ".ip"),
                        config.getInt("games." + gameID + ".signs." + s + ".port")
                );

                // I started off calling them signs but changed to portal to keep it relevant if we use villagers.
                // the list is still called signs though for some reason
                // each sign ( which represents a game server) is added to the signs list
                signs.add(portalManager);
            }
        }
    }

    // return a list of all the games - unused
    public  List<Game> getGames() {return  games;}

    // get a game by ID
    public Game getGame(int id) {
        System.out.println("games length = " + games.size());
        for(Game game : games) {
            System.out.println("games getID = " + game.getId());
            if (game.getId() == id) {
                return game;
            }
        }
        return  null;
    }

    // get a sign post by its location - used when a player clicks on a sign
    public PortalManager getSignPost(Location location) {
        for (PortalManager sign :signs) {
            if (sign.getSignLocation().equals(location)) {
                return sign;
            }
        }
        return null;
    }

    // get a sign iven its gameID and signID
    public PortalManager getSignPost(int gameID, int signID) {
        for (PortalManager sign :signs) {
            if (sign.getGameID() == gameID && sign.getID() == signID) {
                return sign;
            }
        }
        return null;
    }

    // used when the lobby starts to send a message to each server  asking it to respond
    // with its current status so the sign can be updated.
    // not sure if the works though, I always start the loby first so it receives a message when the game starts
    // any received message will arrive in the requestConsumer in the main lobby class
    public void checkGamesOnline(SockExchangeApi api) {
        for (PortalManager p : signs) {
            SpigotServerInfo spigotServerInfo = api.getServerInfo(p.getServerName());
            if (spigotServerInfo == null) {
                System.out.println(p.getServerName() + " is offline ");
                p.setIsFull(true);
                p.setOffline();
            } else {
                if (spigotServerInfo.isOnline()) {
                    System.out.println(p.getServerName() + " is Online");
                    p.updateSign();
                } else {
                    System.out.println(p.getServerName() + " is offline ");
                    p.setIsFull(true);
                    p.setOffline();
                }
            }
        }



    } */
