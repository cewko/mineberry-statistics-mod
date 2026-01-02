package com.mbkd;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class MBKDRenderHandler {
    
    private final Minecraft mc = Minecraft.getMinecraft();
    
    @SubscribeEvent
    public void onRenderLiving(RenderLivingEvent.Post event) {
        if (!MBKDConfig.enabled || !MBKDConfig.showAboveHead) return;
        if (!(event.entity instanceof EntityPlayer)) return;
        
        EntityPlayer player = (EntityPlayer) event.entity;
        
        if (player == mc.thePlayer && mc.gameSettings.thirdPersonView == 0) return;
        
        double distance = player.getDistanceSqToEntity(mc.thePlayer);
        if (distance > 4096) return;
        
        if (player.isInvisible()) return;
        
        String kd = MBKDMod.statsManager.getKD(player.getName());
        if (kd == null) return;
        
        String kdText = getColoredKD(kd) + " K/D";
        
        renderNametagText(player, kdText, event.x, event.y, event.z);
    }
    
    private void renderNametagText(EntityPlayer player, String text, double x, double y, double z) {
        FontRenderer fontRenderer = mc.fontRendererObj;
        RenderManager renderManager = mc.getRenderManager();
        
        float scale = 0.02666667F;
        float playerHeight = player.height + 0.5F;
        
        GlStateManager.pushMatrix();
        GlStateManager.translate(x, y + playerHeight + 0.3, z);
        GlStateManager.rotate(-renderManager.playerViewY, 0, 1, 0);
        GlStateManager.rotate(renderManager.playerViewX, 1, 0, 0);
        GlStateManager.scale(-scale, -scale, scale);
        
        GlStateManager.disableLighting();
        GlStateManager.depthMask(false);
        GlStateManager.disableDepth();
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        
        int textWidth = fontRenderer.getStringWidth(text);
        int xPos = -textWidth / 2;
        
        GlStateManager.disableTexture2D();
        
        net.minecraft.client.renderer.Tessellator tessellator = net.minecraft.client.renderer.Tessellator.getInstance();
        net.minecraft.client.renderer.WorldRenderer worldRenderer = tessellator.getWorldRenderer();
        worldRenderer.begin(7, net.minecraft.client.renderer.vertex.DefaultVertexFormats.POSITION_COLOR);
        worldRenderer.pos(xPos - 1, -1, 0).color(0, 0, 0, 64).endVertex();
        worldRenderer.pos(xPos - 1, 8, 0).color(0, 0, 0, 64).endVertex();
        worldRenderer.pos(xPos + textWidth + 1, 8, 0).color(0, 0, 0, 64).endVertex();
        worldRenderer.pos(xPos + textWidth + 1, -1, 0).color(0, 0, 0, 64).endVertex();
        tessellator.draw();
        
        GlStateManager.enableTexture2D();
        
        fontRenderer.drawString(text, xPos, 0, 0xFFFFFFFF);
        
        GlStateManager.enableDepth();
        GlStateManager.depthMask(true);
        GlStateManager.enableLighting();
        GlStateManager.disableBlend();
        GlStateManager.color(1, 1, 1, 1);
        GlStateManager.popMatrix();
    }
    
    private String getColoredKD(String kdStr) {
        try {
            if (kdStr.equals("?") || kdStr.equals("ERR")) {
                return EnumChatFormatting.GRAY + kdStr;
            }
            
            float kd = Float.parseFloat(kdStr);
            
            if (kd >= 3.0) return EnumChatFormatting.RED + kdStr;
            else if (kd >= 2.0) return EnumChatFormatting.GOLD + kdStr;
            else if (kd >= 1.0) return EnumChatFormatting.YELLOW + kdStr;
            else if (kd >= 0.5) return EnumChatFormatting.WHITE + kdStr;
            else return EnumChatFormatting.GREEN + kdStr;
        } catch (NumberFormatException e) {
            return EnumChatFormatting.GRAY + kdStr;
        }
    }
}