package com.stewart.lobby;

import com.gmail.tracebachi.SockExchange.Messages.ReceivedMessage;
import com.gmail.tracebachi.SockExchange.Messages.ReceivedMessageNotifier;
import com.gmail.tracebachi.SockExchange.Spigot.SockExchangeApi;
import com.google.common.io.ByteArrayDataInput;
import com.stewart.lobby.commands.FlyCommand;
import com.stewart.lobby.commands.LobbyCommand;
import com.stewart.lobby.listeners.ConnectListener;
import com.stewart.lobby.listeners.LobbyListener;
import com.stewart.lobby.manager.ConfigManager;
import com.stewart.lobby.manager.GameManager;
import com.stewart.lobby.manager.LobbyManager;
import com.stewart.lobby.manager.RuleLobbyManager;
import org.bukkit.Bukkit;
import org.bukkit.Difficulty;
import org.bukkit.plugin.java.JavaPlugin;
import org.stewart.bb_api.Bb_api;
import java.util.function.Consumer;

public final class Lobby extends JavaPlugin {

    private GameManager gameManager;
    private SockExchangeApi sockExchangeApi;
    private LobbyManager lobbyManager;
    private RuleLobbyManager ruleLobbyManager;
    private final Bb_api bb_api = (Bb_api) Bukkit.getServer().getPluginManager().getPlugin("bb_api");
    private ReceivedMessageNotifier messageNotifier;

    @Override
    public void onEnable()  {
        // load the config file
        ConfigManager.setupConfig(this);

        if (bb_api == null) {
            System.out.println("---------------------------------------------API IS NULL------------------------");
        } else {
            System.out.println("---------------------------------------------toggle messagesending------------------------");
            bb_api.getMessageManager().toggleMessageSending(true);
        }

        // game manager gets 'game' instances for each game server connected to the lobby
        gameManager = new GameManager(this);
        lobbyManager = new LobbyManager(this);
        ruleLobbyManager = new RuleLobbyManager(this);

        Bukkit.getWorld("world").setDifficulty(Difficulty.PEACEFUL);

        Bukkit.getWorld("world").setStorm(false);
        // Need this to be able to move players to another server
        this.getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");

        // the listener for players connecting to this server
        Bukkit.getPluginManager().registerEvents(new ConnectListener(this), this);

        // the listener for players clicking on a sign
        Bukkit.getPluginManager().registerEvents(new LobbyListener(this), this);

        // register the pw command class
        getCommand("pw").setExecutor(new LobbyCommand(this));
        getCommand("fly").setExecutor(new FlyCommand(this));

        // this is used to get messages from the game server for the purrpose of updating the sign posts
        sockExchangeApi = SockExchangeApi.instance();

        // Get the request notifier which will run a provided Consumer when
        // there is a new message on a specific channel
        messageNotifier = sockExchangeApi.getMessageNotifier();

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


        // this registers the listener for messages from other servers
        messageNotifier.register("LobbyChannel", requestConsumer);

    }

    // returns the game manager instance
    public  GameManager getGameManager() {return gameManager;}

    public  LobbyManager getLobbyManager() {return lobbyManager;}
    public RuleLobbyManager getRuleLobbyManager() { return ruleLobbyManager;}

    public SockExchangeApi getSockExchangeApi () {return sockExchangeApi;}

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        bb_api.getMessageManager().toggleMessageSending(false);
       // messageNotifier.unregister("LobbyChannel", requestConsumer);
    }

    public Bb_api getBb_api() {return bb_api;}

}
