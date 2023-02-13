package com.stewart.lobby.instances;

import com.stewart.lobby.Lobby;
import org.bukkit.Location;

public class GameServer {

    private String sockName;
    private String gameStatus;
    private int currentPlayers;
    private int teamSize;

    public GameServer(String sockName, String gameStatus, int currentPlayers, int teamSize) {
        this.sockName =  sockName;
        this.gameStatus = gameStatus;
        this.currentPlayers = currentPlayers;
        this.teamSize = teamSize;
    }

    public void updateDetails(String gameStatus, int currentPlayers, int teamSize) {
        this.gameStatus = gameStatus;
        this.currentPlayers = currentPlayers;
        this.teamSize = teamSize;
    }

    public String getSockName() {return sockName;}

    public int getCurrentPlayers() {return currentPlayers;}

    public int getTeamSize() {return teamSize;}

    public String getGameStatus() {return gameStatus;}

}
