package com.stewart.lobby.listeners;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import com.stewart.lobby.Lobby;
import com.stewart.lobby.instances.Game;
import com.stewart.lobby.manager.PortalManager;
import com.stewart.lobby.utils.GameInventory;
import com.stewart.lobby.utils.RulesInventory;
import net.citizensnpcs.api.event.NPCLeftClickEvent;
import net.citizensnpcs.api.event.NPCRightClickEvent;
import net.citizensnpcs.api.npc.NPC;
import net.minecraft.server.v1_8_R3.IChatBaseComponent;
import net.minecraft.server.v1_8_R3.PacketPlayOutTitle;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftTNTPrimed;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.*;
import org.bukkit.event.weather.ThunderChangeEvent;
import org.bukkit.event.weather.WeatherChangeEvent;

import java.io.IOException;
import java.util.UUID;


// event for when player clicks on a sign
public class LobbyListener implements Listener {

    private Lobby lobby;

    public LobbyListener(Lobby lobby) {
        this.lobby = lobby;
    }

    @EventHandler
    public void onClick (InventoryClickEvent e) {

        e.setCancelled(true);
        if (e.getClickedInventory() == null) {
            System.out.println("Clicked inventory is null");
            return;
        }
            //  System.out.println("inv click");
            // below here fires the item click event I've moved to the shopentites class for each of the different shop types
            if (ChatColor.translateAlternateColorCodes('&', e.getClickedInventory().getTitle())
                    .equals("" + ChatColor.DARK_GRAY + ChatColor.BOLD + "JOIN A GAME.") &&
                    e.getCurrentItem() != null) {

                Player player = (Player) e.getWhoClicked();
                // the shop click function handles what to do depending on the slot that was clicked
                lobby.getGameManager().gameChosenFromInventory(player, e.getRawSlot());
            } else if (ChatColor.translateAlternateColorCodes('&', e.getClickedInventory().getTitle())
                    .equals("" + ChatColor.DARK_GRAY + ChatColor.BOLD + "ACCEPT THE RULES.") &&
                    e.getCurrentItem() != null) {

                Player player = (Player) e.getWhoClicked();

                if (e.getRawSlot() == 20) {
                    System.out.printf("rules not accepted :(");
                    // rules not accepted
                    player.closeInventory();
                    player.sendMessage( ChatColor.RED + "You must accept the rules before joining this server!");
                }
                if (e.getRawSlot() == 24) {
                    System.out.printf("rules accepted :)");
                    // rules accepted
                    player.sendMessage( ChatColor.GREEN + "Thank you for accepting the rules, we hope you enjoy our server!");
                    lobby.getBb_api().getPlayerManager().logRulesAccepted(player.getUniqueId());
                    lobby.getRuleLobbyManager().removePlayer(player.getUniqueId());
                    lobby.getLobbyManager().playerJoined(player);
                }

            }
    }

    @EventHandler
    public void Chat(AsyncPlayerChatEvent event) {
        System.out.printf("player chatevent fired");
        Player player = event.getPlayer();
        if (lobby.getRuleLobbyManager().containsPlayer(player.getUniqueId())) {
            System.out.printf("cancel chat");
            player.sendMessage(ChatColor.RED + " The rules must be accepted before you can chat");
            player.sendMessage(ChatColor.GOLD + " Click the judge to accept the rules");
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void Chat(PlayerCommandPreprocessEvent event) {
        System.out.printf("player command fired");
        Player player = event.getPlayer();
        if (lobby.getRuleLobbyManager().containsPlayer(player.getUniqueId())) {
            System.out.println("cancel command");
            player.sendMessage(ChatColor.RED + " The rules must be accepted before you can use commands");
            player.sendMessage(ChatColor.GOLD + " Click the judge to accept the rules");
            event.setCancelled(true);
        }
    }

    // prevent any blocks being broken
    @EventHandler
    public void onBlockBreak(BlockBreakEvent e) throws IOException {
        // no block break
        e.setCancelled(true);
    }

    @EventHandler
    public void onLobbyCLick(PlayerInteractEvent e) {
        Player player = e.getPlayer();

        Action action = e.getAction();

        if (action == Action.RIGHT_CLICK_BLOCK || action == Action.RIGHT_CLICK_AIR) {

            int slot = player.getInventory().getHeldItemSlot();
            System.out.println("Slot " + slot + " clicked");
            if (slot == 0) {
                // open game type inventory
                GameInventory gameInventory = new GameInventory(lobby);
                player.openInventory(gameInventory.getGameInventory(player));
            }
            if (slot == 8) {
                // teleport player to parkour
                lobby.getLobbyManager().teleportToParkour(player);
            }
        }
    }

    @EventHandler
    public void damage(EntityDamageEvent event) //Listens to EntityDamageEvent
    {

        System.out.println("Damage type = " + event.getCause().toString());

            // each time a player damages a player I need to log who damaged who in a hashmap in the arena
            // this allows me to determine who killed a player in the EntityDamageEntity event above.
            if (event.getEntity() instanceof Player ) {
                Player damaged =  (Player) event.getEntity();

                // check if the player is currently spawn protected or in the rules area.
                if (lobby.getLobbyManager().playerSpawnProtected(damaged) ||
                        lobby.getRuleLobbyManager().containsPlayer(damaged.getUniqueId())) {
                    event.setCancelled(true);
                    return;
                }

                    CheckPlayerDies(damaged, event);

                if (event instanceof EntityDamageByEntityEvent) {

                    System.out.println("Entity damage entity event");
                    EntityDamageByEntityEvent ev = (EntityDamageByEntityEvent) event;
                    System.out.println("Damager = " + ev.getDamager().toString());
                    // damaged by an entity
                    // each time a player damages a player I need to log who damaged who in a hashmap in the arena
                    // this allows me to determine who killed a player in the EntityDamageEntity event above.
                    if (ev.getEntity() instanceof Player && ev.getDamager() instanceof Player) {
                        Player damager = (Player) ev.getDamager();
                        // make sure the damager is no longer spawn protected.
                        lobby.getLobbyManager().removeSpawnProtect(damager);
                    }
                }
                return;
            }

    }

    private void CheckPlayerDies(Player player, EntityDamageEvent ev) {
        System.out.println("Check player dies fired damage = " + ev.getFinalDamage());
        System.out.println("player health = " + player.getHealth());
        if (player.getHealth() - ev.getFinalDamage() <= 0) {
            System.out.println("Player would have died");
            // cancel death
            ev.setCancelled(true);

            // bring the player back to 'life'
            player.setHealth(20.0);
            player.setFoodLevel(20);
            // handles checking if game won etc & respawning 'killed' player.
            // damager may be null
            lobby.getLobbyManager().playerKilled(player);
            if (player != null) {
                PacketPlayOutTitle title = new PacketPlayOutTitle(PacketPlayOutTitle.EnumTitleAction.TITLE,
                        IChatBaseComponent.ChatSerializer.a("{\"text\":\"" + "§4" + "You died!" + "\"}"), 3, 150, 50);
                ((CraftPlayer) Bukkit.getPlayer(player.getUniqueId())).getHandle().playerConnection.sendPacket(title);
            }
        }
    }

    // prevent any blocks being placed
    @EventHandler
    public void onBlockPlace(BlockPlaceEvent e) throws IOException {
        // no block break
        e.setCancelled(true);
    }

    // prevent any blocks being placed
    @EventHandler
    public void onSpawn(CreatureSpawnEvent e) throws IOException {
        // no block break
        e.setCancelled(true);
        System.out.println("Creature spawn cancelled");
    }

    // prevent player dropping things with q button
    @EventHandler
    public void onPlayerDropItem(PlayerDropItemEvent event){
            event.setCancelled(true);
    }

    @EventHandler
    public void onRightClick(NPCRightClickEvent event){
        NPC npc = event.getNPC();
        Player player = event.getClicker();
        System.out.println("Npc clicked: " + npc.getName());
        npcClickEvent(player, npc.getName());
    }

    @EventHandler
    public void onLeftClick(NPCLeftClickEvent event){
        NPC npc = event.getNPC();
        Player player = event.getClicker();
        System.out.println("Npc clicked: " + npc.getName());
        npcClickEvent(player, npc.getName());
    }

    private void npcClickEvent(Player player, String npcName) {
        if (npcName.toLowerCase().contains("rulemaster")) {
            RulesInventory rulesInventory = new RulesInventory(lobby);
            player.openInventory(rulesInventory.getRulesInventory(player));
        } else {
            Game game = lobby.getGameManager().getGameByNpcName(npcName);
            if (game == null) {
                player.sendMessage("Game server not found, please try again later.");
            } else {
                System.out.println("Game found : " + game.getGameName());
                if (game.isPlayerInQueue(player.getUniqueId())) {
                    player.sendMessage("You are already in the queue for this game!");
                } else {
                    game.playerJoinRequest(player);
                }
            }
        }
    }


    @EventHandler(priority= EventPriority.HIGHEST)
    public void onWeatherChange(WeatherChangeEvent event) {

        boolean rain = event.toWeatherState();
        if(rain)
            event.setCancelled(true);
    }

    @EventHandler(priority=EventPriority.HIGHEST)
    public void onThunderChange(ThunderChangeEvent event) {

        boolean storm = event.toThunderState();
        if(storm)
            event.setCancelled(true);
    }

}
