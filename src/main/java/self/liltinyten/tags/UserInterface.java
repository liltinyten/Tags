package self.liltinyten.tags;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

public class UserInterface {

    public static void applyTUI(Player player, Integer page) {

        player.closeInventory();
        FileConfiguration config = Main.getMainClass().tagListYML;
        Connection connection = Main.getConnection();

        // BEGIN
        // TODO Maybe make the size static
        Inventory tui = Bukkit.createInventory(player, 54, ChatColor.translateAlternateColorCodes('&', "&aTags"));
        try {
            List<String> taglist = config.getStringList("tags");
            List<String> pagelist = new ArrayList<String>();
            List<String> pagelistdb = new ArrayList<String>();
            LinkedHashMap<String, String> pagemapdb = new LinkedHashMap<String, String>();

            // Reset Button
            ItemStack item = new ItemStack(Material.NAME_TAG);
            ItemMeta meta = item.getItemMeta();
            meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&4Reset"));
            List<String> lore = new ArrayList<>();
            lore.add("reset");
            meta.setLore(lore);
            item.setItemMeta(meta);
            tui.setItem(0, item);

            // Using config as storage
            if (connection == null) {
                for (String tag:taglist) {
                    /*
                     * IDEA
                     * Make a for loop to loop through a specific index of tags
                     * this will allow you to get the tags of that specific page
                     */

                    if ((taglist.indexOf(tag) <= 53 * page - 1) && (taglist.indexOf(tag) >= 53 * page - 53 ) ) {
                        pagelist.add(tag);
                    }
                }
                for (String ptag:pagelist) {
                    // Create NameTag and meta
                    ItemStack itemtag = new ItemStack(Material.NAME_TAG);
                    ItemMeta itemmeta = itemtag.getItemMeta();
                    // Set display name
                    itemmeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&r"+config.getString(ptag)));
                    // Set Lore
                    List<String> lores = new ArrayList<>();
                    lores.add(ptag);
                    if (!player.hasPermission("tags."+ptag)) {
                        lores.add("No permission!");
                    }
                    itemmeta.setLore(lores);
                    // Set Item's Meta
                    itemtag.setItemMeta(itemmeta);
                    // Set Item
                    // TODO Integrate pages here
                    tui.setItem(pagelist.indexOf(ptag)+1, itemtag);

                }



            } else {
                // if database is present
                try {
                    // IDEA Use an if statement to check the row int of the getRow() function. If it's of a certain number then use it.



                    ResultSet res = Main.prepareStatement("SELECT * FROM TAGS;").executeQuery();
                    while (res.next()) {
                        String tag = res.getString("tag");
                        String displayname = res.getString("displaytext");
                        if ((res.getRow() <= 54 * page - 1) && (res.getRow() >= 54 * page - 54 ) ) {
                            pagemapdb.put(tag, displayname);
                        }
                    }
                    for (String ptag:pagemapdb.keySet()) {
                        pagelistdb.add(ptag);
                    }

                    for (String ptag:pagelistdb) {
                        ItemStack itemtag = new ItemStack(Material.NAME_TAG);
                        ItemMeta itemmeta = itemtag.getItemMeta();
                        // Set display name
                        itemmeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&r"+pagemapdb.get(ptag)));
                        // Set Lore
                        List<String> lores = new ArrayList<>();
                        lores.add(ptag);
                        if (!player.hasPermission("tags."+ptag)) {
                            lores.add(ChatColor.RED + "No permission!");
                        }
                        itemmeta.setLore(lores);
                        // Set Item's Meta
                        itemtag.setItemMeta(itemmeta);
                        // Set Item
                        Main.tagslist.add(itemtag);
                        // TODO add pages here

                        tui.setItem(pagelistdb.indexOf(ptag)+1, itemtag);
                    }

                } catch (SQLException s) {
                    s.printStackTrace();
                }

            }
        } catch (Exception e) {

        }
        player.openInventory(tui);

    }

}
