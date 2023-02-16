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

    // Removes quotes from strings
    private String removeQuatations(String string) {
        if (string.contains("\"")) {
            return string.replace('"', ' ');
        } else {
            return string;
        }
    }


    // Check if the string is a number
    public static boolean isNumeric(String strNum) {
        if (strNum == null) {
            return false;
        }
        try {
            Integer.parseInt(strNum);
        } catch (NumberFormatException nfe) {
            return false;
        }
        return true;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

        if (sender instanceof Player) {
            Player player = (Player) sender;

            //Menu Command
            if (args.length == 0) {
                if (player.hasPermission("tags.use") || player.isOp()) {
                    player.closeInventory();
                    UserInterface.applyTUI(player, 1);

                } else {
                    player.sendMessage(ChatColor.RED + "You don't have permission to use tags!");
                }

            }

            if (args.length == 1) {

                // Reload Command
                if (args[0].equalsIgnoreCase("reload")) {
                    if (player.hasPermission("tags.reload")) {
                        Bukkit.getPluginManager().disablePlugin(plugin);
                        Bukkit.getPluginManager().enablePlugin(plugin);
                        player.sendMessage("Successfully reloaded Tags!");
                    }

                }

                //List COmmand
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

                // Help Command
                // TODO Add the group commands.
                if (args[0].equalsIgnoreCase("help")) {
                    if (player.hasPermission("tags.help")) {
                        player.sendMessage(ChatColor.YELLOW
                                +"/tags - shows the tags GUI!\n"
                                +"/tags reload - reloads the Tags plugin!\n"
                                +"/tags remove (tagname) - removes a tag!\n"
                                +"/tags create (tagname) (displaytext) - creates a tag!\n"
                                +"/tags list - shows a list of tags!\n"
                                +"/tags help - displays this list of commands!n");
                    } else {
                        player.sendMessage(ChatColor.YELLOW + "/tags - shows the tags GUI!");
                    }
                }


                // Page Stuff
                if (isNumeric(args[0])  && player.hasPermission("tags.use")) {
                    Integer pagenumber = Integer.parseInt(args[0]);
                    // send number into TUI method


                    if (pagenumber > 0 ) {
                        UserInterface.applyTUI(player, pagenumber);
                    } else {
                        player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&f[&aTAGS&f] &7- &c Tag page must be a positive number!"));
                    }

                }

            }

            if (args.length == 2) {

                // Tag Remove Command
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

                // Tag create command
                if (args[0].equalsIgnoreCase("create")) {
                    if (player.hasPermission("tags.edit"))	{
                        String name = args[1];
                        if (!name.contains(",") || name.toLowerCase() != ("tags")) {
                            String[] argsArray = Arrays.copyOfRange(args, 2, args.length);
                            String infoa = StringUtils.join(argsArray, " ");
                            String info = this.removeQuatations(infoa);
                            if (Main.getConnection() == null) {
                                List<String> tags = Main.getMainClass().tagListYML.getStringList("tags");
                                if (tags.contains(name)) {
                                    Main.getMainClass().tagListYML.set(name, info);
                                    Main.getMainClass().tagListYML.set("tags", tags);
                                    Main.getMainClass().saveYml(Main.getMainClass().tagListYML, Main.getMainClass().TagsList);
                                    player.sendMessage(ChatColor.GREEN + "Successfully edited tag " + ChatColor.BLUE + name + ChatColor.GREEN + "!");
                                } else {

                                    Main.getMainClass().tagListYML.set(name, info);
                                    tags.add(name);
                                    Main.getMainClass().tagListYML.set("tags", tags);
                                    Main.getMainClass().saveYml(Main.getMainClass().tagListYML, Main.getMainClass().TagsList);
                                    player.sendMessage(ChatColor.GREEN + "Successfully created tag " + ChatColor.BLUE + name + ChatColor.GREEN + "!");
                                }
                            } else {
                                try {
                                    ResultSet res = Main.prepareStatement("SELECT count(TAG) FROM TAGS WHERE tag = '" + name + "';").executeQuery();
                                    res.next();
                                    if (res.getInt(1) == 0) {
                                        Main.prepareStatement("INSERT INTO `TAGS` (`TAG`, `DISPLAYTEXT`) VALUES ('" + name + "', '" + info + "');").executeUpdate();
                                        player.sendMessage(ChatColor.GREEN + "Successfully created tag!");
                                    } else if (res.getInt(1) != 0) {
                                        Main.prepareStatement("UPDATE TAGS SET DISPLAYTEXT = '" + info + "' WHERE TAG = '" + name + "';").executeUpdate();
                                        player.sendMessage(ChatColor.GREEN + "Successfully updated tag!");
                                    }
                                } catch (SQLException e) {
                                    e.printStackTrace();
                                }
                            }
                        } else {
                            player.sendMessage(ChatColor.RED+ "Tag name cannot equal 'tags' or contain commas.");
                        }
                    } else {
                        player.sendMessage(ChatColor.RED+"You do not have permission for this command!");
                    }
                }
            }

            if (args.length >= 3) {
                if (args[0].equalsIgnoreCase("group")) { // Check argument
                    if (player.hasPermission("tags.edit")) { // Check permission

                        if (args.length == 3) {
                            if (args[1].equalsIgnoreCase("create")) { // Check which group command
                                if (!args[2].equalsIgnoreCase("groups") || args[2].contains(",")) { // Check if group name is one of the config variables.
                                    List<String> groupContent = new ArrayList<>(); // Placeholder info for the group.
                                    List<String> groups = Main.getMainClass().groupListYML.getStringList("groups"); // List of groups from config.
                                    if (!groups.contains(args[2])) { // Check if group Exists
                                        groups.add(args[2]); // Add group to list.

                                        Main.getMainClass().groupListYML.set("groups", groups); // Set in config.
                                        Main.getMainClass().saveYml(Main.getMainClass().groupListYML, Main.getMainClass().groupList); // Save config.

                                        Main.getMainClass().groupListYML.set(args[2], groupContent); // Set group in config.
                                        Main.getMainClass().saveYml(Main.getMainClass().groupListYML, Main.getMainClass().groupList); // Save config

                                        player.sendMessage(ChatColor.GREEN + "Successfully created group " + ChatColor.BLUE + args[2] + ChatColor.GREEN + "!");
                                    } else {
                                        player.sendMessage(ChatColor.RED + "This group already exists!");
                                    }
                                } else {
                                    player.sendMessage(ChatColor.RED + "Group name cannot be 'groups' or contain commas.");
                                }
                            }

                            if (args[1].equalsIgnoreCase("remove")) {
                                if (Main.getMainClass().groupListYML.getStringList("groups").contains(args[2])) { // If group exists.
                                    List<String> groups = Main.getMainClass().groupListYML.getStringList("groups");
                                    if (groups.contains(args[2])) {
                                        groups.remove(args[2]);
                                        Main.getMainClass().groupListYML.set("groups", groups); // Remove from group list.
                                        Main.getMainClass().groupListYML.set(args[2], null); // Set to null.
                                        Main.getMainClass().saveYml(Main.getMainClass().groupListYML, Main.getMainClass().groupList);
                                        player.sendMessage(ChatColor.GREEN + "Successfully removed group " + ChatColor.BLUE + args[2] + ChatColor.GREEN + "!");
                                    } else {
                                        player.sendMessage(ChatColor.BLUE + args[2] + ChatColor.RED + " is not in group "+ChatColor.BLUE+args[1]+ChatColor.RED+"!");
                                    }
                                } else {
                                    player.sendMessage(ChatColor.RED + "This group does not exist!");
                                }
                            }
                        }

                        if (args.length == 4) {
                            if (args[1].equalsIgnoreCase("add")) {
                                List<String> tags = Main.getMainClass().tagListYML.getStringList("tags"); // Tag list
                                List<String> groups = Main.getMainClass().groupListYML.getStringList("groups"); // Group List
                                String groupName = args[2];
                                String tagID = args[3];

                                if (groups.contains(groupName)) { // Check if group exists in config.

                                    List<String> selectedGroup = Main.getMainClass().groupListYML.getStringList(groupName); // Get the group.
                                    if (tags.contains(tagID)) { // Check if tag exists.
                                        if (!selectedGroup.contains(tagID)) { // Check if tag exists in group.
                                            selectedGroup.add(tagID);
                                            Main.getMainClass().groupListYML.set(groupName, selectedGroup); // Set the updated group in config.
                                            Main.getMainClass().saveYml(Main.getMainClass().groupListYML, Main.getMainClass().groupList); // Save config.
                                        } else {
                                            player.sendMessage(ChatColor.RED + "This tag is already present in this group.");
                                        }
                                    } else {
                                        player.sendMessage(ChatColor.RED + "This tag does not exist!");
                                    }


                                } else {
                                    player.sendMessage(ChatColor.RED + "This group does not exist!");
                                }

                            }
                        }
                    }
                }
            }






        } else {
            sender.sendMessage("Only in-game players can use this command!");
        }


        return false;
    }

}
