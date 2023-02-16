package self.liltinyten.tags;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Objects;
import java.util.logging.Level;

public class TagHandler implements Listener {

    @EventHandler
    public void onChat(AsyncPlayerChatEvent e) {
        Player player = e.getPlayer();

        if (Main.getConnection() != null) {
            // If SQL
            // Check if table exists
            // If so get contents
            try {
                ResultSet rs = Main.prepareStatement("SELECT COUNT(*) FROM PLAYERTAGS WHERE UUID = '"+player.getUniqueId().toString()+"';").executeQuery();
                rs.next();
                if (rs.getRow() != 0) {
                    ResultSet rs1 = Main.prepareStatement("SELECT * FROM PLAYERTAGS WHERE UUID = '"+player.getUniqueId().toString()+"';").executeQuery();
                    rs1.next();
                    String tag = rs1.getString("tag");
                    if (!(tag.equals("reset"))) {
                        ResultSet hastag = Main.prepareStatement("SELECT * FROM TAGS WHERE TAG = '"+tag+"';").executeQuery();
                        hastag.next();
                        if (hastag.getRow() != 0) {
                            if (!Main.getMainClass().getConfig().getBoolean("prefix")) {
                                Main.getChat().setPlayerSuffix(player, ChatColor.translateAlternateColorCodes('&', "&r" + hastag.getString("displaytext")));
                            } else {
                                Main.getChat().setPlayerPrefix(player, ChatColor.translateAlternateColorCodes('&', "&r" + hastag.getString("displaytext")));
                            }
                        } else {
                            Main.prepareStatement("UPDATE PLAYERTAGS SET TAG = 'reset' WHERE UUID = '"+player.getUniqueId()+"';").executeUpdate();

                        }
                    } else {
                        // RESET CODE WITH DATABASE
                        if (!Main.getMainClass().getConfig().getBoolean("prefix")) {
                            Main.getChat().setPlayerSuffix(player, ChatColor.translateAlternateColorCodes('&', "&r"));
                        } else {
                            Main.getChat().setPlayerPrefix(player, ChatColor.translateAlternateColorCodes('&', "&r"));
                        }
                    }
                }
            } catch (SQLException | NullPointerException e1) {
                Main.getMainClass().logger.log(Level.SEVERE, Main.ERR +"An error has occurred!" + Main.END);
                e1.printStackTrace();
            }
            // set contents


        } else {
            // NO DATABASE
            if (Main.getMainClass().tagsyml.contains(player.getUniqueId().toString())) {
                if (!Objects.equals(Main.getMainClass().getTagsYML().getString(player.getUniqueId().toString()), "reset")) {
                    if (!Main.getMainClass().getConfig().getBoolean("prefix")) {
                        Main.getChat().setPlayerSuffix(player, ChatColor.translateAlternateColorCodes('&', "&r" + Main.getMainClass().tagListYML.getString(Main.getMainClass().tagsyml.getString(player.getUniqueId().toString()))));
                    } else {
                        Main.getChat().setPlayerPrefix(player, ChatColor.translateAlternateColorCodes('&', "&r" + Main.getMainClass().tagListYML.getString(Main.getMainClass().tagsyml.getString(player.getUniqueId().toString()))));
                    }
                } else {
                    // RESET CODE NO DATABASE
                    if (!Main.getMainClass().getConfig().getBoolean("prefix")) {
                        Main.getChat().setPlayerSuffix(player, "");
                    } else {
                        Main.getChat().setPlayerPrefix(player, "");
                    }
                }
            } else {
                if (Main.getChat() != null) {
                    if (!Main.getMainClass().getConfig().getBoolean("prefix")) {
                        Main.getChat().setPlayerSuffix(player, "");
                    } else {
                        Main.getChat().setPlayerPrefix(player, "");
                    }
                }
            }
        }

    }


}
