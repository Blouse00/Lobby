package com.stewart.lobby.listeners;

import com.stewart.lobby.Lobby;
import com.stewart.lobby.manager.ConfigManager;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;


public class ConnectListener implements Listener {
    private Lobby lobby;

    public ConnectListener(Lobby lobby) {
        this.lobby = lobby;
    }

    // when a player joins the server tp them to the main spawn location.
    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        Player player = e.getPlayer();
        player.getInventory().clear();
        player.setGameMode(GameMode.SURVIVAL);
        Location location = ConfigManager.getLobbySpawn();
        player.teleport(location);
    }

}
