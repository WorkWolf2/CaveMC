package commands;

import it.cavemc.cavemc.CaveMC;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.*;


public class setCave implements CommandExecutor {

    private final JavaPlugin plugin;

    public setCave(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public static CaveMC Main;

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        String onlyPlayer = plugin.getConfig().getString("only-player-command");
        String caveExists = plugin.getConfig().getString("cave-exists");
        String successfullCave = plugin.getConfig().getString("set-cave-successfull");
        String NotEnoughPermission = plugin.getConfig().getString("not-enough-permission");

        if(!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', onlyPlayer));
            return true;
        }

        if (args.length == 0) {
            sender.sendMessage("Utilizzo: /setcave <nome_caverna>");
            return true;
        }

        if(sender.hasPermission("cavemc.cave.*") || sender.hasPermission("cavemc.*") || sender.hasPermission("cavemc.cave.set")) {

            Player player = (Player) sender;
            Location location = player.getLocation();
            String caveName = args[0];

            Block block = player.getWorld().getBlockAt(location);

            double x = location.getBlockX();
            double y = location.getBlockY();
            double z = location.getBlockZ();

            String name = Main.sql.getCave(caveName);
            if (name != null && name.equals(caveName)) {
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', caveExists));
                return true;
            }

            Main.sql.setCave(caveName, x, y, z);
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', successfullCave));
        } else {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', NotEnoughPermission));
        }


        return true;
    }


}
