package com.stewart.lobby.instances;

import com.avaje.ebeaninternal.server.type.RsetDataReader;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import com.stewart.lobby.Lobby;
import com.stewart.lobby.manager.GameManager;
import com.sun.org.apache.xpath.internal.operations.Bool;
import de.simonsator.partyandfriends.spigot.api.pafplayers.PAFPlayer;
import de.simonsator.partyandfriends.spigot.api.pafplayers.PAFPlayerManager;
import de.simonsator.partyandfriends.spigot.api.party.PartyManager;
import de.simonsator.partyandfriends.spigot.api.party.PlayerParty;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.trait.SkinTrait;
import net.minecraft.server.v1_8_R3.ItemStack;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.graalvm.compiler.asm.aarch64.AArch64Assembler;

import java.sql.Timestamp;
import java.util.*;

// the gameManager class will have a list of instances of this game class
// one for each game server listed in the config file
public class Game {

    // the main class, unused at the moment so coupld possibly be removed
    private final Lobby main;

    private final String name;
    private int maxPlayers;
    private final Location npcSpawnLocation;
    private final List<GameServer> serverList = new ArrayList<>();
    private final HashMap<UUID, Timestamp> playersInQueue = new HashMap<>();
  //  private HashMap<UUID, String> partyPlayerQueue = new HashMap<>();
    private final String texture;
    private final String signature;
    private final String nameColour;
    private final Material material;
    private final int inventorySlot;

 //   private org.bukkit.inventory.ItemStack gameItem;

    private boolean isBlocked;
  //  private EntityPlayer npc;

    public Game(Lobby lobby,  String name, Location npcSpawnLocation, String texture, String signature,
                String nameColour, Material material, int inventorySlot) {
        this.isBlocked = false;
      //  this.gameItem = gameItem;
        this.name = name;
        this.npcSpawnLocation = npcSpawnLocation;
        this.main = lobby;
        this.texture = texture;
        this.signature = signature;
        this.nameColour = nameColour;
        this.material = material;
        this.inventorySlot = inventorySlot;
    }

    public void spawnNPC() {

        // https://mineskin.org
      //  String texture = "ewogICJ0aW1lc3RhbXAiIDogMTYxNzYyNjM4OTk0OSwKICAicHJvZmlsZUlkIiA6ICI1ZTIyODhjMzFlOGI0ZTA3YTY0MTc1NTRkNzY4ZTE3NiIsCiAgInByb2ZpbGVOYW1lIiA6ICJNcl9CM25fIiwKICAic2lnbmF0dXJlUmVxdWlyZWQiIDogdHJ1ZSwKICAidGV4dHVyZXMiIDogewogICAgIlNLSU4iIDogewogICAgICAidXJsIiA6ICJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlLzk4MjNkYzk5MDk1ZWY2MzQzODA3ZmY0MDQ5M2JjYmExMjBkYjcyOWY0YThmMjNkNmZmOTU3ZTY5MDk4NjYxOGQiCiAgICB9CiAgfQp9";
      //  String signature ="DQGAVLoOw9ykX3dTbc+/HL1OhPcmj1I3Xo97PuBrp00Df0njEYethdevSON9tBrZqJmR3OP4Ret1p1617pZ2tCfrYv0DSFXsADufkD5nzKxDoxaFvZGMhG9uUgb7F+72FQmu4JGbmA+js2Du2qMDlxJsvd9h+la/zbO1SXgqR2HRSS/BpxRzbsiw7f5ySdLSARYfHQxb/SO2E1cA8wDzhbyoXsnXkHykgVyg+dyR7L2+QBljYcrZjqkisayGYVfA0AWZQWzm6dWChUDV72/9QI6Ry0pQ69w1VoOu7xXy2aD8vGAl9km0tOPpYjH0qhD6RBt2hgeU6YP2JPHkIvAzlcClm4Q7NxjeBOd3I5+WpghjA9U9EzSQtKrZQHjoOqeuFtsvM6qmFfxLhWDL4VhuxjzBYZzxGY3676KnsPKa7+R8SqtEPl6wG7wUjKHP7PW+9anhLPL6zzyvRjnp5Pk2tQbTLcI6gV7i4T2FvrJowBEYp/9bHZNFv2+VfMsRUbHx0VXJ/T81jo2j0w11k4T/rMostv+F6ND4y6I7G3D2qL1MsFU9twUCHNLByzc3YTa1UntvaX4yTEwUgvgTW387ZozOWAiJvZ/9fhSX21zvYqKEMMRJSE/TyYzBPvg/wxGwgIvrwnYh0wAJCjQdZeGmdYAYVEiuIzxSWe0Qgk7XTHU=";
        String npcName = this.nameColour + this.name;

        net.citizensnpcs.api.npc.NPC npc =CitizensAPI.getNPCRegistry().createNPC(EntityType.PLAYER, npcName);
        SkinTrait skin = npc.getOrAddTrait(SkinTrait.class);
        skin.setSkinPersistent("pjs", signature, texture);
        npc.spawn(npcSpawnLocation);

        System.out.println("spawning npc for " + name);


    /*  *** previous way of making npc manually without citizens plugin

        MinecraftServer nmsServer = ((CraftServer) Bukkit.getServer()).getServer();
        WorldServer nmsWorld = ((CraftWorld) Bukkit.getWorlds().get(0)).getHandle();

        GameProfile gameProfile = new GameProfile(UUID.randomUUID(), "BWS");
        gameProfile.getProperties().put("textures", new Property("textures", texture, signature));

        npc = new EntityPlayer(nmsServer, nmsWorld, gameProfile,  new PlayerInteractManager(nmsWorld));
        npc.teleportTo(npcSpawnLocation,false);

        for (Player online : Bukkit.getOnlinePlayers()) {
            addNpcForPlayer(online);
        }*/
    }

    // fired when a player right-clicks on this games npc.
    // also when clicking on inventory item
    // also form checking queue
    public void playerJoinRequest(Player player, Boolean fromQueue) {

     /*   if(playersInQueue.containsKey(player.getUniqueId())) {
            player.sendMessage("You are already in the queue for this game!");
            return;
        }*/

        boolean allowJoin = true;

        // TODO look at the PAF stuff here again. Peter says a non leader could join the server and then the leader could not,
        // may be something to do with the from queue variable.

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
                        Bukkit.getPlayer(p.getUniqueId()).sendMessage("Attempting to join " + this.name + "!");
                    }
                }
            }
        }

        if (allowJoin) {
        if (this.isBlocked) {
            // in the process of sending a player, wait till that's complete
            System.out.println("game is blocked, adding player to queue");
            addPlayerToQueue(player, false);
        } else {
            // player requested to join game

            if (this.name.contains("Bedwars_")) {
                System.out.println("BedWars server join detected");
                JoinBedwarsServer(player);
                return;
            }
            if (this.name.contains("SMP")) {
                System.out.println("SMP server join detected");

                main.getGameManager().removePlayerFromQueues(player.getUniqueId(), null);
                if (!serverList.isEmpty() ) {
                    try {
                        System.out.println("Sending player to server");

                        ByteArrayDataOutput out = ByteStreams.newDataOutput();
                        out.writeUTF("Connect");
                        // get server name from the sign
                        out.writeUTF("smp_0");
                        // teleport the player to the game server, this is done via the bungeecord channel
                        player.sendPluginMessage(main, "BungeeCord", out.toByteArray());
                    } catch (Exception ex) {
                        player.sendMessage(ChatColor.RED + "There was a problem connecting you to that game.  Please try again later!");
                    }
                }
                return;
            }
            if (this.name.contains("Creative")) {
                System.out.println("Creative server join detected");

                main.getGameManager().removePlayerFromQueues(player.getUniqueId(), null);
                if (!serverList.isEmpty() ) {
                    try {
                        System.out.println("Sending player to server");

                        ByteArrayDataOutput out = ByteStreams.newDataOutput();
                        out.writeUTF("Connect");
                        // get server name from the sign
                        out.writeUTF("creative");
                        // teleport the player to the game server, this is done via the bungeecord channel
                        player.sendPluginMessage(main, "BungeeCord", out.toByteArray());
                    } catch (Exception ex) {
                        player.sendMessage(ChatColor.RED + "There was a problem connecting you to that game.  Please try again later!");
                    }
                }
                return;
            }
            if (serverList.isEmpty()) {
                System.out.println("player tried to join game but no servers found");
                player.sendMessage("No game servers found, please try again later.");
            } else {
                // player.sendMessage("Attempting to join " + this.getGameName() + "!");
                // loop through the servers whose status is recruiting.
                // make a list of possible server sockNames
                HashMap<String, Integer> lstAvailable = new HashMap<>();
                for (GameServer gameServer : serverList) {
                    System.out.println("status: " + gameServer.getGameStatus() + " . p: " + gameServer.getCurrentPlayers() +
                            " max " + maxPlayers);
                    if ((gameServer.getGameStatus().equals("RECRUITING") || gameServer.getGameStatus().equals("COUNTDOWN"))
                            && gameServer.getCurrentPlayers() < maxPlayers) {
                        System.out.println("adding eligible server to list of possibles");
                        // Add this server to the list of possible player targets
                        lstAvailable.put(gameServer.getSockName(), gameServer.getCurrentPlayers());
                    }
                }
                System.out.println(lstAvailable.size() + " possible server(s) found");
                if (lstAvailable.isEmpty()) {
                    // no servers are recruiting, put the player in the queue.
                    addPlayerToQueue(player, true);
                    System.out.println("Servers all busy, adding player to queue");
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
                    try {
                        System.out.println("Sending player to server");
                        ByteArrayDataOutput out = ByteStreams.newDataOutput();
                        out.writeUTF("Connect");
                        // get server name from the sign
                        out.writeUTF(sockNameMostPlayers);
                        // teleport the player to the game server, this is done via the bungeecord channel
                        player.sendPluginMessage(main, "BungeeCord", out.toByteArray());

                    } catch (Exception ex) {
                        player.sendMessage(ChatColor.RED + "There was a problem connecting you to that game.  Please try again later!");
                    }
                }
            }
            }
        }
    }

    private void JoinSMPServer(Player player) {


    }

    private void JoinBedwarsServer(Player player) {
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
            SendPlayerToServer(player, serverSockName, party);
        }
    }

    private void SendPlayerToServer(Player player, String sockName, PlayerParty party) {
        try {
            System.out.println("Sending player to server 2");
            ByteArrayDataOutput out = ByteStreams.newDataOutput();
            out.writeUTF("Connect");
            // get server name from the sign
            out.writeUTF(sockName);
            // teleport the player to the game server, this is done via the bungeecord channel
            player.sendPluginMessage(main, "BungeeCord", out.toByteArray());
     /*       if (party != null) {
                AddPartyPlayersToPartyQueue(party, sockName);
            }  */
        } catch (Exception ex) {
            player.sendMessage(ChatColor.RED + "There was a problem connecting you to that game.  Please try again later!");
        }
    }

  /*  private void AddPartyPlayersToPartyQueue(PlayerParty party, String serverSockName) {
        System.out.println("Adding party players (" + party.getPlayers().size() + ") to queue");
        for (PAFPlayer player : party.getPlayers()) {
            System.out.println("Party player name " + player.getName());
            AddIndivdualPartyMemberToQueue(player.getUniqueId(), serverSockName);

        }
        System.out.println("Add party leader to list");
        AddIndivdualPartyMemberToQueue(party.getLeader().getUniqueId(), serverSockName);

    } */

   /* private void AddIndivdualPartyMemberToQueue(UUID uuid, String serverSockName) {
        if (main.getServer().getPlayer(uuid) != null) {
            // then check the player is not already in the partyQueueList
            if (partyPlayerQueue.containsKey(uuid) == false) {
                System.out.println("Adding Party player " + Bukkit.getPlayer(uuid).getName() + " to partyPlayerQueue");
                // add them to the list
                partyPlayerQueue.put(uuid, serverSockName);
            } else {
                System.out.println("Party player " + Bukkit.getPlayer(uuid).getName() + " is already in the partyPlayerQueue");
            }
        } else {
            System.out.println("Party player " + Bukkit.getPlayer(uuid).getName() + " is not on this server");
        }
    }  */

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
            gameServer.updateDetails(status, currentPlayers, 1);
        } else {
            System.out.println("Game server object not found, creating a new one, setting" +
                    " status; " + status + ", numPlayers: " + currentPlayers);
            gameServer = new GameServer(main.getGameManager(), sockName, status, currentPlayers, 1);
            serverList.add(gameServer);
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

    public void addServerToList(GameServer gameServer) {
        serverList.add(gameServer);
        System.out.println("Game name: " + name + ". server list size: " + serverList.size());
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
