package com.stewart.lobby.utils;

import com.stewart.lobby.Lobby;
import org.bukkit.entity.Player;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.*;

public class SkinUtils {
    private final Lobby main;




    public SkinUtils(Lobby main) {
        this.main = main;

    }



    public void clearSkin(Player player) {
        runSkinCommand(player, "clear", main);
    }

    private  void runSkinCommand(Player player, String command, Lobby main) {
        System.out.println("Running skin command for player " + player.getName() + ": " + command);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        DataOutputStream data = new DataOutputStream(out);

        try {
            data.writeUTF("RunSkinCommand");
            data.writeUTF(player.getUniqueId().toString());
            data.writeUTF(command);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        player.sendPluginMessage(main, "zombie:skin", out.toByteArray());
    }





}
