package com.stewart.lobby.listeners;

import com.stewart.lobby.Lobby;
import com.stewart.lobby.manager.ConfigManager;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.UUID;


public class ConnectListener implements Listener {
    private Lobby lobby;

    public ConnectListener(Lobby lobby) {
        this.lobby = lobby;
    }

    // when a player joins the server tp them to the main spawn location.
    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        Player player = e.getPlayer();
        lobby.getLobbyManager().playerJoined(player);

    }

    // when a player joins the server tp them to the main spawn location.
    @EventHandler
    public void onLeave(PlayerQuitEvent e) {
        UUID uuid = e.getPlayer().getUniqueId();
        lobby.getGameManager().removePlayerFromQueues(uuid, null);
    }

}
