package commands;

import de.myzelyam.api.vanish.PlayerHideEvent;
import de.myzelyam.api.vanish.PlayerShowEvent;
import de.myzelyam.api.vanish.VanishAPI;
import de.myzelyam.supervanish.SuperVanish;
import de.myzelyam.supervanish.SuperVanishPlugin;
import de.myzelyam.supervanish.VanishPlayer;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import it.cavemc.cavemc.CaveMC;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import de.myzelyam.api.*;

import static org.bukkit.Bukkit.getServer;

public class friend implements CommandExecutor {

    private final JavaPlugin plugin;

    CaveMC Main;

    public friend(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Solo un giocatore può eseguire il comando!");
            return false;
        }

        Player player = (Player) sender;

        if (args.length < 1) {
            player.sendMessage("/friend add <user>\n/friend remove <user>\n/friend accept <user>\n/friend list");
            return true;
        }

        switch (args[0]) {
            case "add":
                if (args.length == 1) {
                    player.sendMessage("/friend add <user>\n/friend remove <user>\n/friend accept <user>\n/friend list");
                    return true;
                }
                String target = args[1];
                Player friend = Bukkit.getPlayerExact(target);

                if (target.equals(player.getName())) {
                    player.sendMessage("Non puoi mandare una richiesta d'amicizia a te stesso");
                    return true;
                }

                if (friend == null || !friend.isOnline()) {
                    player.sendMessage("Il giocatore non è online!");
                    return true;
                }

                String friendName;
                try {
                    friendName = Main.sqli.getFriend(target);
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }

                List<String> status;

                try {
                    status = new ArrayList<>(Main.sqli.getStatus(target));
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }

                if (status.contains("pending")) {
                    player.sendMessage("Hai già mandato una richiesta d'amicizia a " + target);
                    return true;
                }

                if (status.contains("accepted")) {
                    player.sendMessage("Sei già amico con questo utente");
                    return true;
                }

                if (friendName == null) {
                    String pattern = "MM/dd/yyyy HH:mm:ss";

                    DateFormat df = new SimpleDateFormat(pattern);

                    Date today = Calendar.getInstance().getTime();

                    String todayAsString = df.format(today);
                    CaveMC.sqli.setFriend(player.getName(), target, todayAsString, "pending");
                } else if (friendName.equals(target)) {
                    player.sendMessage("Sei già amico con questo utente");
                }

                friend.sendMessage("Scrivi /friend accept " + player.getName() + " per accettare la richiesta d'amicizia!");
                player.sendMessage("Richiesta d'amicizia inviata a " + friend.getName());
                break;
            case "accept":
                if (args.length == 1) {
                    player.sendMessage("Usage: /friend accept <user>");
                    return false;
                }

                if (Objects.equals(args[1], player.getName())) {
                    player.sendMessage("Non puoi accettare richieste da parte di te stesso");
                    return true;
                }

                List<String> pendingRequests = new ArrayList<>(Main.sqli.getPlayer(player.getName()));
                System.out.println(pendingRequests);
                if (pendingRequests.isEmpty()) {
                    player.sendMessage("non hai richieste da accettare!");
                    return true;
                } else {
                    String acceptTarget = args[1];

                    try {
                        Main.sqli.updateFriend(acceptTarget, "accepted");
                        Player playerTargetted = Bukkit.getPlayerExact(acceptTarget);
                        if (playerTargetted != null) {
                            playerTargetted.sendMessage(player.getName() + " ha accettato la tua richiesta d'amicizia!");
                            player.sendMessage("Adesso sei amico con " + playerTargetted.getName());
                        } else {
                            player.sendMessage("Non hai richieste d'accettare");
                        }


                        Main.sqli.setFinallyFriend(acceptTarget, player.getName());
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                }


                break;
            case "remove":
                if (args.length == 1) {
                    player.sendMessage("/friend add <user>\n/friend remove <user>\n/friend accept <user>\n/friend list");
                    return false;
                }

                String removedTarget = args[1];

                if (removedTarget.equals(player.getName())) {
                    player.sendMessage("Non puoi rimuovere dagli amici te stesso!");
                    return true;
                }

                try {
                    try {
                        friendName = Main.sqli.getFriend(removedTarget);
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }

                    if (friendName == null) {
                        player.sendMessage("Non sei amico con quest'utente!");
                        return true;
                    }
                    if (friendName.equals(removedTarget) || friendName != null) {
                        try {
                            Main.sqli.removefriends(removedTarget);

                            Main.sqli.removeRequest(removedTarget);
                            player.sendMessage("Hai rimosso l'amicizia con " + removedTarget);
                        } catch (SQLException e) {
                            throw new RuntimeException(e);
                        }
                    } else if (friendName == null) {
                        player.sendMessage("Non sei amico con quest'utente!");
                    }
                } catch (RuntimeException e) {
                    throw new RuntimeException(e);
                }
                break;
            case "list":
                if (args.length == 0) {
                    player.sendMessage("/friend add <user>\n/friend remove <user>\n/friend accept <user>\n/friend list");
                    return false;
                }

                List<String> listFriends;
                try {
                    listFriends = new ArrayList<>(Main.sqli.getFriends(player.getName()));
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }

                player.sendMessage("Queste sono le tue amicizie:");
                for (int i = 0; i < listFriends.size(); i++) {
                    String friends = listFriends.get(i);
                    int a;
                    player.sendMessage((i+1) + "." + " " + friends);
                }

                    break;
            case "removeallrequests":
                try {
                    Main.sqli.removeAllRequests();
                    Main.sqli.removeAllFriends();
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
                break;
            default:
                player.sendMessage("/friend add <user>\n/friend remove <user>\n/friend accept <user>\n/friend list");
        }
        return true;
    }

    public static class ActionListener implements Listener {
        @EventHandler
        public void onPlayerJoin(PlayerJoinEvent event) {
            Player player = event.getPlayer();
            String playerName = player.getName();

            try {
                List<String> friendNames = CaveMC.sqli.getFriends(playerName);
                for (String friendName : friendNames) {
                    Player friend = Bukkit.getPlayerExact(friendName);
                    if (friend != null) {
                            friend.sendMessage(playerName + " has joined the server.");
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        @EventHandler
        public void onPlayerQuit(PlayerQuitEvent event) {
            Player player = event.getPlayer();
            String playerName = player.getName();

            try {
                List<String> friendNames = CaveMC.sqli.getFriends(playerName);
                for (String friendName : friendNames) {
                    Player friend = Bukkit.getPlayerExact(friendName);
                    if (friend != null) {
                            friend.sendMessage(playerName + " has left the server.");
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        @EventHandler
        public void PlayerHideEvent(PlayerHideEvent event) {
            Player player = event.getPlayer();
            String playerName = player.getName();

            try {
                List<String> friendNames = CaveMC.sqli.getFriends(playerName);
                for (String friendName : friendNames) {
                    Player friend = Bukkit.getPlayerExact(friendName);
                    if (friend != null) {
                        friend.sendMessage(playerName + " has left the server.");
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        @EventHandler
        public void PlayerShowEvent(PlayerShowEvent event) {
            Player player = event.getPlayer();
            String playerName = player.getName();

            try {
                List<String> friendNames = CaveMC.sqli.getFriends(playerName);
                for (String friendName : friendNames) {
                    Player friend = Bukkit.getPlayerExact(friendName);
                    if (friend != null) {
                        friend.sendMessage(playerName + " has joined the server.");
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public static class TabCompleterFriend implements TabCompleter {
        @Override
        public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
            if (args[0].equals("accept")) {
                List<String> pendingRequests = new ArrayList<>(CaveMC.sqli.getPlayer(sender.getName()));
                List<String> completions = new ArrayList<>();
                String playerName = sender.getName();
                for (String request : pendingRequests) {
                    String[] players = request.split(":");
                    if (players[0].equals(playerName)) {
                        completions.add(players[1]);
                    } else if (players[1].equals(playerName)) {
                        completions.add(players[0]);
                    }
                }
                return completions;
            } else if (args[0].equals("add")) {
                List<String> playerNames = new ArrayList<>();
                Player[] players = new Player[Bukkit.getServer().getOnlinePlayers().size()];
                Bukkit.getServer().getOnlinePlayers().toArray(players);
                for (int i = 0; i < players.length; i++) {
                    playerNames.add(players[i].getName());
                }
                return playerNames;
            } else if (args[0].equals("remove")) {
                List<String> friends = null;
                try {
                    friends = new ArrayList<>(CaveMC.sqli.getFriends(sender.getName()));
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
                return friends;
            }
            return null;
        }
    }

}
