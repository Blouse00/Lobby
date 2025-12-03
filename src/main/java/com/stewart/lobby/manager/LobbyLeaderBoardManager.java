package com.stewart.lobby.manager;

import com.stewart.lobby.Lobby;
import com.stewart.lobby.instances.LobbyLeaderboard;
import org.bukkit.configuration.file.YamlConfiguration;
import redis.clients.jedis.JedisPooled;
import redis.clients.jedis.UnifiedJedis;

import javax.persistence.Lob;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class LobbyLeaderBoardManager {

    List<LobbyLeaderboard> leaderboards;
    List<String> lstRedisKeys;
    private Lobby main;
    YamlConfiguration locationConfig;
    int index = 0;
    int updateSeconds = 5;

    public LobbyLeaderBoardManager(Lobby main) {

        this.main = main;

        leaderboards = new ArrayList<>();

        File file = new File(main.getDataFolder(), "leaderboardlocations.yml");
        locationConfig =  YamlConfiguration.loadConfiguration(file);

        lstRedisKeys = new ArrayList<>();
        lstRedisKeys.add("a_ac_the_ancients_time");
        lstRedisKeys.add("a_ac_adversity_time");
        lstRedisKeys.add("a_ac_underwurld_time");
        lstRedisKeys.add("a_ac_wither_time");
        lstRedisKeys.add("a_ac_urban_decay_time");
        lstRedisKeys.add("a_ac_hells_teeth_time");
        lstRedisKeys.add("a_ac_blaze_of_glory_time");
        lstRedisKeys.add("a_ac_soul_miner_time");
        lstRedisKeys.add("a_ac_pressure_time");
        lstRedisKeys.add("a_ac_kills");
        lstRedisKeys.add("a_ac_wins");
        lstRedisKeys.add("a_ac_wins_games_ratio");
        lstRedisKeys.add("a_fi_best_time");
        lstRedisKeys.add("a_fi_wins");
        lstRedisKeys.add("a_fi_wins_games_ratio");
        lstRedisKeys.add("a_bw_wins");
        lstRedisKeys.add("a_bw_beds");
        lstRedisKeys.add("a_bw_wins_games_ratio");
        lstRedisKeys.add("a_bw_kills");
        lstRedisKeys.add("a_iw_wins");
        lstRedisKeys.add("a_iw_wins_games_ratio");
        lstRedisKeys.add("a_iw_kills");
        lstRedisKeys.add("a_mm_average");
        lstRedisKeys.add("a_mm_wins_ep");
        lstRedisKeys.add("a_mm_premier_scores");
        lstRedisKeys.add("a_mm_diadem_scores");
        lstRedisKeys.add("a_mt_1_scores");
        lstRedisKeys.add("a_mt_2_scores");



        for (String redisKey : lstRedisKeys) {
           // System.out.println("Adding key " + redisKey + " to leaderboards");
            for (String s : locationConfig.getConfigurationSection(redisKey).getKeys(false)) {
                int startX = locationConfig.getInt(redisKey + "." + s + ".c1.x");
                int startY = locationConfig.getInt(redisKey + "." + s + ".c1.y");
                int startZ = locationConfig.getInt(redisKey + "." + s + ".c1.z");
                int endX = locationConfig.getInt(redisKey + "." + s + ".c2.x");
                int endY = locationConfig.getInt(redisKey + "." + s + ".c2.y");
                int endZ = locationConfig.getInt(redisKey + "." + s + ".c2.z");
                leaderboards.add(new LobbyLeaderboard(redisKey, startX, startY, startZ, endX, endY, endZ));
            }
        }


       /* UnifiedJedis jedis = new UnifiedJedis(System.getenv().getOrDefault("REDIS_URL", "redis://localhost:6379"));
        for (LobbyLeaderboard lb : leaderboards) {
            lb.updateLeaderboard(jedis);
        }*/





    }

    public void updateNextLeaderboard() {
        if (leaderboards.isEmpty()) {
            return;
        }
        if (updateSeconds >= 5) {
            if (index >= leaderboards.size()) {
                index = 0;
            }
         //   System.out.println("Updating lobby leaderboard  update seconds = " + updateSeconds + " index = " + index + " of " + leaderboards.size());
          //  UnifiedJedis jedis = new UnifiedJedis(System.getenv().getOrDefault("REDIS_URL", "redis://localhost:6379"));
            JedisPooled jedis = new JedisPooled("localhost", 6379);
            leaderboards.get(index).updateLeaderboard(jedis);
            jedis.close();
            index++;
            updateSeconds = 0;
        } else {
            updateSeconds++;
        }
    }


}
