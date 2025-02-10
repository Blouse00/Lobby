package com.stewart.lobby.manager;

import com.stewart.lobby.Lobby;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.List;

// simple config manager all it does is get the server spawn location from the config file
public class ConfigManager {

    private static FileConfiguration config;

    public static void setupConfig(Lobby lobby) {
        ConfigManager.config = lobby.getConfig();
        lobby.saveDefaultConfig();
    }

    public static boolean getAnnounceGameJoin() {
        return config.getBoolean("announce-game-join");
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

    public  static Location getLobbyReSpawn() {
        return new Location(
                Bukkit.getWorld(config.getString("lobby-respawn.world")),
                config.getDouble("lobby-respawn.x"),
                config.getDouble("lobby-respawn.y"),
                config.getDouble("lobby-respawn.z"),
                (float) config.getDouble("lobby-respawn.yaw"),
                (float) config.getDouble("lobby-respawn.pitch"));
    }

    public  static Location getRulesSpawn() {
        return new Location(
                Bukkit.getWorld(config.getString("rules-spawn.world")),
                config.getDouble("rules-spawn.x"),
                config.getDouble("rules-spawn.y"),
                config.getDouble("rules-spawn.z"),
                (float) config.getDouble("rules-spawn.yaw"),
                (float) config.getDouble("rules-spawn.pitch"));
    }

    public  static Location getRulesNPCSpawn() {
        return new Location(
                Bukkit.getWorld(config.getString("rulesNPC-spawn.world")),
                config.getDouble("rulesNPC-spawn.x"),
                config.getDouble("rulesNPC-spawn.y"),
                config.getDouble("rulesNPC-spawn.z"),
                (float) config.getDouble("rulesNPC-spawn.yaw"),
                (float) config.getDouble("rulesNPC-spawn.pitch"));
    }

    public  static int getRulesInterval() {
        return config.getInt("rules-interval");
    }

    public static String getDiscordToken() {
        return config.getString("discord.token");
    }

    public static String getDiscordServer() {
        return config.getString("discord.server");
    }

    public static String getDiscordChannel() {
        return config.getString("discord.channel");
    }

    public static List<String> getRules() {
        List<String> test1 = config.getStringList("rules");
        return test1;
    }

    public static List<String> getPreRules() {
        List<String> test1 = config.getStringList("rules-pre");
        return test1;
    }

    public  static String getRulesSkinTexture() {
        return config.getString("rules-npc-texture");
    }

    public  static String getRulesSkinSignature() {
        return config.getString("rules-npc-signature");
    }

    public  static String getVotesSkinTexture() {
        return config.getString("votes-npc-texture");
    }

    public  static String getVotesSkinSignature() {
        return config.getString("votes-npc-signature");
    }

    public  static String getDiscordSkinTexture() {
        return config.getString("discord-npc-texture");
    }

    public  static String getDiscordSkinSignature() {
        return config.getString("discord-npc-signature");
    }

    public static List<String> getPostRules() {
        List<String> test1 = config.getStringList("rules-post");
        return test1;
    }


    public  static Location getParkourSpawn() {
        return new Location(
                Bukkit.getWorld(config.getString("lobby-spawn.world")),
                config.getDouble("parkour.x"),
                config.getDouble("parkour.y"),
                config.getDouble("parkour.z"),
                (float) config.getDouble("parkour.yaw"),
                (float) config.getDouble("parkour.pitch"));
    }

}
