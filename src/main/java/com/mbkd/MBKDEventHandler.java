package com.mbkd;

import net.minecraft.client.Minecraft;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent;

import java.util.*;

public class MBKDEventHandler {
    
    private int tickCounter = 0;
    private Set<String> lastPlayerSet = new HashSet<>();
    private Set<String> fetchedPlayers = new HashSet<>();
    
    @SubscribeEvent
    public void onWorldJoin(FMLNetworkEvent.ClientConnectedToServerEvent event) {
        lastPlayerSet.clear();
        fetchedPlayers.clear();
        MBKDMod.statsManager.clearCache();
        MBKDMod.tabRenderer.clearModified();
        System.out.println("[MBKD] Joined server, cleared cache");
    }
    
    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (!MBKDConfig.enabled) return;
        
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.thePlayer == null || mc.getNetHandler() == null) return;
        
        tickCounter++;
        if (tickCounter < 20) return;
        tickCounter = 0;
        
        Collection<NetworkPlayerInfo> players = mc.getNetHandler().getPlayerInfoMap();
        Set<String> currentPlayers = new HashSet<>();
        List<String> toFetch = new ArrayList<>();
        
        for (NetworkPlayerInfo info : players) {
            String name = info.getGameProfile().getName();
            if (name == null || name.isEmpty()) continue;
            if (name.contains("§")) continue;
            
            currentPlayers.add(name);
            
            if (!fetchedPlayers.contains(name) && !MBKDMod.statsManager.hasKD(name)) {
                toFetch.add(name);
            }
        }
        
        if (!lastPlayerSet.isEmpty() && currentPlayers.size() > 3) {
            int common = 0;
            for (String p : currentPlayers) {
                if (lastPlayerSet.contains(p)) common++;
            }
            
            float similarity = (float) common / Math.max(lastPlayerSet.size(), currentPlayers.size());
            
            if (similarity < 0.5f) {
                System.out.println("[MBKD] New lobby detected!");
                fetchedPlayers.clear();
                MBKDMod.statsManager.cancelPending();
                MBKDMod.tabRenderer.clearModified();
                
                toFetch.clear();
                for (String name : currentPlayers) {
                    toFetch.add(name);
                }
            }
        }
        
        lastPlayerSet = new HashSet<>(currentPlayers);
        
        if (!toFetch.isEmpty()) {
            System.out.println("[MBKD] Fetching " + toFetch.size() + " players...");
            fetchedPlayers.addAll(toFetch);
            MBKDMod.statsManager.fetchPlayers(toFetch);
        }
    }
}