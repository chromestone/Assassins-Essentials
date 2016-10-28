package com.gmail.absolutevanillahelp.AssassinsEssentials;

import java.io.*;
import java.util.logging.*;
import org.bukkit.configuration.file.*;

public class AEConfiguration {

	private final String fileName;
    private final AssassinsEssentials plugin;
    
    private File configFile;
    private FileConfiguration fileConfiguration;
 
    public AEConfiguration(AssassinsEssentials plugin, String fileName) {
        this.plugin = plugin;
        this.fileName = fileName;
        File dataFolder = plugin.getDataFolder();
        if (dataFolder == null)
            throw new IllegalStateException();
        this.configFile = new File(plugin.getDataFolder(), fileName);
    }
 
    public void reloadConfig() {        
        fileConfiguration = YamlConfiguration.loadConfiguration(configFile);
        File defConfigFile = new File(fileName);
        if (defConfigFile != null) {
            YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(defConfigFile);
            fileConfiguration.setDefaults(defConfig);
        }
    }
 
    public FileConfiguration getConfig() {
        if (fileConfiguration == null) {
            this.reloadConfig();
        }
        return fileConfiguration;
    }
 
    public void saveConfig() {
        if (fileConfiguration == null || configFile == null) {
            return;
        } else {
            try {
                getConfig().save(configFile);
            } catch (IOException ex) {
                plugin.getLogger().log(Level.SEVERE, "Could not save config to " + configFile, ex);
            }
        }
    }
    
    public void saveDefaultConfig() {
        if (!configFile.exists()) {            
            this.plugin.saveResource(fileName, false);
        }
    }
}
