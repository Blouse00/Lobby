package com.stewart.lobby;

import com.gmail.tracebachi.SockExchange.Messages.ReceivedMessage;
import com.gmail.tracebachi.SockExchange.Messages.ReceivedMessageNotifier;
import com.gmail.tracebachi.SockExchange.Spigot.SockExchangeApi;
import com.gmail.tracebachi.SockExchange.SpigotServerInfo;
import com.google.common.io.ByteArrayDataInput;
import com.stewart.lobby.commands.FlyCommand;
import com.stewart.lobby.commands.LobbyCommand;
import com.stewart.lobby.listeners.ConnectListener;
import com.stewart.lobby.listeners.LobbyListener;
import com.stewart.lobby.manager.*;
import com.stewart.lobby.minigames.SumoMiniGame;
import com.stewart.lobby.utils.LobbyUtils;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.internal.utils.JDALogger;
import org.bukkit.Bukkit;
import org.bukkit.Difficulty;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.stewart.bb_api.Bb_api;
import com.planetgallium.kitpvp.Game;
import org.stewart.bb_api.utils.TempPromos;

import javax.security.auth.login.LoginException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public final class Lobby extends JavaPlugin {

    private GameManager gameManager;
    private SockExchangeApi sockExchangeApi;
    private LobbyManager lobbyManager;
    private RuleLobbyManager ruleLobbyManager;
    private BoostPadManager boostPadManager;
    private final Bb_api bb_api = (Bb_api) Bukkit.getServer().getPluginManager().getPlugin("bb_api");
    private final Game kitPvP = (Game) Bukkit.getServer().getPluginManager().getPlugin("KitPvP");
    private ReceivedMessageNotifier messageNotifier;
    private JDA jda;
    private static Lobby instance;

    //  private TempPromos tempPromos;

    @Override
    public void onEnable() {
        // load the config file
        ConfigManager.setupConfig(this);
        instance = this;

        JDALogger.setFallbackLoggerEnabled(false);
        JDABuilder builder = JDABuilder.createDefault(ConfigManager.getDiscordToken());
        builder.setActivity(Activity.watching("Your server"));

        try {
            jda = builder.build().awaitReady();
            jda.getGuildById(ConfigManager.getDiscordServer()).getTextChannelById(ConfigManager.getDiscordChannel()).sendMessage("testing").queue();
            System.out.println("discord bot connected");
        } catch (Exception e) {
            e.printStackTrace();
        }

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
        boostPadManager = new BoostPadManager(this);

        //   tempPromos = bb_api.startTempPromos(new Location(Bukkit.getWorld("world"), 22.5, 57.5, -22.5));

        Bukkit.getWorld("world").setDifficulty(Difficulty.NORMAL);

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
            try {
                ByteArrayDataInput in = rm.getDataInput();
                String s = in.readLine();
                System.out.println("SockExchange message received " + s);
                // s will be the message
                String[] arrReceived = s.split("\\.");
                // sock name . report-status . state (all caps) . current players .  max players . team size.
                if (arrReceived[1].equals("report-status")) {
                    // sock name for bedwars will be eg bedwars_0, bedwars_1
                   /* if (arrReceived[0].toLowerCase().contains("bedwars")) {
                        // sock status currentPlayers teamSize
                        gameManager.updateBedwarsGameServer(arrReceived[0], arrReceived[2],
                                Integer.parseInt(arrReceived[3]), Integer.parseInt(arrReceived[4]), Integer.parseInt(arrReceived[5]));
                    } else*/
                    if (arrReceived[0].toLowerCase().contains("monster") || arrReceived[0].toLowerCase().contains("bedwars")) {
                        // for monster the same sockname could have multiple game types (quad solo duo)
                        // will pass sockname status currnetPlayers maxPlayers, gameType

                        //     System.out.println("/****************** " + arrReceived[0] + " " +  arrReceived[2] + " " +
                        //             Integer.parseInt(arrReceived[3]) + " " + Integer.parseInt(arrReceived[4]) + " " + arrReceived[5]);
                        gameManager.updateGameServer(arrReceived[0], arrReceived[2],
                                Integer.parseInt(arrReceived[3]), Integer.parseInt(arrReceived[4]), arrReceived[5]);
                    } else {
                        // sockname status currentPlayers maxPlayers
                        gameManager.updateGameServer(arrReceived[0], arrReceived[2],
                                Integer.parseInt(arrReceived[3]), Integer.parseInt(arrReceived[4]), "");
                    }

                } else if (arrReceived[1].equals("player-rejoining")) {
                    lobbyManager.PlayerReJoinGameServer(arrReceived[2]);

                }
            } catch (Exception ex) {
                System.out.println("Sock exchange received message error");
                ex.printStackTrace();
            }
        };


        // this registers the listener for messages from other servers
        messageNotifier.register("LobbyChannel", requestConsumer);

    }

    public void sendMessageToSMPPlayers(String playerName, String gameName) {
        List<String> lstSockNames = new ArrayList<>();
        lstSockNames.add("smp_0");
        lstSockNames.add("man_trap");
        for (String sockName : lstSockNames) {
            SpigotServerInfo spigotServerInfo = sockExchangeApi.getServerInfo(sockName);
            if (spigotServerInfo == null) {
                System.out.println("server not found " + sockName);
            } else {
                if (spigotServerInfo.isOnline()) {
                    System.out.flush();
                    String inputString = "Lobby.play-message." + playerName + "." + gameName;
                    SockExchangeApi api = SockExchangeApi.instance();
                    byte[] byteArray = inputString.getBytes();
                    api.sendToServer("LobbyChannel", byteArray, sockName);
                } else {
                    System.out.println("server is offline " + sockName);
                }
            }
        }
    }

    // returns the game manager instance
    public GameManager getGameManager() {
        return gameManager;
    }

    public LobbyManager getLobbyManager() {
        return lobbyManager;
    }

    public RuleLobbyManager getRuleLobbyManager() {
        return ruleLobbyManager;
    }

    public BoostPadManager getBoostPadManager() {
        return boostPadManager;
    }

    public SockExchangeApi getSockExchangeApi() {
        return sockExchangeApi;
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        bb_api.getMessageManager().toggleMessageSending(false);
        // messageNotifier.unregister("LobbyChannel", requestConsumer);
    }

    public Bb_api getBb_api() {
        return bb_api;
    }

    public JDA getJda() {
        return jda;
    }

    public static Lobby getInstance() {
        return instance;
    }

    public Game getKitPvP() {
        return kitPvP;
    }

    public void sendPlayerToKitPvP(Player player) {
        if (kitPvP != null) {
            kitPvP.getArena().addPlayerToKitPvP(player);
            LobbyUtils.sendGameJoinMessage(player.getName(), "KitPvP");
            if (jda != null) {
                jda.getGuildById(ConfigManager.getDiscordServer())
                        .getTextChannelById(ConfigManager.getDiscordChannel())
                        .sendMessage("Sending " + player.getName() + " to KitPVP").queue();
            }
        } else {
            player.sendMessage("KitPvP plugin not found on server");
        }
    }

    public boolean isPlayerInKitPvP(Player player) {
        if (kitPvP != null) {
            return kitPvP.getArena().isPlayerInArena(player);
        } else {
            return false;
        }
    }
}
