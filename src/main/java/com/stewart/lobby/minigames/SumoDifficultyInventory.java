package com.stewart.lobby.minigames;

import com.stewart.lobby.Lobby;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;

public class SumoDifficultyInventory {


    public SumoDifficultyInventory() {
    }

    // the inventory that is returned to the listener class when it asks for the upgrade shop
    public Inventory getSumoDifficultyInventory(Player player) {


        Inventory inv = Bukkit.createInventory(player, 54, "" + ChatColor.DARK_GRAY + ChatColor.GOLD + "SUMO DIFFICULTY");

        ItemStack noobDif = new ItemStack(Material.WOOL, 1, DyeColor.LIGHT_BLUE.getData());
        ItemMeta noobMeta = noobDif.getItemMeta();
        noobMeta.setDisplayName(ChatColor.GOLD + "NOOB");
        noobDif.setItemMeta(noobMeta);
        inv.setItem(20, noobDif);

        ItemStack easyDif = new ItemStack(Material.WOOL, 1, DyeColor.LIME.getData());
        ItemMeta easyMeta = easyDif.getItemMeta();
        easyMeta.setDisplayName(ChatColor.GOLD + "EASY");
        easyDif.setItemMeta(easyMeta);
        inv.setItem(21, easyDif);


        ItemStack mediumDif = new ItemStack(Material.WOOL, 1, DyeColor.ORANGE.getData());
        ItemMeta mediumMeta = mediumDif.getItemMeta();
        mediumMeta.setDisplayName(ChatColor.GOLD + "MEDIUM");
        mediumDif.setItemMeta(mediumMeta);
        inv.setItem(22, mediumDif);

        ItemStack hardDiff = new ItemStack(Material.WOOL, 1, DyeColor.RED.getData());
        ItemMeta hardMeta = hardDiff.getItemMeta();
        hardMeta.setDisplayName(ChatColor.GOLD + "HARD");
        hardDiff.setItemMeta(hardMeta);
        inv.setItem(23, hardDiff);

        ItemStack hackerDiff = new ItemStack(Material.WOOL, 1, DyeColor.PURPLE.getData());
        ItemMeta HackerMeta = hackerDiff.getItemMeta();
        HackerMeta.setDisplayName(ChatColor.DARK_PURPLE + "HaAaCckKeERr!");
        hackerDiff.setItemMeta(HackerMeta);
        inv.setItem(24, hackerDiff);

        //FRAME
        ItemStack frame = new ItemStack(Material.STAINED_GLASS_PANE, 1, DyeColor.BLACK.getData());
        ItemMeta frameMeta = frame.getItemMeta();
        frameMeta.setDisplayName("");
        for (int i : new int[]{0,1,2,3,4,5,6,7,8,9,17,18,26,27,35,36,44,45,46,47,48,49,50,51,52,53})
            inv.setItem(i, frame);

        return inv;
    }
}
