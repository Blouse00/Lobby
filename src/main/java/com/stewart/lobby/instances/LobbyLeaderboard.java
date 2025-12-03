package com.stewart.lobby.instances;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.stewart.bb_api.instance.Leaderboard;
import redis.clients.jedis.UnifiedJedis;
import redis.clients.jedis.resps.Tuple;

import javax.swing.text.BadLocationException;
import java.text.DecimalFormat;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class LobbyLeaderboard {
    private final int startX;
    private final int startY;
    private final int startZ;
    private final int endX;
    private final int endY;
    private final int endZ;
    private final String redisKey;


    public LobbyLeaderboard(String redisKey, int startX, int startY, int startZ, int endX, int endY, int endZ) {
        this.startX = startX;
        this.startY = startY;
        this.startZ = startZ;
        this.endX = endX;
        this.endY = endY;
        this.endZ = endZ;
        this.redisKey = redisKey;
    }

    public void updateLeaderboard(UnifiedJedis jedis) {
      //  System.out.println("Updating lobby leaderboard for key " + redisKey);
        boolean samex = startX == endX;
        boolean reverse = false;
        if (samex) {
            if (startZ > endZ) {
                reverse = true;
            }
        } else {
            if (startX > endX) {
                reverse = true;
            }
        }

        List<Tuple> elements;
        if (redisKey.contains("_time")) {
            // times not backwards
            elements = jedis.zrangeWithScores(redisKey, 0, (25 - 1));
        } else {
            elements = jedis.zrevrangeWithScores(redisKey, 0, (25 - 1));
        }
        // traps game mt leaderboards split into 2 parts with 15 sections each
        if (redisKey.equals("a_mt_1_scores")) {
            elements = jedis.zrevrangeWithScores("a_mt_scores", 0, 14);
            System.out.println("Updating lobby leaderboard for key a_mt_1_scores got " + elements.size() + " elements");
        } else if (redisKey.equals("a_mt_2_scores")) {
            elements = jedis.zrevrangeWithScores("a_mt_scores", 15, 29);
            System.out.println("Updating lobby leaderboard for key a_mt_2_scores got " + elements.size() + " elements");
        }



       // System.out.println("samex=" + samex + " reverse=" + reverse + " x = " + startX + " to " + endX + " z = " + startZ + " to " + endZ + " y = " + startY + " to " + endY);
        int i = 0;
        for (int y = startY; y >= endY; y--) {
          //  System.out.println("looping  y=" + y + " from " + startY + " to " + endY);
            if (samex) {
                if (!reverse) {
                    for (int z = startZ; z <= endZ; z++) {
                        Location location = new Location(Bukkit.getWorld("world"), startX, y, z);
                        Block block = location.getBlock();
                        if (i >= elements.size()) {
                            updateSign(null, block, i);
                            i++;
                            continue;
                        }
                        updateSign(elements.get(i), block, i);
                        i ++;
                    }
                } else {
                    for (int z = startZ; z >= endZ; z--) {
                        Location location = new Location(Bukkit.getWorld("world"), startX, y, z);
                        Block block = location.getBlock();
                        if (i >= elements.size()) {
                            updateSign(null, block, i);
                            i++;
                            continue;
                        }
                        updateSign(elements.get(i), block, i);
                        i ++;
                    }
                }
            } else {
                if (!reverse) {
                    for (int x = startX; x <= endX; x++) {
                        Location location = new Location(Bukkit.getWorld("world"), x, y, startZ);
                        Block block = location.getBlock();
                        if (i >= elements.size()) {
                            updateSign(null, block, i);
                            i++;
                            continue;
                        }
                        updateSign(elements.get(i), block, i);
                        i ++;
                    }
                } else {
                    for (int x = startX; x >= endX; x--) {
                        Location location = new Location(Bukkit.getWorld("world"), x, y, startZ);
                        Block block = location.getBlock();
                        if (i >= elements.size()) {
                            updateSign(null, block, i);
                            i++;
                            continue;
                        }
                        updateSign(elements.get(i), block, i);
                        i ++;
                    }
                }
            }
        }
    }

  /*  $4 - Dark Red
    $c - Red
    $6 - Orange
    $e - Yellow
    $2 - Dark Green
    $a - Green
    $b - Cyan
    $3 - Aqua
    $1 - Dark Blue
    $9 - Blue
    $d - Pink
    $5 - Purple
    $f - White
    $7 - Gray
    $8 - Dark Gray
    $0 - Black

    $l - Bold
    $0 - Italic
    $m - Strikethrough
    $n - Underline
    $r - Default font and color*/

    private void updateSign(Tuple element,  Block block, int index) {
       // System.out.println("Updating sign at location " + block.getLocation().toString());
        BlockState blockState = block.getState();
        if (blockState instanceof org.bukkit.block.Sign) {
         //   System.out.println("Block is a sign at location ");
            org.bukkit.block.Sign sign = (org.bukkit.block.Sign) blockState;
            // The block is a sign, you can now work with the 'sign' object
            if (element == null) {
              //  System.out.println("No element for index " + index);
               // ChatColor.translateAlternateColorCodes('$',  "$c" +  String.valueOf(index + 1 ));
               // sign.setLine(0,  String.valueOf(index + 1 ));;
                sign.setLine(0,  "");
                sign.setLine(2, "");
                sign.setLine(3, "");
                sign.update(); // Example action
            } else {
              //  System.out.println("Updating sign for element " + element.getElement() + " with score " + element.getScore());
                sign.setLine(0,  ChatColor.translateAlternateColorCodes('$',  "$1" +  ordinal(index + 1)));
                sign.setLine(1,    ChatColor.translateAlternateColorCodes('$',  "$c" + element.getElement()));
                sign.setLine(2, "");
                sign.setLine(3, ChatColor.translateAlternateColorCodes('$',  "$2" + getFormattedScore(element.getScore(), redisKey)));
                sign.update(); // Example action
            }
        } else  {
            System.out.println("Block is not a sign at location " + block.getLocation().toString());
        }
    }

    public static String ordinal(int i) {
        String[] suffixes = new String[] { "th", "st", "nd", "rd", "th", "th", "th", "th", "th", "th" };
        switch (i % 100) {
            case 11:
            case 12:
            case 13:
                return i + "th";
            default:
                return i + suffixes[i % 10];
        }
    }


    private String getFormattedScore(double dScore, String redisKey) {

        if (redisKey.contains("_time")) {
            long intMillis = (long) dScore;
            long hh = TimeUnit.MILLISECONDS.toHours(intMillis);
            intMillis -= TimeUnit.HOURS.toMillis(hh);
            long mm = TimeUnit.MILLISECONDS.toMinutes(intMillis);
            intMillis -= TimeUnit.MINUTES.toMillis(mm);
            long ss = TimeUnit.MILLISECONDS.toSeconds(intMillis);
            intMillis -= TimeUnit.SECONDS.toMillis(ss);
            if (hh == 0) {
                String stringInterval = "%02d:%02d.%03d";
                return String.format(stringInterval , mm, ss, intMillis);
            } else {
                String stringInterval = "%02d:%02d:%02d.%03d";
                return String.format(stringInterval , hh, mm, ss, intMillis);
            }
        }

        if (redisKey.contains("wins_games_ratio")) {
            // need to divide the score by 100 into a double
            DecimalFormat format = new DecimalFormat("0.##");
            return  format.format(dScore/100f) + "%";
        }

        DecimalFormat format = new DecimalFormat("0.#");
        if (redisKey.contains("_kills")) {
            return  format.format(dScore) + " Kills";
        }
        if (redisKey.contains("_beds")) {
            return  format.format(dScore) + " Beds";
        }

        if (redisKey.contains("wins_ep")) {
            return  format.format(dScore) + " EP Wins";
        }

        if (redisKey.contains("_wins")) {
            return  format.format(dScore) + " Wins";
        }

        if (redisKey.contains("_score")||redisKey.contains("_average")) {
            return  format.format(dScore) + " Points";
        }
        return format.format(dScore);
    }



}
