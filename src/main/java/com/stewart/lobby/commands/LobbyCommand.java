package com.stewart.lobby.commands;


import com.stewart.lobby.Lobby;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class LobbyCommand implements CommandExecutor {

    private Lobby main;

    // This is where the 'pw' commands are heard
    //  I don't really have much done via commands
    public LobbyCommand(Lobby main) {
        this.main = main;
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] args) {

        if(commandSender instanceof Player) {
            Player player = (Player) commandSender;
            // this is for updating a team summoner speed, I used it for testing
            // eg "/pw team red summoner g" - thats the 4 args (/pw isn't counted)
            if (args.length == 1 && args[0].equalsIgnoreCase("die")) {
                main.getLobbyManager().playerKilled(player);
            } else {
                player.sendMessage("Invalid command use:");
            }
        }
        return false;
    }
}


