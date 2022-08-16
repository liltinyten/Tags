package self.liltinyten.tags;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public class TagsCommand implements CommandExecutor {
    final Plugin plugin = Bukkit.getPluginManager().getPlugin("Tags");

    private String removeQuatations(String string) {
        if (string.contains("\"")) {
            return string.replace('"', ' ');
        } else {
            return string;
        }
    }

    public static boolean isNumeric(String strNum) {
        if (strNum == null) {
            return false;
        }
        try {
            @SuppressWarnings("unused")
            Integer d = Integer.parseInt(strNum);
        } catch (NumberFormatException nfe) {
            return false;
        }
        return true;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

        if (sender instanceof Player) {
            Player player = (Player) sender;

            if (args.length == 0) {
                if (player.hasPermission("tags.use") || player.isOp()) {
                    player.closeInventory();
                    UserInterface.applyTUI(player, 1);

                } else {
                    player.sendMessage(ChatColor.RED + "You don't have permission to use tags!");
                }

            }
            if (args.length == 1) {
                if (args[0].equalsIgnoreCase("reload")) {
                    if (player.hasPermission("tags.reload")) {
                        Bukkit.getPluginManager().disablePlugin(plugin);
                        Bukkit.getPluginManager().enablePlugin(plugin);
                        player.sendMessage("Successfully reloaded Tags!");
                    }

                }

                if (args[0].equalsIgnoreCase("list")) {
                    if (player.hasPermission("tags.list")) {
                        if (Main.getConnection() == null) {
                            player.sendMessage(ChatColor.AQUA+"Showing list of "+ Main.getMainClass().tagListYML.getStringList("tags").size() +" tags:");
                            player.sendMessage(ChatColor.BLUE + Main.getMainClass().tagListYML.getStringList("tags").toString());
                        } else {

                            try {
                                List<String> list = new ArrayList<>();
                                ResultSet res = Main.getConnection().createStatement().executeQuery("SELECT TAG FROM TAGS");
                                while (res.next()) {
                                    list.add(res.getString("tag"));
                                }
                                player.sendMessage(ChatColor.AQUA+"Showing list of "+ list.size() +" tags:");
                                player.sendMessage(ChatColor.BLUE + list.toString());
                            } catch (SQLException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }

                if (args[0].equalsIgnoreCase("help")) {
                    if (player.hasPermission("tags.help")) {
                        player.sendMessage(ChatColor.YELLOW + "/tags - shows the tags GUI!");
                        player.sendMessage(ChatColor.YELLOW + "/tags reload - reloads the Tags plugin!");
                        player.sendMessage(ChatColor.YELLOW + "/tags remove (tagname) - removes a tag!");
                        player.sendMessage(ChatColor.YELLOW + "/tags create (tagname) (displaytext) - creates a tag!");
                        player.sendMessage(ChatColor.YELLOW + "/tags list - shows a list of tags!");
                        player.sendMessage(ChatColor.YELLOW + "/tags help - displays this list of commands!");
                    }
                }
                // create pages things here
                /*
                 * Make a isNumeric function to check if the string can be converted into an integer. Afterwards, you
                 * need to make the tag inventory able to select the tags to include in the inventory.
                 */
                if (isNumeric(args[0])  && player.hasPermission("tags.use")) {
                    Integer pagenumber = Integer.parseInt(args[0]);
                    // send number into TUI method

                    if (pagenumber > 0 ) {
                        UserInterface.applyTUI(player, pagenumber);
                    } else {
                        player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&f[&aTAGS&f] &7- &c Tag page must be a positive number!"));
                    }
                } else {
                    player.sendMessage( ChatColor.translateAlternateColorCodes('&', "&f[&aTAGS&f] &7- &c Tag page must be a positive number!"));
                }

            }

            if (args.length == 2) {
                if (args[0].equalsIgnoreCase("remove")) {
                    if (player.hasPermission("tags.edit")) {

                        String name = args[1];
                        if (Main.getConnection() == null) {
                            List<String> tags = Main.getMainClass().tagListYML.getStringList("tags");
                            if (tags.contains(name)) {
                                tags.remove(name);
                                Main.getMainClass().tagListYML.set("tags", tags);
                                Main.getMainClass().saveYml(Main.getMainClass().tagListYML, Main.getMainClass().TagsList);
                                player.sendMessage(ChatColor.GREEN+"You have successfully removed "+ChatColor.BLUE+name+ChatColor.GREEN+ " from tags!");
                            } else {
                                player.sendMessage(ChatColor.RED+"Tag not found!");
                            }
                        } else {

                            try {
                                ResultSet res = Main.prepareStatement("SELECT count(*) FROM TAGS WHERE TAG = '"+name+"';").executeQuery();
                                res.next();
                                if (res.getRow() == 0) {
                                    player.sendMessage(ChatColor.RED + "Tag not found!");
                                } else {
                                    Main.prepareStatement("DELETE FROM TAGS WHERE TAGS.TAG = '"+ name +"';").executeUpdate();
                                    player.sendMessage(ChatColor.RED + "Tag removed!");
                                }


                            } catch (SQLException e) {
                                e.printStackTrace();
                            }


                        }
                    } else {
                        player.sendMessage(ChatColor.RED+"You do not have permission for this command!");
                    }
                }


            }


            if (args.length > 2) {
                if (args[0].equalsIgnoreCase("create")) {
                    if (player.hasPermission("tags.edit"))	{
                        String name = args[1];
                        String[] argsArray = Arrays.copyOfRange(args, 2, args.length);
                        String infoa = StringUtils.join(argsArray, " ");
                        String info = this.removeQuatations(infoa);
                        if (Main.getConnection() == null) {
                            List<String> tags = Main.getMainClass().tagListYML.getStringList("tags");
                            if (tags.contains(name)) {
                                Main.getMainClass().tagListYML.set(name, info);
                                Main.getMainClass().tagListYML.set("tags", tags);
                                Main.getMainClass().saveYml(Main.getMainClass().tagListYML, Main.getMainClass().TagsList);
                                player.sendMessage(ChatColor.GREEN + "Successfully edited tag "+ChatColor.BLUE+name+ChatColor.GREEN+"!");
                            } else {

                                Main.getMainClass().tagListYML.set(name, info);
                                tags.add(name);
                                Main.getMainClass().tagListYML.set("tags", tags);
                                Main.getMainClass().saveYml(Main.getMainClass().tagListYML, Main.getMainClass().TagsList);
                                player.sendMessage(ChatColor.GREEN+"Successfully created tag "+ChatColor.BLUE+name+ChatColor.GREEN+"!");
                            }
                        } else {
                            try {
                                ResultSet res = Main.prepareStatement("SELECT count(TAG) FROM TAGS WHERE tag = '"+name+"';").executeQuery();
                                res.next();
                                if (res.getInt(1) == 0) {
                                    Main.prepareStatement("INSERT INTO `TAGS` (`TAG`, `DISPLAYTEXT`) VALUES ('"+name+"', '"+info+"');").executeUpdate();
                                    player.sendMessage(ChatColor.GREEN + "Successfully created tag!");
                                } else if (res.getInt(1) != 0) {
                                    Main.prepareStatement("UPDATE TAGS SET DISPLAYTEXT = '"+info+"' WHERE TAG = '"+name+"';").executeUpdate();
                                    player.sendMessage(ChatColor.GREEN + "Successfully updated tag!");
                                }
                            } catch (SQLException e) {
                                e.printStackTrace();
                            }
                        }
                    } else {
                        player.sendMessage(ChatColor.RED+"You don not have permission for this command!");
                    }
                }
            }






        } else {
            sender.sendMessage("Only in-game players can use this command!");
        }


        return false;
    }

}
