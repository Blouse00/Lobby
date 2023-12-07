package com.stewart.lobby.instances;

import com.stewart.lobby.Lobby;
import com.stewart.lobby.manager.ConfigManager;
import com.stewart.lobby.manager.RuleLobbyManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class PlayerRules {

    private Lobby main;
    private UUID uuid;
    private BukkitTask task;
    private int messageInterval;
    private RuleLobbyManager ruleLobbyManager;
private int iterator;
    public PlayerRules(UUID uuid, Lobby lobby, RuleLobbyManager ruleLobbyManager) {
        this.uuid = uuid;
        this.main = lobby;
        this.ruleLobbyManager = ruleLobbyManager;
        this.iterator = 0;
        this.messageInterval = ConfigManager.getRulesInterval();
        startRules();
    }

    private void startRules() {
        this.task = new BukkitRunnable() {
            @Override
            public void run() {
                if (iterator >= ruleLobbyManager.getLstRules().size() ) {
                    iterator = 0;
                }
                Player player = Bukkit.getPlayer(uuid);
                String alt = ChatColor.DARK_BLUE + player.getDisplayName() + "" + ChatColor.GOLD;
                String msg =  ruleLobbyManager.getLstRules().get(iterator).replace("{playerName}", alt);
                player.sendMessage(msg);
                // increment the itereator so next time will show the next message.
                iterator += 1;
            }
        }.runTaskTimer(main, 1, messageInterval);
    }

    public void stopRules() {
        if (this.task != null) {
            task.cancel();
        }
    }

    public UUID getUuid() { return this.uuid;}


}
