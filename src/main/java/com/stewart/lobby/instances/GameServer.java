package com.stewart.lobby.instances;
import com.gmail.tracebachi.SockExchange.Spigot.SockExchangeApi;
import com.gmail.tracebachi.SockExchange.SpigotServerInfo;
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

    public GameServer(GameManager manager, String sockName, String gameStatus, int currentPlayers, int teamSize, int maxPlayers) {
        this.gameManager = manager;
        this.sockName =  sockName;
        this.gameStatus = gameStatus;
        this.currentPlayers = currentPlayers;
        // teamSize is only used for bedwars
        this.teamSize = teamSize;
        this.maxPlayers = maxPlayers;
        //setMaxPlayers();
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

    public void updateDetails(String gameStatus, int currentPlayers, int teamSize, int maxPlayers) {
        this.gameStatus = gameStatus;
        this.currentPlayers = currentPlayers;
        this.teamSize = teamSize;
        this.maxPlayers = maxPlayers;
    }

    // check if this game server is still online.
    public boolean checkIsOnline() {
       // System.out.println("checkIsOnline");
        if (sockName != null) {
          //  System.out.println("checkIsOnline sockname is " + sockName);
         //   System.out.println("check is online sockname " + sockName);
            SockExchangeApi sockExchangeApi = gameManager.getMain().getSockExchangeApi();
            SpigotServerInfo spigotServerInfo = sockExchangeApi.getServerInfo(sockName);
            if (spigotServerInfo == null) {
                System.out.println("checkIsOnline spigotServerInfo is null for sockname " + sockName);
                return false;
            } else if (!spigotServerInfo.isOnline()) {
               // System.out.println("checkIsOnline server is online");
                return false;
            }
            return true;
        }
        System.out.println("sockname is null");
        return false;
    }

    public String getSockName() {return sockName;}

    public int getCurrentPlayers() {return
            currentPlayers;
    }

    public int getTeamSize() {return teamSize;}

    public int getMaxPlayers() {return maxPlayers;}

    public String getGameStatus() {return gameStatus;}

}
