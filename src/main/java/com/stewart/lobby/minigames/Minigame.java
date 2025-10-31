package com.stewart.lobby.minigames;

import com.stewart.lobby.Lobby;
import com.stewart.lobby.manager.ConfigManager;
import com.stewart.lobby.utils.Countdown;
import net.minecraft.server.v1_8_R3.IChatBaseComponent;
import net.minecraft.server.v1_8_R3.PacketPlayOutTitle;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Minigame {

    protected List<Player> playersInGame = new ArrayList<>();
    protected Countdown countdown;
    protected Lobby main;


    public Minigame() {
        System.out.println("Minigame class initialized");
        countdown = new Countdown(main, this, 5);
    }

    public void addPlayerToGame(Player player) {
        if (!playersInGame.contains(player)) {
            playersInGame.add(player);
          //  System.out.println("Player " + player.getName() + " added to the game.");
        } else {
            System.out.println("Player " + player.getName() + " is already in the game.");
        }
    }

    public void sendMessage(String message) {
        for (Player player : playersInGame) {
            if (player != null && player.isOnline()) {
                player.sendMessage(message);
            }
        }
    }

    public void sendTitleSubtitle(String titleMessage, String subtitleMessage, String titleColor, String subTitleColour) {
        if (titleColor == null) {
            titleColor = "4"; // dark red
        }
        if (subTitleColour == null) {
            subTitleColour = "6"; // gold
        }
        for (Player player : playersInGame) {
            if (player == null || !player.isOnline()) {
                continue;
            }
            PacketPlayOutTitle title = new PacketPlayOutTitle(PacketPlayOutTitle.EnumTitleAction.TITLE,
                    IChatBaseComponent.ChatSerializer.a("{\"text\":\"" + "§" + titleColor + titleMessage + "\"}"), 5, 100, 10);
            ((CraftPlayer) player).getHandle().playerConnection.sendPacket(title);

            PacketPlayOutTitle subtitle = new PacketPlayOutTitle(PacketPlayOutTitle.EnumTitleAction.SUBTITLE,
                    IChatBaseComponent.ChatSerializer.a("{\"text\":\"" + "§" + subTitleColour + subtitleMessage + "\"}"), 5, 100, 10);
            ((CraftPlayer) player).getHandle().playerConnection.sendPacket(subtitle);
        }
    }

    // send big red text to all players in the arena
    public void playSoundAllPlayers(int countdownSeconds) {
        for (Player player : playersInGame) {
            if (player == null || !player.isOnline()) {
                continue;
            }
            World world = player.getWorld();
            if (countdownSeconds == 30 || countdownSeconds == 20 || countdownSeconds == 15 || countdownSeconds == 10) {
                world.playSound(player.getLocation(), Sound.NOTE_PLING, 1, (float) 1);
            }
            if (countdownSeconds < 6) {
                world.playSound(player.getLocation(), Sound.NOTE_PLING, 1, (float) (1.916667 - (0.0833333 * countdownSeconds)));
                world.playSound(player.getLocation(), Sound.NOTE_PLING, 1, (float) 1);
            }
        }
    }

    public void playGameStartSounds() {
        for (Player player : playersInGame) {
            World world = player.getWorld();
            Location location = player.getLocation();
            world.playSound(location, Sound.NOTE_PLING, 1, (float) 2);
            world.playSound(location, Sound.NOTE_PLING, 1, (float) 1);
            world.playSound(location, Sound.ENDERDRAGON_GROWL, 1, (float) 1);
        }
    }

    public void start() {
    }

    public void gameEnd(boolean playerWon) {
    }

    public void setLobbyInstance(Lobby main) {
        System.out.println("----------------------------Setting lobby instance in Minigame class.");
        if (main == null) {
            throw new IllegalArgumentException("Lobby instance cannot be null.");
        }
        this.main = main;
    }

    public void freezePlayers(boolean freeze) {
        for (Player player : playersInGame) {
            if (player != null && player.isOnline()) {
                if (freeze) {
                    player.setWalkSpeed(0f);
                } else {
                    player.setWalkSpeed(0.2f);
                }
            }
        }
    }

    public void countDownTick() {
    }
}
