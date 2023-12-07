package com.stewart.lobby.manager;

import com.stewart.lobby.Lobby;
import com.stewart.lobby.instances.PlayerRules;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.trait.SkinTrait;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class RuleLobbyManager {

    private Lobby main;
    private List<String> lstRules;
    private List<PlayerRules> lstPlayerRules;
    private Location spawnLocation;
    private Location npcSpawnLocation;

    public RuleLobbyManager(Lobby lobby) {
        this.main = lobby;
        lstRules = new ArrayList<>();
        lstPlayerRules = new ArrayList<>();
        for (String s : ConfigManager.getPreRules()) {

            lstRules.add(ChatColor.GOLD + s);
        }
        for (String s : ConfigManager.getRules()) {
            lstRules.add(ChatColor.GOLD + " => " + ChatColor.YELLOW + s);
        }
        for (String s : ConfigManager.getPostRules()) {
            lstRules.add(ChatColor.GOLD + s);
        }
        spawnLocation = ConfigManager.getRulesSpawn();
        npcSpawnLocation = ConfigManager.getRulesNPCSpawn();
        spawnNPC();
    }

    private void spawnNPC() {
        Bukkit.getScheduler().scheduleSyncDelayedTask(main, new Runnable() {
            @Override
            public void run() {
                String npcName = ChatColor.GOLD + "Rulemaster";

                net.citizensnpcs.api.npc.NPC npc = CitizensAPI.getNPCRegistry().createNPC(EntityType.PLAYER, npcName);
                SkinTrait skin = npc.getOrAddTrait(SkinTrait.class);
                skin.setSkinPersistent("judge", ConfigManager.getRulesSkinSignature(), ConfigManager.getRulesSkinTexture());
                npc.spawn(npcSpawnLocation);

                System.out.println("spawning npc for rules");
            }
        }, 200L);

    }


    public void addPlayer(UUID uuid) {
        lstPlayerRules.add(new PlayerRules(uuid, main, this));
        Player player = Bukkit.getPlayer(uuid);
        player.getInventory().clear();
        player.setGameMode(GameMode.SURVIVAL);
        player.teleport(spawnLocation);
    }

    public boolean containsPlayer(UUID uuid) {
        for (PlayerRules pr : lstPlayerRules) {
            if (pr.getUuid().equals(uuid)) {
                return true;
            }
        }
        return false;
    }

    public void removePlayer(UUID uuid) {
        System.out.println("removing player from rules list length = " + lstPlayerRules.size());
        // first cancel the runnable
        for (PlayerRules pr : lstPlayerRules) {
            if (pr.getUuid().equals(uuid)) {
                System.out.println("Stopping player runnable");
                pr.stopRules();
            }
        }
        // then remove them from the list
        lstPlayerRules.removeIf(obj -> obj.getUuid().equals(uuid));
        System.out.println("removed player from rules list length = " + lstPlayerRules.size());
    }

    public List<String> getLstRules() { return lstRules;}


}
