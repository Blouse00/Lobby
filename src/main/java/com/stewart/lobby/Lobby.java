package com.stewart.lobby;


import com.gmail.tracebachi.SockExchange.Messages.ReceivedMessage;
import com.gmail.tracebachi.SockExchange.Messages.ReceivedMessageNotifier;
import com.gmail.tracebachi.SockExchange.Spigot.SockExchangeApi;
import com.google.common.io.ByteArrayDataInput;
import com.stewart.lobby.commands.LobbyCommand;
import com.stewart.lobby.listeners.ConnectListener;
import com.stewart.lobby.listeners.LobbyListener;
import com.stewart.lobby.manager.ConfigManager;
import com.stewart.lobby.manager.GameManager;
import com.stewart.lobby.manager.LobbyManager;
import org.bukkit.Bukkit;
import org.bukkit.Difficulty;
import org.bukkit.plugin.java.JavaPlugin;
import org.stewart.bb_api.Bb_api;
import java.util.function.Consumer;

public final class Lobby extends JavaPlugin {

    private GameManager gameManager;
    private SockExchangeApi sockExchangeApi;
    private LobbyManager lobbyManager;

    private Bb_api bb_api = (Bb_api) Bukkit.getServer().getPluginManager().getPlugin("bb_api");

    private ReceivedMessageNotifier messageNotifier;

    @Override
    public void onEnable()  {
        // load the config file
        ConfigManager.setupConfig(this);

        if (bb_api == null) {
            System.out.println("---------------------------------------------API IS NULL------------------------");
        }

    //   List<GameLeaderboards> lb = bb_api.leaderboardManager.getGameLeaderBoards("ac_");

       /* for (GameLeaderboards lead : lb) {
            System.out.println("Name " + lead.getGameName());
            if (lead.GetListLeaderboard().size()  > 0) {
                System.out.printf("first " + lead.GetListLeaderboard().get(0).playerName + " " + lead.GetListLeaderboard().get(0).score);
                if (lead.GetListLeaderboard().size()  > 1) {
                    System.out.printf("2nd " + lead.GetListLeaderboard().get(1).playerName + " " + lead.GetListLeaderboard().get(1).score);
                    if (lead.GetListLeaderboard().size()  > 2) {
                        System.out.printf("3d " + lead.GetListLeaderboard().get(2).playerName + " " + lead.GetListLeaderboard().get(2).score);
                    }
                }
            }
        }*/

       // Bb_api bb_api = new Bb_api();

        // game manager gets 'game' instances for each game server connected to the lobby
        gameManager = new GameManager(this);
        lobbyManager = new LobbyManager(this);

        Bukkit.getWorld("world").setDifficulty(Difficulty.PEACEFUL);

        System.out.printf("11111111111");

        Bukkit.getWorld("world").setStorm(false);
        // Need this to be able to move players to another server
        this.getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");

        // the listener for players connecting to this server
        Bukkit.getPluginManager().registerEvents(new ConnectListener(this), this);

        // the listener for players clicking on a sign
        Bukkit.getPluginManager().registerEvents(new LobbyListener(this), this);

        System.out.printf("22222222222222");

        // register the pw command class
        getCommand("pw").setExecutor(new LobbyCommand(this));

        System.out.printf("33333333333333");

        // this is used to get messages from the game server for the purrpose of updating the sign posts
        sockExchangeApi = SockExchangeApi.instance();

        System.out.printf("444444444444444");

        // Get the request notifier which will run a provided Consumer when
        // there is a new message on a specific channel
        messageNotifier = sockExchangeApi.getMessageNotifier();

        System.out.printf("5555555555555555");

     /*   // on server start (1 sec delay) loop through all signs, and check if server is online.
        Bukkit.getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
            @Override
            public void run() {
                gameManager.checkGamesOnline(sockExchangeApi);
            }
        }, 20L); */

        //  this is fired when this server receives a 'Sockexchange' message from another server.
        // the message is in the format 0-0-0-0
        // bedwars sends a message when the server starts, each time a new player joins/leaves and when the game
        // starts (no longer recruiting)
        Consumer<ReceivedMessage> requestConsumer = rm -> {
            try{
                ByteArrayDataInput in = rm.getDataInput();
                String s = in.readLine();
                System.out.println("SockExchange message received " + s);
                // s will be the message
                String[] arrReceived = s.split("\\.");
                // sock name . report-status . state (all caps) . current players .  max players . team size.
                if (arrReceived[1].equals("report-status") ) {
                    // sock name for bedwars will be eg bedwars_0, bedwars_1
                    if (arrReceived[0].toLowerCase().contains("bedwars")) {
                        gameManager.updateBedwarsGameServer(arrReceived[0],  arrReceived[2],
                                Integer.parseInt(arrReceived[3]), Integer.parseInt((arrReceived[5])));
                    } else {
                        gameManager.updateGameServer(arrReceived[0],  arrReceived[2],
                                Integer.parseInt(arrReceived[3]), Integer.parseInt((arrReceived[4])));
                    }

                }
            }catch(Exception ex){
                System.out.println("Sock exchange received message error");
                ex.printStackTrace();
            }
        };

        /* original one before  new queue


        //  this is fired when this server receives a 'Sockexchange' message from another server.
        // the message is in the format 0-0-0-0
        // bedwars sends a message when the server starts, each time a new player joins/leaves and when the game
        // starts (no longer recruiting)
        Consumer<ReceivedMessage> requestConsumer = rm -> {
            try{
            ByteArrayDataInput in = rm.getDataInput();
            String s = in.readLine();
            System.out.println("I got readlone " + s);
            // s will be the message
            String[] r = s.split("-");
            // r will be an array of 4 items created by splitting the message string at the '-' character.
            // the first value is the gameID and the second is the server id, both these values are stored in the
            // config file,  the getSignpost() function returns the sign linked to the server the message came from
            PortalManager sign = gameManager.getSignPost(Integer.parseInt(r[0]), Integer.parseInt(r[1]));
            // 3rd value (zero index) is number of players on the server
            sign.setNumPlayers(Integer.parseInt(r[2]));
            // 4th value is 1 for full, 0 for not full
            // TODO update this so still acepts players once minimum players value is reached
            sign.setIsFull(Integer.parseInt(r[3]) == 1 ? true : false);
            // this will update the sign with new values
            sign.updateSign();
            }catch(Exception ex){
                System.out.println("xxxxxxxxxxxx GameChannel");
                ex.printStackTrace();
            }
        };

         */
        // this registers the listener for messages from other servers
        messageNotifier.register("LobbyChannel", requestConsumer);

    }

    // returns the game manager instance
    public  GameManager getGameManager() {return gameManager;}

    public  LobbyManager getLobbyManager() {return lobbyManager;}

    public SockExchangeApi getSockExchangeApi () {return sockExchangeApi;}

    @Override
    public void onDisable() {
        // Plugin shutdown logic
       // messageNotifier.unregister("LobbyChannel", requestConsumer);
    }

    public Bb_api getBb_api() {return bb_api;}

}
