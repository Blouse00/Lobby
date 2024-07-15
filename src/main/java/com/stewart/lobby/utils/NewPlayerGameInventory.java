package com.stewart.lobby.utils;

import com.stewart.lobby.Lobby;
import com.stewart.lobby.instances.Game;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class NewPlayerGameInventory {

    private Lobby main;

    public NewPlayerGameInventory(Lobby main) {
        this.main = main;
    }

    // the inventory that is returned to the listener class when it asks for the upgrade shop
    public Inventory getGameInventory(Player player) {

        Inventory inv = Bukkit.createInventory(player, 54, "" + ChatColor.GOLD + ChatColor.BOLD + "JUMP INTO A GAME.");

        ItemStack joinGame = new ItemStack(new ItemStack(Material.WOOL, 1, (byte) 5));
        ItemMeta joinMeta = joinGame.getItemMeta();
        joinMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        joinMeta.setDisplayName(ChatColor.GREEN + "Join a game!");
        ArrayList<String> lore = new ArrayList<>();
        lore.add("Join the mini-game");
        lore.add("most ready to start!");
        joinMeta.setLore(lore);
        joinGame.setItemMeta(joinMeta);
        inv.setItem(20, joinGame);

        ItemStack allGames = new ItemStack(new ItemStack(Material.WOOL, 1, (byte) 1));
        ItemMeta allGameSMeta = allGames.getItemMeta();
        allGameSMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        allGameSMeta.setDisplayName(ChatColor.GOLD + "See all games");
        lore = new ArrayList<>();
        lore.add("See the full");
        lore.add("game menu!");
        allGameSMeta.setLore(lore);
        allGames.setItemMeta(allGameSMeta);
        inv.setItem(22, allGames);

        ItemStack noGame = new ItemStack(new ItemStack(Material.WOOL, 1, (byte) 14));
        ItemMeta noGameMeta = noGame.getItemMeta();
        noGameMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        noGameMeta.setDisplayName(ChatColor.RED + "Stay in the lobby");
        noGame.setItemMeta(noGameMeta);
        inv.setItem(24, noGame);

        //FRAME
        ItemStack frame = new ItemStack(Material.STAINED_GLASS_PANE, 1, DyeColor.BLACK.getData());
        ItemMeta frameMeta = frame.getItemMeta();
        frameMeta.setDisplayName("");
        for (int i : new int[]{0,1,2,3,4,5,6,7,8,9,17,18,26,27,35,36,44,45,46,47,48,49,50,51,52,53})
            inv.setItem(i, frame);

        return inv;
    }
}

