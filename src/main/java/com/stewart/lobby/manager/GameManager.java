package com.stewart.lobby.manager;

import com.gmail.tracebachi.SockExchange.Spigot.SockExchangeApi;
import com.gmail.tracebachi.SockExchange.SpigotServerInfo;
import com.stewart.lobby.Lobby;
import com.stewart.lobby.instances.Game;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.ArrayList;
import java.util.List;

// game manager keeps a list of all the game instances for each game listed in the config file
// it also has a list of portalmanagers, these are basically the but could be changed to be villagers etc.
public class GameManager {

    private List<Game> games = new ArrayList<>();
    private List<PortalManager> signs = new ArrayList<>();

    // when the class is constructed get all the games from the config file
    public GameManager(Lobby lobby) {
        FileConfiguration config = lobby.getConfig();
        for (String gameID : config.getConfigurationSection("games").getKeys(false)) {

            // add each game to the list of games
            games.add(new Game(lobby, Integer.parseInt(gameID),
                    config.getString("games." + gameID + ".game"),
                    config.getInt("games." + gameID + ".max-players"),
                    config.getBoolean("games." + gameID + ".has-sub-lobby")
            ));

            // it may be that each game has more than one server.
            // I'll loop through each games 'signs' in the config file to get each server for the game
            // each config sign will have it's coordnates.  There should be an ingame sign at each coordinate

            for (String s : config.getConfigurationSection("games." + gameID + ".signs").getKeys(false)) {
                Location location = new Location(
                        Bukkit.getWorld("World"),
                        config.getDouble("games." + gameID + ".signs." + s + ".x"),
                        config.getDouble("games." + gameID + ".signs." + s +  ".y"),
                        config.getDouble("games." + gameID + ".signs." + s + ".z"));

                PortalManager portalManager = new PortalManager(lobby,Integer.parseInt(s), Integer.parseInt(gameID), location,
                        config.getString("games." + gameID + ".signs." + s + ".server"),
                        config.getString("games." + gameID + ".signs." + s + ".ip"),
                        config.getInt("games." + gameID + ".signs." + s + ".port")
                );

                // I started off calling them signs but changed to portal to keep it relevant if we use villagers.
                // the list is still called signs though for some reason
                // each sign ( which represents a game server) is added to the signs list
                signs.add(portalManager);
            }
        }
    }

    // return a list of all the games - unused
    public  List<Game> getGames() {return  games;}

    // get a game by ID
    public Game getGame(int id) {
        System.out.println("games length = " + games.size());
        for(Game game : games) {
            System.out.println("games getID = " + game.getId());
            if (game.getId() == id) {
                return game;
            }
        }
        return  null;
    }

    // get a sign post by its location - used when a player clicks on a sign
    public PortalManager getSignPost(Location location) {
        for (PortalManager sign :signs) {
            if (sign.getSignLocation().equals(location)) {
                return sign;
            }
        }
        return null;
    }

    // get a sign iven its gameID and signID
    public PortalManager getSignPost(int gameID, int signID) {
        for (PortalManager sign :signs) {
            if (sign.getGameID() == gameID && sign.getID() == signID) {
                return sign;
            }
        }
        return null;
    }

    // used when the lobby starts to send a message to each server  asking it to respond
    // with its current status so the sign can be updated.
    // not sure if the works though, I always start the loby first so it receives a message when the game starts
    // any received message will arrive in the requestConsumer in the main lobby class
    public void checkGamesOnline(SockExchangeApi api) {
        for (PortalManager p : signs) {
            SpigotServerInfo spigotServerInfo = api.getServerInfo(p.getServerName());
            if (spigotServerInfo == null) {
                System.out.println(p.getServerName() + " is offline ");
                p.setIsFull(true);
                p.setOffline();
            } else {
                if (spigotServerInfo.isOnline()) {
                    System.out.println(p.getServerName() + " is Online");
                    p.updateSign();
                } else {
                    System.out.println(p.getServerName() + " is offline ");
                    p.setIsFull(true);
                    p.setOffline();
                }
            }
        }



    }
}
