package self.liltinyten.tags;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.ChatColor;

public class TagsMenuHandler implements Listener {




    @EventHandler
    public void onClick(InventoryClickEvent e) {
        Player player = (Player) e.getWhoClicked();
        if (player.getInventory().getHolder() instanceof Player && player.getInventory().getHolder().getOpenInventory().getTitle().equals(ChatColor.translateAlternateColorCodes('&', "&aTags"))) {
            if (e.getCurrentItem() != null) {
                if (e.getCurrentItem().getType() == Material.NAME_TAG) {
                    List<String> lores = e.getCurrentItem().getItemMeta().getLore();
                    if (player.hasPermission("tags."+lores.get(0)) || lores.get(0).equals("reset")) {
                        if (Main.getConnection() == null) {
                            if (!lores.get(0).equals("reset")) {
                                if (Main.getChat() != null) {
                                    Main.getMainClass().tagsyml.set(player.getUniqueId().toString(), lores.get(0));
                                    Main.getMainClass().saveYml(Main.getMainClass().tagsyml, Main.getMainClass().PlayerTags);
                                    player.sendMessage(ChatColor.GREEN + "Successfully set tag!");
                                    String tagText = ChatColor.translateAlternateColorCodes('&', "&r"+Main.getMainClass().tagListYML.getString(Main.getMainClass().tagsyml.getString(player.getUniqueId().toString())));
                                    if (!Main.getMainClass().getConfig().getBoolean("prefix")) {
                                        Main.getChat().setPlayerSuffix(player, tagText);
                                    } else {
                                        Main.getChat().setPlayerPrefix(player, tagText);
                                    }

                                    player.closeInventory();
                                } else {
                                    player.closeInventory();
                                    player.sendMessage(ChatColor.RED + "No chat plugin found, did not set!");
                                }
                            } else {
                                if (Main.getChat() != null) {
                                    Main.getMainClass().tagsyml.set(player.getUniqueId().toString(), lores.get(0));
                                    Main.getMainClass().saveYml(Main.getMainClass().tagsyml, Main.getMainClass().PlayerTags);
                                    if (!Main.getMainClass().getConfig().getBoolean("prefix")) {
                                        Main.getChat().setPlayerSuffix(player, "");
                                    } else {
                                        Main.getChat().setPlayerPrefix(player, "");
                                    }
                                    player.sendMessage(ChatColor.GREEN + "Successfully removed tag!");
                                    player.closeInventory();
                                } else {
                                    player.closeInventory();
                                    player.sendMessage(ChatColor.RED + "No chat plugin found, did not set!");
                                }
                            }
                        } else {
                            // DataBase present
                            String tag = e.getCurrentItem().getItemMeta().getLore().get(0);
                            try {
                                // GET TAG
                                ResultSet rs = Main.prepareStatement("SELECT UUID FROM PLAYERTAGS WHERE UUID = '"+player.getUniqueId()+"';").executeQuery();
                                rs.next();
                                if (rs.getRow() == 0) {
                                    // INSERT PLAYER
                                    if (Main.getChat() != null) {
                                        Main.prepareStatement("INSERT INTO PLAYERTAGS(UUID, TAG) VALUES ('"+player.getUniqueId()+"', '"+tag+"');").executeUpdate();
                                        player.sendMessage(ChatColor.GREEN + "Successfully set tag!");
                                        player.closeInventory();
                                    } else {
                                        player.closeInventory();
                                        player.sendMessage(ChatColor.RED + "No chat plugin found, did not set!");
                                    }
                                } else {
                                    // UPDATE TAG
                                    if (Main.getChat() != null) {
                                        Main.prepareStatement("UPDATE PLAYERTAGS SET TAG = '"+tag+"' WHERE UUID = '"+player.getUniqueId()+"';").executeUpdate();
                                        player.sendMessage(ChatColor.GREEN + "Successfully set tag!");
                                        player.closeInventory();
                                    } else {
                                        player.closeInventory();
                                        player.sendMessage(ChatColor.RED + "No chat plugin found, did not set!");
                                    }
                                }

                            } catch (SQLException a) {
                                a.printStackTrace();
                            }

                        }
                    } else {
                        player.sendMessage(ChatColor.RED + "You don't have permission to use this tag!");
                    }

                }

                if (e.getCurrentItem().getType() == Material.EMERALD_BLOCK) {
                    UserInterface.applyTUI(player, Integer.parseInt(e.getCurrentItem().getItemMeta().getLore().get(0)));
                }

                if (e.getCurrentItem().getType() == Material.REDSTONE_BLOCK) {
                    UserInterface.applyTUI(player, Integer.parseInt(e.getCurrentItem().getItemMeta().getLore().get(0)));
                }

            }



            e.setCancelled(true);
        }


    }
}
