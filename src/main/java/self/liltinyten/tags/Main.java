package self.liltinyten.tags;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;


import net.milkbowl.vault.chat.Chat;

import org.bukkit.ChatColor;

public class Main extends JavaPlugin {

    private static Main mainclass;
    private static Chat chat = null;

    // Player tags
    public File PlayerTags = new File(this.getDataFolder() + "/playertags.yml");
    public FileConfiguration tagsyml = YamlConfiguration.loadConfiguration(PlayerTags);

    // Database Settings
    public File dbsettings = new File(this.getDataFolder() + "/database.yml");
    public FileConfiguration dbyml = YamlConfiguration.loadConfiguration(dbsettings);



    private static Connection connection;
    private String host, database, username, password;
    private int port;

    @Override
    public void onEnable() {
        setupChat();
        System.out.println("[TAGS] - ENABLED");
        getServer().getPluginManager().registerEvents(new TagsMenuHandler(), this);
        getServer().getPluginManager().registerEvents(new TagHandler(), this);
        getCommand("tags").setExecutor(new TagsCommand());
        getCommand("tags").setTabCompleter(new CommandCompleter());
        this.saveDefaultConfig();
        this.reloadConfig();
        this.saveYml(tagsyml, PlayerTags);


        if (!dbsettings.exists()) {
            this.createDBYML();
        } else {
            try {
                dbyml.load(dbsettings);
            } catch (IOException e) {
                e.printStackTrace();
            } catch (InvalidConfigurationException e) {
                e.printStackTrace();
            }
        }


        mainclass = this;
        // Database stuff
        if (dbyml.getBoolean("usedatabase")) {
            System.out.println("[TAGS] - Trying database connection...");
            host = dbyml.getString("host");
            port = dbyml.getInt("port");
            database = dbyml.getString("databasename");
            username = dbyml.getString("username");
            password = dbyml.getString("password");
            try {
                openConnection();
                System.out.println("[TAGS] - Connected!");
            } catch (SQLException e) {

                getServer().getConsoleSender().sendMessage(ChatColor.RED + "[ERROR]" + ChatColor.WHITE + "[TAGS] - Connection error!");
            }

        } else {
            connection = null;
        }
        if (connection != null) {
            try {

                prepareStatement("CREATE TABLE IF NOT EXISTS PLAYERTAGS ("
                        + "UUID VARCHAR(50) NOT NULL PRIMARY KEY,"
                        + "TAG VARCHAR(80) NOT NULL"
                        + ") CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin").executeUpdate();
                prepareStatement("CREATE TABLE IF NOT EXISTS TAGS ("
                        + "TAG VARCHAR(20) NOT NULL PRIMARY KEY,"
                        + "DISPLAYTEXT VARCHAR(60) NOT NULL"
                        + ") CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin").executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

    }
    // More DataBase stuff
    public void openConnection() throws SQLException {
            if (connection != null && !connection.isClosed()) {
                return;
            }

            connection = DriverManager.getConnection("jdbc:mysql://" +
                            this.host+ ":"
                            +this.port+ "/"
                            +this.database,
                    this.username,
                this.password);

    }

    public static PreparedStatement prepareStatement(String query) {
        PreparedStatement ps = null;
        try {
            ps = connection.prepareStatement(query);
        } catch (SQLException s) {
            s.printStackTrace();
        }

        return ps;
    }

    public static Connection getConnection() {
        return connection;
    }


    public void createDBYML() {
        if (!dbsettings.exists()) {
            this.dbyml.set("usedatabase", (Object)false);
            this.dbyml.set("host", (Object)"localhost");
            this.dbyml.set("port", (Object)3306);
            this.dbyml.set("databasename", (Object)"database");
            this.dbyml.set("username", (Object)"root");
            this.dbyml.set("password", (Object)"");
            this.saveYml(this.dbyml, this.dbsettings);
        }
    }




    // Vault Stuff
    private boolean setupChat() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        RegisteredServiceProvider<Chat> rsp = getServer().getServicesManager().getRegistration(Chat.class);
        if (rsp == null) {
            return false;
        }
        chat = rsp.getProvider();
        return chat != null;
    }

    public static Chat getChat() {
        return chat;
    }




    // Getters
    public static  Main getMainClass() {
        return mainclass;
    }

    public FileConfiguration getTagsYML() {
        return tagsyml;
    }


    public static List<ItemStack> tagslist = new ArrayList<>();
    // end of size stuff



    // Apply tags user interface
    public void applyTUI(Player player, Integer page) {

        player.closeInventory();
        FileConfiguration config = this.getConfig();

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



                    ResultSet res = prepareStatement("SELECT * FROM TAGS;").executeQuery();
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
                        tagslist.add(itemtag);
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



    @Override
    public void onDisable() {

        try {
            if (connection != null) {
                getConnection().close();
            }
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        for (Player p:Bukkit.getServer().getOnlinePlayers()) {
            if (p.getInventory().getHolder().getOpenInventory().getTitle().equals(ChatColor.translateAlternateColorCodes('&', "&aTags"))) {
                p.closeInventory();
            }
        }


        System.out.println("[TAGS] - DISABLED");
    }

    public void saveYml(FileConfiguration ymlConfig, File ymlFile) {
        try {
            ymlConfig.save(ymlFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
