package self.liltinyten.tags;

import java.util.Arrays;
import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import com.google.common.collect.Lists;

public class CommandCompleter implements TabCompleter {

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
        // the list that contains the possible arguments
        List<String> arguments = Arrays.asList("reload", "remove", "create", "list", "help");
        List<String> completions = Lists.newArrayList();

        if (args.length == 1) {
            for (String s:arguments) {
                if(s.toLowerCase().startsWith(args[0].toLowerCase()))
                    completions.add(s);

            }
            return completions;
        }

        return null;
    }

}
