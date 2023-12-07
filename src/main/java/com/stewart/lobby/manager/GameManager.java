package com.stewart.lobby.manager;

import com.gmail.tracebachi.SockExchange.Spigot.SockExchangeApi;
import com.gmail.tracebachi.SockExchange.SpigotServerInfo;
import com.stewart.lobby.Lobby;
import com.stewart.lobby.instances.Game;
import com.stewart.lobby.instances.GameServer;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.npc.NPCRegistry;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.File;
import java.sql.Timestamp;
import java.util.*;

// game manager keeps a list of all the game instances for each game listed in the config file
public class GameManager {

    private Lobby main;
    private List<Game> gameList = new ArrayList<>();
    YamlConfiguration gameConfig;
    private List<GameServer> bedwarsServerList = new ArrayList<>();

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
                setUpBedwars();
                setUpGames();
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

    public void setUpGames() {
        String[]  arrSubtypes = {"assault", "smp", "fia"};
        // first loop through each game type
        for (String s : arrSubtypes) {
            // eg 'bedwars_solo'
            String gameName = gameConfig.getString( s + ".name");
            String texture = gameConfig.getString( s + ".npc-skin-texture");
            String signature = gameConfig.getString( s + ".npc-skin-signature");
            String nameColour = gameConfig.getString(s + ".npc-name-colour");
            Material material = Material.DIAMOND_BLOCK;
            int inventorySocket = -1;
            if (gameConfig.contains(s + ".npc-name-colour")) {
                material = Material.getMaterial(gameConfig.getString( s + ".material"));
                inventorySocket = gameConfig.getInt( s + ".inventory-slot");
            }

            Location location = new Location(Bukkit.getWorld("world"),
                    gameConfig.getDouble( s + ".npc-x"),
                    gameConfig.getDouble( s + ".npc-y"),
                    gameConfig.getDouble(s + ".npc-z"),
                    (float) gameConfig.getDouble(s + ".npc-yaw"),
                    (float) gameConfig.getDouble(s + ".npc-pitch"));

            Game game   = new Game(main, gameName, location, texture, signature, nameColour, material, inventorySocket );
            game.spawnNPC();
            gameList.add(game);

            if (s == "smp") {
                game.updateGameServer(gameConfig.getString(s + ".sockname"),
                        "RECRUITING", 1, 10);
            } else {

                System.out.println("game name " + s);
                for (String s1 : gameConfig.getConfigurationSection(s + ".servers").getKeys(false)) {
                    SockExchangeApi sockExchangeApi = main.getSockExchangeApi();
                    String sockName = gameConfig.getString(s + ".servers." + s1 + ".sockName");
                    System.out.println("game server " + sockName);
                    SpigotServerInfo spigotServerInfo = sockExchangeApi.getServerInfo(sockName);
                    if (spigotServerInfo == null) {
                        System.out.println("server not found " + sockName);
                    } else {
                        if (spigotServerInfo.isOnline()) {
                            System.out.println("server is online requesting status");
                            // server is online
                            System.out.flush();
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
    }

    public void setUpBedwars() {
       String[]  arrSubtypes = {"solo", "duo", "quad"};
        // first loop through each game type
        for (String s : arrSubtypes) {
            // eg 'bedwars_solo'
            String gameName = gameConfig.getString("bedwars" + "." + s + ".name");
            String texture = gameConfig.getString("bedwars" + "." + s + ".npc-skin-texture");
            String signature = gameConfig.getString("bedwars" + "." + s + ".npc-skin-signature");
            String nameColour = gameConfig.getString("bedwars" + "." + s + ".npc-name-colour");
            Material material = Material.DIAMOND_BLOCK;
            int inventorySocket = -1;
            if (gameConfig.contains("bedwars" + "." + s + ".npc-name-colour")) {
                material = Material.getMaterial(gameConfig.getString("bedwars" + "." + s + ".material"));
                inventorySocket = gameConfig.getInt("bedwars" + "." + s + ".inventory-slot");
            }

            Location location = new Location(Bukkit.getWorld("world"),
                    gameConfig.getDouble("bedwars" + "." + s + ".npc-x"),
                    gameConfig.getDouble("bedwars" + "." + s + ".npc-y"),
                    gameConfig.getDouble("bedwars" + "." + s + ".npc-z"),
                    (float) gameConfig.getDouble("bedwars" + "." + s + ".npc-yaw"),
                    (float) gameConfig.getDouble("bedwars" + "." + s + ".npc-pitch"));

            Game game = new Game(main, gameName, location, texture, signature, nameColour, material, inventorySocket);
            game.spawnNPC();
            gameList.add(game);
        }

        // the loop through the servers for that type
        for (String s1 : gameConfig.getConfigurationSection("bedwars.servers").getKeys(false)) {
            SockExchangeApi sockExchangeApi = main.getSockExchangeApi();
            String sockName = gameConfig.getString("bedwars.servers." + s1 + ".sockName");
            SpigotServerInfo spigotServerInfo = sockExchangeApi.getServerInfo(sockName);
            if (spigotServerInfo == null) {
                System.out.println("server not found " + sockName);
            } else {
                if (spigotServerInfo.isOnline()) {
                    // server is online
                    // send a message to the game server requesting it to return its status
                    System.out.flush();
                    // string should be split at .
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

    public void updateBedwarsGameServer(String sockName, String status, int currentPlayers, int teamSize) {
        // find the server to be updated
        System.out.println("Updating/creating bedwars server object status after sock message from its server.");
        GameServer bedwarsServer = getBedwarsServerBySockName(sockName);
        if (bedwarsServer != null) {
            System.out.println("Bedwars server object already exists, setting teamSize: " + teamSize + ", status; " +
                    status + ", numPlayers: " + currentPlayers);
            // team size determines the servers game type (solos, duos or quads)
            // it also determines the max number of players.
            bedwarsServer.updateDetails(status, currentPlayers, teamSize);
        } else {
            System.out.println("Bedwar server object not found, creating a new one, setting teamSize: " + teamSize +
                    ", status; " + status + ", numPlayers: " + currentPlayers);
            bedwarsServer = new GameServer(this, sockName, status, currentPlayers, teamSize);
            bedwarsServerList.add(bedwarsServer);
        }
        if (teamSize ==1) {
            Game bedwarsGame = getGameByName("Bedwars_solo");
            bedwarsGame.setIsBlocked(false);
            bedwarsGame.checkQueue();
        }
        if (teamSize ==2) {
            Game bedwarsGame = getGameByName("Bedwars_duos");
            bedwarsGame.setIsBlocked(false);
            bedwarsGame.checkQueue();
        }
        if (teamSize ==4) {
            Game bedwarsGame = getGameByName("Bedwars_quads");
            bedwarsGame.setIsBlocked(false);
            bedwarsGame.checkQueue();
        }
        if (teamSize == 0) {
            CheckBedwarsQueuesFreshServer(bedwarsServer);
        }
    }

    // A bedwars server is available to be set to a specific game type,
    // check the bedwars (solo, duo, quad) queues to see if anyone has been waiting for
    // a server to become available.  Set this up if required.
    private void CheckBedwarsQueuesFreshServer(GameServer bedwarsServer) {
        System.out.println("Checking bedwars queues as idle server is available");
        Game bedwarsGame = getGameByName("Bedwars_solo");
        int teamSize = 0;
        Timestamp ts = new Timestamp(System.currentTimeMillis());
        if (bedwarsGame.getQueueSize() > 0) {
            System.out.println("player found in solo queue");
            teamSize = 1;
            ts = bedwarsGame.getEarliestTimestampFromQueue();
        }

        Game bedwarsGame2 = getGameByName("Bedwars_duos");
        if (bedwarsGame2.getQueueSize() > 0) {
            Timestamp ts2 = bedwarsGame2.getEarliestTimestampFromQueue();

            if (ts2.before(ts)) {
                System.out.println("player found in duo queue waited longer");
                ts = ts2;
                teamSize = 2;
            }
        }

        Game bedwarsGame4 = getGameByName("Bedwars_quads");
        if (bedwarsGame4.getQueueSize() > 0) {
            Timestamp ts4 = bedwarsGame4.getEarliestTimestampFromQueue();
            if (ts4.before(ts)) {
                System.out.println("player found in quad queue waited longer");
                teamSize = 4;
            }
        }

        if (teamSize != 0) {
            setBedwarsServerToTeamSize(bedwarsServer.getSockName(), teamSize);
        }
    }

    public String getBedwarsServerRecruiting(int teamSize, int numPlayers) {
        HashMap<String, Integer> lstAvailable = new HashMap<>();
        System.out.println("Searching for bedwars server for team size: " + teamSize + ", party size : " + numPlayers);
        for (GameServer gameServer : bedwarsServerList) {
            System.out.println("currrent server status: " + gameServer.getGameStatus() + ", players: " +gameServer.getCurrentPlayers() +
                    ", max players: " + gameServer.getMaxPlayers());
            if ((gameServer.getGameStatus().equals("RECRUITING") || gameServer.getGameStatus().equals("COUNTDOWN"))
                    &&  ((gameServer.getMaxPlayers() - gameServer.getCurrentPlayers()) > numPlayers) &&
                    gameServer.getTeamSize() == teamSize) {
                System.out.println("adding eligible server to list of possibles");
                // Add this server to the list of possible player targets
                lstAvailable.put(gameServer.getSockName(), gameServer.getCurrentPlayers());
            }
        }
        if (lstAvailable.size() == 0) {
            // no servers are recruiting this game mode (solo,duo etc)
            return null;
        } else {
            // loop through possible servers & send player to the fullest
            Integer p = -1;
            String sockNameMostPlayers = "";
            for (Map.Entry<String, Integer> available : lstAvailable.entrySet()) {
                if (available.getValue() > p) {
                    p = available.getValue();
                    sockNameMostPlayers = available.getKey();
                }
            }
            System.out.println("server with most players: (" + p + ") is sockName: " + sockNameMostPlayers);
            return sockNameMostPlayers;
        }
    }

    public String getSocknameOfIdleBedwarsServer() {

        String sockName = "";
        // get a server whos teamSIze is et to 0
        for (GameServer gameServer : bedwarsServerList) {
            if (gameServer.getTeamSize() == 0) {
                sockName = gameServer.getSockName();
            }
        }
        if (sockName != "") {
            System.out.println("getSocknameOfIdleBedwarsServer called, bedwars server list size: " +
                    bedwarsServerList.size() + ", idle found: " + sockName);
        } else {
            System.out.println("getSocknameOfIdleBedwarsServer called, bedwars server list size: " +
                    bedwarsServerList.size() + ", idle NOT found.");
        }
        return sockName;
    }


    public void setBedwarsServerToTeamSize(String sockName, int teamSize) {

            // tell it to set up for the passed teamSize
            SockExchangeApi sockExchangeApi = main.getSockExchangeApi();
            SpigotServerInfo spigotServerInfo = sockExchangeApi.getServerInfo(sockName);
            if (spigotServerInfo == null) {
                System.out.println("server not found " + sockName);
            } else {
                if (spigotServerInfo.isOnline()) {
                    System.out.flush();

                    String inputString = "Lobby.set-team-size." + teamSize;
                    System.out.println("Requesting bedwars server set team size: " + sockName + ", size:" + teamSize);
                    SockExchangeApi api = SockExchangeApi.instance();
                    byte[] byteArray = inputString.getBytes();
                    api.sendToServer("LobbyChannel", byteArray, sockName);
                } else {
                    // server is offline
                    System.out.println("server is offline " + sockName);
                }
            }
    }

    public void updateGameServer(String sockName,  String status, int currentPlayers, int maxPlayers) {
        Game game = null;
        System.out.printf("update gane server called");
        if (sockName.startsWith("assault")) {
            System.out.println("Updating assault course server object");
            game = getGameByName("Assault_Course");
        }
        if (sockName.startsWith("fulliron")) {
            System.out.println("Updating full iron armour server object");
            game = getGameByName("Full_Iron_Armour");
        }
        if (game != null) {
            System.out.println("game found sock name: " + sockName + ", status " + status);
            game.setMaxPlayers(maxPlayers);
            game.updateGameServer(sockName, status, currentPlayers, maxPlayers);
            game.checkQueue();
        } else {
            System.out.println("UpdateGameServer game not found " + sockName);
        }
    }

    // remove player from all game queues except the one passed
    public void removePlayerFromQueues(UUID uuid, Game gameKeep) {
        for (Game game :gameList) {
          //  System.out.println("remove from queue start, size: " + game.getQueueSize());
            if (game.isPlayerInQueue(uuid)) {
                System.out.println("removing player from queue for " + game.getGameName());
            }
            if (gameKeep == null) {
                game.removePlayerFromQueue(uuid);
            } else {
                if (!game.equals(gameKeep)) {
                    game.removePlayerFromQueue(uuid);
                }
            }
           // System.out.println("remove from queue finish, size: " + game.getQueueSize());
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
           // System.out.println("passed name: " + name + ", game name: " + game.getGameName());
            if (name.contains(game.getGameName())) {
                return game;
            }
        }
        return  null;
    }

    public void gameChosenFromInventory(Player player, int slot) {
        for (Game game : gameList) {
            if (game.getInventorySlot() == slot) {
                player.closeInventory();
                if (game.isPlayerInQueue(player.getUniqueId())) {
                    player.sendMessage("You are already in the queue for this game!");
                } else {
                    game.playerJoinRequest(player);
                }
                return;
            }
        }
    }

    public List<Game> getGameList() { return this.gameList;   }

    public GameServer getBedwarsServerBySockName(String sockName) {
        if (bedwarsServerList == null) {
            return null;
        }
        for (GameServer server : bedwarsServerList) {
            if (server.getSockName().equalsIgnoreCase(sockName)) {
                return server;
            }
        }
        return  null;
    }

    public GameServer getGameServerBySockName(String sockName) {
        if (gameList == null) {
            return null;
        }
        for (Game game : gameList) {
            System.out.println("passed sockname: " + sockName + ", game name: " + game.getGameName());
            GameServer gameServer = game.getServerBySockName(sockName);
            if (gameServer != null) {
                return gameServer;
            }
        }
        return  null;
    }

}

