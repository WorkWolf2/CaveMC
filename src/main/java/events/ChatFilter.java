package events;

import it.cavemc.cavemc.CaveMC;
import net.md_5.bungee.api.chat.ClickEvent;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.plugin.Plugin;
import net.md_5.bungee.api.chat.TextComponent;

import java.util.List;

import static org.bukkit.Bukkit.getPlayerExact;
import static org.bukkit.Bukkit.getServer;

public class ChatFilter implements Listener {
    private Plugin plugin = CaveMC.getPlugin(CaveMC.class);
    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent e){
        Player player = e.getPlayer();
        String message = e.getMessage();

        List<String> bannedWords = plugin.getConfig().getStringList("banned-words");
        String tempmute = plugin.getConfig().getString("temp-mute");
        String reason = plugin.getConfig().getString("reason");

        for (String word : bannedWords) {
            if(message.contains(word)) {
                if (!player.hasPermission("cavemc.bypass")) {
                    e.setCancelled(true);

                    for (Player onlinePlayer : getServer().getOnlinePlayers()) {
                        if (onlinePlayer.hasPermission("cavemc.read.badwords")) {
                            String mute = "╏ MUTA ";
                            String bannedMessage = ChatColor.DARK_PURPLE + player.getName() + ChatColor.DARK_PURPLE + ": " + ChatColor.WHITE + message;

                            TextComponent muteComponent = new TextComponent(mute);
                            TextComponent textComponent = new TextComponent(bannedMessage);

                            muteComponent.setBold(true);
                            muteComponent.setColor(net.md_5.bungee.api.ChatColor.DARK_PURPLE);

                            muteComponent.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/tempmute " + player.getName() + " " + tempmute + " " + reason));

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
