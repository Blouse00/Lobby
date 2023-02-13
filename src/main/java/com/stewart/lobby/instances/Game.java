package com.stewart.lobby.instances;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import com.stewart.lobby.Lobby;
import de.simonsator.partyandfriends.spigot.api.pafplayers.PAFPlayer;
import de.simonsator.partyandfriends.spigot.api.pafplayers.PAFPlayerManager;
import de.simonsator.partyandfriends.spigot.api.party.PartyManager;
import de.simonsator.partyandfriends.spigot.api.party.PlayerParty;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.trait.SkinTrait;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import java.sql.Timestamp;
import java.util.*;

// the gameManager class will have a list of instances of this game class
// one for each game server listed in the config file
public class Game {

    // the main class, unused at the moment so coupld possibly be removed
    private Lobby main;

    private String name;
    private int maxPlayers;
    private Location npcSpawnLocation;
    private List<GameServer> serverList = new ArrayList<>();
    private HashMap<UUID, Timestamp> playersInQueue = new HashMap<>();
    private String texture;
    private String signature;
    private String nameColour;
    private int gameColourInt;
    private int inventorySlot;

    private boolean isBlocked;
  //  private EntityPlayer npc;

    public Game(Lobby lobby,  String name, Location npcSpawnLocation, String texture, String signature,
                String nameColour, int gameColourInt, int inventorySlot) {
        this.isBlocked = false;
        this.name = name;
        this.npcSpawnLocation = npcSpawnLocation;
        this.main = lobby;
        this.texture = texture;
        this.signature = signature;
        this.nameColour = nameColour;
        this.gameColourInt = gameColourInt;
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
    public void playerJoinRequest(Player player) {
        PAFPlayer pafPlayer = PAFPlayerManager.getInstance().getPlayer(player.getUniqueId());
        PlayerParty party = PartyManager.getInstance().getParty(pafPlayer);
        if (party != null) {
            // The party exists. You can use different methods of the PlayerParty object to get information about the party, e.g. who is in the party.
            // The player is not in a party
            for (PAFPlayer p : party.getAllPlayers()) {
                System.out.println("party " + p.getName());
            }
        } else {

            System.out.println("player not in a party " +  player.getDisplayName());
        }

        if (this.isBlocked) {
            // in the process of sending a player, wait till that's complete
            if (!playersInQueue.containsKey(player.getUniqueId())) {
                addPlayerToQueue(player);
                System.out.println("game is blocked, adding player to queue");
            } else {
                System.out.println("game is blocked, player already in queue");
            }
        } else {
            // player requested to join game
            if (serverList.size() == 0) {
                System.out.println("player tried to join game but no servers found");
                player.sendMessage("No game servers found, please try again later.");
            } else {
                // loop through the servers whose status is recruiting or countdown.
                // make a list of possible server sockNames
                HashMap<String, Integer> lstAvailable = new HashMap<>();
                for (GameServer gameServer : serverList) {
                    System.out.println("status: " + gameServer.getGameStatus() + " . p: " +gameServer.getCurrentPlayers() +
                             " max " + maxPlayers);
                    if ((gameServer.getGameStatus().equals("COUNTDOWN") || gameServer.getGameStatus().equals("RECRUITING"))
                            && gameServer.getCurrentPlayers() < maxPlayers) {
                        System.out.println("adding server");
                        // Add this server to the list of possible player targets
                        lstAvailable.put(gameServer.getSockName(), gameServer.getCurrentPlayers());
                    }
                }
                System.out.println("list size = " + lstAvailable.size());
                if (lstAvailable.size() == 0) {
                     // no servers are recruiting, put the player in the queue.
                    addPlayerToQueue(player);
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

    public void addPlayerToQueue(Player player) {
        playersInQueue.put(player.getUniqueId(), new Timestamp(System.currentTimeMillis()));
        player.sendMessage("Servers are busy, you have been added to the queue.");
        System.out.println("Queue size: " + playersInQueue.size());
    }


    public  void checkQueue() {
        System.out.println("Checking game queue for " + name);
        System.out.println("players in the queue = " + playersInQueue.size());
        if (playersInQueue.size() > 0) {
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
                playerJoinRequest(Bukkit.getServer().getPlayer(uuidFirst));
            }
            // The player leaving the server listener will remove this player from the queue.
            // I only need to do the first person in the queue here as the game server will
            // return a 'status-report' when the player joins, this will cause the queue to be
            // checked again.
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

    public int getGameColourInt() {return gameColourInt;}

    public int getInventorySlot() {return inventorySlot;}

    public int getQueueSize() {return playersInQueue.size();}

}

/*

   // the main class, unused at the moment so coupld possibly be removed
    private Lobby lobby;

    private  int id;
    private String name;
    private int maxPlayers;
    private int currentPlayers;
    // not used at the moment but will be used to denote a game server that has its own sub lobbys,
    // this will be servers with multiple arenas on them such as spleef or sumo.
    private boolean hasSubLobby;

    public Game(Lobby lobby, int id, String name, int maxPlayers, boolean hasSubLobby) {
        this.lobby = lobby;
        this.id=id;
        this.name = name;
        this.maxPlayers = maxPlayers;
        this.currentPlayers = 0;
        this.hasSubLobby = hasSubLobby;
    }



    // return the game id
    public int getId() {return  id;}

    // get the name of the game - for the sign
    public String getName() {return name;}

    // return max players for the game, again for the sign.
    public int getmaxPlayers() {return maxPlayers;}
 */
