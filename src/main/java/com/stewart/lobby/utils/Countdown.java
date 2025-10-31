package com.stewart.lobby.utils;

import com.stewart.lobby.Lobby;
import com.stewart.lobby.minigames.Minigame;
import org.bukkit.ChatColor;
import org.bukkit.scheduler.BukkitRunnable;

public class Countdown extends BukkitRunnable {

    private final Lobby main;
    private final Minigame minigame;
    private int countdownSeconds;
    private boolean isRunning;

    // the countdown class for the lobby, should be fairy self-explanatory,
    // pretty much as used in the udemy course
    public Countdown(Lobby main, Minigame minigame, int countdownSeconds) {
        this.main = Lobby.getInstance();
        this.minigame = minigame;
        this.countdownSeconds = countdownSeconds;
        this.isRunning = false;
    }

    // start the countdown
    public void start() {
        runTaskTimer(main, 0, 20);
        isRunning = true;
    }



    public boolean isRunning() { return this.isRunning;}

    // fires every tick of the countdown
    @Override
    public void run() {
        minigame.countDownTick();
        if (countdownSeconds == 0) {
            minigame.sendTitleSubtitle("Go!", "", null, null);
            minigame.playGameStartSounds();
            cancel();
            minigame.start();
            return;
        }

        // for the last 5 seconds have a big number on the screen and a chat message.
        if (countdownSeconds <= 5 && countdownSeconds > 0) {
            minigame.sendMessage(ChatColor.GREEN + "Game will start in " + countdownSeconds + " second" + (countdownSeconds ==1 ? "." : "s."));
            minigame.sendTitleSubtitle(String.valueOf(countdownSeconds), "", String.valueOf(countdownSeconds), null);
            minigame.playSoundAllPlayers(countdownSeconds);
        }


        countdownSeconds --;
    }
}
