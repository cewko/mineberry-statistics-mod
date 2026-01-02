package com.mbkd;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

@Mod(modid = MBKDMod.MODID, version = MBKDMod.VERSION, clientSideOnly = true)
public class MBKDMod {
    public static final String MODID = "MBKD";
    public static final String VERSION = "1.0.0";
    
    @Mod.Instance(MODID)
    public static MBKDMod instance;
    
    public static MBKDStatsManager statsManager;
    public static MBKDConfig config;
    public static MBKDTabRenderer tabRenderer;
    
    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        config = new MBKDConfig(event.getSuggestedConfigurationFile());
    }
    
    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        statsManager = new MBKDStatsManager();
        tabRenderer = new MBKDTabRenderer();
        
        MinecraftForge.EVENT_BUS.register(new MBKDEventHandler());
        MinecraftForge.EVENT_BUS.register(new MBKDRenderHandler());
        MinecraftForge.EVENT_BUS.register(tabRenderer);
        
        net.minecraftforge.client.ClientCommandHandler.instance.registerCommand(new MBKDCommand());
        
        System.out.println("[MBKD] Mod initialized!");
    }
}