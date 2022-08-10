package self.liltinyten.tags;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import com.google.common.collect.Lists;

public class CommandCompleter implements TabCompleter {

    public List<String> getAllTags() throws SQLException {
        List<String> tags = Lists.newArrayList();

        // Fixed

        // Get From Database
        if (Main.getConnection() != null) {
            ResultSet res = Main.getConnection().createStatement().executeQuery("SELECT TAG FROM TAGS");
            while (res.next()) {
                tags.add(res.getString("tag"));
            }

        // Database is offline
        } else {
            return Main.getMainClass().getTagListYML().getStringList("tags");
        }

        return  tags;
    }



    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
        // the list that contains the possible arguments
        List<String> arguments = Arrays.asList("reload", "remove", "create", "list", "help");

        // The list to return
        List<String> completions = Lists.newArrayList();

        if (args.length == 1) {
            for (String s:arguments) {
                if(s.toLowerCase().startsWith(args[0].toLowerCase()))
                    completions.add(s);

            }
            return completions;
        }

        if (args.length == 2) {
            if (args[0].toLowerCase().startsWith("remove") || args[0].toLowerCase().startsWith("create")) {
                try {

                    // The list with tag names
                    List<String> temp = getAllTags();
                    for (String s:temp) {
                        if (s.toLowerCase().startsWith(args[1].toLowerCase())) {
                            completions.add(s);
                        }
                    }
                    return completions;
                } catch (SQLException e) {
                    System.out.println(ChatColor.RED + "[ERROR] " + ChatColor.WHITE + "[TAGS] - There was an error fetching tags completions!" );
                }
            }
        }

        return null;
    }

}
