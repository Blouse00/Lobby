package com.stewart.lobby.instances;
import com.stewart.lobby.Lobby;

import java.util.*;

public class AutoGameSelector {

    private final Lobby main;

    private List<GameForAutoJoin> lstAllGameServers = new ArrayList<>();

    public  AutoGameSelector (Lobby lobby) {
        this.main = lobby;
    }

    public String getGameMostLikelyToStart() {

        CheckNonBedwarsGames();
        CheckBedwarsGames();
      //  System.out.println("Checked all game servers, total found = " + lstAllGameServers.size());
        if (lstAllGameServers.isEmpty()) {
            return "";
        } else {
            return getBestFromList(lstAllGameServers);
        }
    }

    private void CheckNonBedwarsGames() {
        for (Game game : main.getGameManager().getGameList()) {
            // ignore creative and smp
          //  System.out.println("getGameMostLikelyToStart searching game " + game.getGameName());
            if (!game.getGameName().contains("SMP") &&
                    !game.getGameName().equalsIgnoreCase("Creative")) {
                int minPlayers = getGameMinPlayers(game.getGameName());
                // could be multiple servers for each game
                for (GameServer gameServer : game.getServerList()) {
                    // only include recruiting & countdown games
                    if (gameServer.getGameStatus().equals("RECRUITING") || gameServer.getGameStatus().equals("COUNTDOWN")) {
                        // cant be at max players
                        if (gameServer.getCurrentPlayers() < gameServer.getMaxPlayers()) {
                            System.out.println("Adding server to the list for single game. Min players = " + minPlayers + ", current players = " + gameServer.getCurrentPlayers());
                            lstAllGameServers.add(new GameForAutoJoin(game.getGameName(), minPlayers, gameServer.getCurrentPlayers()));
                        }else{
                            System.out.println("NOT INCLUDING - game recruiting or countdown at max players");
                        }
                    } else{
                        System.out.println("NOT INCLUDING - game not recruiting or countdown");
                    }
                }
                if (game.getServerList().isEmpty()) {
                    System.out.println("NOT INCLUDING - game had no servers");
                }
            } else {
                System.out.println("NOT INCLUDING - game was SMP or creative");
            }
        }
    }

    private void CheckBedwarsGames() {
        System.out.println("Checking bedwars server list size = " + main.getGameManager().getBedwarsGameList().size());
        for (GameServer gameServer : main.getGameManager().getBedwarsGameList()) {

            // only include recruiting games
            if (gameServer.getGameStatus().equals("RECRUITING")) {
                // cant be at max players
                if (gameServer.getTeamSize() == 0) {
                    System.out.println("Bedwars server is in standby, adding bedwars solo to the list");
                    // this server is in standby, add it to the list as solo (fastest to start at only 2 players)
                    lstAllGameServers.add(new GameForAutoJoin("Bedwars_solo", 2, 0));
                } else {
                    // this server has players on it waiting
                    switch (gameServer.getTeamSize()) {
                        case (1):
                            System.out.println("Bedwars solo server recruiting with players");
                            lstAllGameServers.add(new GameForAutoJoin("Bedwars_solo", 2, gameServer.getCurrentPlayers()));
                            break;
                        case (2):
                            System.out.println("Bedwars duo server recruiting with players");
                            lstAllGameServers.add(new GameForAutoJoin("Bedwars_duos", 4, gameServer.getCurrentPlayers()));
                            break;
                        default:
                            System.out.println("Bedwars quad server recruiting with players");
                            lstAllGameServers.add(new GameForAutoJoin("Bedwars_quads", 4, gameServer.getCurrentPlayers()));
                            break;
                    }
                }
            }
        }
    }

    private String getBestFromList(List<GameForAutoJoin> lst) {
        if (lst.size() == 1) {
            return lst.get(0).getName();
        } else {
            // should sort the list and put the one with the highest score first
           // lst.sort(new GameComparator());

            // should sort by score then my defined order for the different game types
            lst.sort(Comparator.comparing(GameForAutoJoin::getScore).reversed()
                    .thenComparing(c -> definedOrder.indexOf(c.getName())));
            // check the sorting
           /* int j =0;
            for (GameForAutoJoin game : lst) {
                System.out.println("index " + j +" score " + game.getScore() + ", name " + game.getName());
                j++;
            }*/
            return lst.get(0).getName();
        }
    }

    List<String> definedOrder = // define your custom order
            Arrays.asList("Assault_Course", "Full_Iron_Armour", "Icewars", "BETA_Icewars", "Bedwars_solo", "Bedwars_duos", "Bedwars_quads");

    private int getGameMinPlayers(String gameName) {
        switch (gameName.toLowerCase()) {
            case "bedwars_duos":
            case "bedwars_quads":
                return 4;
            case "assault_course":
            case "full_iron_armour":
                return 1;
            default:  // icewars, bedwars_solo
                return 2;
        }
    }

    private static class GameForAutoJoin {
        public String name;
        public int minPlayers;
        public int intCurrentPlayers;

        public GameForAutoJoin (String _name, int _minPlayers, int _currentPlayers) {
            this.name = _name;
            this.minPlayers = _minPlayers;
            this.intCurrentPlayers = _currentPlayers;
        }

        public int getScore() {
            return intCurrentPlayers - minPlayers;
        }

        public String getName() {return name;}
    }

}


