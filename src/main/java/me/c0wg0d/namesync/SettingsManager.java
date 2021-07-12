package me.c0wg0d.namesync;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;

import java.io.File;
import java.io.IOException;

public class SettingsManager {

    private SettingsManager() {
    }

    static SettingsManager instance = new SettingsManager();

    public static SettingsManager getInstance() {
        return instance;
    }

    Plugin p;
    FileConfiguration config;
    File configFile;

    public void init(Plugin p) {
        config = p.getConfig();
        config.options().copyDefaults(true);
        configFile = new File(p.getDataFolder(), "config.yml");
        saveConfig();
    }

    public FileConfiguration getConfig() {
        return config;
    }

    public void saveConfig() {
        try {
            config.save(configFile);
        }
        catch (IOException e) {
            Bukkit.getServer().getLogger()
                    .severe(ChatColor.RED + "Could not save config.yml!");
        }
    }

    public void reloadConfig() {
        config = YamlConfiguration.loadConfiguration(configFile);
    }

    public PluginDescriptionFile getDesc() {
        return p.getDescription();
    }
}
