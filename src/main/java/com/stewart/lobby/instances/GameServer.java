package com.stewart.lobby.instances;
import com.stewart.lobby.manager.GameManager;
import java.util.List;

public class GameServer {

    private GameManager gameManager;
    private String sockName;
    private String gameStatus;
    private int currentPlayers;
    private int teamSize;
    private int maxPlayers;
    private List<String> playersInQueue;

    public GameServer(GameManager manager, String sockName, String gameStatus, int currentPlayers, int teamSize) {
        this.gameManager = manager;
        this.sockName =  sockName;
        this.gameStatus = gameStatus;
        this.currentPlayers = currentPlayers;
        this.teamSize = teamSize;
        setMaxPlayers();
    }

    public void setMaxPlayers() {
        this.maxPlayers = 10; // default value, not picked for any reason. will update first player joins
        if (teamSize == 1) {
            this.maxPlayers = 12;
        }
        if (teamSize ==2 || teamSize == 4 ) {
            this.maxPlayers = 16;
        }
    }


    public void updateDetails(String gameStatus, int currentPlayers, int teamSize) {
        this.gameStatus = gameStatus;
        this.currentPlayers = currentPlayers;
        this.teamSize = teamSize;
        setMaxPlayers();
    }



    public String getSockName() {return sockName;}

    public int getCurrentPlayers() {return
            currentPlayers;
    }

    public int getTeamSize() {return teamSize;}

    public int getMaxPlayers() {return maxPlayers;}

    public String getGameStatus() {return gameStatus;}

}
