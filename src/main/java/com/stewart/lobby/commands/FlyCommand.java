package com.stewart.lobby.commands;

import com.stewart.lobby.Lobby;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class FlyCommand  implements CommandExecutor {

    private Lobby main;

    // This is where the 'pw' commands are heard
    //  I don't really have much done via commands
    public FlyCommand(Lobby main) {
        this.main = main;
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] args) {

        if(commandSender instanceof Player) {
            Player player = (Player) commandSender;
            // check player permissions
            if (!player.hasPermission("command.fly")) {
                player.sendMessage("You do not have permission for that command.");
                return true;
            }

            // get the target (either self, or another online player)
            Player target = player;
            if (args.length > 0) {
                target = Bukkit.getPlayerExact(args[0]);
                if (target == null) {
                    player.sendMessage("Player not found/online.");
                    return true;
                }
            }

            // find if target is already in flight mode or not and invert it.
            boolean setFlying = !target.getAllowFlight();

            // toggle flight mode
            target.setAllowFlight(setFlying);

            // if disabling flight, kick target out of fly mode (otherwise they can fly even after toggling off)
            if (!setFlying) {
                target.setFlying(false);
            }

            // notify player of action
            if (setFlying) {
                target.sendMessage("Fly mode toggled ON");
            } else {
                target.sendMessage("Fly mode toggled OFF");
            }

            // notify command sender (if not performing command on self)
            if (target != player) {
                if (setFlying) {
                    player.sendMessage(target.getName() + "'s fly mod toggled ON");
                } else {
                    player.sendMessage(target.getName() + "'s fly mod toggled OFF");
                }
            }

            // finished.
            return true;
        }
        return false;
    }
}