package commands;

import it.cavemc.cavemc.CaveMC;
import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.SQLException;
import java.util.*;


public class unCave implements CommandExecutor {

    private static JavaPlugin plugin;

    public unCave(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public static CaveMC Main;

    public static final Map<UUID, Location> teleportRequests = new HashMap<>();

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        String notEnoughPermission = plugin.getConfig().getString("not-enough-permission");
        String playerNotCaved = plugin.getConfig().getString("player-not-caved");
        String playeruncaved = plugin.getConfig().getString("player-uncaved");

        if (args.length != 1) {
            sender.sendMessage("Uso: /uncave <nome_giocatore>");
        }

        Player player = (Player) sender;

        if(player.hasPermission("cavemc.*") || player.hasPermission("cavemc.cave.*") || player.hasPermission("cavemc.cave.uncave")) {

            String target = null;

            try {
                target = args[0];
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

            String prisoner = Main.sql.getPrisoners(target);

            if(prisoner == null) {
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', playerNotCaved));
                return true;
            }

            double x,y,z;
            try {
                x = Main.sql.getPrisX(prisoner);
                y = Main.sql.getPrisY(prisoner);
                z = Main.sql.getPrisZ(prisoner);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }

            Player freeTarget = Bukkit.getPlayerExact(target);

            String world;
            try {
                world = Main.sql.getWorldPreLoc(prisoner);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }

            World preWorld = Bukkit.getWorld(world);

            Location location = new Location(preWorld, x, y, z);

            UUID targetId = freeTarget != null ? freeTarget.getUniqueId() : Bukkit.getOfflinePlayer(target).getUniqueId();

            if (!(freeTarget.isOnline())) {
                Main.sql.removePrisoners(prisoner);
                teleportRequests.put(targetId, location);
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', playeruncaved));
                return true;
            }

            freeTarget.teleport(location);
            freeTarget.setGameMode(GameMode.SURVIVAL);

            Main.sql.removePrisoners(prisoner);

            player.sendMessage(ChatColor.translateAlternateColorCodes('&', playeruncaved));

        } else {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', notEnoughPermission));
        }

        return true;
    }

    public static class TabCompleterUnCave implements TabCompleter {
        @Override
        public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {

            if (args.length == 0) {
                return null;
            }

            if (args.length == 1) {
                List<String> playerNames = new ArrayList<>(Main.sql.getAllPrisoners());
                return playerNames;
            }
            return null;
        }
    }

    public static class PrisonerJoinListener implements Listener {
        @EventHandler
        public void onPlayerJoin(PlayerJoinEvent e) {
            Player player = e.getPlayer();
            UUID playerId = player.getUniqueId();
            if (teleportRequests.containsKey(playerId)) {
                Location location = teleportRequests.get(playerId);
                if (location != null) {
                    player.teleport(location);
                    player.setGameMode(GameMode.SURVIVAL);
                }
                teleportRequests.remove(playerId);
            }
        }
    }
    }
