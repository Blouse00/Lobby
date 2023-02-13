package com.stewart.lobby.manager;

import com.stewart.lobby.Lobby;
import com.stewart.lobby.instances.Game;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.Sign;

// an instance of this is created for each sign to another server
public class PortalManager {

    private Lobby lobby;
    private int id;
    private int gameID;
    private Location sign;
    private String serverName;
    private int port;
    private String ip;
    private boolean isFull;
    private int currentPlayers;

    // when created I need the sign location, and some server information
    public PortalManager(Lobby lobby, int id, int gameID, Location location, String serverName,
                         String ip, int port) {
        this.lobby = lobby;
        this.id = id;
        this.gameID = gameID;
        this.sign = location;
        this.isFull = false;
        this.serverName = serverName;
        this.ip = ip;
        this.port = port;
        this.currentPlayers = 0;
    }

    // called when the sign needs to be updated
   /* // called from the requestConsumer in the main lobby class when it receives a message from another server.
    public void updateSign() {
        // Game name + sign ID
        // Game state
        // Players current/max
        System.out.println("!-------------update sign---");
        // get the game this sign is connected to
        Game game = lobby.getGameManager().getGame(this.gameID);
        // ternary operation the sting is set dependin on the boolean value isFull
        String gameStatus = isFull ? ChatColor.RED + "FULL" : ChatColor.GREEN + "Recruiting";
        if (game != null) {
            // get the actual sign block
            Sign signBlock = (Sign) sign.getBlock().getState();
            // sign one is game name and server id (bedwars 0)
            signBlock.setLine(0,  ChatColor.DARK_PURPLE + game.getName());
            // Full / recruiting
            signBlock.setLine(1,gameStatus);
            // players/max players
            signBlock.setLine(2,currentPlayers + " / " + game.getmaxPlayers());
            signBlock.setLine(3, "");
            signBlock.update();
        }
    }

    // fired if this server is offline
    public void setOffline() {
        System.out.println("!-------------set sign offline---");
        Game game = lobby.getGameManager().getGame(this.gameID);
        if (game != null) {
            Sign signBlock = (Sign) sign.getBlock().getState();
            signBlock.setLine(0,game.getName() + " " + this.id);
            signBlock.setLine(1,ChatColor.RED + "OFFLINE");
            signBlock.setLine(2,"-");
            signBlock.setLine(3, "");
            signBlock.update();
        }
    }  */

    // return the sign location, server name, gameID, signID & isFull
    public  Location getSignLocation() {return sign;}
    public String getServerName() {return serverName;}
    public int getGameID() {return gameID;}
    public int getID() {return id;}
    public boolean getIsFull() {return isFull;}

    // set the number of players for this game
    public void setNumPlayers(int numPlayers) {this.currentPlayers = numPlayers;}
    // set if it s full or not
    public void setIsFull(boolean isFull) {
        System.out.println("setting isfull to " + isFull);
        this.isFull = isFull;
    }

}
