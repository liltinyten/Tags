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
import java.util.logging.Level;
import java.util.logging.Logger;

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

    // Tag List
    public File TagsList = new File(this.getDataFolder() + "/tagslist.yml");
    public FileConfiguration tagListYML = YamlConfiguration.loadConfiguration(TagsList);

    // Database Settings (CLEANUP)
    public File dbsettings = new File(this.getDataFolder() + "/database.yml");
    public FileConfiguration dbyml = YamlConfiguration.loadConfiguration(dbsettings);

    //Logger
    public Logger logger;

    // Colors
    public static final String ERR = "\u001B[31m";
    public static final String END = "\u001B[0m";

    private static Connection connection;
    private String host, database, username, password;
    private int port;

    @Override
    public void onEnable() {
        // CLEANUP

        logger = getLogger();
        setupChat();
        logger.log(Level.INFO, "ENABLED");
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
        // CLEANUP
        if (dbyml.getBoolean("usedatabase")) {
            logger.log(Level.INFO, "[TAGS] - Trying database connection...");
            host = dbyml.getString("host");
            port = dbyml.getInt("port");
            database = dbyml.getString("databasename");
            username = dbyml.getString("username");
            password = dbyml.getString("password");
            try {
                openConnection();
                logger.log(Level.INFO, "Connected");
            } catch (SQLException e) {

                logger.log(Level.SEVERE, ERR + "Connection Error!" + END);
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
            this.dbyml.set("usedatabase", false);
            this.dbyml.set("host", "localhost");
            this.dbyml.set("port", 3306);
            this.dbyml.set("databasename", "database");
            this.dbyml.set("username", "root");
            this.dbyml.set("password", "");
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

    public FileConfiguration getTagListYML() {return tagListYML;}


    public static List<ItemStack> tagslist = new ArrayList<>();
    // end of size stuff



    // TUI Main class -> UserInterface



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

        logger.log(Level.INFO, "DISABLED");
    }

    public void saveYml(FileConfiguration ymlConfig, File ymlFile) {
        try {
            ymlConfig.save(ymlFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
