package com.stewart.lobby.commands;

import com.stewart.lobby.Lobby;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.stewart.bb_api.manager.PlayerManager;
import redis.clients.jedis.JedisPooled;

public class BBCrate  implements CommandExecutor {

    private Lobby main;

    public BBCrate(Lobby main) {
        this.main = main;
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] args) {

        // this command is used to detect when the votifier plugin gives a reward for voting for all
        // sites in a single day

        // /bbcrate playername amount (amount is optional, defaults to 1)

        Player player = null;

        if(commandSender instanceof Player)  {
            player = (Player) commandSender;
            if (!player.isOp()) {
                return true;
            }
        }

        if (args.length == 0) {
            if (player != null) {
                player.sendMessage("Invalid command use: must specify player name.");
            }
            System.out.println("bbcrate command used without player name argument");
            return true;
        }
        int amount = 1;

        Player b = Bukkit.getPlayer(args[0]);
        if (b == null) {
            if (player != null) {
                player.sendMessage("Player " + args[0] + " not found/online, giving key regardless.");
            }
            System.out.println("bbcrate command used with player name " + args[0] + " argument that is not online, giving key regardless.");
            return true;
        }

        if (args.length == 2) {
            // if arg[1] is an integer, set amount to that, otherwise return
            try {
                amount = Integer.parseInt(args[1]);
            } catch (NumberFormatException e) {
                if (player != null) {
                    player.sendMessage("Invalid command use: argument must be an integer.");
                }
            }
        }

        // a different redis database (2) for player keys, this one should never be flushed
        JedisPooled jedisPlayerKeys = new JedisPooled("redis://localhost:6379/2");
        String key = "bbcratekeys";
        Double currentScore = jedisPlayerKeys.zscore(key, args[0]);
        if (currentScore == null) {
            jedisPlayerKeys.zadd(key, amount, args[0]);//ZADD
        } else {
            jedisPlayerKeys.zincrby(key, amount, args[0]);//ZADD
        }
        if (player != null) {
            player.sendMessage(ChatColor.GREEN + "Gave " + amount + " crate key(s) to player " + args[0] + ". They now have " + (currentScore == null ? amount : currentScore + amount) + " crate key(s).");
        }
        System.out.println("Gave " + amount + " crate key(s) to player " + args[0] + ". They now have " + (currentScore == null ? amount : currentScore + amount) + " crate key(s).");
        jedisPlayerKeys.close();

        return false;
    }
}