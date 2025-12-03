package com.stewart.lobby.utils;

import com.stewart.lobby.Lobby;
import io.netty.buffer.Unpooled;
import net.minecraft.server.v1_8_R3.PacketDataSerializer;
import net.minecraft.server.v1_8_R3.PacketPlayOutCustomPayload;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scoreboard.ScoreboardManager;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

public class LobbyUtils {

    public static ItemStack comsticsMenuItem() {
        ItemStack is = new ItemStack(Material.GOLD_INGOT);
        ItemMeta im = is.getItemMeta();
        im.setDisplayName(ChatColor.GOLD + "Cosmetics");
        im.setLore(Arrays.asList(
                ChatColor.GRAY + "Click to open the cosmetics menu."
        ));
        return is;

    }

    public static void sendGameJoinMessage(String playerName, String gameName) {
        String broadCastMessage = ChatColor.DARK_BLUE + "■" + ChatColor.GOLD + "BashyBashy" + ChatColor.DARK_BLUE + "■ " +
                ChatColor.GREEN + playerName + ChatColor.DARK_BLUE + " ▶▶▶ " + ChatColor.GREEN + "Joined " +
                ChatColor.DARK_BLUE + "▶▶▶ " + ChatColor.LIGHT_PURPLE + gameName;
        for (Player p : Bukkit.getOnlinePlayers()) {
            p.sendMessage(broadCastMessage);
            p.playSound(p.getLocation(), Sound.CHICKEN_EGG_POP, 1.0f, 1.0f);


        }
    }

    public static void leaveKitPVP(Player player, Lobby lobby) {
        lobby.getKitPvP().getArena().removePlayerFromKitPvP(player);
        // reset scoreboard
        ScoreboardManager sm = Bukkit.getServer().getScoreboardManager();
        player.setScoreboard(sm.getNewScoreboard());
        // teleport to lobby spawn
        lobby.getLobbyManager().playerJoined(player);
    }

    public static void openVoteMasterBook(Player player)
    {
        ItemStack book = getVoteBook();
        ItemStack oldItem = player.getItemInHand(); //Get item in hand so we can set it back
        player.setItemInHand(book); //Set item in hand to book
        PacketPlayOutCustomPayload packet = new PacketPlayOutCustomPayload("MC|BOpen", new PacketDataSerializer(Unpooled.buffer())); //Create packet that tells the player to open a book
        CraftPlayer craftPlayer = (CraftPlayer)player; //Craftplayer for sending packet
        craftPlayer.getHandle().playerConnection.sendPacket(packet); //Send packet
        player.setItemInHand(oldItem); //Set item in hand back
    }

    @NotNull
    private static ItemStack getVoteBook() {
        String p1 = (ChatColor.GOLD + "Vote for BashyBashy\n" +
                ChatColor.RESET + "Earn BashyCoins every time you vote!\n" +
                "\n" +
                "Voting helps this server by making it visible to more players.\n" +
                "\n" +
                "1 Click the vote NPC and select Open voting menu.\n" +
                "\n" +
                ChatColor.GRAY + "Continued...");
        ItemStack book = new ItemStack(Material.WRITTEN_BOOK); //Create book ItemStack
        BookMeta meta = (BookMeta)book.getItemMeta(); //Get BookMeta
        meta.addPage(p1); //Add a page
        String p2 = ("2 Each green block in the menu represents a site that can be voted on.\n" +
                "\n" +
                "3 Clicking a block puts a link to that site in your chat window.\n" +
                "\n" +
                ChatColor.GRAY + "Continued...");
        meta.addPage(p2);
        String p3 = ("4 Click the link in your chat window then the yes button to go to the voting site.\n" +
                "\n" +
                "5 Follow the sites instructions to vote for BashyBashy.com\n" +
                "\n" +
                "6 Repeat for each voting site and get BashyCoins for each vote!");
        meta.addPage(p3); //Add another page
        book.setItemMeta(meta); //Set meta
        return book;
    }

    public static String getMinecraftVersionFromVIAProtocol(int protocolNumber) {
        switch (protocolNumber) {
            case 47:
                return "1.8 - 1.8.9";
            case 107:
                return "1.9";
            case 108:
                return "1.9.1";
            case 109:
                return "1.9.2";
            case 110:
                return "1.9.3 - 1.9.4";
            case 210:
                return "1.10 - 1.10.2";
            case 315:
                return "1.11";
            case 316:
                return "1.11.1 - 1.11.2";
            case 335:
                return "1.12";
            case 338:
                return "1.12.1";
            case 340:
                return "1.12.2";
            case 393:
                return "1.13";
            case 401:
                return "1.13.1";
            case 404:
                return "1.13.2";
            case 477:
                return "1.14";
            case 480:
                return "1.14.1";
            case 485:
                return "1.14.2";
            case 490:
                return "1.14.3";
            case 498:
                return "1.14.4";
            case 573:
                return "1.15";
            case 575:
                return "1.15.1";
            case 578:
                return "1.15.2";
            case 732:
                return "1.16";
            case 736:
                return "1.16.1";
            case 751:
                return "1.16.2";
            case 753:
                return "1.16.3";
            case 754:
                return "1.16.4 - 1.16.5";
            case 755:
                return "1.17";
            case 756:
                return "1.17.1";
            case 757:
                return "1.18 - 1.18.1";
            case 758:
                return "1.18.2";
            case 759:
                return "1.19";
            case 760:
                return "1.19.1 -1.19.2";
            case 761:
                return "1.19.3";
            case 762:
                return "1.19.4";
            case 763:
                return "1.20 - 1.20.1";
            case 764:
                return "1.20.2";
            case 765:
                return "1.20.3 - 1.20.4";
            case 766:
                return "1.20.5 - 1.20.6";
            case 767:
                return "1.21";
            case 768:
                return "1.22 - 1.22.3";
            case 769:
                return "1.21.4";
            case 770:
                return "1.21.5";
            default:
                return "";
        }
    }


}
