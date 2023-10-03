package events;

import it.cavemc.cavemc.CaveMC;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.entity.Player;

import java.util.List;

public class PluginHider implements Listener {
    private Plugin plugin = CaveMC.getPlugin(CaveMC.class);

    @EventHandler
    public void onCommandProcess(PlayerCommandPreprocessEvent e) {
        String commandpre = e.getMessage();
        String[] finalcommand = commandpre.split(" ");
        String command = finalcommand[0].replaceAll("/", "");
        Player player = e.getPlayer();

        String pluginHideMessage = plugin.getConfig().getString("plugin-hidemessage");
        if (!player.hasPermission("cavemc.pluginhider.bypass")) {
            for (String blockedcmds : plugin.getConfig().getStringList("blocked-cmds")) {

                if (command.equalsIgnoreCase(blockedcmds)) {
                    player.sendMessage(ChatColor.translateAlternateColorCodes('&', pluginHideMessage));
                    e.setCancelled(true);
                    return;
                }
            }
            return;
        }
    }
}
