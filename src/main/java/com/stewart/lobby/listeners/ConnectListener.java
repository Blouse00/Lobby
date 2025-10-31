package com.stewart.lobby.listeners;

import com.stewart.lobby.Lobby;


import com.stewart.lobby.manager.ConfigManager;
import com.stewart.lobby.utils.LobbyUtils;
import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.ViaAPI;
import org.bukkit.Bukkit;
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
        player.setWalkSpeed(0.2f);
        lobby.getLobbyManager().addToNoPvpList(player.getUniqueId());
        e.setJoinMessage("");

        if (lobby.getBb_api().getPlayerManager().getCustomPlayer(player.getUniqueId()).getRulesAccepted() == null) {
            System.out.println("is new player");
            lobby.getRuleLobbyManager().addPlayer(player.getUniqueId());
        } else {
            lobby.getLobbyManager().playerJoined(player);
            System.out.println("has already accepted the rules");
        }

        Bukkit.getScheduler().scheduleSyncDelayedTask(lobby, new Runnable() {
            @Override
            public void run() {
                sendPlayerVersionToDiscordOnJoin(player);
            }
        }, 20L);
    }

    private void sendPlayerVersionToDiscordOnJoin(Player player) {
        if (player != null) {
            ViaAPI api = Via.getAPI(); // Get the API
            int version = api.getPlayerVersion(player); // Get the protocol version
            String strVersion = LobbyUtils.getMinecraftVersionFromVIAProtocol(version);
            Bukkit.getScheduler().scheduleSyncDelayedTask(lobby, () -> addAnticheatBypassPermission(version == 770, player), 20L);


            String versionMessage = "";
            if (strVersion.equals("")) {
                versionMessage = "Protocol number " + version + " MC version not found - player name " + player.getName() + " joined Lobby";
            } else {
                versionMessage = "MC version " + strVersion + ", protocol number " + version + ", player name " + player.getName() + " joined Lobby";
            }
            if (lobby.getJda() != null) {
                lobby.getJda().getGuildById(ConfigManager.getDiscordServer())
                        .getTextChannelById(ConfigManager.getDiscordChannel())
                        .sendMessage(versionMessage).queue();
            }
        }
    }

    private void addAnticheatBypassPermission(boolean add, Player player) {
        if (player != null && player.hasPermission("grim.exempt")) {
            System.out.printf("Player " + player.getName() + " already has bypass permission");
        }
        if (add && player!= null && !player.hasPermission("grim.exempt")) {
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "lp user " + player.getName() + " permission set grim.exempt true");
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "lp user " + player.getName() + " permission set vulcan.bypass true");
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "lp user " + player.getName() + " permission unset vulkan.bypass");
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "lp sync");
            System.out.printf("add bypass permission to player " + player.getName());
        }
        if (!add && player!= null && player.hasPermission("grim.exempt")) {
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "lp user " + player.getName() + " permission unset grim.exempt");
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "lp user " + player.getName() + " permission unset vulcan.bypass");
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "lp user " + player.getName() + " permission unset vulkan.bypass");
            System.out.printf("remove bypass permission from player " + player.getName());
        }
    }

    // when a player joins the server tp them to the main spawn location.
    @EventHandler
    public void onLeave(PlayerQuitEvent e) {
        UUID uuid = e.getPlayer().getUniqueId();
        lobby.getGameManager().removePlayerFromQueues(uuid, null);
        lobby.getRuleLobbyManager().removePlayer(uuid);
        e.setQuitMessage("");
    }

}
