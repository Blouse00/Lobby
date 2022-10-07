package com.stewart.lobby.instances;

import com.stewart.lobby.Lobby;

// the gameManager class will have a list of instances of this game class
// one for each game server listed in the config file
public class Game {

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


    /* INFO */

    // return the game id
    public int getId() {return  id;}

    // get the name of the game - for the sign
    public String getName() {return name;}

    // return max players for the game, again for the sign.
    public int getmaxPlayers() {return maxPlayers;}

}
