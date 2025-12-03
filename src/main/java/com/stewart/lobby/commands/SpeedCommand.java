package com.stewart.lobby.commands;

import com.stewart.lobby.Lobby;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SpeedCommand implements CommandExecutor {

    private Lobby main;

    // This is where the 'pw' commands are heard
    //  I don't really have much done via commands
    public SpeedCommand(Lobby main) {
        this.main = main;
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] args) {

        if(commandSender instanceof Player player) {
            if (player.hasPermission("group.g100") || player.hasPermission("group.g200") ||
                    player.hasPermission("group.g300") || player.hasPermission("group.g400") ||
                    player.hasPermission("group.g500") || player.hasPermission("group.g600")) {
                main.getLobbyManager().playerUsedSpeedCommand(player);
            } else {
                player.sendMessage(ChatColor.RED + "You do not have permission for that command.");
            }
        }
        return false;
    }
}