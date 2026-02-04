/*
 * Project: Joc Survival 2D
 * Author:  Tudor Baranga(Tudorrr323)
 * Date:    Jan 23, 2026
 *
 * Copyright (c) 2026 Tudor Baranga(Tudorrr323). All rights reserved.
 * This code is proprietary software. Use is strictly prohibited without permission.
 */
package ui;

import java.awt.*;
import java.awt.geom.*;
import java.awt.image.BufferedImage;

import utils.Assets;
import utils.Enums;
import entities.Enemy;
import entities.Building;
import entities.Player;
import entities.Projectile;

public class IconRenderer {

    // --- TEXT ---
    public static void drawTextWithShadow(Graphics2D g2, String text, int x, int y) {
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        if (utils.Assets.PIXEL_FONT != null) g2.setFont(utils.Assets.PIXEL_FONT.deriveFont(g2.getFont().getSize2D()));
        g2.setColor(new Color(0,0,0,220)); 
        g2.drawString(text, x + 2, y + 2); 
        g2.drawString(text, x - 1, y - 1);
        g2.setColor(Color.WHITE); 
        g2.drawString(text, x, y);
    }

    public static void drawProjectile(Graphics2D g2, Projectile p) {
        if (utils.Assets.PROJECTILE_ARROW != null) {
            AffineTransform old = g2.getTransform();
            g2.translate(p.x, p.y);
            g2.rotate(Math.toRadians(p.rotation));
            // Arrow sprite is usually pointing right. Adjust scale if needed.
            // Tiny Swords arrow is around 64x16? Let's assume standard sizing.
            // Center it.
            g2.drawImage(utils.Assets.PROJECTILE_ARROW, -32, -8, 64, 16, null);
            g2.setTransform(old);
        } else {
            g2.setColor(Color.YELLOW);
            g2.fillOval((int)p.x - 3, (int)p.y - 3, 6, 6);
        }
    }

    // --- PLAYER (KNIGHT) ---
    public static void drawKnight(Graphics2D g2, int x, int y, int size, Player player) {
        if (player == null) return;
        Player.AnimState state = player.animState;
        int frame = player.animFrame;
        boolean facingLeft = player.facingLeft;
        
        BufferedImage sheet = Assets.WARRIOR_IDLE;
        
        // --- PAWN SPECIAL LOGIC ---
        if (Assets.SELECTED_CHAR_INDEX == 3) {
            items.Item held = player.getSelectedItem();
            
            if (state == Player.AnimState.IDLE) {
                if (held == null) sheet = Assets.PAWN_IDLE_EMPTY;
                else if (held.specificType == items.Item.Specific.SWORD) sheet = Assets.PAWN_IDLE_SWORD;
                else if (held.specificType == items.Item.Specific.AXE) sheet = Assets.PAWN_IDLE_AXE;
                else if (held.specificType == items.Item.Specific.PICKAXE) sheet = Assets.PAWN_IDLE_PICK;
                else if (held.specificType == items.Item.Specific.WOOD) sheet = Assets.PAWN_IDLE_WOOD;
                else sheet = Assets.PAWN_IDLE_EMPTY;
            }
            else if (state == Player.AnimState.RUN) {
                if (held == null) sheet = Assets.PAWN_RUN_EMPTY;
                else if (held.specificType == items.Item.Specific.SWORD) sheet = Assets.PAWN_RUN_SWORD;
                else if (held.specificType == items.Item.Specific.AXE) sheet = Assets.PAWN_RUN_AXE;
                else if (held.specificType == items.Item.Specific.PICKAXE) sheet = Assets.PAWN_RUN_PICK;
                else if (held.specificType == items.Item.Specific.WOOD) sheet = Assets.PAWN_RUN_WOOD;
                else sheet = Assets.PAWN_RUN_EMPTY;
            }
            else if (state == Player.AnimState.ATTACK_1 || state == Player.AnimState.ATTACK_2) {
                // Force attack animation based on held item, regardless of direction (Pawn has simple anims)
                if (held != null) {
                    if (held.specificType == items.Item.Specific.SWORD) sheet = Assets.PAWN_INT_SWORD;
                    else if (held.specificType == items.Item.Specific.AXE) sheet = Assets.PAWN_INT_AXE;
                    else if (held.specificType == items.Item.Specific.PICKAXE) sheet = Assets.PAWN_INT_PICK;
                    else sheet = Assets.PAWN_INT_SWORD; // Default
                } else {
                    sheet = Assets.PAWN_INT_SWORD;
                }
            }
        } 
        else {
            // --- STANDARD LOGIC FOR OTHER CHARACTERS ---
            if (state == Player.AnimState.RUN) {
                sheet = Assets.WARRIOR_RUN;
            } 
            else if (state == Player.AnimState.ATTACK_1 || state == Player.AnimState.ATTACK_2) {
                 if (player.attackDirY < 0) { // UP
                     if (player.attackDirX != 0) sheet = Assets.WARRIOR_ATTACK_UP_RIGHT;
                     else sheet = Assets.WARRIOR_ATTACK_UP;
                 } else if (player.attackDirY > 0) { // DOWN
                     if (player.attackDirX != 0) sheet = Assets.WARRIOR_ATTACK_DOWN_RIGHT;
                     else sheet = Assets.WARRIOR_ATTACK_DOWN;
                 } else { // SIDE
                     sheet = Assets.WARRIOR_ATTACK_1;
                 }
            }
            else if (state == Player.AnimState.BLOCK) {
                 if (player.attackDirY < 0) { // UP
                     if (player.attackDirX != 0) sheet = Assets.WARRIOR_GUARD_UP_RIGHT;
                     else sheet = Assets.WARRIOR_GUARD_UP;
                 } else if (player.attackDirY > 0) { // DOWN
                     if (player.attackDirX != 0) sheet = Assets.WARRIOR_GUARD_DOWN_RIGHT;
                     else sheet = Assets.WARRIOR_GUARD_DOWN;
                 } else { // SIDE
                     sheet = Assets.WARRIOR_GUARD;
                 }
            }
        }
        
        if (sheet != null) {
            // Assume square frames based on height to support different sprite sizes
            int frameH = sheet.getHeight();
            int frameW = frameH; 
            
            // Safety Check: Ensure frame index is valid for this sheet
            int maxFrames = sheet.getWidth() / frameW;
            if (frame >= maxFrames) frame = frame % maxFrames;
            
            int frameX = frame * frameW;
            
            // Scale logic: The standard 192px sprite fits well as 3x tile size.
            // If sprite is larger/smaller, scale proportionally.
            double scale = (size * 3.0) / 192.0;
            int drawW = (int)(frameW * scale);
            int drawH = (int)(frameH * scale);
            
            // Center the drawing rect on the tile center
            // Tile center is x + size/2, y + size/2
            // Image center is drawW/2, drawH/2
            // Top-left should be: (x + size/2) - drawW/2
            
            int offX = x + size/2 - drawW/2;
            int offY = y + size/2 - drawH/2 - (int)(size * 0.25); // Keep the slight vertical offset bias
            
            if (facingLeft) {
                g2.drawImage(sheet, offX + drawW, offY, offX, offY + drawH, frameX, 0, frameX + frameW, frameH, null);
            } else {
                g2.drawImage(sheet, offX, offY, offX + drawW, offY + drawH, frameX, 0, frameX + frameW, frameH, null);
            }
        } else {
            // Fallback
            g2.setColor(new Color(0,0,0,80)); g2.fillOval(x+15, y+50, 30, 8);
            g2.setColor(Assets.SKIN); g2.fillOval(x+size/4, y+size/4, size/2, size/2);
        }
    }

    // --- ICONS ---
    public static void drawSpawnIcon(Graphics2D g2, int x, int y, int size) { 
        g2.setColor(new Color(0, 150, 255, 80)); 
        g2.fillOval(x + 5, y + 5, size - 10, size - 10); 
    }

    public static void drawVendor(Graphics2D g2, int x, int y, int size) {
        double s = size / 60.0;
        int merchX = x; int merchY = y + (int)(10*s);
        g2.setColor(new Color(60, 40, 100)); 
        int[] px = {merchX + (int)(5*s), merchX + (int)(25*s), merchX + (int)(30*s), merchX};
        int[] py = {merchY + (int)(15*s), merchY + (int)(15*s), merchY + (int)(45*s), merchY + (int)(45*s)};
        g2.fillPolygon(px, py, 4);
        g2.setColor(Assets.SKIN); g2.fillOval(merchX + (int)(8*s), merchY + (int)(5*s), (int)(14*s), (int)(14*s));
        g2.setColor(new Color(50, 30, 80)); g2.fillArc(merchX + (int)(6*s), merchY + (int)(2*s), (int)(18*s), (int)(15*s), 0, 180);
        g2.setColor(Color.WHITE); g2.fillArc(merchX + (int)(8*s), merchY + (int)(12*s), (int)(14*s), (int)(10*s), 180, 180);
        int cartX = x + (int)(25*s); g2.setColor(new Color(40, 20, 10)); g2.fillOval(cartX + (int)(5*s), y + size - (int)(20*s), (int)(18*s), (int)(18*s));
        g2.setColor(new Color(120, 80, 40)); g2.fillRect(cartX, y + (int)(25*s), size - (int)(20*s), (int)(25*s));
        GradientPaint canopy = new GradientPaint(cartX, y, new Color(200, 180, 150), cartX, y + (int)(20*s), new Color(160, 140, 110));
        g2.setPaint(canopy); g2.fillArc(cartX - (int)(5*s), y + (int)(10*s), size - (int)(10*s), (int)(40*s), 0, 180);
    }
    
    public static void drawGoldCoin(Graphics2D g2, int x, int y, int size) {
        double s = size / 60.0; g2.setColor(new Color(255, 215, 0)); g2.fillOval(x+(int)(10*s), y+(int)(10*s), (int)(40*s), (int)(40*s));
        g2.setColor(new Color(218, 165, 32)); g2.setStroke(new BasicStroke((float)(2*s))); g2.drawOval(x+(int)(10*s), y+(int)(10*s), (int)(40*s), (int)(40*s));
        if (utils.Assets.PIXEL_FONT != null) g2.setFont(utils.Assets.PIXEL_FONT.deriveFont(Font.BOLD, (int)(24*s)));
        else g2.setFont(new Font("Arial", Font.BOLD, (int)(24*s))); 
        g2.drawString("$", x+(int)(24*s), y+(int)(38*s)); g2.setStroke(new BasicStroke(1));
    }
    
    public static void drawPotion(Graphics2D g2, int x, int y, int size) {
        g2.setColor(Color.WHITE); g2.drawRoundRect(x+20, y+15, 20, 30, 5, 5); g2.fillRect(x+25, y+10, 10, 5);
        g2.setColor(Color.RED); g2.fillRoundRect(x+22, y+25, 16, 18, 5, 5); g2.setColor(new Color(255,255,255,100)); g2.fillOval(x+25, y+28, 5, 5);
    }

    public static void renderWaterIcon(Graphics2D g2, int x, int y, int size, int tick) {
        GradientPaint waterGp = new GradientPaint(x, y, Assets.WATER_LIGHT, x + size, y + size, Assets.WATER_DEEP);
        g2.setPaint(waterGp); g2.fillRoundRect(x + 5, y + 5, size - 10, size - 10, 10, 10); g2.setColor(new Color(255, 255, 255, 60));
        int wO = (int)(Math.sin((tick + x) * 0.1) * (size / 12.0)); g2.drawLine(x + 10, y + size / 2 + wO, x + size - 10, y + size / 2 + wO);
    }

    public static void renderWaterTile(Graphics2D g2, int x, int y, int size, int tick) {
        GradientPaint waterGp = new GradientPaint(x, y, Assets.WATER_LIGHT, x + size, y + size, Assets.WATER_DEEP);
        g2.setPaint(waterGp); g2.fillRect(x, y, size, size); g2.setColor(new Color(255, 255, 255, 40));
        int wO = (int)(Math.sin((tick + x) * 0.1) * 5); g2.drawLine(x, y + 20 + wO, x + size, y + 20 + wO);
    }

    // --- RESOURCES ---
    public static void drawTree(Graphics2D g2, int x, int y, int size, Enums.Quality q) {
        g2.setColor(new Color(0,0,0,50)); g2.fillOval(x+size/4, y+size-8, size/2, 6); 
        GradientPaint trunk = new GradientPaint(x, y, new Color(70, 50, 20), x+size/2, y+size, new Color(40, 30, 10)); g2.setPaint(trunk); g2.fillRect(x + size/2 - 4, y + size/2, 8, size/2 - 2);
        g2.setColor(new Color(20, 80, 20)); g2.fillOval(x + 5, y + size/3, size - 10, size/2);
        g2.setColor(new Color(40, 140, 40)); g2.fillOval(x + 2, y + size/4, size/2 + 5, size/2); g2.fillOval(x + size/2 - 5, y + size/4, size/2 + 2, size/2);
        g2.setColor(new Color(80, 180, 60)); g2.fillOval(x + size/4, y, size/2, size/2);
    }

    public static void drawRock(Graphics2D g2, int x, int y, int size, Enums.Quality q) {
        g2.setColor(new Color(0,0,0,50)); g2.fillOval(x+10, y+size-8, size-20, 6);
        int[] px = {x+10, x+size-15, x+size-5, x+25, x+5}; int[] py = {y+size-5, y+size-10, y+25, y+10, y+25};
        GradientPaint rockGrad = new GradientPaint(x, y, new Color(180, 180, 180), x+size, y+size, new Color(60, 60, 65)); g2.setPaint(rockGrad); g2.fillPolygon(px, py, 5);
    }

    public static void renderGrain(Graphics2D g2, int x, int y, int size, Enums.Quality q) {
        double s = size / 60.0;
        g2.setColor(new Color(200, 160, 0)); g2.setStroke(new BasicStroke((float)(2*s)));
        g2.drawLine(x + (int)(15*s), y + (int)(55*s), x + (int)(10*s), y + (int)(15*s)); 
        g2.drawLine(x + (int)(30*s), y + (int)(55*s), x + (int)(30*s), y + (int)(10*s)); 
        g2.drawLine(x + (int)(45*s), y + (int)(55*s), x + (int)(50*s), y + (int)(20*s));
        g2.setStroke(new BasicStroke(1)); g2.setColor(new Color(255, 215, 0));
        g2.fillOval(x + (int)(8*s), y + (int)(10*s), (int)(6*s), (int)(12*s)); 
        g2.fillOval(x + (int)(27*s), y + (int)(5*s), (int)(6*s), (int)(14*s)); 
        g2.fillOval(x + (int)(47*s), y + (int)(15*s), (int)(6*s), (int)(12*s));
    }

    // --- ENEMIES (GAME VERSION - SIMPLE SKINS) ---
    public static void renderEnemy(Graphics2D g2, Enemy e, int x, int y, int size) {
        if (size > 55) {
            g2.setColor(new Color(0,0,0,150)); g2.fillRoundRect(x+10, y-8, size-20, 5, 2, 2);
            g2.setColor(Color.RED); float hpPercent = (float)e.getHealth() / e.getMaxHealth(); if(hpPercent < 0) hpPercent = 0;
            g2.fillRoundRect(x+10, y-8, (int)((size-20) * hpPercent), 5, 2, 2);
        }
        switch(e.type) {
            case ZOMBIE: drawZombieSimple(g2, x, y, size); break;
            case SKELETON: drawSkeletonSimple(g2, x, y, size); break;
            case RAT: drawRatSimple(g2, x, y, size); break;
            case HUNTER: drawHunterSimple(g2, x, y, size); break;
            case WITCH: drawWitchSimple(g2, x, y, size); break;
        }
    }

    // --- ENEMIES (LOADING VERSION - ANIMATED SKINS) ---
    public static void renderLoadingEnemy(Graphics2D g2, Enemy e, int x, int y, int size, int walkAnim) {
        switch(e.type) {
            case ZOMBIE: drawZombieAnimated(g2, x, y, size, walkAnim); break;
            case SKELETON: drawSkeletonAnimated(g2, x, y, size, walkAnim); break;
            case RAT: drawRatAnimated(g2, x, y, size, walkAnim); break;
            case HUNTER: drawHunterAnimated(g2, x, y, size, walkAnim); break;
            case WITCH: drawWitchAnimated(g2, x, y, size, e.getHealth() <= 0, walkAnim); break;
        }
    }

    // --- SIMPLE DRAWERS ---
    private static void drawZombieSimple(Graphics2D g2, int x, int y, int size) {
        g2.setColor(new Color(40, 60, 20)); g2.fillRoundRect(x + 15, y + 15, size - 30, size - 30, 10, 10);
        g2.setColor(new Color(60, 80, 30)); g2.fillOval(x + 15, y + 5, size - 30, size - 30);
        g2.setColor(Color.RED); g2.fillOval(x + 20, y + 15, 6, 6); g2.fillOval(x + 34, y + 15, 6, 6);
    }
    private static void drawSkeletonSimple(Graphics2D g2, int x, int y, int size) {
        g2.setColor(Color.GRAY); g2.fillRect(x + 25, y + 20, 10, 30);
        g2.setColor(Color.WHITE); g2.fillOval(x + 15, y + 5, 30, 30);
        g2.setColor(Color.BLACK); g2.fillOval(x + 20, y + 15, 6, 6); g2.fillOval(x + 34, y + 15, 6, 6);
    }
    private static void drawRatSimple(Graphics2D g2, int x, int y, int size) {
        g2.setColor(Color.DARK_GRAY); g2.fillOval(x + 10, y + 30, 40, 20);
        g2.setColor(Color.PINK); g2.drawArc(x + 45, y + 35, 15, 10, 0, 90);
    }
    private static void drawHunterSimple(Graphics2D g2, int x, int y, int size) {
        g2.setColor(new Color(90, 70, 50)); g2.fillRoundRect(x + 15, y + 15, 30, 35, 10, 10);
        g2.setColor(Assets.SKIN); g2.fillOval(x + 15, y + 5, 30, 30);
    }
    private static void drawWitchSimple(Graphics2D g2, int x, int y, int size) {
        g2.setColor(new Color(60, 20, 80)); g2.fillPolygon(new int[]{x+10, x+50, x+30}, new int[]{y+50, y+50, y+10}, 3);
        g2.setColor(new Color(100, 180, 100)); g2.fillOval(x + 20, y + 10, 20, 20);
    }

    // --- ANIMATED DRAWERS ---
    private static void drawZombieAnimated(Graphics2D g2, int x, int y, int size, int walkAnim) { 
        double s = size / 60.0; int fo = (int)(Math.sin(walkAnim * 0.4) * (8 * s));
        g2.setColor(new Color(20, 20, 20)); g2.fillOval(x + (int)(12*s) + fo, y + (int)(45*s), (int)(14*s), (int)(12*s)); g2.fillOval(x + (int)(34*s) - fo, y + (int)(45*s), (int)(14*s), (int)(12*s));
        GradientPaint skin = new GradientPaint(x, y, new Color(100, 140, 60), x+size, y+size, new Color(40, 60, 20)); g2.setPaint(skin); 
        g2.fillRoundRect(x + (int)(15*s), y + (int)(10*s), size - (int)(30*s), (int)(40*s), (int)(8*s), (int)(8*s)); 
        g2.setColor(new Color(60, 80, 100)); g2.fillRect(x + (int)(15*s), y + (int)(35*s), size - (int)(30*s), (int)(15*s)); 
        g2.setColor(Color.RED); g2.fillOval(x + (int)(20*s), y + (int)(18*s), (int)(6*s), (int)(6*s)); g2.fillOval(x + (int)(34*s), y + (int)(18*s), (int)(6*s), (int)(6*s)); 
    }

    private static void drawSkeletonAnimated(Graphics2D g2, int x, int y, int size, int walkAnim) { 
        double s = size / 60.0; int fo = (int)(Math.sin(walkAnim * 0.4) * (8 * s));
        g2.setColor(new Color(30, 30, 30)); g2.fillOval(x + (int)(12*s) + fo, y + (int)(45*s), (int)(14*s), (int)(12*s)); g2.fillOval(x + (int)(34*s) - fo, y + (int)(45*s), (int)(14*s), (int)(12*s));
        g2.setColor(new Color(220, 220, 220)); g2.fillOval(x + (int)(20*s), y + (int)(8*s), (int)(20*s), (int)(22*s)); 
        g2.fillRect(x + (int)(28*s), y + (int)(30*s), (int)(4*s), (int)(15*s)); 
        g2.setColor(Color.BLACK); g2.fillOval(x + (int)(24*s), y + (int)(14*s), (int)(5*s), (int)(5*s)); g2.fillOval(x + (int)(31*s), y + (int)(14*s), (int)(5*s), (int)(5*s)); 
    }

    private static void drawRatAnimated(Graphics2D g2, int x, int y, int size, int walkAnim) { 
        double s = size / 60.0; int fo = (int)(Math.sin(walkAnim * 0.4) * (5 * s));
        g2.setColor(Color.DARK_GRAY); g2.fillOval(x + (int)(10*s), y + (int)(35*s), (int)(40*s), (int)(20*s));
        g2.setColor(Color.GRAY); g2.fillOval(x + (int)(5*s) + fo, y + (int)(40*s), (int)(15*s), (int)(15*s)); 
        g2.setColor(Color.PINK); g2.drawArc(x + (int)(45*s), y + (int)(40*s), (int)(20*s), (int)(10*s), 0, 90);
    }

    private static void drawHunterAnimated(Graphics2D g2, int x, int y, int size, int walkAnim) { 
        double s = size / 60.0; int fo = (int)(Math.sin(walkAnim * 0.4) * (8 * s));
        g2.setColor(new Color(40, 30, 20)); g2.fillOval(x + (int)(12*s) + fo, y + (int)(45*s), (int)(14*s), (int)(12*s)); g2.fillOval(x + (int)(34*s) - fo, y + (int)(45*s), (int)(14*s), (int)(12*s));
        g2.setColor(new Color(90, 70, 50)); g2.fillRoundRect(x + (int)(15*s), y + (int)(15*s), (int)(30*s), (int)(35*s), (int)(10*s), (int)(10*s)); 
        g2.setColor(Assets.SKIN); g2.fillOval(x + (int)(15*s), y, (int)(30*s), (int)(30*s));
    }

    private static void drawWitchAnimated(Graphics2D g2, int x, int y, int size, boolean isDead, int walkAnim) {
        double s = size / 60.0; long time = System.currentTimeMillis();
        int hover = isDead ? 0 : (int)(Math.sin(time * 0.005) * (5 * s));
        int wx = x; int wy = y + hover;
        g2.setColor(new Color(0,0,0,100)); g2.fillOval(wx + (int)(15*s), y + size - (int)(10*s), (int)(30*s), (int)(8*s));
        Graphics2D broom = (Graphics2D) g2.create(); broom.rotate(Math.toRadians(10), wx + size/2, wy + size/2); 
        broom.setColor(new Color(120, 80, 40)); broom.fillRect(wx - (int)(45*s), wy + size/2 + (int)(10*s), (int)(80*s), (int)(5*s));
        broom.setColor(new Color(200, 180, 100)); broom.fillOval(wx + (int)(35*s), wy + size/2 + (int)(5*s), (int)(25*s), (int)(15*s)); broom.dispose();
        g2.setColor(new Color(60, 20, 80)); int[] rx = {wx+(int)(0*s), wx+(int)(20*s), wx+(int)(30*s), wx-(int)(10*s)}; int[] ry = {wy+(int)(20*s), wy+(int)(20*s), wy+(int)(50*s), wy+(int)(50*s)}; g2.fillPolygon(rx, ry, 4);
        g2.setColor(new Color(100, 180, 100)); g2.fillOval(wx + (int)(0*s), wy + (int)(5*s), (int)(20*s), (int)(20*s));
        g2.setColor(Color.RED); g2.fillOval(wx + (int)(4*s), wy + (int)(12*s), (int)(3*s), (int)(3*s)); g2.fillOval(wx + (int)(12*s), wy + (int)(12*s), (int)(3*s), (int)(3*s));
        g2.setColor(new Color(20, 20, 20)); g2.fillOval(wx - (int)(10*s), wy + (int)(5*s), (int)(40*s), (int)(8*s));
        int[] hx = {wx + (int)(0*s), wx + (int)(20*s), wx + (int)(30*s)}; int[] hy = {wy + (int)(5*s), wy + (int)(5*s), wy - (int)(20*s)}; g2.fillPolygon(hx, hy, 3);
    }

    // --- ITEMS ---
    public static void drawWoodIcon(Graphics2D g2, int x, int y, int size) { 
        double s = size / 60.0; g2.setColor(new Color(90, 60, 30)); 
        int[] px = {x+(int)(5*s), x+(int)(45*s), x+(int)(55*s), x+(int)(15*s)}; int[] py = {y+(int)(15*s), y+(int)(5*s), y+(int)(45*s), y+(int)(55*s)}; 
        g2.fillPolygon(px, py, 4); 
    }
    public static void drawStoneIcon(Graphics2D g2, int x, int y, int size) { 
        double s = size / 60.0; g2.setColor(new Color(120, 120, 125)); 
        int[] px = {x+(int)(10*s), x+(int)(50*s), x+(int)(55*s), x+(int)(15*s)}; int[] py = {y+(int)(15*s), y+(int)(10*s), y+(int)(50*s), y+(int)(55*s)}; 
        g2.fillPolygon(px, py, 4); 
    }
    public static void drawBread(Graphics2D g2, int x, int y, int size) { g2.setColor(Assets.BREAD_COLOR); g2.fillRoundRect(x+5, y+15, size-10, size-25, 15, 15); }
    public static void drawHelmet(Graphics2D g2, int x, int y, int size) { g2.setColor(Color.GRAY); g2.fillArc(x+10, y+10, size-20, size-20, 0, 180); }
    public static void drawChestplate(Graphics2D g2, int x, int y, int size) { g2.setColor(Color.GRAY); int[] bx = {x+15, x+45, x+50, x+10}; int[] by = {y+10, y+10, y+50, y+50}; g2.fillPolygon(bx, by, 4); }
    public static void drawPants(Graphics2D g2, int x, int y, int size) { g2.setColor(Color.GRAY); g2.fillRect(x+15, y+10, size-30, 15); g2.fillRect(x+15, y+25, 12, 20); g2.fillRect(x+size-27, y+25, 12, 20); }
    public static void drawBoots(Graphics2D g2, int x, int y, int size) { g2.setColor(Color.GRAY); g2.fillRoundRect(x+10, y+25, 12, 15, 5, 5); g2.fillRoundRect(x+size-22, y+25, 12, 15, 5, 5); }
    public static void drawSword(Graphics2D g2, int x, int y, int size) { 
        if (Assets.ITEM_SWORD != null) {
            int drawSize = (int)(size * 1.5);
            int off = (drawSize - size) / 2;
            g2.drawImage(Assets.ITEM_SWORD, x - off, y - off, drawSize, drawSize, null);
        } else { 
            double s = size/60.0; g2.setColor(new Color(220, 220, 230)); g2.fillRect(x+(int)(28*s), y+(int)(5*s), (int)(4*s), (int)(40*s)); g2.setColor(new Color(180, 140, 20)); g2.fillRect(x+(int)(20*s), y+(int)(40*s), (int)(20*s), (int)(6*s)); 
        } 
    }
    public static void drawAxe(Graphics2D g2, int x, int y, int size) { 
        if (Assets.ITEM_AXE != null) {
            int drawSize = (int)(size * 1.5);
            int off = (drawSize - size) / 2;
            g2.drawImage(Assets.ITEM_AXE, x - off, y - off, drawSize, drawSize, null);
        } else { 
            g2.setColor(new Color(100, 60, 20)); g2.fillRect(x+size/2-2, y+5, 4, size-10); g2.setColor(Color.LIGHT_GRAY); g2.fillArc(x+size/2-2, y+5, 18, 18, 90, 180); 
        } 
    }
    public static void drawPickaxe(Graphics2D g2, int x, int y, int size) { 
        if (Assets.ITEM_PICKAXE != null) {
            int drawSize = (int)(size * 1.5);
            int off = (drawSize - size) / 2;
            g2.drawImage(Assets.ITEM_PICKAXE, x - off, y - off, drawSize, drawSize, null);
        } else { 
            g2.setColor(new Color(100, 60, 20)); g2.fillRect(x+size/2-2, y+5, 4, size-10); g2.setColor(Color.GRAY); g2.setStroke(new BasicStroke(3)); g2.drawArc(x+size/2-12, y+5, 30, 15, 0, 180); g2.setStroke(new BasicStroke(1)); 
        } 
    }
    public static void drawCampfire(Graphics2D g2, int x, int y, int size) { g2.setColor(new Color(60, 40, 20)); g2.setStroke(new BasicStroke(3)); g2.drawLine(x + 15, y + size - 15, x + size - 15, y + size - 25); g2.setStroke(new BasicStroke(1)); g2.setColor(Assets.FIRE_ORANGE); g2.fillOval(x + 15, y + size - 35, 30, 20); }
    public static void drawTent(Graphics2D g2, int x, int y, int size) { int[] px = {x + 10, x + size/2, x + size - 10}; int[] py = {y + size - 10, y + 10, y + size - 10}; g2.setColor(Assets.TENT_BASE); g2.fillPolygon(px, py, 3); }
    
    // --- BUILDINGS ---
    public static void drawBuilding(Graphics2D g2, Building b, int x, int y, int size) { if (b.type == Building.Type.FOUNTAIN) drawFountain(g2, x, y, size); else drawMonument(g2, x, y, size); }
    public static void drawFountain(Graphics2D g2, int x, int y, int size) { g2.setColor(Assets.STONE_BASE); g2.fillOval(x+5, y+10, size-10, size-20); }
    public static void drawMonument(Graphics2D g2, int x, int y, int size) { g2.setColor(new Color(80, 80, 80)); g2.fillRect(x+10, y+size-15, size-20, 10); }
    public static void drawArmorPlaceholder(Graphics2D g2, int x, int y, int size, int type) { g2.setColor(new Color(255, 255, 255, 30)); g2.drawRect(x + 15, y + 10, size - 30, size - 25); }
}