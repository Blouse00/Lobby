package com.stewart.lobby;


import com.gmail.tracebachi.SockExchange.Messages.ReceivedMessage;
import com.gmail.tracebachi.SockExchange.Messages.ReceivedMessageNotifier;
import com.gmail.tracebachi.SockExchange.Spigot.SockExchangeApi;
import com.google.common.io.ByteArrayDataInput;
import com.stewart.lobby.listeners.ConnectListener;
import com.stewart.lobby.listeners.LobbyListener;
import com.stewart.lobby.manager.ConfigManager;
import com.stewart.lobby.manager.GameManager;
import com.stewart.lobby.manager.PortalManager;
import net.minecraft.server.v1_8_R3.EntityInsentient;
import net.minecraft.server.v1_8_R3.EntityTypes;
import org.bukkit.Bukkit;
import org.bukkit.Difficulty;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public final class Lobby extends JavaPlugin {

   // public PluginListener pluginListener;

    private GameManager gameManager;

    private ReceivedMessageNotifier messageNotifier;

    @Override
    public void onEnable()  {
        // load the config file
        ConfigManager.setupConfig(this);

        // game manager gets 'game' instances for each game server connected to the lobby
        gameManager = new GameManager(this);

        Bukkit.getWorld("world").setDifficulty(Difficulty.PEACEFUL);


        // Need this to be able to move players to another server
        this.getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");

        // the listener for players connecting to this server
        Bukkit.getPluginManager().registerEvents(new ConnectListener(this), this);

        // the listener for players clicking on a sign
        Bukkit.getPluginManager().registerEvents(new LobbyListener(this), this);

        // this is used to get messages from the game server for the purrpose of updating the sign posts
        SockExchangeApi api = SockExchangeApi.instance();

        // Get the request notifier which will run a provided Consumer when
        // there is a new message on a specific channel
        messageNotifier = api.getMessageNotifier();

        // on server start (1 sec delay) loop through all signs, and check if server is online.
        Bukkit.getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
            @Override
            public void run() {
                gameManager.checkGamesOnline(api);
            }
        }, 20L);

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
        // this registers the listener for messages from other servers
        messageNotifier.register("LobbyChannel", requestConsumer);

    }

    // returns the game manager instance
    public  GameManager getGameManager() {return gameManager;}

    @Override
    public void onDisable() {
        // Plugin shutdown logic
       // messageNotifier.unregister("LobbyChannel", requestConsumer);
    }

}
