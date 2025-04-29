package com.stewart.lobby.utils;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

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
