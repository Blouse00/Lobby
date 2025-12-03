package com.stewart.lobby.utils;

import com.stewart.lobby.Lobby;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;

public class VoteInventory {

    private Lobby main;

    public VoteInventory(Lobby main) {
        this.main = main;
    }

    // the inventory that is returned to the listener class when it asks for the upgrade shop
    public Inventory getVoteInventory(Player player) {

        // List<Game> gameList = main.getGameManager().getGameList();

        Inventory inv = Bukkit.createInventory(player, 54, "" + ChatColor.GOLD + ChatColor.BOLD + "VOTE FOR BASHYBASHY");

        ItemStack redWool = new ItemStack(new ItemStack(Material.BOOK, 1));
        ItemMeta redWoolMeta = redWool.getItemMeta();
        redWoolMeta.setDisplayName(ChatColor.GREEN + "Voting Information.");
        redWoolMeta.setLore(new ArrayList<String>() {{
            ;
            add(ChatColor.YELLOW + "Click to read how and");
            add(ChatColor.YELLOW + "why you should vote!");
        }});
        redWool.setItemMeta(redWoolMeta);
        inv.setItem(20, redWool);

        ItemStack greenWool = new ItemStack(new ItemStack(Material.WOOL, 1, (short) 5));
        ItemMeta greenWoolMeta = greenWool.getItemMeta();
        greenWoolMeta.setDisplayName(ChatColor.GREEN + "Open Voting Menu!");
        greenWool.setItemMeta(greenWoolMeta);
        inv.setItem(24, greenWool);

        //FRAME
        ItemStack frame = new ItemStack(Material.STAINED_GLASS_PANE, 1, DyeColor.BLACK.getData());
        ItemMeta frameMeta = frame.getItemMeta();
        frameMeta.setDisplayName("");
        for (int i : new int[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 17, 18, 26, 27, 35, 36, 44, 45, 46, 47, 48, 49, 50, 51, 52, 53})
            inv.setItem(i, frame);

        return inv;
    }
}