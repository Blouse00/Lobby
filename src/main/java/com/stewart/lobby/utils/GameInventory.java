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
          //  if (game.getGameColourInt() > -1) {
                ItemStack wool = new ItemStack(new ItemStack(game.getMaterial()));
                ItemMeta woolMeta = wool.getItemMeta();
                woolMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
                woolMeta.setDisplayName(ChatColor.GOLD + "Join " + game.getGameName());
                wool.setItemMeta(woolMeta);
                inv.setItem(game.getInventorySlot(), wool);
          //  }
        }

        //FRAME
        ItemStack frame = new ItemStack(Material.STAINED_GLASS_PANE, 1, DyeColor.BLACK.getData());
        ItemMeta frameMeta = frame.getItemMeta();
        frameMeta.setDisplayName("");
        for (int i : new int[]{0,1,2,3,4,5,6,7,8,9,17,18,26,27,35,36,44,45,46,47,48,49,50,51,52,53})
            inv.setItem(i, frame);

        return inv;
    }
}
