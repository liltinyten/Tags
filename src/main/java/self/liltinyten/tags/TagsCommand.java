package self.liltinyten.tags;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
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
                                +"/tags help - displays this list of commands!\n"
                                +"/tags group create (groupname)\n"
                                +"/tags group delete (groupname)\n"
                                +"/tags group addtag (groupname) (tagname)\n"
                                +"/tags group removetag (groupname) (tagname)\n"
                                +"/tags user (username) addtag (tagname)\n"
                                +"/tags user (username) removetag (tagname)\n"
                                +"/tags user (username) addgroup (groupname)\n"
                                +"/tags user (username) removegroup (groupname)\n");
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

                //NOTICE This is bugged see issue No. 13.
                //Tag Remove Command
                if (args[0].equalsIgnoreCase("remove")) {
                    if (player.hasPermission("tags.edit")) {

                        String name = args[1];
                        if (Main.getConnection() == null) {
                            List<String> tags = Main.getMainClass().tagListYML.getStringList("tags");
                            if (tags.contains(name)) {
                                tags.remove(name);
                                Main.getMainClass().tagListYML.set("tags", tags);
                                Main.getMainClass().saveYml(Main.getMainClass().tagListYML, Main.getMainClass().TagsList);

                                // Remove from group
                                List<String> groups = Main.getMainClass().groupListYML.getStringList("groups");
                                for (String group:groups) {
                                    List<String> groupTags = Main.getMainClass().groupListYML.getStringList(group);
                                    if (groupTags.contains(name)) {
                                        groupTags.remove(name);
                                        Main.getMainClass().groupListYML.set(group, groupTags);
                                    }
                                }
                                Main.getMainClass().saveYml(Main.getMainClass().groupListYML, Main.getMainClass().groupList);


                                // Remove from player permissions.
                                {
                                    Set<String> uuids = Main.getMainClass().tagPermissionListYML.getKeys(false);
                                    for (String id : uuids) {
                                            List<String> permissions = Main.getMainClass().tagPermissionListYML.getStringList(id);
                                            if (permissions.contains(name)) {
                                                permissions.remove(name);
                                                Main.getMainClass().tagPermissionListYML.set(id, permissions);
                                            }
                                    }
                                    Main.getMainClass().saveYml(Main.getMainClass().getTagListYML(), Main.getMainClass().tagPermissionList);
                                }


                                // Remove from current permissions.
                                Set<String> uuids = Main.getMainClass().tagsyml.getKeys(false);
                                for (String id: uuids) {
                                    String currentPermission = Main.getMainClass().tagsyml.getString(id);
                                    if (currentPermission == name) {
                                        currentPermission = "reset";
                                    }
                                    Main.getMainClass().tagsyml.set(id, currentPermission);
                                }
                                Main.getMainClass().saveYml(Main.getMainClass().tagsyml, Main.getMainClass().PlayerTags);


                                player.sendMessage(ChatColor.GREEN+"You have successfully removed "+ChatColor.BLUE+name+ChatColor.GREEN+ " from tags!");
                            } else {
                                player.sendMessage(ChatColor.RED+"Tag not found!");
                            }
                        } else {

                            try {
                                // Removing Tag
                                ResultSet res = Main.prepareStatement("SELECT count(*) FROM TAGS WHERE TAG = '"+name+"';").executeQuery();
                                res.next();
                                if (res.getRow() == 0) {
                                    player.sendMessage(ChatColor.RED + "Tag not found!");
                                } else {
                                    Main.prepareStatement("DELETE FROM TAGS WHERE TAGS.TAG = '"+ name +"';").executeUpdate();
                                    player.sendMessage(ChatColor.RED + "Tag removed!");
                                }

                                // Removing from group
                                ResultSet groups = Main.prepareStatement("SELECT * FROM GROUPS WHERE TAGS LIKE '%"+name+"%';").executeQuery();
                                while (groups.next()) {
                                    if (groups.getRow() != 0) {
                                        //TODO CHECK FUNCTIONALITY
                                        Main.prepareStatement("UPDATE GROUPS SET '"+groups.getString("groupname")+"' = '' WHERE TAGS ="+groups.getString("tags")).executeUpdate();
                                    }

                                }


                                /* TODO Remove the tag from any group that contains it and remove set any player who has the tag as their current
                                    permission to "reset".
                                 */







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
                        if (!name.contains("*") && name.toLowerCase() != ("tags")) {
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
                                        Main.prepareStatement("INSERT INTO `TAGS` VALUES ('" + name + "', '" + info + "');").executeUpdate();
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
                            player.sendMessage(ChatColor.RED+ "Tag name cannot equal 'tags' or contain asterisks.");
                        }
                    } else {
                        player.sendMessage(ChatColor.RED+"You do not have permission for this command!");
                    }
                }
            }


            //GROUP STUFF
            if (args.length >= 3) {
                if (args[0].equalsIgnoreCase("group")) { // Check argument
                    if (player.hasPermission("tags.edit")) { // Check permission

                        if (args.length == 3) {

                            // Group create command.
                            if (args[1].equalsIgnoreCase("create")) { // Check which group command
                                if (!args[2].equalsIgnoreCase("groups") && !args[2].contains("*")) { // Check if group name is one of the config variables.

                                    String name = args[2];

                                    // Using Config
                                    if (Main.getConnection() == null) {
                                        List<String> groupContent = new ArrayList<>(); // Placeholder info for the group.
                                        List<String> groups = Main.getMainClass().groupListYML.getStringList("groups"); // List of groups from config.
                                        if (!groups.contains(name)) { // Check if group Exists
                                            groups.add(name); // Add group to list.

                                            Main.getMainClass().groupListYML.set("groups", groups); // Set in config.
                                            Main.getMainClass().saveYml(Main.getMainClass().groupListYML, Main.getMainClass().groupList); // Save config.

                                            Main.getMainClass().groupListYML.set(name, groupContent); // Set group in config.
                                            Main.getMainClass().saveYml(Main.getMainClass().groupListYML, Main.getMainClass().groupList); // Save config

                                            player.sendMessage(ChatColor.GREEN + "Successfully created group " + ChatColor.BLUE + name + ChatColor.GREEN + "!");
                                        } else {
                                            player.sendMessage(ChatColor.RED + "This group already exists!");
                                        }
                                    } else {
                                        // Using Database
                                        try {
                                            ResultSet res = Main.prepareStatement("SELECT count(GROUPNAME) FROM GROUPS WHERE GROUPNAME = '" + name + "';").executeQuery();
                                            res.next();
                                            if (res.getInt(1) == 0) {
                                                Main.prepareStatement("INSERT INTO `GROUPS` (`GROUPNAME`, `TAGS`) VALUES ('" + name + "', ' ');").executeUpdate();
                                                player.sendMessage(ChatColor.GREEN + "Successfully created group!");
                                            } else if (res.getInt(1) != 0) {
                                                player.sendMessage(ChatColor.RED + "This group already exists!");
                                            }
                                        } catch (SQLException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                } else {
                                    player.sendMessage(ChatColor.RED + "Group name cannot be 'groups' or contain asterisks.");
                                }
                            }


                            // NOTICE This is bugged see issue No. 13
                            // Group remove command
                            if (args[1].equalsIgnoreCase("delete")) {

                                // USING CONFIG
                                if (Main.getConnection() == null) {
                                    if (Main.getMainClass().groupListYML.getStringList("groups").contains(args[2])) { // If group exists.
                                        List<String> groups = Main.getMainClass().groupListYML.getStringList("groups");
                                        if (groups.contains(args[2])) {
                                            groups.remove(args[2]);
                                            Main.getMainClass().groupListYML.set("groups", groups); // Remove from group list.
                                            Main.getMainClass().groupListYML.set(args[2], null); // Set to null.
                                            Main.getMainClass().saveYml(Main.getMainClass().groupListYML, Main.getMainClass().groupList);
                                            player.sendMessage(ChatColor.GREEN + "Successfully removed group " + ChatColor.BLUE + args[2] + ChatColor.GREEN + "!");
                                        } else {
                                            player.sendMessage(ChatColor.BLUE + args[2] + ChatColor.RED + " is not in group " + ChatColor.BLUE + args[1] + ChatColor.RED + "!");
                                        }
                                    } else {
                                        player.sendMessage(ChatColor.RED + "This group does not exist!");
                                    }


                                } else {
                                    // USING DATABASE
                                    try  {
                                        ResultSet res = Main.prepareStatement("SELECT count(*) FROM GROUPS WHERE GROUPNAME = '"+args[2]+"';").executeQuery();
                                        res.next();
                                        if (res.getRow() == 0) {
                                            player.sendMessage(ChatColor.RED + "Group not found!");
                                        } else {
                                            Main.prepareStatement("DELETE FROM GROUPS WHERE GROUPS.GROUPNAME = '"+ args[2] +"';").executeUpdate();
                                            player.sendMessage(ChatColor.RED + "Group removed!");
                                        }
                                    } catch (SQLException e) {
                                        e.printStackTrace();
                                    }

                                }
                            }
                        }

                        if (args.length == 4) {
                            // Add to group.
                            if (args[1].equalsIgnoreCase("add")) {
                                String groupName = args[2];
                                String tagID = args[3];

                                // Using CONFIG
                                if (Main.getConnection() == null) {
                                    List<String> tags = Main.getMainClass().tagListYML.getStringList("tags"); // Tag list
                                    List<String> groups = Main.getMainClass().groupListYML.getStringList("groups"); // Group List


                                    if (groups.contains(groupName)) { // Check if group exists in config.

                                        List<String> selectedGroup = Main.getMainClass().groupListYML.getStringList(groupName); // Get the group.
                                        if (tags.contains(tagID)) { // Check if tag exists.
                                            if (!selectedGroup.contains(tagID)) { // Check if tag exists in group.
                                                selectedGroup.add(tagID);
                                                Main.getMainClass().groupListYML.set(groupName, selectedGroup); // Set the updated group in config.
                                                Main.getMainClass().saveYml(Main.getMainClass().groupListYML, Main.getMainClass().groupList); // Save config.
                                                player.sendMessage(ChatColor.GREEN + "Successfully added tag to group!");


                                            } else {
                                                player.sendMessage(ChatColor.RED + "This tag is already present in this group.");
                                            }
                                        } else {
                                            player.sendMessage(ChatColor.RED + "This tag does not exist!");
                                        }


                                    } else {
                                        player.sendMessage(ChatColor.RED + "This group does not exist!");
                                    }
                                } else {
                                    // Using DATABASE

                                    try {
                                        ResultSet checkGroupExist = Main.prepareStatement("SELECT COUNT(*) FROM TAGS WHERE TAG = '"+tagID+"';").executeQuery();
                                        checkGroupExist.next();
                                        if (checkGroupExist.getRow() != 0) {
                                            ResultSet res = Main.prepareStatement("SELECT TAGS FROM GROUPS WHERE GROUPNAME = '" + groupName + "';").executeQuery();
                                            res.next();
                                            if (res.getRow() != 0 ) {


                                                String groupTagsString = res.getString("tags");
                                                String[] groupTags = groupTagsString.split(" ");
                                                List<String> groupArray = new ArrayList<>();

                                                if (!groupArray.contains(tagID)) {
                                                    for (String tag : groupTags) {
                                                        groupArray.add(tag);
                                                    }

                                                    String newTags = tagID;

                                                    for (String tag : groupArray) {
                                                        newTags += " " + tag;
                                                    }
                                                    Main.prepareStatement("UPDATE GROUPS SET TAGS = '" + newTags + "' WHERE GROUPNAME = '" + groupName + "';").executeUpdate();
                                                    player.sendMessage(ChatColor.GREEN + "Successfully added tag to group!");

                                                } else {
                                                    player.sendMessage(ChatColor.RED + "This tag is already present in this group!");
                                                }
                                            } else {
                                                player.sendMessage(ChatColor.RED+"This group does not exist!");
                                            }
                                        } else {
                                            player.sendMessage(ChatColor.RED+"This tag does not exist!");
                                        }
                                    } catch (SQLException e) {
                                        e.printStackTrace();
                                    }

                                }

                            }


                            // Remove from group
                            if (args[1].equalsIgnoreCase("remove")) {
                                String groupName = args[2];
                                String tagID = args[3];

                                // Using CONFIG
                                if (Main.getConnection() == null) {
                                    List<String> tags = Main.getMainClass().tagListYML.getStringList("tags"); // Tag list
                                    List<String> groups = Main.getMainClass().groupListYML.getStringList("groups"); // Group List


                                    if (groups.contains(groupName)) { // Check if group exists in config.

                                        List<String> selectedGroup = Main.getMainClass().groupListYML.getStringList(groupName); // Get the group.
                                        if (tags.contains(tagID)) { // Check if tag exists.
                                            if (selectedGroup.contains(tagID)) { // Check if tag exists in group.
                                                selectedGroup.remove(tagID);
                                                Main.getMainClass().groupListYML.set(groupName, selectedGroup); // Set the updated group in config.
                                                Main.getMainClass().saveYml(Main.getMainClass().groupListYML, Main.getMainClass().groupList); // Save config.
                                                player.sendMessage(ChatColor.GREEN + "Successfully removed tag from group!");
                                            } else {
                                                player.sendMessage(ChatColor.RED + "This tag does not exist in that group.");
                                            }
                                        } else {
                                            player.sendMessage(ChatColor.RED + "This tag does not exist!");
                                        }


                                    } else {
                                        player.sendMessage(ChatColor.RED + "This group does not exist!");
                                    }
                                } else {
                                    // Using DATABASE

                                    try {
                                        ResultSet checkGroupExist = Main.prepareStatement("SELECT COUNT(*) FROM TAGS WHERE TAG = '"+tagID+"';").executeQuery();
                                        checkGroupExist.next();
                                        if (checkGroupExist.getRow() != 0) {
                                            ResultSet res = Main.prepareStatement("SELECT TAGS FROM GROUPS WHERE GROUPNAME = '" + groupName + "';").executeQuery();
                                            res.next();
                                            if (res.getRow() != 0 ) {

                                                String groupTagsString = res.getString("tags");
                                                String[] groupTags = groupTagsString.split(" ");
                                                List<String> groupArray = new ArrayList<>();

                                                for (String tag : groupTags) {
                                                    groupArray.add(tag);
                                                }

                                                if (groupArray.contains(tagID)) {

                                                    groupArray.remove(tagID);

                                                    String newTags = "";

                                                    for (String tag : groupArray) {
                                                        newTags += tag+" ";
                                                    }
                                                    Main.prepareStatement("UPDATE GROUPS SET TAGS = '" + newTags + "' WHERE GROUPNAME = '" + groupName + "';").executeUpdate();
                                                    player.sendMessage(ChatColor.GREEN + "Successfully removed tag from group!");
                                                } else {
                                                    player.sendMessage(ChatColor.RED + "This tag is not present in this group!");
                                                }
                                            } else {
                                                player.sendMessage(ChatColor.RED+"This group does not exist!");
                                            }
                                        } else {
                                            player.sendMessage(ChatColor.RED+"This tag does not exist!");
                                        }
                                    } catch (SQLException e) {
                                        e.printStackTrace();
                                    }

                                }

                            }
                        }
                    }
                }

                // USER STUFF
                if (args[0].equalsIgnoreCase("user")) {

                    if (Bukkit.getPlayerExact(args[1]) != null) {
                        Player user = Bukkit.getPlayerExact(args[1]);

                        if (args[2].equalsIgnoreCase("addTag")) {
                            String tagId = args[3];
                            if (Main.getConnection() == null) {
                                // CONFIG
                                if (Main.getMainClass().tagListYML.getStringList("tags").contains(tagId)) {
                                    if (Main.getMainClass().tagPermissionListYML.contains(user.getUniqueId().toString())) {
                                        // If the user already has a list of permissions.
                                        List<String> permissions = Main.getMainClass().tagPermissionListYML.getStringList(user.getUniqueId().toString());
                                        if (!permissions.contains(tagId)) {
                                            permissions.add(tagId);
                                            Main.getMainClass().tagPermissionListYML.set(user.getUniqueId().toString(), permissions);
                                            Main.getMainClass().saveYml(Main.getMainClass().tagPermissionListYML, Main.getMainClass().tagPermissionList);
                                            player.sendMessage(ChatColor.GREEN + "Successfully added tag to player!");
                                        } else {
                                            player.sendMessage(ChatColor.RED + "This user already has access to this tag!");
                                        }


                                    } else {
                                        // If the user doesn't have a list of permissions.
                                        ArrayList<String> permissions = new ArrayList<>();
                                        permissions.add(tagId);
                                        Main.getMainClass().tagPermissionListYML.set(user.getUniqueId().toString(), permissions);
                                        Main.getMainClass().saveYml(Main.getMainClass().tagPermissionListYML, Main.getMainClass().tagPermissionList);
                                        player.sendMessage(ChatColor.GREEN + "Successfully added tag to user!");


                                    }
                                } else {
                                    player.sendMessage(ChatColor.RED + "The specified tag does not exist!");
                                }


                            } else {
                                // DATABASE

                                try {
                                    ResultSet res = Main.prepareStatement("SELECT TAG FROM TAGS WHERE TAG = '" + tagId + "';").executeQuery();
                                    res.next();
                                    if (res.getRow() != 0 ) {

                                        ResultSet permissions = Main.prepareStatement("SELECT PERMS FROM PERMISSIONS WHERE UUID = '"+user.getUniqueId().toString()+"';").executeQuery();
                                        permissions.next();
                                        // If a permission set exists.
                                        if (permissions.getRow() != 0) {
                                            String rawPermissions = permissions.getString("PERMS");
                                            String setPermssions = "";
                                            String[] permissionSet = rawPermissions.split(" ");
                                            ArrayList<String> permissionArray = new ArrayList<>();

                                            for (String perm: permissionSet) {
                                                permissionArray.add(perm);
                                            }

                                            if (!permissionArray.contains(tagId)) {
                                                permissionArray.add(tagId);

                                                for (String perm:permissionArray) {
                                                    setPermssions += perm+" ";
                                                }


                                                Main.prepareStatement("UPDATE PERMISSIONS SET PERMS = '"+setPermssions+"' WHERE UUID = '"+user.getUniqueId().toString()+"';").executeUpdate();
                                                player.sendMessage(ChatColor.GREEN + "Successfully added tag to user!");

                                            } else {
                                                player.sendMessage(ChatColor.RED + "This user already has permssion for this tag!");
                                            }


                                        } else {
                                            Main.prepareStatement("INSERT INTO `PERMISSIONS` (`UUID`, `PERMS`) VALUES ('" + user.getUniqueId().toString() + "', '"+tagId+"');").executeUpdate();
                                        }

                                    } else {
                                        player.sendMessage(ChatColor.RED+"This tag does not exist!");
                                    }
                                } catch (SQLException e) {
                                    e.printStackTrace();
                                }



                            }
                        }

                        if (args[2].equalsIgnoreCase("removeTag")) {
                            String tagId = args[3];

                            if (Main.getConnection() == null) {
                                // CONFIG
                                if (Main.getMainClass().tagListYML.getStringList("tags").contains(tagId)) {
                                    if (Main.getMainClass().tagPermissionListYML.contains(user.getUniqueId().toString())) {
                                        List<String> permissions = Main.getMainClass().tagPermissionListYML.getStringList(user.getUniqueId().toString());
                                        if (permissions.contains(tagId)) {
                                            permissions.remove(tagId);
                                            Main.getMainClass().tagPermissionListYML.set(user.getUniqueId().toString(), permissions);
                                            Main.getMainClass().saveYml(Main.getMainClass().tagPermissionListYML, Main.getMainClass().tagPermissionList);
                                            player.sendMessage(ChatColor.GREEN + "Successfully removed tag from player!");
                                        } else {
                                            player.sendMessage(ChatColor.RED + "This user does not have permission for this tag.");
                                        }

                                    } else {
                                        player.sendMessage(ChatColor.RED + "This user does not have permission for this tag.");
                                    }
                                } else {
                                    player.sendMessage(ChatColor.RED + "The specified tag does not exist!");
                                }
                            } else {
                                // DATABASE

                                try {
                                    ResultSet res = Main.prepareStatement("SELECT TAG FROM TAGS WHERE TAG = '" + tagId + "';").executeQuery();
                                    res.next();
                                    if (res.getRow() != 0 ) {
                                        ResultSet permissions = Main.prepareStatement("SELECT PERMS FROM PERMISSIONS WHERE UUID = '"+user.getUniqueId().toString()+"';").executeQuery();
                                        permissions.next();
                                        // If a permission set exists.
                                        if (permissions.getRow() != 0) {
                                            String rawPermissions = permissions.getString("PERMS");
                                            String setPermssions = "";
                                            String[] permissionSet = rawPermissions.split(" ");
                                            ArrayList<String> permissionArray = new ArrayList<>();

                                            for (String perm: permissionSet) {
                                                permissionArray.add(perm);
                                            }

                                            if (permissionArray.contains(tagId)) {
                                                permissionArray.remove(tagId);

                                                for (String perm:permissionArray) {
                                                    setPermssions += perm+" ";
                                                }


                                                Main.prepareStatement("UPDATE PERMISSIONS SET PERMS = '"+setPermssions+"' WHERE UUID = '"+user.getUniqueId().toString()+"';").executeUpdate();
                                                player.sendMessage((ChatColor.GREEN + "Successfully removed tag from user!"));
                                            } else {
                                                player.sendMessage(ChatColor.RED + "This user does not have permssion for this tag!");
                                            }


                                        } else {
                                            player.sendMessage(ChatColor.RED+"This user does not have permission for this tag!");
                                        }

                                    } else {
                                        player.sendMessage(ChatColor.RED+"This tag does not exist!");
                                    }
                                } catch (SQLException e) {
                                    e.printStackTrace();
                                }
                            }
                        }

                        if (args[2].equalsIgnoreCase("addGroup")) {
                            String groupId = args[3];
                            if (Main.getConnection() == null) {
                                // CONFIG
                                if (Main.getMainClass().groupListYML.getStringList("groups").contains(groupId)) {
                                    if (Main.getMainClass().tagPermissionListYML.contains(user.getUniqueId().toString())) {
                                        List<String> permissions = Main.getMainClass().tagPermissionListYML.getStringList(user.getUniqueId().toString());
                                        if (!permissions.contains("*"+groupId)) {
                                            permissions.add("*"+groupId);
                                            Main.getMainClass().tagPermissionListYML.set(user.getUniqueId().toString(), permissions);
                                            Main.getMainClass().saveYml(Main.getMainClass().tagPermissionListYML, Main.getMainClass().tagPermissionList);
                                            player.sendMessage(ChatColor.GREEN + "Successfully added player to group!");

                                        } else {
                                            player.sendMessage(ChatColor.RED+"This player is already part of this group!");
                                        }

                                    } else {
                                        ArrayList<String> permissions = new ArrayList<>();
                                        permissions.add("*"+groupId);
                                        Main.getMainClass().tagPermissionListYML.set(user.getUniqueId().toString(), permissions);
                                        Main.getMainClass().saveYml(Main.getMainClass().tagPermissionListYML, Main.getMainClass().tagPermissionList);
                                        player.sendMessage(ChatColor.GREEN + "Successfully added user to group!");

                                    }


                                } else {
                                    player.sendMessage(ChatColor.RED + "This group does not exist!");
                                }


                            } else {
                                // DATABASE

                                try {
                                        ResultSet res = Main.prepareStatement("SELECT TAGS FROM GROUPS WHERE GROUPNAME = '" + groupId + "';").executeQuery();
                                        res.next();
                                        if (res.getRow() != 0 ) {

                                            ResultSet permissions = Main.prepareStatement("SELECT PERMS FROM PERMISSIONS WHERE UUID = '"+user.getUniqueId().toString()+"';").executeQuery();
                                            permissions.next();
                                            // If a permission set exists.
                                            if (permissions.getRow() != 0) {
                                                String rawPermissions = permissions.getString("PERMS");
                                                String setPermssions = "";
                                                String[] permissionSet = rawPermissions.split(" ");
                                                ArrayList<String> permissionArray = new ArrayList<>();

                                                for (String perm: permissionSet) {
                                                    permissionArray.add(perm);
                                                }

                                                if (!permissionArray.contains("*"+groupId)) {
                                                    permissionArray.add("*"+groupId);

                                                    for (String perm:permissionArray) {
                                                        setPermssions += perm+" ";
                                                    }


                                                    Main.prepareStatement("UPDATE PERMISSIONS SET PERMS = '"+setPermssions+"' WHERE UUID = '"+user.getUniqueId().toString()+"';").executeUpdate();
                                                    player.sendMessage(ChatColor.GREEN + "Successfully added user to group!");
                                                } else {
                                                    player.sendMessage(ChatColor.RED + "This user already has permssion for this group!");
                                                }


                                            } else {
                                                Main.prepareStatement("INSERT INTO `PERMISSIONS` (`UUID`, `PERMS`) VALUES ('" + user.getUniqueId().toString() + "', '"+"*"+groupId+"');").executeUpdate();
                                            }

                                        } else {
                                            player.sendMessage(ChatColor.RED+"This group does not exist!");
                                        }
                                } catch (SQLException e) {
                                    e.printStackTrace();
                                }

                            }
                        }

                        if (args[2].equalsIgnoreCase("removeGroup")) {
                            String groupId = args[3];
                            if (Main.getConnection() == null) {

                                // CONFIG
                                if (Main.getMainClass().groupListYML.getStringList("groups").contains(groupId)) {
                                    if (Main.getMainClass().tagPermissionListYML.contains(user.getUniqueId().toString())) {
                                        List<String> permissions = Main.getMainClass().tagPermissionListYML.getStringList(user.getUniqueId().toString());
                                        if (permissions.contains("*"+groupId)) {
                                            permissions.remove("*"+groupId);
                                            Main.getMainClass().tagPermissionListYML.set(user.getUniqueId().toString(), permissions);
                                            Main.getMainClass().saveYml(Main.getMainClass().tagPermissionListYML, Main.getMainClass().tagPermissionList);
                                            player.sendMessage(ChatColor.GREEN + "Successfully removed player from group!");
                                        } else {
                                            player.sendMessage(ChatColor.RED+"This player is not part of this group!");
                                        }

                                    } else {
                                        player.sendMessage(ChatColor.RED +" This user does not have permission for this group!");

                                    }


                                } else {
                                    player.sendMessage(ChatColor.RED + "This group does not exist!");
                                }

                            } else {
                                // DATABASE

                                try {
                                    ResultSet res = Main.prepareStatement("SELECT TAGS FROM GROUPS WHERE GROUPNAME = '" + groupId + "';").executeQuery();
                                    res.next();
                                    if (res.getRow() != 0 ) {

                                        ResultSet permissions = Main.prepareStatement("SELECT PERMS FROM PERMISSIONS WHERE UUID = '"+user.getUniqueId().toString()+"';").executeQuery();
                                        permissions.next();
                                        // If a permission set exists.
                                        if (permissions.getRow() != 0) {
                                            String rawPermissions = permissions.getString("PERMS");
                                            String setPermssions = "";
                                            String[] permissionSet = rawPermissions.split(" ");
                                            ArrayList<String> permissionArray = new ArrayList<>();

                                            for (String perm: permissionSet) {
                                                permissionArray.add(perm);
                                            }

                                            if (permissionArray.contains("*"+groupId)) {
                                                permissionArray.remove("*"+groupId);

                                                for (String perm:permissionArray) {
                                                    setPermssions += perm+" ";
                                                }


                                                Main.prepareStatement("UPDATE PERMISSIONS SET PERMS = '"+setPermssions+"' WHERE UUID = '"+user.getUniqueId().toString()+"';").executeUpdate();
                                                player.sendMessage(ChatColor.GREEN + "Successfully removed user from group!");
                                            } else {
                                                player.sendMessage(ChatColor.RED + "This user does not have permssion for this group!");
                                            }


                                        } else {
                                            // No permissions for anything.
                                            player.sendMessage(ChatColor.RED+"This user does not have permission for this group!");
                                        }

                                    } else {
                                        player.sendMessage(ChatColor.RED+"This group does not exist!");
                                    }
                                } catch (SQLException e) {
                                    e.printStackTrace();
                                }
                            }
                        }


                    } else {
                        player.sendMessage(ChatColor.RED + "The given player could not be found!");
                    }






                }



            }







        } else {
            sender.sendMessage("Only in-game players can use this command!");
        }


        return false;
    }

}