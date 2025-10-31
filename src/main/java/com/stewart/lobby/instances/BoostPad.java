package com.stewart.lobby.instances;

import com.stewart.lobby.Lobby;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.bukkit.Sound.ENDERDRAGON_WINGS;

public class BoostPad {

    private final Lobby main;
    final private Location startLocation;
    private final double boostDistance;
    final private boolean doPlayerYawCheck;
    final private Vector initialDirection;
    final private Vector ongoingDirection;
    private final List<UUID> boostedPlayers = new ArrayList<>();
    private final float yawToTarget;

    public BoostPad(Lobby lobby, Location locStart, Location locEnd, double yBoost, double boostSpeed,
                    double boostDistanceRatio, boolean doPlayerYawCheck) {
        main = lobby;
        this.startLocation = locStart.clone();
        Location endLocation = locEnd.clone();
        this.doPlayerYawCheck = doPlayerYawCheck;

        // if I don't clone the start location this code adds a yaw/pitch element to it which means
        // when checking the players location against the start location they will never match.
        Location clonedStartLocation = startLocation.clone();
        clonedStartLocation.setDirection(endLocation.toVector().subtract(clonedStartLocation.toVector())); //set the origin's direction to be the direction vector between point A and B.
        yawToTarget = clonedStartLocation.getYaw(); //yay yaw
        // the initial direction vector has a bit of y element to boost the player up a bit
        initialDirection = endLocation.clone().toVector().subtract(startLocation.toVector()).normalize()
                .add(new Vector(0.0,yBoost,0.0))
                .normalize().multiply(boostSpeed);
        // the ongoing direction vector boosts the player horizontally
        ongoingDirection = endLocation.clone().toVector().subtract(startLocation.toVector()).normalize()
                .normalize().multiply(boostSpeed);

        double tripDistance = startLocation.distance(endLocation);
        boostDistance = tripDistance * boostDistanceRatio;
    }

    public void boostPlayer(Player player) {

        // return if player is already boosting
        if (boostedPlayers.contains(player.getUniqueId())) return;

        // return if we need to check the player yaw, and it is not aligned in the direction of boost
        if (!checkPlayerYaw(player)) return;

        // add player to the booster players list so he cant be boosted again - pressure plates will trigger
        // multiple times
        boostedPlayers.add(player.getUniqueId());

        // remove him from this list after 2 seconds
        Bukkit.getScheduler().scheduleSyncDelayedTask(main, () -> {
            boostedPlayers.remove(player.getUniqueId());
        }, 40L); // 40 ticks = 2 seconds

        player.teleport(new Location(startLocation.getWorld(), startLocation.getX() + 0.5, startLocation.getY(), startLocation.getZ() + 0.5, player.getLocation().getYaw(), player.getLocation().getPitch()));

        // run this every tick
        new BukkitRunnable() {
            private int i = 0;
            public void run() {
                // if the player is over the set distance from the start location or the boost has been running for
                // 1 second stop boosting
                double distance = player.getLocation().distance(startLocation);
                //  System.out.println("tick " + i + ", distance from start " + distance + ", boostDistance " + boostDistance);
                if (i >= 20 || distance > boostDistance) {
                    cancel();
                }
                // make a flapping sound for the player every half second
                if (i % 10 == 0) {
                    player.getWorld().playSound(player.getLocation(), ENDERDRAGON_WINGS, 1.0f, 1.0f);
                }
                // for the first 5 ticks boost them in the initial direction which has a y (up) element
                // after that just forwards
                if (i > 5) {
                    player.setVelocity(ongoingDirection);
                } else {
                    player.setVelocity(initialDirection);
                }
                // System.out.println("tick " + i);
                ++i;
            }
        }.runTaskTimer(main, 0L, 1L);

    }

    public Location getStartLocation() {
        return startLocation;
    }

    private boolean checkPlayerYaw(Player player) {
        if (!doPlayerYawCheck) {
            return true;
        }
        float playerYaw = player.getLocation().getYaw();
        return isAngleInRange(playerYaw + 180, yawToTarget + 180 - 90, yawToTarget + 180 + 90);
    }

    private boolean isAngleInRange(double angle, double lowerBound, double upperBound) {
        // Normalize the angle to be within 0 to 360 degrees
        angle = (angle % 360 + 360) % 360;
        lowerBound = (lowerBound % 360 + 360) % 360;
        upperBound = (upperBound % 360 + 360) % 360;
        if (lowerBound <= upperBound) {
            // Normal case: range does not wrap around 0
            return angle >= lowerBound && angle <= upperBound;
        } else {
            // Wrap-around case: range crosses 0 (e.g., 350 to 20)
            // Check if angle is in the first part (350-360) or second part (0-20)
            return angle >= lowerBound || angle <= upperBound;
        }
    }



}
