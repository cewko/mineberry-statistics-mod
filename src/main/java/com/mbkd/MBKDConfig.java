package com.mbkd;

import net.minecraftforge.common.config.Configuration;
import java.io.File;

public class MBKDConfig {
    public static String apiUrl = "http://localhost:3000";
    public static boolean showInTab = true;
    public static boolean showAboveHead = true;
    public static boolean enabled = true;
    
    private Configuration config;
    
    public MBKDConfig(File configFile) {
        config = new Configuration(configFile);
        loadConfig();
    }
    
    public void loadConfig() {
        try {
            config.load();
            
            apiUrl = config.getString("apiUrl", "general", "http://localhost:3000",
                "URL of the K/D statistics bot API");
            showInTab = config.getBoolean("showInTab", "display", true,
                "Show K/D in TAB menu");
            showAboveHead = config.getBoolean("showAboveHead", "display", true,
                "Show K/D above player heads");
            enabled = config.getBoolean("enabled", "general", true,
                "Enable/disable the mod");
                
        } catch (Exception e) {
            System.err.println("[MBKD] Error loading config: " + e.getMessage());
        } finally {
            if (config.hasChanged()) {
                config.save();
            }
        }
    }
    
    public void saveConfig() {
        config.save();
    }
}