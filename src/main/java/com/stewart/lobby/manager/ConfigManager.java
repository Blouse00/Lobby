package com.stewart.lobby.manager;

import com.stewart.lobby.Lobby;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;

// simple config manager all it does is get the server spawn location from the config file
public class ConfigManager {

    private static FileConfiguration config;

    public static void setupConfig(Lobby lobby) {
        ConfigManager.config = lobby.getConfig();
        lobby.saveDefaultConfig();
    }

    public  static Location getLobbySpawn() {
        return new Location(
                Bukkit.getWorld(config.getString("lobby-spawn.world")),
                config.getDouble("lobby-spawn.x"),
                config.getDouble("lobby-spawn.y"),
                config.getDouble("lobby-spawn.z"),
                (float) config.getDouble("lobby-spawn.yaw"),
                (float) config.getDouble("lobby-spawn.pitch"));
    }

}
