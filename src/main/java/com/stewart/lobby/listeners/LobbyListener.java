package com.stewart.lobby.listeners;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import com.stewart.lobby.Lobby;
import com.stewart.lobby.manager.PortalManager;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;

import java.io.IOException;


// event for when player clicks on a sign
public class LobbyListener implements Listener {

    private Lobby lobby;

    public LobbyListener(Lobby lobby) {
        this.lobby = lobby;
    }

    @EventHandler
    public void onSignClick(PlayerInteractEvent e) {

        if (e.hasBlock()  && e.getClickedBlock().getType().equals(Material.WALL_SIGN)) {
            PortalManager sign = lobby.getGameManager().getSignPost(e.getClickedBlock().getLocation());
            if (sign != null) {
                // They have clicked on a sign
                // check the game state is recruiting
                if (sign.getIsFull() == false) {
                    try {
                        ByteArrayDataOutput out = ByteStreams.newDataOutput();
                        out.writeUTF("Connect");
                        // get server name from the sign
                        out.writeUTF(sign.getServerName());
                        // teleport the player to the game server, this is done via the bungeecord channel
                        e.getPlayer().sendPluginMessage(lobby, "BungeeCord", out.toByteArray());
                    } catch (Exception ex) {
                        e.getPlayer().sendMessage(ChatColor.RED + "There was a problem connecting you to that game.  Please try again later!");
                    }
                }
            }
        }
    }

    // prevent any blocks being broken
    @EventHandler
    public void onBlockBreak(BlockBreakEvent e) throws IOException {
        // no block break
        e.setCancelled(true);
    }

    // prevent any blocks being placed
    @EventHandler
    public void onBlockPlace(BlockPlaceEvent e) throws IOException {
        // no block break
        e.setCancelled(true);
    }

    // prevent any blocks being placed
    @EventHandler
    public void onSpawn(CreatureSpawnEvent e) throws IOException {
        // no block break
        e.setCancelled(true);
        System.out.println("Creature spawn cancelled");
    }

    // prevent player dropping things with q button
    @EventHandler
    public void onPlayerDropItem(PlayerDropItemEvent event){
            event.setCancelled(true);
    }

}
