package commands;

import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.*;

import it.cavemc.cavemc.CaveMC;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;


import java.sql.SQLException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Cave implements CommandExecutor {
    public static CaveMC Main;

    private static JavaPlugin plugin;

    public static final Map<UUID, Location> teleportRequests = new HashMap<>();

    public Cave(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        String onlyPlayerCommand = plugin.getConfig().getString("only-player-command");
        String caveNotExists = plugin.getConfig().getString("cave-not-exists");
        String playerNotOnline = plugin.getConfig().getString("player-not-online");
        String playerCaved = plugin.getConfig().getString("player-caved");
        String notEnoughPermission = plugin.getConfig().getString("not-enough-permission");
        String playerJustCaved = plugin.getConfig().getString("player-just-caved");

        if (!(sender instanceof Player)) {
            System.out.println(ChatColor.translateAlternateColorCodes('&', onlyPlayerCommand));
            return true;
        }

        if (args.length < 2) {
            sender.sendMessage("Utilizzo: /cave <nome_giocatore> <caverna> <tempo>");
            return true;
        }
        if (sender.hasPermission("cavemc.cave.*") || sender.hasPermission("cavemc.cave.caves") || sender.hasPermission("cavemc.*")) {

            Player player = (Player) sender;
            String playerName = args[0];
            String caveName = args[1];
            String timeInput = args[2];

            long time = (long) Integer.parseInt(String.valueOf(timeInput.substring(0, timeInput.length() - 1)));

            Player target = Bukkit.getPlayerExact(playerName);
            UUID targetId = target != null ? target.getUniqueId() : Bukkit.getOfflinePlayer(playerName).getUniqueId();

            double x, y, z;
            try {
                x = Main.sql.getCoordX(caveName);
                y = Main.sql.getCoordY(caveName);
                z = Main.sql.getCoordZ(caveName);
                String name = Main.sql.getCave(caveName);
                if (name == null) {
                    player.sendMessage(ChatColor.translateAlternateColorCodes('&', caveNotExists));
                    return true;
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }

            if (target == null) {
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', playerNotOnline));
                return true;
            }

            Location preLoc = target.getLocation();
            double preX = preLoc.getX();
            double preY = preLoc.getY();
            double preZ = preLoc.getZ();
            World world = preLoc.getWorld();

            String worldString = world.getName(); // get the pre world name to insert it into the database

            //time translator

            char suffix = timeInput.charAt(timeInput.length() - 1);;

            switch (suffix) {
                case 's':
                    break;
                case 'm':
                    time *= 60;
                    break;
                case 'h':
                    time *= 3600;
                    break;
                case 'd':
                    time *= 86400;
                    break;
                case 'M':
                    time *= 2592000;
                    break;
                case 'y':
                    time *= 31536000;
                    break;
                default:
                    break;
            }

            Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
                @Override
                public void run() {
                    String prisonerName = Main.sql.getPrisoners(target.getName());
                    if (StringUtils.isNotBlank(prisonerName)) {
                        if (target.isOnline() && prisonerName.contains(target.getName())) {
                            target.teleport(preLoc);
                            target.setGameMode(GameMode.SURVIVAL);
                            Main.sql.removePrisoners(target.getName());
                        } else if (!target.isOnline() && prisonerName.contains(target.getName())) {
                            teleportRequests.put(targetId, preLoc);
                        }
                    }
                }
            }, time * 20L);

            String targetChoosed = Main.sql.getPrisoners(playerName);

            if (target.isOnline() && !target.getName().equals(targetChoosed)) {

                World tpWorld = Bukkit.getWorld("world");

                Location caveLocation = new Location(tpWorld, x, y, z);

                target.setGameMode(GameMode.ADVENTURE);

                Main.sql.setPrisoners(target.getName(), preX, preY, preZ, worldString);

                target.teleport(caveLocation);

                player.sendMessage(ChatColor.translateAlternateColorCodes('&', playerCaved));

            } else {
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', playerJustCaved));
            }

        }else {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', notEnoughPermission));
        }
        return true;
    }

    public static class TabCompleterCave implements TabCompleter {
        @Override
        public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {

            if (args.length == 1) {
                List<String> playerNames = new ArrayList<>();
                Player[] players = new Player[Bukkit.getServer().getOnlinePlayers().size()];
                Bukkit.getServer().getOnlinePlayers().toArray(players);
                for (int i = 0; i < players.length; i++) {
                    playerNames.add(players[i].getName());
                }

                return playerNames;
            } else if (args.length == 2) {
                List<String> caves = new ArrayList<>(Main.sql.getAllCaves());

                return caves;
            } else if (args.length == 3) {
                List<String> times = new ArrayList<>();
                times.add("1m"); times.add("5m"); times.add("2d");
                times.add("7d"); times.add("1M"); times.add("1y");

                return times;
            }
            return null;
        }
    }

    public static class PlayerJoinListener implements Listener {
        @EventHandler
        public void onPlayerJoin(PlayerJoinEvent e) {
            Player player = e.getPlayer();
            UUID playerId = player.getUniqueId();
            if (teleportRequests.containsKey(playerId) && Main.sql.getPrisoners(player.getName()).contains(player.getName())) {
                Location location = teleportRequests.get(playerId);
                if (location != null) {
                    player.teleport(location);
                    player.setGameMode(GameMode.SURVIVAL);
                    Main.sql.removePrisoners(player.getName());
                }
                teleportRequests.remove(playerId);
            }
        }
    }

    public static class CommandBlocker implements Listener {
        @EventHandler
        public void PlayerCommandPreprocessEvent(PlayerCommandPreprocessEvent e) {
            String PlayerTryToCommand = plugin.getConfig().getString("player-try-command");
                List<String> prisoner = new ArrayList<>(Main.sql.getAllPrisoners());
                Player Eplayer = e.getPlayer();
                String player = Eplayer.getName();
                if(prisoner.contains(player)) {
                   e.setCancelled(true);
                   Eplayer.sendMessage(ChatColor.translateAlternateColorCodes('&', PlayerTryToCommand));
                }
        }
    }

}

