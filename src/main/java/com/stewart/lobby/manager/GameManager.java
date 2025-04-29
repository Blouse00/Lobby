package com.stewart.lobby.manager;

import com.gmail.tracebachi.SockExchange.Spigot.SockExchangeApi;
import com.gmail.tracebachi.SockExchange.SpigotServerInfo;
import com.stewart.lobby.Lobby;
import com.stewart.lobby.instances.AutoGameSelector;
import com.stewart.lobby.instances.Game;
import com.stewart.lobby.instances.GameServer;
import com.stewart.lobby.instances.PlayerServerInfo;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.npc.NPCRegistry;
import net.citizensnpcs.trait.SkinTrait;
import org.bukkit.*;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.EntityType;
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
    private final List<GameServer> bedwarsServerList = new ArrayList<>();
    private final HashMap<UUID, Integer> lstPlayersForAutoJoin = new HashMap<>();
    private final HashMap<String, Integer> mapGameNameSlot = new HashMap<>();
    // this will be used to store the players current server and the time they were sent there
    // to be used for reconnecting them if they disconnect
    private HashMap<UUID, PlayerServerInfo> mapPlayerServerInfo = new HashMap<>();
    private final String[]  arrAllGameSubtypes = {"assault", "smp", "fia", "icewars"};
    private final String[]  arrBWSubtypes = {"bedwars_solo", "bedwars_duo", "bedwars_quad"};
    private final String[]  arrFFSubtypes = {"fiend_fight_solo",  "fiend_fight_duo",  "fiend_fight_quad", "fiend_fight_one_team"};

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
                setUpFiendFight();
                requestGameStatusForAllGames();
            }
        }, 60L);
        Bukkit.getScheduler().scheduleSyncRepeatingTask(main, () -> CheckAutoJoinPlayers(), 60, 20);

      //  Bukkit.getScheduler().scheduleSyncRepeatingTask(main, () -> checkBedwarsServersOnline(), 600, 600);
    }

   /* private void checkBedwarsServersOnline() {
       // System.out.println("checkBedwarsServersOnline servers = " + bedwarsServerList.size());

        // A list for the sockNames of any servers that are no longer reachable
        List<String> sockNamesToRemove = new ArrayList<>();

        // Fill the list by checking each gameServer
        for (GameServer gameServer : bedwarsServerList) {
            if (!gameServer.checkIsOnline()) {
             //   System.out.println("bedwars server was offline");
                // offline
                sockNamesToRemove.add(gameServer.getSockName());
            } else {
              //  System.out.println("bedwars server was online");
            }
        }

        // loop through the list of sockNames of servers that are no longer available
        for (String sockName : sockNamesToRemove) {
            // remove them from the list of available servers
            System.out.println("Game server list (size)" + bedwarsServerList.size());
            bedwarsServerList.removeIf(obj -> obj.getSockName().equals(sockName));
            System.out.println("Removed offline server " + sockName + " from the game server list (size)" + bedwarsServerList.size());
        }

    }*/

    private void CheckAutoJoinPlayers() {
        List<UUID> toRemove = new ArrayList<>();
        // loop through the hashmap and send anyone who has been in it over 10 seconds to the server most likely to start
        for (Map.Entry<UUID, Integer> entry : lstPlayersForAutoJoin.entrySet()) {
            // the key is the players uuid
            UUID key = entry.getKey();
            // increment seconds by 1
            entry.setValue(entry.getValue() + 1);
            // the value is how long they have been in the list
            Integer value = entry.getValue();
            if (value > 25) {
                // send them to the server most likely to start and remove them from this list
                System.out.println("sending player to best server from timer");
                sendPlayerToBestServer(key);
                toRemove.add(key);
            }
        }
        for (UUID uuid :toRemove) {
            lstPlayersForAutoJoin.remove(uuid);
        }
    }

    public boolean sendPlayerToBestServer(UUID uuid) {
        System.out.println("sendPlayerToBestServer");
        if (Bukkit.getPlayer(uuid) != null) {
            AutoGameSelector autoGameSelector = new AutoGameSelector(main);
            String gameMostLikelyToStart = autoGameSelector.getGameMostLikelyToStart();
            System.out.println("best game is: " + gameMostLikelyToStart);
            Player player = Bukkit.getPlayer(uuid);

          /*  for (String string :mapGameNameSlot.keySet()) {
                System.out.println("game name slot " + string + " " + mapGameNameSlot.get(string));
            }*/

            // get the slot that applies to this game
            Integer slot = mapGameNameSlot.get(gameMostLikelyToStart);
            System.out.println("slot is " + slot);
            // use existing code to send player to that game
            if (slot != null) {
                gameChosenFromInventory(player, slot);
                return true;
            } else {
                Bukkit.getPlayer(uuid).sendMessage(ChatColor.RED + "All games are busy, please try again soon!");
                return false;
            }

        }
        return false;
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
        // first loop through each game type
        for (String s : arrAllGameSubtypes) {
            System.out.println("game name " + s);
            // eg 'bedwars_solo'
            String gameName = gameConfig.getString( s + ".name");
          /*  String texture = gameConfig.getString( s + ".npc-skin-texture");
            String signature = gameConfig.getString( s + ".npc-skin-signature");
            String nameColour = gameConfig.getString(s + ".npc-name-colour");*/
            Material material = Material.DIAMOND_BLOCK;
            int inventorySocket = -1;
            if (gameConfig.contains(s + ".npc-name-colour")) {
                material = Material.getMaterial(gameConfig.getString( s + ".material"));
                inventorySocket = gameConfig.getInt( s + ".inventory-slot");
            }

            addGameToNameSlotMap(s, inventorySocket);

            Game game   = new Game(main, gameName,  material, inventorySocket );

         //   List<Location> lstLocation = new ArrayList<>();
            spawnGameNPCs(s, gameName);


          /*  for (String s1 : gameConfig.getConfigurationSection(s + ".npc-locations").getKeys(false)) {
                Location location = new Location(Bukkit.getWorld("world"),
                        gameConfig.getDouble(s + ".npc-locations." + s1 + ".npc-x"),
                        gameConfig.getDouble(s + ".npc-locations." + s1 + ".npc-y"),
                        gameConfig.getDouble(s + ".npc-locations." + s1 + ".npc-z"),
                        (float) gameConfig.getDouble(s + ".npc-locations." + s1 + ".npc-yaw"),
                        (float) gameConfig.getDouble(s + ".npc-locations." + s1 + ".npc-pitch"));

                lstLocation.add(location);
            }*/



          //  if (s.startsWith("monster") == false) {
            //    game.spawnNPC();
           // }

            gameList.add(game);



        }
    }

   /* public void setUpBedwars() {
        // game name in config were Bedwars_solo Bedwars_duos Bedwars_quads
        String[]  arrSubtypes = {"Bedwars_solo",  "Bedwars_duos",  "Bedwars_quads"};
        String texture = gameConfig.getString("bedwars.npc-skin-texture");
        String signature = gameConfig.getString("bedwars.npc-skin-signature");
        String nameColour = gameConfig.getString("bedwars.npc-name-colour");
        Material material = Material.getMaterial(gameConfig.getString("bedwars.material"));
        *//*int  inventorySocket = gameConfig.getInt("bedwars.inventory-slot");

        addGameToNameSlotMap("Bedwars_solo", inventorySocket);
        addGameToNameSlotMap("Bedwars_duos", inventorySocket);
        addGameToNameSlotMap("Bedwars_quads", inventorySocket);*//*

        int inventorySocket = 0;
        // first loop through each game type
        for (String gameName : arrSubtypes) {
            // eg 'bedwars_solo'
            if (gameName.equals("Bedwars_solo")) {
                inventorySocket = 25;
                addGameToNameSlotMap("Bedwars_solo", inventorySocket);
            }

            if (gameName.equals("Bedwars_duos")) {
                inventorySocket = 34;
                addGameToNameSlotMap("Bedwars_duos", inventorySocket);
            }

            if (gameName.equals("Bedwars_quads")) {
                inventorySocket = 43;
                addGameToNameSlotMap("Bedwars_quads", inventorySocket);
            }

            // create the games but without any nps
            List<Location> lstLocation = new ArrayList<>();

            Game game = new Game(main, gameName, lstLocation, texture, signature, nameColour, material, inventorySocket);
          //  game.spawnNPC();
            gameList.add(game);
        }

        // get the locations and spawn the NPCs for bedwars
        for (String s : gameConfig.getConfigurationSection("bedwars.npc-locations").getKeys(false)) {
            Location location = new Location(Bukkit.getWorld("world"),
                    gameConfig.getDouble("bedwars.npc-locations." + s + ".npc-x"),
                    gameConfig.getDouble("bedwars.npc-locations." + s + ".npc-y"),
                    gameConfig.getDouble("bedwars.npc-locations." + s + ".npc-z"),
                    (float) gameConfig.getDouble("bedwars.npc-locations." + s + ".npc-yaw"),
                    (float) gameConfig.getDouble("bedwars.npc-locations." + s + ".npc-pitch"));

            spawnNPC(location, texture, signature, nameColour, "Bedwars");
        }
    }*/

    public void setUpBedwars() {
        // game name in config were Bedwars_solo Bedwars_duos Bedwars_quads
        //   String[]  arrSubtypes = {"fiend_fight_solo",  "fiend_fight_duo",  "fiend_fight_quad", "fiend_fight_one_team"};
     /*   String texture = gameConfig.getString("bedwars.npc-skin-texture");
        String signature = gameConfig.getString("bedwars.npc-skin-signature");
        String nameColour = gameConfig.getString("bedwars.npc-name-colour");*/
        Material material = Material.getMaterial(gameConfig.getString("bedwars.material"));
        int  inventorySocket = gameConfig.getInt("bedwars.inventory-slot");

        // first loop through each game type
        for (String gameName : arrBWSubtypes) {
            if (gameName.equalsIgnoreCase("Bedwars_solo")) {
                inventorySocket = 25;
                addGameToNameSlotMap("Bedwars_solo", inventorySocket);
            }
            if (gameName.equalsIgnoreCase("Bedwars_duo")) {
                inventorySocket = 34;
                addGameToNameSlotMap("Bedwars_duo", inventorySocket);
            }
            if (gameName.equalsIgnoreCase("Bedwars_quad")) {
                inventorySocket = 43;
                addGameToNameSlotMap("Bedwars_quad", inventorySocket);
            }
            Game game = new Game(main, gameName, material, inventorySocket);
          //  game.spawnNPC();
            gameList.add(game);
        }

        spawnGameNPCs("bedwars", "Bedwars");



      /*  // get the locations and spawn the NPCs for fiend fight
        for (String s : gameConfig.getConfigurationSection( "bedwars.npc-locations").getKeys(false)) {
            Location location = new Location(Bukkit.getWorld("world"),
                    gameConfig.getDouble("bedwars.npc-locations." + s + ".npc-x"),
                    gameConfig.getDouble("bedwars.npc-locations." + s + ".npc-y"),
                    gameConfig.getDouble("bedwars.npc-locations." + s + ".npc-z"),
                    (float) gameConfig.getDouble("bedwars.npc-locations." + s + ".npc-yaw"),
                    (float) gameConfig.getDouble("bedwars.npc-locations." + s + ".npc-pitch"));

            spawnNPC(location, texture, signature, nameColour, "FiendFight");
        }*/
    }

    private void spawnGameNPCs(String configName, String npcName) {
        String texture = gameConfig.getString(configName + ".npc-skin-texture");
        String signature = gameConfig.getString(configName + ".npc-skin-signature");
        String nameColour = gameConfig.getString(configName + ".npc-name-colour");
        // get the locations and spawn the NPCs for fiend fight
        for (String s : gameConfig.getConfigurationSection( configName + ".npc-locations").getKeys(false)) {
            Location location = new Location(Bukkit.getWorld("world"),
                    gameConfig.getDouble(configName + ".npc-locations." + s + ".npc-x"),
                    gameConfig.getDouble(configName + ".npc-locations." + s + ".npc-y"),
                    gameConfig.getDouble(configName + ".npc-locations." + s + ".npc-z"),
                    (float) gameConfig.getDouble(configName + ".npc-locations." + s + ".npc-yaw"),
                    (float) gameConfig.getDouble(configName + ".npc-locations." + s + ".npc-pitch"));

            spawnNPC(location, texture, signature, nameColour, npcName);
        }
    }


    public void setUpFiendFight() {
        // game name in config were Bedwars_solo Bedwars_duos Bedwars_quads
     //   String[]  arrSubtypes = {"fiend_fight_solo",  "fiend_fight_duo",  "fiend_fight_quad", "fiend_fight_one_team"};
      /*  String texture = gameConfig.getString("monster.npc-skin-texture");
        String signature = gameConfig.getString("monster.npc-skin-signature");
        String nameColour = gameConfig.getString("monster.npc-name-colour");*/
        Material material = Material.getMaterial(gameConfig.getString("monster.material"));
        int  inventorySocket = gameConfig.getInt("monster.inventory-slot");

        // first loop through each game type
        for (String gameName : arrFFSubtypes) {

            if (gameName.equals("fiend_fight_one_team")) {
                inventorySocket = 10;
                material = Material.GOLD_HELMET;
                addGameToNameSlotMap("fiend_fight_one_team", inventorySocket);
            }
            if (gameName.equals("fiend_fight_solo")) {
                inventorySocket = 19;
                material = Material.LEATHER_HELMET;
                addGameToNameSlotMap("fiend_fight_solo", inventorySocket);
            }
            if (gameName.equals("fiend_fight_duo")) {
                inventorySocket = 28;
                material = Material.CHAINMAIL_HELMET;
                addGameToNameSlotMap("fiend_fight_duo", inventorySocket);
            }
            if (gameName.equals("fiend_fight_quad")) {
                inventorySocket = 37;
                material = Material.IRON_HELMET;
                addGameToNameSlotMap("fiend_fight_quad", inventorySocket);
            }
            Game game = new Game(main, gameName, material, inventorySocket);
            gameList.add(game);
        }

        spawnGameNPCs("monster", "FiendFight");

      /*  // get the locations and spawn the NPCs for fiend fight
        for (String s : gameConfig.getConfigurationSection( "monster.npc-locations").getKeys(false)) {
            Location location = new Location(Bukkit.getWorld("world"),
                    gameConfig.getDouble("monster.npc-locations." + s + ".npc-x"),
                    gameConfig.getDouble("monster.npc-locations." + s + ".npc-y"),
                    gameConfig.getDouble("monster.npc-locations." + s + ".npc-z"),
                    (float) gameConfig.getDouble("monster.npc-locations." + s + ".npc-yaw"),
                    (float) gameConfig.getDouble("monster.npc-locations." + s + ".npc-pitch"));

            spawnNPC(location, texture, signature, nameColour, "FiendFight");
        }*/
    }

    public void spawnNPC(Location location, String texture, String signature, String nameColour, String name) {
        String npcName = nameColour + name;
        net.citizensnpcs.api.npc.NPC npc =CitizensAPI.getNPCRegistry().createNPC(EntityType.PLAYER, npcName);
        SkinTrait skin = npc.getOrAddTrait(SkinTrait.class);
        skin.setSkinPersistent("pjs", signature, texture);
        npc.spawn(location);
    }

    private void requestGameStatusForAllGames() {
        long i = 10;
        SockExchangeApi sockExchangeApi = main.getSockExchangeApi();

        for (String s : arrAllGameSubtypes) {
            for (String s1 : gameConfig.getConfigurationSection(s + ".servers").getKeys(false)) {
                String sockName = gameConfig.getString(s + ".servers." + s1 + ".sockName");
                String inputString = "Lobby.request-status." + sockName + "." + s;
                Bukkit.getScheduler().scheduleSyncDelayedTask(main, () -> requestGameStatus(sockExchangeApi, sockName, inputString), i);
                i += 10;
            }
        }

        for (String s : arrBWSubtypes) {
            for (String s1 : gameConfig.getConfigurationSection("bedwars.servers").getKeys(false)) {
                String sockName = gameConfig.getString("bedwars.servers." + s1 + ".sockName");
                String inputString = "Lobby.request-status." + sockName + "." + s;
                Bukkit.getScheduler().scheduleSyncDelayedTask(main, () -> requestGameStatus(sockExchangeApi, sockName, inputString), i);
                i += 10;
            }
        }

        for (String s : arrFFSubtypes) {
            for (String s1 : gameConfig.getConfigurationSection("monster.servers").getKeys(false)) {
                String sockName = gameConfig.getString("monster.servers." + s1 + ".sockName");
                String inputString = "Lobby.request-status." + sockName + "." + s;
                Bukkit.getScheduler().scheduleSyncDelayedTask(main, () -> requestGameStatus(sockExchangeApi, sockName, inputString), i);
                i += 10;
            }
        }
    }

    private void requestGameStatus(SockExchangeApi sockExchangeApi, String sockName, String inputString) {
        System.out.println("game server " + sockName);
        SpigotServerInfo spigotServerInfo = sockExchangeApi.getServerInfo(sockName);
        if (spigotServerInfo == null) {
            System.out.println("server not found " + sockName);
        } else {
            if (spigotServerInfo.isOnline()) {
                System.out.println("server is online requesting status");
                // server is online
                System.out.flush();
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
/*
    public void updateBedwarsGameServer(String sockName, String status, int currentPlayers, int maxPlayers, int teamSize) {
        // find the server to be updated
        System.out.println("Updating/creating bedwars server object status after sock message from its server.");
        GameServer bedwarsServer = getBedwarsServerBySockName(sockName);
        if (bedwarsServer != null) {
            System.out.println("Bedwars server object already exists, setting teamSize: " + teamSize + ", status; " +
                    status + ", numPlayers: " + currentPlayers);
            // team size determines the servers game type (solos, duos or quads)
            // it also determines the max number of players.
            bedwarsServer.updateDetails(status, currentPlayers, teamSize, maxPlayers);
        } else {
            System.out.println("Bedwar server object not found, creating a new one, setting teamSize: " + teamSize +
                    ", status; " + status + ", numPlayers: " + currentPlayers + " sock " +sockName);
            bedwarsServer = new GameServer(this, sockName, status, currentPlayers, teamSize, maxPlayers);
            bedwarsServerList.add(bedwarsServer);
            System.out.println("num bedwars servres = " + bedwarsServerList.size());
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
    }*/

     public void updateGameServer(String sockName,  String status, int currentPlayers, int maxPlayers, String gameType) {
        Game game = null;
        System.out.printf("update gane server called " + sockName + " " + gameType);
        if (sockName.startsWith("assault")) {
            System.out.println("Updating assault course server object");
            game = getGameByName("Assault_Course");
        }
        if (sockName.startsWith("fulliron")) {
            System.out.println("Updating full iron armour server object");
            game = getGameByName("Full_Iron_Armour");
        }
        if (sockName.startsWith("icewars")) {
            System.out.println("Updating icewars server object");
            game = getGameByName("BETA_Icewars");
        }
        if (sockName.startsWith("smp")) {
            System.out.println("Updating smp server object");
            game = getGameByName("1.8 SMP");
        }
        if (sockName.startsWith("creative")) {
            System.out.println("Updating creative server object");
            game = getGameByName("creative");
        }
        if (sockName.startsWith("monster")) {
            System.out.println("Updating fiend fight " + gameType + " server object");
            game = getGameByName("fiend_fight_" + gameType);
        }
         if (sockName.startsWith("bedwars")) {
             System.out.println("Updating bedwars " + gameType + " server object");
             game = getGameByName("bedwars_" + gameType);
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
     //   System.out.println("getGameByName called with name: " + name);
        if (gameList == null) {
            return null;
        }

        for (Game game : gameList) {
          //  System.out.println("------------ get game by name looping through existing games - " + game.getGameName());
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
          //  System.out.println("passed name: " + name + ", game name: " + game.getGameName());
            if (name.contains(game.getGameName())) {
                return game;
            }
        }
        return  null;
    }

    public void gameChosenFromInventory(Player player, int slot) {
        for (Game game : gameList) {
          //  System.out.println("game name is " + game.getGameName());
            if (game.getInventorySlot() == slot) {
                player.closeInventory();
              //  System.out.println("ame chosen is " + game.getGameName());
                if (game.isPlayerInQueue(player.getUniqueId())) {
                    player.sendMessage("You are already in the queue for this game!");
                } else {
                    game.playerJoinRequest(player, false);
                }
                return;
            }
        }
        player.sendMessage("Game not found!");
    }

    public void gameChosenFromInventoryByName(Player player, String gameName) {
        for (Game game : gameList) {
           // System.out.println("game name is " + game.getGameName());
            if (game.getGameName().equalsIgnoreCase(gameName)) {
                player.closeInventory();
             //   System.out.println("ame chosen is " + game.getGameName());
                if (game.isPlayerInQueue(player.getUniqueId())) {
                    player.sendMessage("You are already in the queue for this game!");
                } else {
                    game.playerJoinRequest(player, false);
                }
                return;
            }
        }
        player.sendMessage("Game not found!");
    }

    public List<Game> getGameList() { return this.gameList;   }

    public List<GameServer> getBedwarsGameList() { return this.bedwarsServerList;   }

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

    // the code for the auto game selector returns the game name, I need to map that to the slot so I can use existing
    // code to send player to the game
    private void addGameToNameSlotMap(String name, int slot) {
        System.out.println("Adding game to mapGameNameSlot");
        switch (name){
            case("assault"):
                mapGameNameSlot.put("Assault_Course", slot);
                break;
            case("smp"):
                mapGameNameSlot.put("SMP", slot);
                break;
            case("fia"):
                mapGameNameSlot.put("Full_Iron_Armour", slot);
                break;
            case("creative"):
                mapGameNameSlot.put("Creative", slot);
                break;
            case("icewars"):
                mapGameNameSlot.put("Icewars", slot);
                mapGameNameSlot.put("BETA_Icewars", slot);
                break;
            case("Bedwars_solo"):
                mapGameNameSlot.put("Bedwars_solo", slot);
                break;
            case("Bedwars_duo"):
                mapGameNameSlot.put("Bedwars_duo", slot);
                break;
            case("Bedwars_quad"):
                mapGameNameSlot.put("Bedwars_quad", slot);
                break;
            case("fiend_fight_solo"):
                mapGameNameSlot.put("fiend_fight_solo", slot);
                break;
            case("fiend_fight_duo"):
                mapGameNameSlot.put("fiend_fight_duo", slot);
                break;
            case("fiend_fight_quad"):
                mapGameNameSlot.put("fiend_fight_quad", slot);
                break;
            case("fiend_fight_one_team"):
                mapGameNameSlot.put("fiend_fight_one_team", slot);
                break;
        }
    }

    public void AddPlayerToAutoJoin(UUID uuid) {
        lstPlayersForAutoJoin.put(uuid, 0);
    }

    public void RemovePlayerFromAutoJoin(UUID uuid) {
        lstPlayersForAutoJoin.remove(uuid);
    }

    public Lobby getMain() {return main;}

    public void addPlayerServerInfo(UUID uuid, String sockName) {
        System.out.println("-- Adding player to mapPlayerServerInfo sfter sending them to a server");
        PlayerServerInfo playerServerInfo = new PlayerServerInfo(sockName, uuid, new Timestamp(System.currentTimeMillis()));
        mapPlayerServerInfo.put(uuid, playerServerInfo);
    }

    public void removePlayerServerInfoOverMinutes(int minutes) {
        List<PlayerServerInfo> toRemove = new ArrayList<>();
        for (Map.Entry<UUID, PlayerServerInfo> entry : mapPlayerServerInfo.entrySet()) {
            PlayerServerInfo playerServerInfo = entry.getValue();
            long diff = System.currentTimeMillis() - playerServerInfo.getTimeSentToServer().getTime();
            long diffMinutes = diff / (60 * 1000) % 60;
            if (diffMinutes > minutes) {
                toRemove.add(playerServerInfo);
            }
        }
        for (PlayerServerInfo playerServerInfo : toRemove) {
            System.out.println("Removing player from mapPlayerServerInfo, diffMinutes: " + minutes);
            mapPlayerServerInfo.remove(playerServerInfo.getUuid());
        }
    }

    public PlayerServerInfo getPlayerServerInfo(UUID uuid) {
        return mapPlayerServerInfo.get(uuid);
    }

    public void removePlayerServerInfo(UUID uuid) {
        mapPlayerServerInfo.remove(uuid);
    }
}

