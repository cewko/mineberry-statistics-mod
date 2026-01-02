package com.mbkd;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;

import java.util.Arrays;
import java.util.List;

public class MBKDCommand extends CommandBase {
    
    @Override
    public String getCommandName() {
        return "mkd";
    }
    
    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "/mkd <toggle|clear|status|tab|head>";
    }
    
    @Override
    public int getRequiredPermissionLevel() {
        return 0;
    }
    
    @Override
    public List<String> getCommandAliases() {
        return Arrays.asList("kd", "mbkd");
    }
    
    @Override
    public void processCommand(ICommandSender sender, String[] args) {
        if (args.length == 0) {
            showHelp(sender);
            return;
        }
        
        String sub = args[0].toLowerCase();
        
        switch (sub) {
            case "toggle":
                toggleMod(sender);
                break;
            case "clear":
                clearCache(sender);
                break;
            case "status":
                showStatus(sender);
                break;
            case "tab":
                toggleTab(sender);
                break;
            case "head":
                toggleHead(sender);
                break;
            default:
                showHelp(sender);
        }
    }
    
    private void showHelp(ICommandSender sender) {
        sendMessage(sender, EnumChatFormatting.GOLD + "=== MBKD Commands ===");
        sendMessage(sender, EnumChatFormatting.YELLOW + "/mkd toggle" + EnumChatFormatting.WHITE + " - Enable/disable mod");
        sendMessage(sender, EnumChatFormatting.YELLOW + "/mkd tab" + EnumChatFormatting.WHITE + " - Toggle TAB display");
        sendMessage(sender, EnumChatFormatting.YELLOW + "/mkd head" + EnumChatFormatting.WHITE + " - Toggle above-head display");
        sendMessage(sender, EnumChatFormatting.YELLOW + "/mkd clear" + EnumChatFormatting.WHITE + " - Clear K/D cache");
        sendMessage(sender, EnumChatFormatting.YELLOW + "/mkd status" + EnumChatFormatting.WHITE + " - Show mod status");
    }
    
    private void toggleMod(ICommandSender sender) {
        MBKDConfig.enabled = !MBKDConfig.enabled;
        if (!MBKDConfig.enabled) {
            MBKDMod.tabRenderer.clearModified();
        }
        String status = MBKDConfig.enabled ? 
            EnumChatFormatting.GREEN + "ENABLED" : 
            EnumChatFormatting.RED + "DISABLED";
        sendMessage(sender, "MBKD is now " + status);
    }
    
    private void toggleTab(ICommandSender sender) {
        MBKDConfig.showInTab = !MBKDConfig.showInTab;
        if (!MBKDConfig.showInTab) {
            MBKDMod.tabRenderer.clearModified();
        }
        String status = MBKDConfig.showInTab ? 
            EnumChatFormatting.GREEN + "ON" : 
            EnumChatFormatting.RED + "OFF";
        sendMessage(sender, "TAB display: " + status);
    }
    
    private void toggleHead(ICommandSender sender) {
        MBKDConfig.showAboveHead = !MBKDConfig.showAboveHead;
        String status = MBKDConfig.showAboveHead ? 
            EnumChatFormatting.GREEN + "ON" : 
            EnumChatFormatting.RED + "OFF";
        sendMessage(sender, "Above-head display: " + status);
    }
    
    private void clearCache(ICommandSender sender) {
        MBKDMod.statsManager.clearCache();
        MBKDMod.tabRenderer.clearModified();
        sendMessage(sender, EnumChatFormatting.GREEN + "K/D cache cleared");
    }
    
    private void showStatus(ICommandSender sender) {
        boolean apiOnline = MBKDMod.statsManager.isAPIOnline();
        int cacheSize = MBKDMod.statsManager.getCacheSize();
        
        sendMessage(sender, EnumChatFormatting.GOLD + "=== MBKD Status ===");
        sendMessage(sender, "Mod: " + (MBKDConfig.enabled ? 
            EnumChatFormatting.GREEN + "ENABLED" : EnumChatFormatting.RED + "DISABLED"));
        sendMessage(sender, "API: " + (apiOnline ? 
            EnumChatFormatting.GREEN + "ONLINE" : EnumChatFormatting.RED + "OFFLINE"));
        sendMessage(sender, "Cached: " + EnumChatFormatting.YELLOW + cacheSize + " players");
        sendMessage(sender, "TAB: " + (MBKDConfig.showInTab ? 
            EnumChatFormatting.GREEN + "ON" : EnumChatFormatting.RED + "OFF"));
        sendMessage(sender, "Head: " + (MBKDConfig.showAboveHead ? 
            EnumChatFormatting.GREEN + "ON" : EnumChatFormatting.RED + "OFF"));
    }
    
    private void sendMessage(ICommandSender sender, String message) {
        sender.addChatMessage(new ChatComponentText(
            EnumChatFormatting.GRAY + "[MBKD] " + EnumChatFormatting.RESET + message
        ));
    }
}