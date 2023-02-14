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
    /*
        TODO: Make it so that there is a page for buttons.
     */



    public static void applyTUI(Player player, Integer page) {

        player.closeInventory();
        FileConfiguration config = Main.getMainClass().tagListYML;
        Connection connection = Main.getConnection();

        // BEGIN
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

            // Page Buttons
            ItemStack nextButton = new ItemStack(Material.EMERALD_BLOCK);
            ItemMeta nextMeta = nextButton.getItemMeta();
            List<String> nextLore = new ArrayList<String>();
            nextLore.add(String.valueOf(page+1));
            nextMeta.setLore(nextLore);
            nextMeta.setDisplayName(ChatColor.GREEN + "Next");
            nextButton.setItemMeta(nextMeta);
            tui.setItem(53, nextButton);

            if (page > 1) {
                ItemStack backButton = new ItemStack(Material.REDSTONE_BLOCK);
                ItemMeta backMeta = backButton.getItemMeta();
                List<String> backLore = new ArrayList<String>();
                backLore.add(String.valueOf(page - 1));
                backMeta.setLore(backLore);
                backMeta.setDisplayName(ChatColor.RED + "Back");
                backButton.setItemMeta(backMeta);
                tui.setItem(45, backButton);
            }

            // Loading the tags

            // Using config as storage
            if (connection == null) {
                for (String tag:taglist) {
                    /*
                     * IDEA
                     * Make a for loop to loop through a specific index of tags
                     * this will allow you to get the tags of that specific page
                     */

                    if ((taglist.indexOf(tag) <= 44 * page - 1) && (taglist.indexOf(tag) >= 44 * page - 44 ) ) {
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
                        if ((res.getRow() <= 44 * page - 1) && (res.getRow() >= 44 * page - 44 ) ) {
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
                        tui.setItem(pagelistdb.indexOf(ptag)+1, itemtag);
                    }

                } catch (SQLException s) {
                    s.printStackTrace();
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        player.openInventory(tui);

    }

}
