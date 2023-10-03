package it.cavemc.cavemc;

import commands.*;
import events.CAPSFilter;
import events.ChatFilter;
import events.IpFilter;
import events.PluginHider;
import org.bukkit.command.CommandExecutor;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;

public final class CaveMC extends JavaPlugin implements CommandExecutor {


    public static Database sql;
    public static FriendDB sqli;
    @Override
    public void onEnable() {
       System.out.println("CaveMC abilitato! :)");

       getConfig().options().copyDefaults(true);
       saveDefaultConfig();

        //Conn db
        connection();

        // REGISTRAZIONE EVENTI
        getServer().getPluginManager().registerEvents(new CAPSFilter(), this);
        getServer().getPluginManager().registerEvents(new ChatFilter(), this);
        getServer().getPluginManager().registerEvents(new PluginHider(), this);
        getServer().getPluginManager().registerEvents(new IpFilter(), this);
        getServer().getPluginManager().registerEvents(new Cave.PlayerJoinListener(), this);
        getServer().getPluginManager().registerEvents(new unCave.PrisonerJoinListener(), this);
        getServer().getPluginManager().registerEvents(new Cave.CommandBlocker(), this);
        getServer().getPluginManager().registerEvents(new friend.ActionListener(), this);

        //REGISTRAZIONE DEL TAB COMPLETER
        getCommand("cave").setTabCompleter(new Cave.TabCompleterCave());
        getCommand("uncave").setTabCompleter(new unCave.TabCompleterUnCave());
        getCommand("friend").setTabCompleter(new friend.TabCompleterFriend());

       // REGISTRAZIONE COMANDI
        //this.getCommand("cmreload").setExecutor(new reload());
        getCommand("setcave").setExecutor(new setCave(this));
        getCommand("cave").setExecutor(new Cave(this));
        getCommand("uncave").setExecutor(new unCave(this));
        getCommand("friend").setExecutor(new friend(this));
    }

    @Override
    public void onDisable() {
        System.out.println("CaveMC disabilato! :(");
        sql.closeConnection();
        sqli.closeConnection();
    }

    public void connection() {
        Conn();
        DbAmici();
    }

    public void Conn() {
        File file = new File(getDataFolder() + "/caves.db");
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try {
            sql = new Database(file.getAbsolutePath());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public void DbAmici() {
        File file = new File(getDataFolder(), "/amici.db");
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        try {
            sqli = new FriendDB(file.getAbsolutePath());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

}
