package com.stewart.lobby.instances;

import com.gmail.tracebachi.SockExchange.Spigot.SockExchangeApi;
import com.gmail.tracebachi.SockExchange.SpigotServerInfo;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import com.stewart.lobby.Lobby;
import com.stewart.lobby.manager.ConfigManager;
import com.stewart.lobby.manager.GameManager;
import com.stewart.lobby.utils.LobbyUtils;
import de.simonsator.partyandfriends.spigot.api.pafplayers.PAFPlayer;
import de.simonsator.partyandfriends.spigot.api.pafplayers.PAFPlayerManager;
import de.simonsator.partyandfriends.spigot.api.party.PartyManager;
import de.simonsator.partyandfriends.spigot.api.party.PlayerParty;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.trait.SkinTrait;
import org.bukkit.*;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.stewart.bb_api.instance.CustomPlayer;

import java.net.InetSocketAddress;
import java.net.Socket;
import java.sql.Timestamp;
import java.util.*;

// the gameManager class will have a list of instances of this game class
// one for each game server listed in the config file
public class Game {

    // the main class, unused at the moment so coupld possibly be removed
    private final Lobby main;

    private final String name;
    private int maxPlayers;
  //  private final List<Location> npcSpawnLocation;
    private final List<GameServer> serverList = new ArrayList<>();
    private final HashMap<UUID, Timestamp> playersInQueue = new HashMap<>();
  //  private HashMap<UUID, String> partyPlayerQueue = new HashMap<>();
   /* private final String texture;
    private final String signature;
    private final String nameColour;*/
    private final Material material;
    private final int inventorySlot;

 //   private org.bukkit.inventory.ItemStack gameItem;

    private boolean isBlocked;
  //  private EntityPlayer npc;

    public Game(Lobby lobby,  String name,  Material material, int inventorySlot) {
/*    public Game(Lobby lobby,  String name, List<Location> npcSpawnLocation, String texture, String signature,
                String nameColour, Material material, int inventorySlot) {*/
        this.isBlocked = false;
      //  this.gameItem = gameItem;
        this.name = name;
        //this.npcSpawnLocation = npcSpawnLocation;
        this.main = lobby;
       /* this.texture = texture;
        this.signature = signature;
        this.nameColour = nameColour;*/
        this.material = material;
        this.inventorySlot = inventorySlot;

        // after 1 minute check all game servers are online then every 20 seconds after that
        // TODO change times from 10 seconds for testing to 1m (1200) & 30sec (600)

        Bukkit.getScheduler().scheduleSyncRepeatingTask(main, () -> checkGameServersOnline(), 600, 600);
    }

/*    public void spawnNPC() {

        for (Location location : npcSpawnLocation) {
            String npcName = this.nameColour + this.name;
            net.citizensnpcs.api.npc.NPC npc =CitizensAPI.getNPCRegistry().createNPC(EntityType.PLAYER, npcName);
            SkinTrait skin = npc.getOrAddTrait(SkinTrait.class);
            skin.setSkinPersistent("pjs", signature, texture);
            npc.spawn(location);
        }


       System.out.println("spawning npc for " + name);
    }*/

    private void checkGameServersOnline() {
      //  System.out.println("checkGameServersOnline servers = " +serverList.size());

        // A list for the sockNames of any servers that are no longer reachable
        List<String> sockNamesToRemove = new ArrayList<>();

        // Fill the list by checking each gameServer
        for (GameServer gameServer : serverList) {
           if (!gameServer.checkIsOnline()) {
        //       System.out.println("server was offline");
               // offline
               sockNamesToRemove.add(gameServer.getSockName());
           } else {
          //     System.out.println("server was online");
           }
        }

        // loop through the list of sockNames of servers that are no longer available
        for (String sockName : sockNamesToRemove) {
            // remove them from the list of available servers
            System.out.println("Game server list (size)" + serverList.size());
            serverList.removeIf(obj -> obj.getSockName().equals(sockName));
            System.out.println("Removed offline server " + sockName + " from the game server list (size)" + serverList.size());
        }

    }


    // fired when a player right-clicks on this games npc.
    // also when clicking on inventory item
    // also form checking queue
    public void playerJoinRequest(Player player, Boolean fromQueue) {

        System.out.println("player join request " + this.getGameName() + " player " + player.getName() +
                " fromQueue " + fromQueue);

        boolean allowJoin = true;

        if(!fromQueue) {
            player.sendMessage("Attempting to join " + this.name + "!");
            PAFPlayer pafPlayer = PAFPlayerManager.getInstance().getPlayer(player.getUniqueId());
            PlayerParty party = PartyManager.getInstance().getParty(pafPlayer);

            if (party != null) {
                if (!party.isLeader(pafPlayer)) {
                    // if in a party only the leader can join a game
                   allowJoin = false;
                   player.sendMessage(ChatColor.RED + "Only the party leader may join a game!");
                    player.sendMessage(ChatColor.RED + "Use the command " + ChatColor.BLUE + " party leave "
                            + ChatColor.RED + " leave your party.");
                } else {
                    for (PAFPlayer p : party.getPlayers()) {
                        if (Bukkit.getPlayer(p.getUniqueId()) != null) {
                            Bukkit.getPlayer(p.getUniqueId()).sendMessage("Attempting to join " + this.name + "!");
                        }
                    }
                }
            }
        }

        if (allowJoin) {
        if (this.isBlocked) {
            // in the process of sending a player, wait till that's complete
          //  System.out.println("game is blocked, adding player to queue");
            addPlayerToQueue(player, false);
        } else {
            // player requested to join game

            /*if (this.name.contains("Bedwars_")) {
                System.out.println("BedWars server join detected");
                JoinBedwarsServer(player);
                return;
            }
*/
            if (this.name.contains("SMP") && !getCanAccessSMP(player)) {
                System.out.println("Player not eligible for smp");
                return;
            }
            // player.sendMessage("Attempting to join " + this.getGameName() + "!");
            // loop through the servers whose status is recruiting.
            // make a list of possible server sockNames
            HashMap<String, Integer> lstAvailable = new HashMap<>();
            for (GameServer gameServer : serverList) {
              //  System.out.println("status: " + gameServer.getGameStatus() + " . p: " + gameServer.getCurrentPlayers() +
              //          " max " + maxPlayers);
                if ((gameServer.getGameStatus().equals("RECRUITING") || gameServer.getGameStatus().equals("COUNTDOWN"))
                        && gameServer.getCurrentPlayers() < maxPlayers) {
               //     System.out.println("adding eligible server to list of possibles");
                    // Add this server to the list of possible player targets
                    lstAvailable.put(gameServer.getSockName(), gameServer.getCurrentPlayers());
                }
            }
         //   System.out.println(lstAvailable.size() + " possible server(s) found");
            if (lstAvailable.isEmpty()) {
                // no servers are recruiting, put the player in the queue.
                addPlayerToQueue(player, true);
             //   System.out.println("Servers all busy, adding player to queue");
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
                System.out.println("server with most players (" + p + ") is " + sockNameMostPlayers);
                // sockNameMostPlayers will be the sockName of the server with the most players
                // send the player there
                main.getGameManager().removePlayerFromQueues(player.getUniqueId(), null);
                if (player != null) {
                    if (this.name.toLowerCase().contains("fiend") || this.name.toLowerCase().contains("bedwars")) {
                     //   System.out.println("Checking if player is in a party");
                        PAFPlayer pafPlayer = PAFPlayerManager.getInstance().getPlayer(player.getUniqueId());
                        PlayerParty party = PartyManager.getInstance().getParty(pafPlayer);
                        String commaSeparatedPlayerNames = player.getName();
                        if (party != null) {
                         //   System.out.println("Player is in a party");
                            // let the monster server know what game type all the party players will be joining
                            StringBuilder str = new StringBuilder();
                            for (PAFPlayer partyPlayer : party.getAllPlayers()) {
                                str.append(partyPlayer.getName());
                                str.append(",");
                            }
                            // remoe last comma
                            commaSeparatedPlayerNames = str.toString();
                            commaSeparatedPlayerNames = commaSeparatedPlayerNames.substring(0, commaSeparatedPlayerNames.length() - 1);
                        }
                        if (warnServerApproachingPlayers(commaSeparatedPlayerNames, sockNameMostPlayers)) {
                            // after a second send the player to the server
                            String finalSockNameMostPlayers = sockNameMostPlayers;
                            Bukkit.getScheduler().scheduleSyncDelayedTask(main, () -> SendPlayerToServer(player, finalSockNameMostPlayers), 20L);
                        }
                    } else {
                        // not monster game - just send them to the server
                        SendPlayerToServer(player, sockNameMostPlayers);
                    }

                }
            }
            }
        }
    }

    private boolean warnServerApproachingPlayers(String commaSeparatedPlayerNames,
                                              String sockName) {
        SockExchangeApi sockExchangeApi = main.getSockExchangeApi();
        SpigotServerInfo spigotServerInfo = sockExchangeApi.getServerInfo(sockName);
        if (spigotServerInfo == null) {
            System.out.println("server not found " + sockName);
            return false;
        } else {
            if (spigotServerInfo.isOnline()) {
                System.out.flush();
                String inputString = "Lobby.players-joining." + name + "." + commaSeparatedPlayerNames;
            //    System.out.println("Game.warnServerApproachingPlayers: " + inputString);
                SockExchangeApi api = SockExchangeApi.instance();
                byte[] byteArray = inputString.getBytes();
                api.sendToServer("LobbyChannel", byteArray, sockName);
                return true;
            } else {
                // server is offline
            //    System.out.println("server is offline " + sockName);
                return false;
            }
        }
    }
    private boolean getCanAccessSMP(Player player) {
        int smpAccess = main.getBb_api().getGenericQueries().getIntValue("bb_players", "player_uuid",
                player.getUniqueId().toString(), "smp_access");
        System.out.println("smp access == " + smpAccess);
        // 0 = karma must be >= 6, 1 = allow, >1 = deny
        if (smpAccess == 1) {
            return true;
        }
        if (smpAccess == 0) {
            // karma must be over 6 or a paid member/mod
            if (player.hasPermission("group.mod") || player.hasPermission("group.supporter")) {
                System.out.println("smp join is mod or supporter");
                return true;
            }
            CustomPlayer customPlayer = main.getBb_api().getPlayerManager().getCustomPlayer(player.getUniqueId());
            int playerKarma = customPlayer.getPlayerKarma().getCurrentKarma();
            if (playerKarma >= 6) {
                return true;
            } else {
                // anything else (should be 2) - deny
                player.sendMessage(getDecorativeRow());
                player.sendMessage(ChatColor.RED + "You need at least 6 karma to join our SMP server");
                player.sendMessage("Play our mini-games to earn Karma, 1 game played = 1 karma - or get other players to give you karma");
                player.sendMessage("Use the command " + ChatColor.YELLOW + "/karma help" + ChatColor.WHITE + " for more information.");
                player.sendMessage(getDecorativeRow());
                String versionMessage = "Player " + player.getName() + " tried to join SMP but only has " + playerKarma + " karma";
                if (main.getJda() != null) {
                    main.getJda().getGuildById(ConfigManager.getDiscordServer())
                            .getTextChannelById(ConfigManager.getDiscordChannel())
                            .sendMessage(versionMessage).queue();
                }
                return false;
            }
        }
        // anything else (should be 2) - deny
        player.sendMessage(getDecorativeRow());
        player.sendMessage(ChatColor.RED + "You have been BLOCKED from joining the SMP server");
        player.sendMessage("Contact us at " + ChatColor.BLUE + "https://bashybashy.com" + ChatColor.WHITE +
                " or on our discord " + ChatColor.BLUE + "https://discord.gg/Ypx4kTRbHp" + ChatColor.WHITE +
                " to have the block removed");
        player.sendMessage(getDecorativeRow());
        // alert discord
        String versionMessage = "Player " + player.getName() + " tried to join SMP but is BLOCKED";
        if (main.getJda() != null) {
            main.getJda().getGuildById(ConfigManager.getDiscordServer())
                    .getTextChannelById(ConfigManager.getDiscordChannel())
                    .sendMessage(versionMessage).queue();
        }
        return false;
    }

    private String getDecorativeRow() {
        StringBuilder sb  = new StringBuilder();
        for(int i = 0; i < 31; ++i) {
            //  sb.append(ChatColor.BOLD + "" + ChatColor.RED + "<").append(ChatColor.BLUE + "<").append(ChatColor.YELLOW + "<");
            //  sb.append(ChatColor.YELLOW + ">").append(ChatColor.BLUE + ">").append(ChatColor.RED + ">");
            sb.append(ChatColor.YELLOW + "*").append(ChatColor.BLACK + "*");
        }
        return sb.toString();
    }

   /* private void JoinBedwarsServer(Player player) {
        PAFPlayer pafPlayer = PAFPlayerManager.getInstance().getPlayer(player.getUniqueId());
        PlayerParty party = PartyManager.getInstance().getParty(pafPlayer);
        // if its a party I need to know how many players there are, default this to 1 for non-party player
        int numPlayersToJoin = 1;
        if (party != null) {
            System.out.println("player is in a party " +  player.getDisplayName());
            for (PAFPlayer p : party.getPlayers()) {
                System.out.println("Player in party is " + p.getName());
            }
            System.out.println("Party leader is " + party.getLeader().getName());


            // need to get the number of players in the party
            // + 1 as its normal players + leader. Calling size() on getAllPlayers has weird effects (adds
            // duplicate players to the party.
            numPlayersToJoin = party.getPlayers().size() + 1;

        }

        int teamSize = 1;
       // String msg = "";
        // get the type of bedwars game the player wishes to join.
        if (this.getGameName().equals("Bedwars_solo")) {
           // msg = "Attempting to join Bedwars solos!";
        }
        if (this.getGameName().equals("Bedwars_duos")) {
           // msg = "Attempting to join Bedwars duos!";
            teamSize = 2;
        }
        if (this.getGameName().equals("Bedwars_quads")) {
          //  msg = "Attempting to join Bedwars quads!";
            teamSize = 4;
        }


       // player.sendMessage(msg);
        GameManager manager = main.getGameManager();
        String serverSockName = manager.getBedwarsServerRecruiting(teamSize, numPlayersToJoin);
        if (serverSockName == null) {
            System.out.println("No servers set up for " + this.getGameName());
            // see if there are idle servers (teamSize = 0) we can set for our use.
            serverSockName = manager.getSocknameOfIdleBedwarsServer();
            if (serverSockName != "") {
                manager.setBedwarsServerToTeamSize(serverSockName, teamSize);
                // do not give the you have been added to the queue message as they should get there pretty quick

                addPlayerToQueue(player, false);
            } else {
                addPlayerToQueue(player, true);
            }

        } else {
            // send the player to the server
            SendPlayerToServer(player, serverSockName);
        }
    }*/

    private void SendPlayerToServer(Player player, String sockName) {
        try {
            System.out.println("Sending player to server " + sockName + " player " + player.getName());
            ByteArrayDataOutput out = ByteStreams.newDataOutput();
            out.writeUTF("Connect");
            // get server name from the sign
            out.writeUTF(sockName);
            // teleport the player to the game server, this is done via the bungeecord channel
            if (main.getJda() != null) {
                main.getJda().getGuildById(ConfigManager.getDiscordServer())
                        .getTextChannelById(ConfigManager.getDiscordChannel())
                        .sendMessage("Sending " + player.getName() + " to " + sockName).queue();
            }
            player.sendPluginMessage(main, "BungeeCord", out.toByteArray());
            if (sockName.toLowerCase().contains("monster") ||sockName.toLowerCase().contains("assault")
                    || sockName.toLowerCase().contains("bedwars")) {
                main.getGameManager().addPlayerServerInfo(player.getUniqueId(), sockName);
            }
            LobbyUtils.sendGameJoinMessage(player.getName(), this.name);

            main.sendMessageToSMPPlayers(player.getName(), this.name);
        } catch (Exception ex) {
            player.sendMessage(ChatColor.RED + "There was a problem connecting you to that game.  Please try again later!");
        }




    }

    public void addPlayerToQueue(Player player, boolean sendMessage) {
        if (!playersInQueue.containsKey(player.getUniqueId())) {
            // remove player from all other queues first;
            main.getGameManager().removePlayerFromQueues(player.getUniqueId(), null);
            playersInQueue.put(player.getUniqueId(), new Timestamp(System.currentTimeMillis()));
            if (sendMessage) {
                player.sendMessage("Servers are busy, you have been added to the queue.");
                System.out.println("------------------------------------------------SHOWED MESSAGE-----------------------");
            } else {
                System.out.println("------------------------------------------------NO MESSAGE-----------------------");
            }
        } else {
            System.out.println("Player already in queue");
        }
        System.out.println("Queue size: " + playersInQueue.size());
    }

    public void checkQueue() {

        System.out.println("Checking game queue for " + name);

        System.out.println("players in the queue = " + playersInQueue.size());
        if (!playersInQueue.isEmpty()) {
            // pick the first person in the queue and check that they exist on the server,
            Timestamp ts = new Timestamp(System.currentTimeMillis());
            UUID uuidFirst = null;
            for (Map.Entry<UUID, Timestamp> p : playersInQueue.entrySet()) {
                if (p.getValue().before(ts)) {
                    ts = p.getValue();
                    uuidFirst = p.getKey();
                }
            }

            if (Bukkit.getServer().getPlayer(uuidFirst) == null) {
                // if not remove them from the queue & recall this function (checkQueue())
                System.out.println("Player at front of the queue not on the server");
                playersInQueue.remove(uuidFirst);
                checkQueue();
                return;
            } else {
                // player is on the server
                System.out.println("Player at front of the queue is on the server");
                playerJoinRequest(Bukkit.getServer().getPlayer(uuidFirst), true);
            }
            // The player leaving the server listener will remove this player from the queue.
            // I only need to do the first person in the queue here as the game server will
            // return a 'status-report' when the player joins, this will cause the queue to be
            // checked again.
        }
    }

    public void updateGameServer(String sockName, String status, int currentPlayers, int maxPlayers) {
        GameServer gameServer = getGameServerBySockName(sockName);
        if (gameServer != null) {
            System.out.println("Game server object already exists, setting status; " +
                    status + ", numPlayers: " + currentPlayers);
            // team size is not required for non bedwars games, may need to add it back in
            // if we make other team games
            // it also determines the max number of players.
            // if the game status is live or finishing just get rid of the gameserver, lobby only needs to know about
            // recruiting/countdown servers
            if (status.equals("RECRUITING") || status.equals("COUNTDOWN")) {
                gameServer.updateDetails(status, currentPlayers, 1, maxPlayers);
            } else {
                System.out.println("Game.updateGameServer removing server from list as it is no longer recruiting");
                serverList.remove(gameServer);
            }
        } else {
            if (status.equals("RECRUITING") || status.equals("COUNTDOWN")) {
                System.out.println("Game server object not found, creating a new one, setting" +
                        " status; " + status + ", numPlayers: " + currentPlayers + " sockname " + sockName);
                gameServer = new GameServer(main.getGameManager(), sockName, status, currentPlayers, 1, maxPlayers);
                serverList.add(gameServer);
            } else {
                System.out.println("Game.updateGameServer server not found but not adding as it is no longer recruiting");
            }
            System.out.println("num gameServers = " + serverList.size());
        }
    }

    private GameServer getGameServerBySockName(String sockName) {
        System.out.printf("Looking for gameserver sockname " + sockName );
        for (GameServer gameServer : serverList) {
            System.out.printf("gameserver found sockname is " + sockName);
            if (gameServer.getSockName().equals(sockName)) {
                return gameServer;
            }
        }
        return  null;
    }

    public List<GameServer> getServerList() { return  this.serverList;}

    public Timestamp getEarliestTimestampFromQueue() {
        if (playersInQueue.isEmpty()) {
            return null;
        } else {
            Timestamp ts = new Timestamp(System.currentTimeMillis());
            for (Map.Entry<UUID, Timestamp> p : playersInQueue.entrySet()) {
                if (p.getValue().before(ts)) {
                    ts = p.getValue();
                }
            }
            return  ts;
        }
    }

    public void removePlayerFromQueue(UUID uuid) {
        playersInQueue.remove(uuid);
    }

    public void setMaxPlayers(int maxPlayers) {this.maxPlayers = maxPlayers;}

    public GameServer getServerBySockName(String sockName) {
        if (serverList == null) {
            return null;
        }
        for (GameServer gameServer : serverList) {
            if (gameServer.getSockName().equalsIgnoreCase(sockName)) {
                return gameServer;
            }
        }
        return  null;
    }

    public String getGameName() {return name;}

    public void setIsBlocked(boolean isBlocked) {this.isBlocked = isBlocked;}

    // return max players for the game, again for the sign.
    public int getmaxPlayers() {return maxPlayers;}

    public Material getMaterial() {return material;}

    public int getInventorySlot() {return inventorySlot;}

    public int getQueueSize() {return playersInQueue.size();}

    public boolean isPlayerInQueue(UUID uuid) {return playersInQueue.containsKey(uuid);}

}
