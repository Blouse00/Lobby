package com.stewart.lobby.manager;

import com.stewart.lobby.Lobby;
import com.stewart.lobby.instances.BoostPad;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class BoostPadManager {

    private final Lobby main;
    private List<BoostPad> boostPadList = new ArrayList<>();

    public BoostPadManager(Lobby lobby) {
        this.main = lobby;
        initializeBoostPads();
    }

    private void initializeBoostPads() {
        // Implementation for initializing boost pads
        Location startLocation1 = new Location(Bukkit.getWorld("world"), -28, 46, -33);
        Location endLocation1 = new Location(Bukkit.getWorld("world"), -100, 46, -69);
        boostPadList.add(new BoostPad(main, startLocation1, endLocation1,0.1, 3.9, 0.6,
                true));
        boostPadList.add(new BoostPad(main, endLocation1, startLocation1,0.1, 3.9, 0.6,
                true));

        Location startLocation2 = new Location(Bukkit.getWorld("world"), -22, 46, -40);
        Location endLocation2 = new Location(Bukkit.getWorld("world"), -67, 46, -85);
        boostPadList.add(new BoostPad(main, startLocation2, endLocation2,0.1, 3.9, 0.6,
                true));
        boostPadList.add(new BoostPad(main, endLocation2, startLocation2,0.1, 3.9, 0.6,
                true));

        Location startLocation3 = new Location(Bukkit.getWorld("world"), -10, 46, -42);
        Location endLocation3 = new Location(Bukkit.getWorld("world"), -33, 46, -96);
        boostPadList.add(new BoostPad(main, startLocation3, endLocation3,0.1, 3.9, 0.6,
                true));
        boostPadList.add(new BoostPad(main, endLocation3, startLocation3,0.1, 3.9, 0.6,
                true));

        Location startLocation4 = new Location(Bukkit.getWorld("world"), 0, 45, -49);
        Location endLocation4 = new Location(Bukkit.getWorld("world"), 0, 46, -101);
        boostPadList.add(new BoostPad(main, startLocation4, endLocation4,0.1, 3.9, 0.4,
                true));
        boostPadList.add(new BoostPad(main, endLocation4, startLocation4,0.1, 3.9, 0.6,
                true));

        Location startLocation5 = new Location(Bukkit.getWorld("world"), 10, 46, -42);
        Location endLocation5 = new Location(Bukkit.getWorld("world"), 33, 46, -96);
        boostPadList.add(new BoostPad(main, startLocation5, endLocation5,0.1, 3.9, 0.6,
                true));
        boostPadList.add(new BoostPad(main, endLocation5, startLocation5,0.1, 3.9, 0.6,
                true));

        Location startLocation6 = new Location(Bukkit.getWorld("world"), 21, 46, -39);
        Location endLocation6 = new Location(Bukkit.getWorld("world"), 70, 46, -88);
        boostPadList.add(new BoostPad(main, startLocation6, endLocation6,0.1, 3.9, 0.6,
                true));
        boostPadList.add(new BoostPad(main, endLocation6, startLocation6,0.1, 3.9, 0.6,
                true));

        Location startLocation7 = new Location(Bukkit.getWorld("world"), 27, 46, -33);
        Location endLocation7 = new Location(Bukkit.getWorld("world"), 99, 46, -69);
        boostPadList.add(new BoostPad(main, startLocation7, endLocation7,0.1, 3.9, 0.6,
                true));
        boostPadList.add(new BoostPad(main, endLocation7, startLocation7,0.1, 3.9, 0.6,
                true));
    }

    public void checkPlayerBoostPads(Location padLocation, Player player) {
        for (BoostPad boostPad : boostPadList) {
            // System.out.println("pad loc = " + boostPad.getStartLocation().toString());
            if (padLocation.equals(boostPad.getStartLocation())) {
                boostPad.boostPlayer(player);
            }
        }
    }
}
