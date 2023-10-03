package events;

import it.cavemc.cavemc.CaveMC;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.plugin.Plugin;

import static org.bukkit.Bukkit.getServer;

public class IpFilter implements Listener {
    private Plugin plugin = CaveMC.getPlugin(CaveMC.class);

    @EventHandler
    public void onIpChat(AsyncPlayerChatEvent e) {
        Player player = e.getPlayer();
        String message = e.getMessage();

        Boolean ipsblock = plugin.getConfig().getBoolean("blockips");
        String ipstempmute = plugin.getConfig().getString("ips-temp-mute");
        String ipsreason = plugin.getConfig().getString("ips-reason");

        if (ipsblock == true) {
            if (message.endsWith(".it") || message.endsWith(".net") || message.endsWith(".eu") || message.endsWith(".club") || message.endsWith(".play")) {
                if (!player.hasPermission("cavemc.spamip")) {
                    e.setCancelled(true);

                    for (Player onlinePlayer : getServer().getOnlinePlayers()) {
                        if (onlinePlayer.hasPermission("cavemc.read.spamip")) {
                            String mute = "╏ MUTA ";
                            String bannedMessage = ChatColor.GOLD + player.getName() + ChatColor.YELLOW + ": " + ChatColor.WHITE + message;

                            TextComponent muteComponent = new TextComponent(mute);
                            TextComponent textComponent = new TextComponent(bannedMessage);

                            muteComponent.setBold(true);
                            muteComponent.setColor(net.md_5.bungee.api.ChatColor.GOLD);

                            muteComponent.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/tempmute " + player.getName() + " " + ipstempmute + " " + ipsreason));

                            muteComponent.addExtra(textComponent);

                            onlinePlayer.spigot().sendMessage(muteComponent);
                        }
                    }
                    return;
                }
            }
        }
    }
}
