package com.stewart.lobby.commands;

import com.stewart.lobby.Lobby;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class JumpCommand implements CommandExecutor {

    private Lobby main;

    // This is where the 'pw' commands are heard
    //  I don't really have much done via commands
    public JumpCommand(Lobby main) {
        this.main = main;
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] args) {

        if(commandSender instanceof Player player) {
            if (player.hasPermission("rank.jump")) {
                main.getLobbyManager().playerUsedJumpCommand(player);
            } else {
                player.sendMessage(ChatColor.RED + "You do not have permission for that command.");
            }
        }
        return false;
    }
}