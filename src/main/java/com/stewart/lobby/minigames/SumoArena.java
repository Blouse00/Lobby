package com.stewart.lobby.minigames;

import com.stewart.lobby.Lobby;
import org.bukkit.Location;

public class SumoArena {
    private Location spawn1;
    private Location spawn2;
    private int yMin;
    private boolean inUse = false;
    private SumoMiniGame sumoMiniGame;
    private String arenaId;

    public SumoArena(String id, Location spawn1, Location spawn2, int yMin, Lobby lobby) {
        this.yMin = yMin;
        this.spawn1 = spawn1;
        this.spawn2 = spawn2;
        this.arenaId = id;
        sumoMiniGame = new SumoMiniGame( lobby, this);
    }

    public Location getSpawn1() {
        return spawn1;
    }
    public Location getSpawn2() {
        return spawn2;
    }
    public int getyMin() {
        return yMin;
    }
    public boolean isInUse() {
        return inUse;
    }
    public void setInUse(boolean inUse) { this.inUse = inUse;}
    public String getArenaId() { return arenaId; }
    public SumoMiniGame getSumoMiniGame() { return sumoMiniGame; }
}
