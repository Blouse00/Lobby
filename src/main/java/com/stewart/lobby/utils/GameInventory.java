package com.stewart.lobby.utils;

import com.stewart.lobby.Lobby;
import com.stewart.lobby.instances.Game;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class GameInventory {

    private Lobby main;

    public GameInventory(Lobby main) {
        this.main = main;
    }

    // the inventory that is returned to the listener class when it asks for the upgrade shop
    public Inventory getGameInventory(Player player) {

        List<Game> gameList = main.getGameManager().getGameList();

        Inventory inv = Bukkit.createInventory(player, 54, "" + ChatColor.DARK_GRAY + ChatColor.BOLD + "JOIN A GAME.");

        for (Game game : gameList) {
          //  System.out.println("--------------------game name " + game.getGameName());
       //     if ((game.getGameName().toLowerCase().startsWith("fiend") && player.hasPermission("group.admin")) ||
         //           !game.getGameName().toLowerCase().startsWith("fiend")) {
                //  if (game.getGameColourInt() > -1) {
                ItemStack wool = new ItemStack(new ItemStack(game.getMaterial()));
                ItemMeta woolMeta = wool.getItemMeta();
                woolMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
                setGameLore(woolMeta, game.getGameName());
                woolMeta.setDisplayName(ChatColor.GOLD + "Join " + game.getGameName());
                wool.setItemMeta(woolMeta);
                inv.setItem(game.getInventorySlot(), wool);
                //  }
         //   }
        }

        ItemStack kitPvpItem = new ItemStack(Material.MELON);
        ItemMeta kitPvpItemMeta = kitPvpItem.getItemMeta();
        kitPvpItemMeta.setDisplayName(ChatColor.GOLD + "Kit PvP");
        kitPvpItem.setItemMeta(kitPvpItemMeta);
        inv.setItem(12, kitPvpItem);

        ItemStack miniGameItem = new ItemStack(Material.BLAZE_ROD);
        ItemMeta miniGameItemItemMeta = miniGameItem.getItemMeta();
        miniGameItemItemMeta.setDisplayName(ChatColor.GOLD + "Sumo Practice");
        miniGameItem.setItemMeta(miniGameItemItemMeta);
        inv.setItem(14,miniGameItem);

        //FRAME
        ItemStack frame = new ItemStack(Material.STAINED_GLASS_PANE, 1, DyeColor.BLACK.getData());
        ItemMeta frameMeta = frame.getItemMeta();
        frameMeta.setDisplayName("");
        for (int i : new int[]{0,1,2,3,4,5,6,7,8,9,17,18,26,27,35,36,44,45,46,47,48,49,50,51,52,53})
            inv.setItem(i, frame);

        return inv;
    }

    public Inventory getBedwarsInventory(Player player) {


        Inventory inv = Bukkit.createInventory(player, 54, "" + ChatColor.DARK_GRAY + ChatColor.BOLD + "BEDWARS GAME TYPE.");

        ItemStack wool = new ItemStack(new ItemStack(Material.BED));
        ItemMeta woolMeta = wool.getItemMeta();
        woolMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        GameInventory.setGameLore(woolMeta, "Bedwars_solo");
        woolMeta.setDisplayName(ChatColor.GOLD + "Join bedwars solo");
        wool.setItemMeta(woolMeta);
        inv.setItem(21, wool);

        ItemStack wool2 = new ItemStack(new ItemStack(Material.BED));
        ItemMeta woolMeta2 = wool2.getItemMeta();
        woolMeta2.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        GameInventory.setGameLore(woolMeta2, "Bedwars_duo");
        woolMeta2.setDisplayName(ChatColor.GOLD + "Join bedwars duos");
        wool2.setItemMeta(woolMeta2);
        inv.setItem(22, wool2);

        ItemStack wool3 = new ItemStack(new ItemStack(Material.BED));
        ItemMeta woolMeta3 = wool3.getItemMeta();
        woolMeta3.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        GameInventory.setGameLore(woolMeta3, "Bedwars_quad");
        woolMeta3.setDisplayName(ChatColor.GOLD + "Join bedwars quads");
        wool3.setItemMeta(woolMeta3);
        inv.setItem(23, wool3);

        //FRAME
        ItemStack frame = new ItemStack(Material.STAINED_GLASS_PANE, 1, DyeColor.BLACK.getData());
        ItemMeta frameMeta = frame.getItemMeta();
        frameMeta.setDisplayName("");
        for (int i : new int[]{0,1,2,3,4,5,6,7,8,9,17,18,26,27,35,36,44,45,46,47,48,49,50,51,52,53})
            inv.setItem(i, frame);

        return inv;
    }

    public Inventory getFiendFightInventory(Player player) {


        Inventory inv = Bukkit.createInventory(player, 54, "" + ChatColor.DARK_GRAY + ChatColor.BOLD + "FIEND FIGHT GAME TYPE.");

        ItemStack wool4 = new ItemStack(new ItemStack(Material.GOLD_HELMET));
        ItemMeta woolMeta4 = wool4.getItemMeta();
        woolMeta4.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        GameInventory.setGameLore(woolMeta4, "Fiend_fight_one_team");
        woolMeta4.setDisplayName(ChatColor.GOLD + "Join fiend fight one team");
        wool4.setItemMeta(woolMeta4);
        inv.setItem(20, wool4);

        ItemStack wool = new ItemStack(new ItemStack(Material.LEATHER_HELMET));
        ItemMeta woolMeta = wool.getItemMeta();
        woolMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        GameInventory.setGameLore(woolMeta, "Fiend_fight_solo");
        woolMeta.setDisplayName(ChatColor.GOLD + "Join fiend fight solo");
        wool.setItemMeta(woolMeta);
        inv.setItem(22, wool);

        ItemStack wool2 = new ItemStack(new ItemStack(Material.CHAINMAIL_HELMET));
        ItemMeta woolMeta2 = wool2.getItemMeta();
        woolMeta2.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        GameInventory.setGameLore(woolMeta2, "Fiend_fight_duo");
        woolMeta2.setDisplayName(ChatColor.GOLD + "Join fiend fight duos");
        wool2.setItemMeta(woolMeta2);
        inv.setItem(23, wool2);

        ItemStack wool3 = new ItemStack(new ItemStack(Material.IRON_HELMET));
        ItemMeta woolMeta3 = wool3.getItemMeta();
        woolMeta3.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        GameInventory.setGameLore(woolMeta3, "Fiend_fight_quad");
        woolMeta3.setDisplayName(ChatColor.GOLD + "Join fiend fight quads");
        wool3.setItemMeta(woolMeta3);
        inv.setItem(24, wool3);



        //FRAME
        ItemStack frame = new ItemStack(Material.STAINED_GLASS_PANE, 1, DyeColor.BLACK.getData());
        ItemMeta frameMeta = frame.getItemMeta();
        frameMeta.setDisplayName("");
        for (int i : new int[]{0,1,2,3,4,5,6,7,8,9,17,18,26,27,35,36,44,45,46,47,48,49,50,51,52,53})
            inv.setItem(i, frame);

        return inv;
    }

    public static void setGameLore(ItemMeta itemMeta, String gameName) {
        List<String> lst = new ArrayList<>();
        switch (gameName.toLowerCase()) {
            case ("bedwars_solo"): {
                lst.add(ChatColor.GRAY + "Protect your bed to respawn, Destroy others!");
                lst.add(ChatColor.GRAY + "Buy items and upgrades to get an advantage.");
                lst.add(ChatColor.GRAY + "Bridge to other islands and attack other players.");
                lst.add(ChatColor.GRAY + "Shops at mid offer special enchants & items!");
                itemMeta.setLore(lst);
                break;
            }
            case ("bedwars_quads"):
            case ("bedwars_duos"): {
                lst.add(ChatColor.GRAY + "Protect your teams bed to respawn, Destroy others!");
                lst.add(ChatColor.GRAY + "Buy items and upgrades to get an advantage.");
                lst.add(ChatColor.GRAY + "Bridge to other islands and attack other teams.");
                lst.add(ChatColor.GRAY + "Shops at mid offer special enchants & items!");
                itemMeta.setLore(lst);
                break;
            }
            case ("fiend_fight_solo"): {
                lst.add(ChatColor.GRAY + "Fight through arenas of mobs!");
                lst.add(ChatColor.GRAY + "Compete against other players.");
                lst.add(ChatColor.GRAY + "Try to get the best score!");
                lst.add(ChatColor.GRAY + "Buy items and upgrades to get an advantage.");
                itemMeta.setLore(lst);
                break;
            }
            case ("fiend_fight_quad"):
            case ("fiend_fight_duo"): {
                lst.add(ChatColor.GRAY + "Fight through arenas of mobs!");
                lst.add(ChatColor.GRAY + "By yourself or with a team.");
                lst.add(ChatColor.GRAY + "Try to get the best score!");
                lst.add(ChatColor.GRAY + "Buy items and upgrades to get an advantage.");
                itemMeta.setLore(lst);
                break;
            }
            case ("fiend_fight_one_team"): {
                lst.add(ChatColor.GRAY + "Fight through arenas of mobs!");
                lst.add(ChatColor.GRAY + "By yourself or with " + ChatColor.BOLD + ChatColor.RED + "YOUR PARTY.");
                lst.add(ChatColor.GRAY + "Try to get the best score!");
                lst.add(ChatColor.GRAY + "Buy items and upgrades to get an advantage.");
                itemMeta.setLore(lst);
                break;
            }
            case ("assault_course"): {
                lst.add(ChatColor.GRAY + "Parkour with punching!, Race to the finish line");
                lst.add(ChatColor.GRAY + "Punch or shoot other players into the void!");
                lst.add(ChatColor.GRAY + "Passing a checkpoint makes it your new respawn point.");
                lst.add(ChatColor.GRAY + "Try to get the fastest times for each arena!");
                itemMeta.setLore(lst);
                break;
            }
            case ("beta_icewars"): {
                lst.add(ChatColor.GRAY + "Icewars is skywars and spleefs cursed offspring!");
                lst.add(ChatColor.GRAY + "Snowballs destroy most blocks they land on.");
                lst.add(ChatColor.GRAY + "Use summoned items to buy equipment and upgrades.");
                lst.add(ChatColor.GRAY + "If you die its Game over - no respawns!");
                itemMeta.setLore(lst);
                break;
            }
            case ("full_iron_armour"): {
                lst.add(ChatColor.GRAY + "First player to wear a full set of iron armour wins!");
                lst.add(ChatColor.GRAY + "Will you craft your armour, or steal it off others???");
                lst.add(ChatColor.GRAY + "Use shop items to cause chaos and mischief!");
                itemMeta.setLore(lst);
                break;
            }
            case ("creative"): {
                lst.add(ChatColor.GRAY + "Join our creative mode server.");
                lst.add(ChatColor.GRAY + "Claim your own plot to start building.");
                lst.add(ChatColor.GRAY + "Your imagination is the limit!");
                itemMeta.setLore(lst);
                break;
            }
            case ("1.8 smp"): {
                lst.add(ChatColor.GRAY + "Join our 1.8 survival multiplayer server.");
                lst.add(ChatColor.GRAY + "Claim your own plot and build a house.");
                lst.add(ChatColor.GRAY + "Play how you want - mine, farm or build.");
                lst.add(ChatColor.GRAY + "Go kill the Enderdragon! -The end resets regularly.");
                itemMeta.setLore(lst);
                break;
            }
            case ("traps game"): {
                lst.add(ChatColor.GRAY + "Join our 1.8 SMP traps game.");
                lst.add(ChatColor.GRAY + "Place traps to kill other players.");
                lst.add(ChatColor.GRAY + "Complete quests to get points.");
                lst.add(ChatColor.GRAY + "Watch out for loot-chests .... and Giants!");
                itemMeta.setLore(lst);
                break;
            }
            default: {
                break;
            }
        }

    }
}
