package com.mbkd;

import net.minecraft.client.Minecraft;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.util.IChatComponent;
import net.minecraft.util.ChatComponentText;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class MBKDTabRenderer {
    
    private int tickCounter = 0;
    private Field displayNameField;
    private boolean fieldFound = false;
    private Set<String> modifiedPlayers = new HashSet<>();
    private boolean wasEnabled = true;
    private boolean wasTabOn = true;
    
    public MBKDTabRenderer() {
        try {
            for (Field f : NetworkPlayerInfo.class.getDeclaredFields()) {
                if (f.getType() == IChatComponent.class) {
                    f.setAccessible(true);
                    displayNameField = f;
                    fieldFound = true;
                    System.out.println("[MBKD] Found displayName field: " + f.getName());
                    break;
                }
            }
            if (!fieldFound) {
                System.out.println("[MBKD] ERROR: Could not find displayName field");
            }
        } catch (Exception e) {
            System.out.println("[MBKD] ERROR: " + e.getMessage());
        }
    }
    
    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (!fieldFound) return;
        
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.thePlayer == null || mc.getNetHandler() == null) return;
        
        tickCounter++;
        if (tickCounter < 5) return;
        tickCounter = 0;
        
        boolean currentEnabled = MBKDConfig.enabled && MBKDConfig.showInTab;
        
        if ((wasEnabled || wasTabOn) && !currentEnabled) {
            System.out.println("[MBKD] Tab display disabled, clearing...");
            resetAllDisplays();
        }
        
        wasEnabled = MBKDConfig.enabled;
        wasTabOn = MBKDConfig.showInTab;
        
        if (!currentEnabled) return;
        
        Collection<NetworkPlayerInfo> players = mc.getNetHandler().getPlayerInfoMap();
        
        for (NetworkPlayerInfo info : players) {
            try {
                updatePlayerDisplay(info);
            } catch (Exception e) {}
        }
    }
    
    private void resetAllDisplays() {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.getNetHandler() == null) return;
        
        Collection<NetworkPlayerInfo> players = mc.getNetHandler().getPlayerInfoMap();
        
        for (NetworkPlayerInfo info : players) {
            try {
                displayNameField.set(info, null);
            } catch (Exception e) {}
        }
        
        modifiedPlayers.clear();
        System.out.println("[MBKD] Reset all displays");
    }
    
    public void clearModified() {
        resetAllDisplays();
    }
    
    private void updatePlayerDisplay(NetworkPlayerInfo info) throws Exception {
        String name = info.getGameProfile().getName();
        if (name == null || name.isEmpty()) return;
        if (name.contains("§") || name.toLowerCase().contains("npc")) return;
        
        String kd = MBKDMod.statsManager.getKD(name);
        if (kd == null) return;
        
        String lowerName = name.toLowerCase();
        if (modifiedPlayers.contains(lowerName)) {
            IChatComponent current = (IChatComponent) displayNameField.get(info);
            if (current != null && current.getFormattedText().contains("[")) {
                return;
            }
        }
        
        String kdColored = getColoredKD(kd);
        String baseName;
        
        ScorePlayerTeam team = info.getPlayerTeam();
        if (team != null) {
            baseName = ScorePlayerTeam.formatPlayerName(team, name);
        } else {
            baseName = name;
        }
        
        String newDisplay = baseName + " \u00A77[\u00A7r" + kdColored + "\u00A77]";
        displayNameField.set(info, new ChatComponentText(newDisplay));
        modifiedPlayers.add(lowerName);
    }
    
    private String getColoredKD(String kdStr) {
        try {
            if (kdStr.equals("?") || kdStr.equals("N/A") || kdStr.equals("ERR")) {
                return "\u00A77" + kdStr;
            }
            float kd = Float.parseFloat(kdStr);
            if (kd >= 3.0) return "\u00A7c" + kdStr;
            if (kd >= 2.0) return "\u00A76" + kdStr;
            if (kd >= 1.0) return "\u00A7e" + kdStr;
            if (kd >= 0.5) return "\u00A7f" + kdStr;
            return "\u00A7a" + kdStr;
        } catch (Exception e) {
            return "\u00A77" + kdStr;
        }
    }
}