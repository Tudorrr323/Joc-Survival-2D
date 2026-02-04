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
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

import engine.GamePanel;
import entities.*;
import items.Item;
import utils.Assets;
import utils.Enums;
import utils.SaveInfo;
import world.WorldMap;

public class UIRenderer {

    private GamePanel gp;

    // UI Rectangles
    public Rectangle btnCraftingOpen, btnInventoryOpen, btnMapOpen, btnCloseWindow;
    public Rectangle btnStartGame, btnContinue, btnLoadGame, btnExitMenu, btnRestart, btnEditor;
    public Rectangle btnResume, btnSaveMenu, btnLoadMenu, btnMainMenu, btnExitPause;
    
    // Slot UI
    public Rectangle btnSlot1, btnSlot2, btnSlot3, btnBack;
    public Rectangle btnDelete1, btnDelete2, btnDelete3;
    
    // Editor UI
    public Rectangle btnCreateMap, btnLoadMap, btnSaveMap;
    public Rectangle btnConfirmYes, btnConfirmNo, btnSaveAndExit;
    public Rectangle btnExitYes, btnExitNo;
    public Rectangle btnExitGameConfirmYes, btnExitGameConfirmNo;
    public Rectangle btnChar1; // Deprecated, use charButtons
    public Rectangle[] charButtons;
    public Rectangle btnRibbon;
    public Rectangle btnCharStart;
    public Rectangle btnNameBox;
    public Rectangle[] ribbonDropdownRects;
    public boolean showRibbonDropdown = false;
    public int selectedRibbonIndex = 0;
    
    public Rectangle[] playMapBtns, editMapBtns, delMapBtns;
    public Rectangle btnCamUp, btnCamDown, btnCamLeft, btnCamRight;
    public Rectangle[] paletteRects;

    public Rectangle[] hotbarRects = new Rectangle[5];
    public Rectangle[] backpackRects = new Rectangle[16];
    public Rectangle[] armorRects = new Rectangle[4];
    
    public Rectangle[] shopBuyRects;
    public Rectangle[] shopSellRects;

    public List<CraftingButton> craftButtons;
    public List<ShopButton> shopButtons;

    public UIRenderer(GamePanel gp) {
        this.gp = gp;
        initUI();
    }
    
    public void initUI() {
        int w = gp.getWidth(), h = gp.getHeight();
        if (w == 0) w = 1024; if (h == 0) h = 768;
        
        int btnW = 320, btnH = 90, gap = 10;
        int menuCenterX = w / 2 - btnW / 2, menuStartY = h / 2 - 220; 
        
        btnStartGame = new Rectangle(menuCenterX, menuStartY, btnW, btnH);
        btnContinue  = new Rectangle(menuCenterX, menuStartY + (btnH + gap), btnW, btnH);
        btnLoadGame  = new Rectangle(menuCenterX, menuStartY + (btnH + gap) * 2, btnW, btnH);
        btnEditor    = new Rectangle(menuCenterX, menuStartY + (btnH + gap) * 3, btnW, btnH);
        btnExitMenu  = new Rectangle(menuCenterX, menuStartY + (btnH + gap) * 4, btnW, btnH);
        
        int slotW = 600, slotH = 100, slotX = w / 2 - slotW / 2, slotStartY = h / 2 - 200;
        btnSlot1 = new Rectangle(slotX, slotStartY, slotW, slotH);
        btnDelete1 = new Rectangle(slotX + slotW + 10, slotStartY + (slotH - 60) / 2, 60, 60);
        btnSlot2 = new Rectangle(slotX, slotStartY + 120, slotW, slotH);
        btnDelete2 = new Rectangle(slotX + slotW + 10, slotStartY + 120 + (slotH - 60) / 2, 60, 60);
        btnSlot3 = new Rectangle(slotX, slotStartY + 240, slotW, slotH);
        btnDelete3 = new Rectangle(slotX + slotW + 10, slotStartY + 240 + (slotH - 60) / 2, 60, 60);
        btnBack  = new Rectangle(w / 2 - 120, slotStartY + 400, 240, 80);
        
        btnCreateMap = new Rectangle(w / 2 - 180, h - 100, 360, 60); 
        btnSaveMap   = new Rectangle(w - 210, 20, 140, 50);
        btnRestart = new Rectangle(w / 2 - 160, h / 2 + 50, 320, 90);
        
        if (btnConfirmYes == null) btnConfirmYes = new Rectangle(0,0,100,50);
        if (btnConfirmNo == null) btnConfirmNo = new Rectangle(0,0,100,50);
        if (btnSaveAndExit == null) btnSaveAndExit = new Rectangle(0,0,250,50);
        
        if (btnExitYes == null) btnExitYes = new Rectangle(0,0,100,50);
        if (btnExitNo == null) btnExitNo = new Rectangle(0,0,100,50);
        
        if (btnExitGameConfirmYes == null) btnExitGameConfirmYes = new Rectangle(0,0,100,50);
        if (btnExitGameConfirmNo == null) btnExitGameConfirmNo = new Rectangle(0,0,100,50);
        
        // charButtons are initialized in drawCharacterSelection
        
        int pauseStartY = h / 2 - 240; 
        btnResume = new Rectangle(menuCenterX, pauseStartY, btnW, btnH);
        btnSaveMenu = new Rectangle(menuCenterX, pauseStartY + (btnH + gap), btnW, btnH);
        btnLoadMenu = new Rectangle(menuCenterX, pauseStartY + (btnH + gap) * 2, btnW, btnH);
        btnMainMenu = new Rectangle(menuCenterX, pauseStartY + (btnH + gap) * 3, btnW, btnH);
        btnExitPause = new Rectangle(menuCenterX, pauseStartY + (btnH + gap) * 4, btnW, btnH);

        int uiX = w - 140, uiY = h;        
        btnMapOpen = new Rectangle(uiX, uiY - 190, 120, 50);      
        btnCraftingOpen = new Rectangle(uiX, uiY - 130, 120, 50); 
        btnInventoryOpen = new Rectangle(uiX, uiY - 70, 120, 50); 
        
        if (btnCloseWindow == null) btnCloseWindow = new Rectangle(w - 60, 20, 40, 40);
        
        // Initialize Character Selection UI
        int numChars = Assets.HERO_AVATARS.size();
        if (numChars > 0) {
            if (charButtons == null || charButtons.length != numChars) charButtons = new Rectangle[numChars];
            
            // Paper Dimensions (matching drawCharacterSelection)
            int paperW = 900;
            int paperH = 750;
            int px = w/2 - paperW/2;
            int py = h/2 - paperH/2 + 20;
            
            // Hero Cards (Top Row)
            int btnSize = 150;
            int heroGap = (paperW - (numChars * btnSize)) / (numChars + 1); // Equal spacing
            int startY = py + 60;
            
            for(int i=0; i<numChars; i++) {
                int cx = px + heroGap + i * (btnSize + heroGap);
                charButtons[i] = new Rectangle(cx, startY, btnSize, btnSize);
            }
            
            // Bottom Row (Now moved higher up, just below heroes)
            int bottomY = py + 230;
            
            // Left: Sword Selector (300x80)
            int leftCenter = px + paperW / 4;
            btnRibbon = new Rectangle(leftCenter - 150, bottomY, 300, 80);
            
            // Right: Name Input Box Area (Hitbox aligned with visual box)
            int rightCenter = px + 3 * paperW / 4;
            btnNameBox = new Rectangle(rightCenter - 160, bottomY + 30, 320, 80);
            
            // Bottom Right: Start Button (Moved to the actual corner)
            btnCharStart = new Rectangle(px + paperW - 340, py + paperH - 130, 320, 90);
        }

        initCraftingButtons();
        paletteRects = null; btnCamUp = null; btnCamDown = null; btnCamLeft = null; btnCamRight = null;
    }
    
    private void initCraftingButtons() {
        craftButtons = new ArrayList<>();
        craftButtons.add(new CraftingButton("Fountain", "HP Restore", 5, 3, 0, Item.Specific.FOUNTAIN));
        craftButtons.add(new CraftingButton("Monument", "+5 Dmg", 10, 5, 0, Item.Specific.MONUMENT));
        craftButtons.add(new CraftingButton("Bread", "Heal 30HP", 0, 0, 3, Item.Specific.BREAD));
    }

    public void drawModernMenu(Graphics2D g2, int w, int h) {
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        // Background is now rendered by GamePanel simulation
        /* 
        if (Assets.UI_WATER_BG != null) {
            int tw = Assets.UI_WATER_BG.getWidth() * 4, th = Assets.UI_WATER_BG.getHeight() * 4;
            for (int y = 0; y < h; y += th) for (int x = 0; x < w; x += tw) g2.drawImage(Assets.UI_WATER_BG, x, y, tw, th, null);
        } else { g2.setPaint(new GradientPaint(0, 0, new Color(20, 20, 30), 0, h, new Color(50, 30, 20))); g2.fillRect(0, 0, w, h); }
        */
        
        // Semi-transparent overlay for readability
        g2.setColor(new Color(0, 0, 0, 100));
        g2.fillRect(0, 0, w, h);
        
        int pw = 550, ph = 680, px = w/2 - pw/2, py = h/2 - ph/2 + 20;
        if (Assets.UI_PAPER != null) { g2.setColor(new Color(40, 30, 20)); g2.fillRect(px + 20, py + 20, pw - 40, ph - 40); draw9Slice(g2, Assets.UI_PAPER, px, py, pw, ph); }
        else { g2.setColor(new Color(200, 190, 160)); g2.fillRect(px, py, pw, ph); }
        
        if (Assets.UI_BANNER != null) g2.drawImage(Assets.UI_BANNER, w/2 - 300, py - 50, 600, 160, null);
        g2.setColor(Color.WHITE); g2.setFont(Assets.PIXEL_FONT != null ? Assets.PIXEL_FONT.deriveFont(54f) : new Font("Arial", Font.BOLD, 54));
        String title = "SURVIVAL 2D"; g2.drawString(title, w/2 - g2.getFontMetrics().stringWidth(title)/2, py + 35);
        
        g2.setColor(new Color(60, 40, 30)); g2.setFont(Assets.PIXEL_FONT != null ? Assets.PIXEL_FONT.deriveFont(20f) : new Font("Arial", Font.BOLD, 20));
        String info = "v0.2.1 | Created by Tudor Baranga"; g2.drawString(info, w/2 - g2.getFontMetrics().stringWidth(info)/2, py + ph - 40);

        drawMenuButton(g2, btnStartGame, "NEW GAME", Assets.UI_BTN_BLUE, Assets.UI_BTN_BLUE_PR, true);
        if (gp.hasSaveFile) drawMenuButton(g2, btnContinue, "CONTINUE", Assets.UI_BTN_BLUE, Assets.UI_BTN_BLUE_PR, true);
        else drawMenuButton(g2, btnContinue, "CONTINUE", Assets.UI_BTN_DISABLED, null, false);
        
        drawMenuButton(g2, btnLoadGame, "LOAD GAME", Assets.UI_BTN_BLUE, Assets.UI_BTN_BLUE_PR, true);
        drawMenuButton(g2, btnEditor, "MAP EDITOR", Assets.UI_BTN_BLUE, Assets.UI_BTN_BLUE_PR, true);
        drawMenuButton(g2, btnExitMenu, "EXIT", Assets.UI_BTN_RED, Assets.UI_BTN_RED_PR, true);
    }

    public void drawPauseMenu(Graphics2D g2) {
        int w = gp.getWidth(), h = gp.getHeight(), boxW = 500, boxH = 650, cx = w/2 - boxW/2, cy = h/2 - boxH/2;
        g2.setColor(new Color(0, 0, 0, 150)); g2.fillRect(0, 0, w, h);
        if (Assets.UI_MODAL != null) draw9Slice(g2, Assets.UI_MODAL, cx, cy, boxW, boxH);
        else { g2.setColor(Assets.UI_DARK); g2.fillRoundRect(cx, cy, boxW, boxH, 20, 20); }
        
        g2.setColor(new Color(60, 40, 30)); g2.setFont(Assets.PIXEL_FONT != null ? Assets.PIXEL_FONT.deriveFont(44f) : new Font("Arial", Font.BOLD, 40));
        String t = "PAUSED"; g2.drawString(t, w/2 - g2.getFontMetrics().stringWidth(t)/2, cy + 70); 
        
        drawMenuButton(g2, btnResume, "CONTINUE", Assets.UI_BTN_BLUE, Assets.UI_BTN_BLUE_PR, true); 
        drawMenuButton(g2, btnSaveMenu, "SAVE GAME", Assets.UI_BTN_BLUE, Assets.UI_BTN_BLUE_PR, true); 
        drawMenuButton(g2, btnLoadMenu, "LOAD GAME", Assets.UI_BTN_BLUE, Assets.UI_BTN_BLUE_PR, true); 
        drawMenuButton(g2, btnMainMenu, "MAIN MENU", Assets.UI_BTN_BLUE, Assets.UI_BTN_BLUE_PR, true); 
        drawMenuButton(g2, btnExitPause, "EXIT GAME", Assets.UI_BTN_RED, Assets.UI_BTN_RED_PR, true); 
    }
    
    public void drawExitConfirmation(Graphics2D g2) {
        int w = gp.getWidth(), h = gp.getHeight();
        g2.setColor(new Color(0, 0, 0, 150));
        g2.fillRect(0, 0, w, h);
        
        int mw = 500, mh = 350;
        int mx = w/2 - mw/2, my = h/2 - mh/2;
        
        if (Assets.UI_MENU_BG != null) {
            drawWoodTable9Slice(g2, Assets.UI_MENU_BG, mx, my, mw, mh);
        } else {
            g2.setColor(new Color(100, 60, 20));
            g2.fillRoundRect(mx, my, mw, mh, 20, 20);
        }

        g2.setColor(Color.WHITE);
        g2.setFont(Assets.PIXEL_FONT != null ? Assets.PIXEL_FONT.deriveFont(40f) : new Font("Arial", Font.BOLD, 40));
        String q1 = "Are you sure";
        String q2 = "you want to quit?";
        g2.drawString(q1, mx + mw/2 - g2.getFontMetrics().stringWidth(q1)/2, my + 110);
        g2.drawString(q2, mx + mw/2 - g2.getFontMetrics().stringWidth(q2)/2, my + 150);
        
        btnExitYes.setBounds(mx + 80, my + 230, 120, 60);
        btnExitNo.setBounds(mx + 300, my + 230, 120, 60);
        
        drawMenuButton(g2, btnExitYes, "YES", Assets.UI_BTN_BLUE, Assets.UI_BTN_BLUE_PR, true);
        drawMenuButton(g2, btnExitNo, "NO", Assets.UI_BTN_RED, Assets.UI_BTN_RED_PR, true);
    }

    public void drawExitGameConfirmation(Graphics2D g2) {
        int w = gp.getWidth(), h = gp.getHeight();
        
        int mw = 500, mh = 350;
        int mx = w/2 - mw/2, my = h/2 - mh/2;
        
        if (Assets.UI_MENU_BG != null) {
            drawWoodTable9Slice(g2, Assets.UI_MENU_BG, mx, my, mw, mh);
        } else {
            g2.setColor(new Color(100, 60, 20));
            g2.fillRoundRect(mx, my, mw, mh, 20, 20);
        }

        g2.setColor(Color.WHITE);
        g2.setFont(Assets.PIXEL_FONT != null ? Assets.PIXEL_FONT.deriveFont(48f) : new Font("Arial", Font.BOLD, 46));
        String q1 = "Are you sure";
        String q2 = "you want to quit?";
        g2.drawString(q1, mx + mw/2 - g2.getFontMetrics().stringWidth(q1)/2, my + 110);
        g2.drawString(q2, mx + mw/2 - g2.getFontMetrics().stringWidth(q2)/2, my + 150);
        
        btnExitGameConfirmYes.setBounds(mx + 80, my + 230, 120, 60);
        btnExitGameConfirmNo.setBounds(mx + 300, my + 230, 120, 60);
        
        drawMenuButton(g2, btnExitGameConfirmYes, "YES", Assets.UI_BTN_BLUE, Assets.UI_BTN_BLUE_PR, true);
        drawMenuButton(g2, btnExitGameConfirmNo, "NO", Assets.UI_BTN_RED, Assets.UI_BTN_RED_PR, true);
    }

    public void drawCharacterSelection(Graphics2D g2) {
        int w = gp.getWidth(), h = gp.getHeight();
        g2.setColor(new Color(0, 0, 0, 150));
        g2.fillRect(0, 0, w, h);
        
        g2.setColor(Color.WHITE);
        g2.setFont(Assets.PIXEL_FONT != null ? Assets.PIXEL_FONT.deriveFont(48f) : new Font("Arial", Font.BOLD, 46));
        String title = "CHOOSE YOUR HERO";
        g2.drawString(title, w/2 - g2.getFontMetrics().stringWidth(title)/2, 100);
        
        // Draw Special Paper Background
        if (Assets.UI_SPECIAL_PAPER != null) {
            int paperW = 900;
            int paperH = 750;
            int paperX = w/2 - paperW/2;
            int paperY = h/2 - paperH/2 + 20;
            
            draw9Slice(g2, Assets.UI_SPECIAL_PAPER, paperX, paperY, paperW, paperH);
        }
        
        drawCloseButton(g2, w - 80, 40);
        
        // Ensure buttons are initialized (safety check)
        if (charButtons == null || charButtons.length != Assets.HERO_AVATARS.size()) {
             initUI(); // Re-init if missing
             if (charButtons == null) return;
        }
        
        for(int i=0; i<charButtons.length; i++) {
            if (charButtons[i] == null) continue;
            Rectangle r = charButtons[i];
            
            boolean hover = r.contains(gp.inputHandler.mouseX, gp.inputHandler.mouseY);
            boolean selected = (gp.selectedCharIndex == i);
            
            // Draw Card Background
            if (Assets.UI_PAPER != null) {
                draw9Slice(g2, Assets.UI_PAPER, r.x, r.y, r.width, r.height);
            } else {
                g2.setColor(new Color(200, 190, 160));
                g2.fillRect(r.x, r.y, r.width, r.height);
            }
            
            // Highlight
            if (hover || selected) {
                g2.setColor(new Color(255, 255, 255, 50));
                g2.fillRect(r.x, r.y, r.width, r.height);
                g2.setColor(selected ? Color.GREEN : Color.YELLOW);
                g2.setStroke(new BasicStroke(3));
                g2.drawRect(r.x, r.y, r.width, r.height);
                g2.setStroke(new BasicStroke(1));
            }
            
            // Draw Avatar
            if (i < Assets.HERO_AVATARS.size()) {
                BufferedImage avatar = Assets.HERO_AVATARS.get(i);
                if (avatar != null) {
                    g2.drawImage(avatar, r.x + 15, r.y + 15, 120, 120, null);
                }
            }
        }
        
        // --- DRAW RIBBON BUTTON ---
        if (btnRibbon != null) {
            // Draw Background (Card style)
            if (Assets.UI_PAPER != null) {
                draw9Slice(g2, Assets.UI_PAPER, btnRibbon.x, btnRibbon.y, btnRibbon.width, btnRibbon.height);
            } else {
                g2.setColor(new Color(200, 190, 160));
                g2.fillRect(btnRibbon.x, btnRibbon.y, btnRibbon.width, btnRibbon.height);
            }

            if (Assets.RIBBON_VARIANTS.size() > 0) {
                BufferedImage rib = Assets.RIBBON_VARIANTS.get(selectedRibbonIndex % Assets.RIBBON_VARIANTS.size());
                // Draw sword scaled by height to fit 300x80 slot (approx 175x70)
                int targetH = 70;
                int targetW = (int)((float)rib.getWidth() / rib.getHeight() * targetH);
                int targetX = btnRibbon.x + (btnRibbon.width - targetW) / 2;
                int targetY = btnRibbon.y + (btnRibbon.height - targetH) / 2;
                
                g2.drawImage(rib, targetX, targetY, targetW, targetH, null);
                
                if (btnRibbon.contains(gp.inputHandler.mouseX, gp.inputHandler.mouseY)) {
                    g2.setColor(new Color(255, 255, 255, 50));
                    g2.fillRect(btnRibbon.x, btnRibbon.y, btnRibbon.width, btnRibbon.height);
                }
            }
        }
        
        // --- DRAW DROPDOWN (ALWAYS OPEN) ---
        if (Assets.RIBBON_VARIANTS.size() > 0 && btnRibbon != null) {
            int numRibbons = Assets.RIBBON_VARIANTS.size();
            // Re-init dropdown rects if needed (dynamic)
            if (ribbonDropdownRects == null || ribbonDropdownRects.length != numRibbons) ribbonDropdownRects = new Rectangle[numRibbons];
            
            int dw = 300;
            int dh = 80;
            int dx = btnRibbon.x;
            int dy = btnRibbon.y + 85; 
            
            int drawIndex = 0;
            for(int i=0; i<numRibbons; i++) {
                if (i == selectedRibbonIndex) {
                    ribbonDropdownRects[i] = null; 
                    continue; 
                }

                if (ribbonDropdownRects[i] == null) ribbonDropdownRects[i] = new Rectangle(dx, dy + drawIndex * (dh + 5), dw, dh);
                else ribbonDropdownRects[i].setBounds(dx, dy + drawIndex * (dh + 5), dw, dh);
                
                if (Assets.UI_PAPER != null) draw9Slice(g2, Assets.UI_PAPER, ribbonDropdownRects[i].x, ribbonDropdownRects[i].y, dw, dh);
                else { g2.setColor(new Color(200, 190, 160)); g2.fillRect(ribbonDropdownRects[i].x, ribbonDropdownRects[i].y, dw, dh); }
                
                BufferedImage rib = Assets.RIBBON_VARIANTS.get(i);
                int targetH = 70;
                int targetW = (int)((float)rib.getWidth() / rib.getHeight() * targetH);
                int targetX = ribbonDropdownRects[i].x + (dw - targetW) / 2;
                int targetY = ribbonDropdownRects[i].y + (dh - targetH) / 2;

                g2.drawImage(rib, targetX, targetY, targetW, targetH, null);
                
                if (i == selectedRibbonIndex) {
                    g2.setColor(Color.GREEN);
                    g2.setStroke(new BasicStroke(2));
                    g2.drawRect(ribbonDropdownRects[i].x, ribbonDropdownRects[i].y, dw, dh);
                    g2.setStroke(new BasicStroke(1));
                }
                
                drawIndex++;
            }
        } else {
            ribbonDropdownRects = null;
        }

        // --- DRAW NAME INPUT BOX ---
        if (btnNameBox != null) {
            // Label above (Adjusted to stay at py + 250)
            g2.setColor(Color.WHITE);
            g2.setFont(Assets.PIXEL_FONT != null ? Assets.PIXEL_FONT.deriveFont(40f) : new Font("Arial", Font.BOLD, 22));
            g2.drawString("HERO NAME", btnNameBox.x, btnNameBox.y - 10);

            // "Ser" prefix (Adjusted to stay at py + 310)
            String prefix = "Ser ";
            g2.setFont(Assets.PIXEL_FONT != null ? Assets.PIXEL_FONT.deriveFont(40f) : new Font("Arial", Font.BOLD, 30));
            int preW = g2.getFontMetrics().stringWidth(prefix);
            g2.drawString(prefix, btnNameBox.x - preW - 5, btnNameBox.y + 50);

            // Box Background (Aligned with hitbox)
            if (Assets.UI_PAPER != null) {
                draw9Slice(g2, Assets.UI_PAPER != null ? Assets.UI_PAPER : Assets.UI_PAPER, btnNameBox.x, btnNameBox.y, btnNameBox.width, btnNameBox.height);
            } else {
                g2.setColor(new Color(50, 50, 60));
                g2.fillRect(btnNameBox.x, btnNameBox.y, btnNameBox.width, btnNameBox.height);
            }

            if (gp.isEditingHeroName) {
                g2.setColor(Color.YELLOW);
                g2.setStroke(new BasicStroke(2));
                g2.drawRect(btnNameBox.x, btnNameBox.y, btnNameBox.width, btnNameBox.height);
                g2.setStroke(new BasicStroke(1));
            }

            // Name Text (Centered vertically in box)
            g2.setColor(Color.BLACK);
            String nameText = gp.heroNameInput.toString() + (gp.isEditingHeroName && (gp.tickCounter / 20) % 2 == 0 ? "|" : "");
            g2.drawString(nameText, btnNameBox.x + 20, btnNameBox.y + 52);

            // Validation message
            if (gp.heroNameInput.length() > 12) {
                g2.setColor(Color.RED);
                g2.setFont(Assets.PIXEL_FONT != null ? Assets.PIXEL_FONT.deriveFont(30f) : new Font("Arial", Font.BOLD, 22));
                g2.drawString("NAME IS TOO LONG!", btnNameBox.x, btnNameBox.y + btnNameBox.height + 30);
            }
        }
        
        drawMenuButton(g2, btnCharStart, "START GAME", Assets.UI_BTN_BLUE, Assets.UI_BTN_BLUE_PR, true);
    }

    private void drawWoodTable9Slice(Graphics2D g2, java.awt.image.BufferedImage img, int x, int y, int w, int h) {
        // Specific coordinates for Tiny Swords WoodTable.png (448x448)
        // With manual trimming of internal "seams" to remove dark borders/shadows
        
        // Top Row: y=49 to 127. Trimming bottom ~8 pixels to remove shadow/border
        // Mid Row: y=192 to 255. Trimming top ~8px and bottom ~8px
        // Bot Row: y=320 to 422. Trimming top ~8px
        
        int trimY = 10; // Vertical trim amount for internal seams
        int trimX = 10; // Horizontal trim amount for internal seams

        int[] sx = {50, 192 + trimX, 320};
        int[] sw = {78, 64 - 2*trimX, 78};
        
        int[] sy = {49, 192 + trimY, 320 + trimY};
        int[] sh = {79 - trimY, 64 - 2*trimY, 103 - trimY};

        // Destination dimensions
        // Left/Top and Right/Bottom retain their original size (sw/sh)
        // Center stretches to fill the remaining space
        
        int[] dw = {sw[0], w - sw[0] - sw[2], sw[2]};
        int[] dh = {sh[0], h - sh[0] - sh[2], sh[2]};
        
        // Destination coords
        int[] dx = {x, x + dw[0], x + dw[0] + dw[1]};
        int[] dy = {y, y + dh[0], y + dh[0] + dh[1]};
        
        for(int row=0; row<3; row++) {
            for(int col=0; col<3; col++) {
                 g2.drawImage(img, 
                     dx[col], dy[row], dx[col] + dw[col], dy[row] + dh[row], 
                     sx[col], sy[row], sx[col] + sw[col], sy[row] + sh[row], 
                     null);
            }
        }
    }

    public void drawMenuButton(Graphics2D g2, Rectangle r, String text, java.awt.image.BufferedImage img, java.awt.image.BufferedImage prImg, boolean hoverable) {
        if (r == null) 
            return;
        boolean pressed = (gp.pressedButton != null && gp.pressedButton.equals(r));
        boolean hover = hoverable && !pressed && r.contains(gp.inputHandler.mouseX, gp.inputHandler.mouseY);
        java.awt.image.BufferedImage texture = img;
        if (pressed && prImg != null) 
            texture = prImg; 
        else if (hover && Assets.UI_BUTTON_HOVER != null) 
            texture = Assets.UI_BUTTON_HOVER;
        
        if (texture != null) { 
            if (pressed && prImg == null) 
                draw9Slice(g2, texture, r.x, r.y + 4, r.width, r.height - 4); 
            else 
                draw9Slice(g2, texture, r.x, r.y, r.width, r.height); 
            }
        else { 
            g2.setColor(pressed ? Color.LIGHT_GRAY : (hover ? Color.YELLOW : Color.GRAY)); 
            g2.fillRoundRect(r.x, r.y, r.width, r.height, 15, 15); 
        }
        
                g2.setColor(new Color(40, 40, 60)); g2.setFont(Assets.PIXEL_FONT != null ? Assets.PIXEL_FONT.deriveFont(42f) : new Font("Arial", Font.BOLD, 40));
        
                FontMetrics fm = g2.getFontMetrics();
        
                int textY = r.y + (r.height + fm.getAscent()) / 2 - 11; 
        
                if (img == Assets.UI_BTN_DISABLED) textY += 4;
        
                if (pressed) textY += 4;
        g2.drawString(text, r.x + r.width/2 - fm.stringWidth(text)/2, textY);
    }

    private void draw9Slice(Graphics2D g2, java.awt.image.BufferedImage img, int x, int y, int w, int h) {
        if (img == null) return;
        int iw = img.getWidth(), ih = img.getHeight();
        int sw = iw / 3, sh = ih / 3;
        g2.drawImage(img, x, y, x+sw, y+sh, 0, 0, sw, sh, null);
        g2.drawImage(img, x+w-sw, y, x+w, y+sh, iw-sw, 0, iw, sh, null);
        g2.drawImage(img, x, y+h-sh, x+sw, y+h, 0, ih-sh, sw, ih, null);
        g2.drawImage(img, x+w-sw, y+h-sh, x+w, y+h, iw-sw, ih-sh, iw, ih, null);
        g2.drawImage(img, x+sw, y, x+w-sw, y+sh, sw, 0, iw-sw, sh, null);
        g2.drawImage(img, x+sw, y+h-sh, x+w-sw, y+h, sw, ih-sh, iw-sw, ih, null);
        g2.drawImage(img, x, y+sh, x+sw, y+h-sh, 0, sh, sw, ih-sh, null);
        g2.drawImage(img, x+w-sw, y+sh, x+w, y+h-sh, iw-sw, sh, iw, ih-sh, null);
        g2.drawImage(img, x+sw, y+sh, x+w-sw, y+h-sh, sw, sh, iw-sw, ih-sh, null);
    }
    
    public void drawHUD(Graphics2D g2, Player p) {
        drawHotbar(g2, p); g2.setColor(Color.RED); g2.fillRect(20, 20, 200, 20); g2.setColor(new Color(0, 100, 0)); g2.fillRect(20, 20, (int)(200 * ((double)p.getHealth()/p.getMaxHealth())), 20); g2.setColor(Color.WHITE); g2.drawRect(20, 20, 200, 20);
        g2.setFont(Assets.PIXEL_FONT != null ? Assets.PIXEL_FONT.deriveFont(14f) : new Font("Arial", Font.BOLD, 12)); g2.drawString("HP: " + p.getHealth() + "/" + p.getMaxHealth(), 30, 35);
        g2.drawString("Level: " + p.getLevel(), 20, 55);
    }

    public void drawCraftingMenu(Graphics2D g2, Player p) {
        int w = gp.getWidth(), h = gp.getHeight(), boxW = 500, boxH = 400, cx = w/2 - boxW/2, cy = h/2 - boxH/2;
        g2.setColor(new Color(0, 0, 0, 200)); g2.fillRect(0, 0, w, h); g2.setColor(Assets.UI_DARK); g2.fillRoundRect(cx, cy, boxW, boxH, 20, 20); g2.setColor(Assets.UI_BORDER); g2.setStroke(new BasicStroke(3)); g2.drawRoundRect(cx, cy, boxW, boxH, 20, 20);
        g2.setColor(Color.WHITE); g2.setFont(Assets.PIXEL_FONT != null ? Assets.PIXEL_FONT.deriveFont(32f) : new Font("Arial", Font.BOLD, 30)); g2.drawString("CRAFTING", cx + 20, cy + 40); drawCloseButton(g2, cx + boxW - 50, cy + 10);
        for(int i=0; i<craftButtons.size(); i++) {
            CraftingButton cb = craftButtons.get(i); if(cb.rect == null) cb.rect = new Rectangle(); cb.rect.setBounds(cx + 20, cy + 60 + i * 80, boxW - 40, 70);
            boolean can = p.hasItem(Item.Specific.WOOD, cb.woodCost) && p.hasItem(Item.Specific.STONE, cb.stoneCost) && p.hasItem(Item.Specific.GRAIN, cb.grainCost);
            g2.setColor(cb.rect.contains(gp.inputHandler.mouseX, gp.inputHandler.mouseY) ? new Color(80, 80, 100) : new Color(60, 60, 70)); g2.fillRoundRect(cb.rect.x, cb.rect.y, cb.rect.width, cb.rect.height, 10, 10);
            g2.setColor(can ? Color.GREEN : Color.RED); g2.drawRoundRect(cb.rect.x, cb.rect.y, cb.rect.width, cb.rect.height, 10, 10);
            drawItemIcon(g2, new Item(cb.name, cb.type == Item.Specific.BREAD ? Item.Type.CONSUMABLE : Item.Type.BUILDING, cb.type, 0), cb.rect.x + 10, cb.rect.y + 10, 50);
            g2.setColor(Color.WHITE); g2.setFont(Assets.PIXEL_FONT != null ? Assets.PIXEL_FONT.deriveFont(22f) : new Font("Arial", Font.BOLD, 20)); g2.drawString(cb.name, cb.rect.x + 70, cb.rect.y + 25);
            g2.setFont(Assets.PIXEL_FONT != null ? Assets.PIXEL_FONT.deriveFont(16f) : new Font("Arial", Font.PLAIN, 14)); g2.drawString(cb.desc, cb.rect.x + 70, cb.rect.y + 45);
            int kx = cb.rect.x + 230, ky = cb.rect.y + 17; g2.setFont(Assets.PIXEL_FONT != null ? Assets.PIXEL_FONT.deriveFont(20f) : new Font("Arial", Font.BOLD, 18));
            if (cb.woodCost > 0) { IconRenderer.drawWoodIcon(g2, kx, ky, 36); g2.setColor(Color.WHITE); g2.drawString(String.valueOf(cb.woodCost), kx + 42, ky + 25); kx += 90; }
            if (cb.stoneCost > 0) { IconRenderer.drawStoneIcon(g2, kx, ky, 36); g2.setColor(Color.WHITE); g2.drawString(String.valueOf(cb.stoneCost), kx + 42, ky + 25); kx += 90; }
            if (cb.grainCost > 0) { IconRenderer.renderGrain(g2, kx, ky, 36, Enums.Quality.COMMON); g2.setColor(Color.WHITE); g2.drawString(String.valueOf(cb.grainCost), kx + 42, ky + 25); }
        }
    }
    
    public void drawInventoryMenu(Graphics2D g2, Player p) {
        int w = gp.getWidth(), h = gp.getHeight(), boxW = 600, boxH = 500, cx = w/2 - boxW/2, cy = h/2 - boxH/2;
        g2.setColor(new Color(0, 0, 0, 200)); g2.fillRect(0, 0, w, h); g2.setColor(Assets.UI_DARK); g2.fillRoundRect(cx, cy, boxW, boxH, 20, 20); g2.setColor(Assets.UI_BORDER); g2.setStroke(new BasicStroke(3)); g2.drawRoundRect(cx, cy, boxW, boxH, 20, 20);
        g2.setColor(Color.WHITE); g2.setFont(Assets.PIXEL_FONT != null ? Assets.PIXEL_FONT.deriveFont(32f) : new Font("Arial", Font.BOLD, 30)); g2.drawString("INVENTORY", cx + 20, cy + 40); drawCloseButton(g2, cx + boxW - 50, cy + 10);
        for(int i=0; i<4; i++) { int ax = cx + 40, ay = cy + 80 + i * 90; if(armorRects[i] == null) armorRects[i] = new Rectangle(); armorRects[i].setBounds(ax, ay, 70, 70); g2.setColor(new Color(0,0,0,100)); g2.fillRoundRect(ax, ay, 70, 70, 10, 10); if (p.armor[i] != null) drawItemIcon(g2, p.armor[i], ax+5, ay+5, 60); else IconRenderer.drawArmorPlaceholder(g2, ax, ay, 70, i); }
        for(int i=0; i<16; i++) { int col = i % 4, row = i / 4, bx = cx + 200 + col * 85, by = cy + 80 + row * 85; if(backpackRects[i] == null) backpackRects[i] = new Rectangle(); backpackRects[i].setBounds(bx, by, 70, 70); g2.setColor(new Color(0,0,0,100)); g2.fillRoundRect(bx, by, 70, 70, 10, 10); if (p.backpack[i] != null && p.backpack[i] != gp.draggingItem) { drawItemIcon(g2, p.backpack[i], bx+5, by+5, 60); drawItemQuantity(g2, p.backpack[i], bx+5, by+5, 60); } }
        int sy = cy + boxH - 40; IconRenderer.drawSword(g2, cx + 50, sy - 30, 40); g2.setColor(Color.WHITE); g2.setFont(Assets.PIXEL_FONT != null ? Assets.PIXEL_FONT.deriveFont(22f) : new Font("Arial", Font.BOLD, 20)); g2.drawString(String.valueOf(p.getTotalDamage()), cx + 95, sy);
        IconRenderer.drawChestplate(g2, cx + 240, sy - 30, 40); g2.drawString(String.valueOf(p.getDefense()), cx + 285, sy);
        IconRenderer.drawGoldCoin(g2, cx + 430, sy - 30, 40); g2.drawString(String.valueOf(p.getGold()), cx + 475, sy); drawHotbar(g2, p);
    }
    
    public void drawSimpleButton(Graphics2D g2, Rectangle r, String text, Color c) { g2.setColor(r.contains(gp.inputHandler.mouseX, gp.inputHandler.mouseY) ? c.brighter() : c); g2.fillRoundRect(r.x, r.y, r.width, r.height, 10, 10); g2.setColor(Color.WHITE); g2.setFont(Assets.PIXEL_FONT != null ? Assets.PIXEL_FONT.deriveFont(18f) : new Font("Arial", Font.BOLD, 16)); FontMetrics fm = g2.getFontMetrics(); g2.drawString(text, r.x + r.width/2 - fm.stringWidth(text)/2, r.y + r.height/2 + fm.getAscent()/2 - 2); }
    
    public void drawCloseButton(Graphics2D g2, int x, int y) { 
        btnCloseWindow.setBounds(x, y, 60, 60); 
        
        if (Assets.UI_BTN_BG != null) {
            g2.drawImage(Assets.UI_BTN_BG, x, y, 60, 60, null);
        }
        
        boolean hover = btnCloseWindow.contains(gp.inputHandler.mouseX, gp.inputHandler.mouseY);
        boolean pressed = hover && gp.inputHandler.isMousePressed && gp.pressedButton != null && gp.pressedButton.equals(btnCloseWindow);
        
        java.awt.image.BufferedImage img = Assets.UI_BTN_CLOSE;
        if (pressed) img = Assets.UI_BTN_CLOSE_PRESSED;
        
        if (img != null) {
            // Centered icon (60-40)/2 = 10.
            // Since drawImage draws from top-left, we use x+10, y+10.
            // If the user says it's not centered, maybe the image has padding. 
            // I will stick to mathematical centering of the 40x40 draw rect.
            int iconSize = 40;
            int iconX = x + (60 - iconSize) / 2;
            int iconY = y + (60 - iconSize) / 2 + 4;
            g2.drawImage(img, iconX, iconY, iconSize, iconSize, null);
        } else {
            // Fallback
            g2.setColor(new Color(150, 0, 0)); 
            g2.fillRoundRect(x + 10, y + 10, 40, 40, 10, 10); 
            g2.setColor(Color.WHITE); 
            g2.setFont(new Font("Arial", Font.BOLD, 20)); 
            g2.drawString("X", x + 23, y + 37); 
        }
    }

    public void drawGameOverScreen(Graphics2D g2) {
        int w = gp.getWidth(), h = gp.getHeight(); g2.setColor(new Color(0, 0, 0, 180)); g2.fillRect(0, 0, w, h);
        g2.setColor(Color.RED); g2.setFont(Assets.PIXEL_FONT != null ? Assets.PIXEL_FONT.deriveFont(72f) : new Font("Arial", Font.BOLD, 70));
        String msg = "YOU DIED"; g2.drawString(msg, w/2 - g2.getFontMetrics().stringWidth(msg)/2, h/2 - 50);
        drawMenuButton(g2, btnRestart, "RESTART", Assets.UI_BTN_BLUE, Assets.UI_BTN_BLUE_PR, true); 
        drawMenuButton(g2, btnMainMenu, "MAIN MENU", Assets.UI_BTN_RED, Assets.UI_BTN_RED_PR, true);
    }

    public void drawSlotSelectionMenu(Graphics2D g2) {
        int w = gp.getWidth(), h = gp.getHeight(), cx = w / 2, cy = h / 2;
        if (gp.showSaveDeleteConfirm) {
            g2.setColor(new Color(0, 0, 0, 200)); g2.fillRect(0, 0, w, h); drawSaveSlot(g2, 1, btnSlot1, btnDelete1); drawSaveSlot(g2, 2, btnSlot2, btnDelete2); drawSaveSlot(g2, 3, btnSlot3, btnDelete3);
            g2.setColor(new Color(40, 40, 50)); g2.fillRoundRect(cx - 200, cy - 100, 400, 200, 20, 20); g2.setColor(Color.WHITE); g2.drawRoundRect(cx - 200, cy - 100, 400, 200, 20, 20);
            String m = "Delete this save?"; g2.setFont(Assets.PIXEL_FONT != null ? Assets.PIXEL_FONT.deriveFont(28f) : new Font("Arial", Font.BOLD, 25)); g2.drawString(m, cx - g2.getFontMetrics().stringWidth(m)/2, cy - 20);
            btnConfirmYes.setBounds(cx - 110, cy + 30, 100, 50); btnConfirmNo.setBounds(cx + 10, cy + 30, 100, 50);
            drawMenuButton(g2, btnConfirmYes, "DELETE", Assets.UI_BTN_RED, Assets.UI_BTN_RED_PR, true); drawMenuButton(g2, btnConfirmNo, "NO", Assets.UI_BTN_BLUE, Assets.UI_BTN_BLUE_PR, true); return;
        }
        if (gp.showLoadConfirm) {
            g2.setColor(new Color(0, 0, 0, 200)); g2.fillRect(0, 0, w, h); drawSaveSlot(g2, 1, btnSlot1, btnDelete1); drawSaveSlot(g2, 2, btnSlot2, btnDelete2); drawSaveSlot(g2, 3, btnSlot3, btnDelete3);
            g2.setColor(new Color(40, 40, 50)); g2.fillRoundRect(cx - 200, cy - 100, 400, 200, 20, 20); g2.setColor(Color.WHITE); g2.drawRoundRect(cx - 200, cy - 100, 400, 200, 20, 20);
            String m = "Load this game?"; g2.setFont(Assets.PIXEL_FONT != null ? Assets.PIXEL_FONT.deriveFont(28f) : new Font("Arial", Font.BOLD, 25)); g2.drawString(m, cx - g2.getFontMetrics().stringWidth(m)/2, cy - 20);
            btnConfirmYes.setBounds(cx - 110, cy + 30, 100, 50); btnConfirmNo.setBounds(cx + 10, cy + 30, 100, 50);
            drawMenuButton(g2, btnConfirmYes, "YES", Assets.UI_BTN_BLUE, Assets.UI_BTN_BLUE_PR, true); drawMenuButton(g2, btnConfirmNo, "NO", Assets.UI_BTN_RED, Assets.UI_BTN_RED_PR, true); return;
        }
        g2.setColor(new Color(0, 0, 0, 200)); g2.fillRect(0, 0, w, h); String title = (gp.currentState == GamePanel.GameState.PAUSE_SAVE) ? "SAVE GAME" : "LOAD GAME";
        g2.setColor(Color.WHITE); g2.setFont(Assets.PIXEL_FONT != null ? Assets.PIXEL_FONT.deriveFont(54f) : new Font("Arial", Font.BOLD, 50)); g2.drawString(title, w/2 - g2.getFontMetrics().stringWidth(title)/2, 150);
        
        drawCloseButton(g2, w - 80, 40);
        
        drawSaveSlot(g2, 1, btnSlot1, btnDelete1); drawSaveSlot(g2, 2, btnSlot2, btnDelete2); drawSaveSlot(g2, 3, btnSlot3, btnDelete3);
        drawMenuButton(g2, btnBack, "BACK", Assets.UI_BTN_BLUE, Assets.UI_BTN_BLUE_PR, true);
        if (gp.menuMessage != null && !gp.menuMessage.isEmpty()) { g2.setColor(gp.menuMessage.contains("SAVED") ? Color.GREEN : Color.RED); g2.setFont(Assets.PIXEL_FONT != null ? Assets.PIXEL_FONT.deriveFont(44f) : new Font("Arial", Font.BOLD, 40)); g2.drawString(gp.menuMessage, w/2 - g2.getFontMetrics().stringWidth(gp.menuMessage)/2, h/2); }
        
        if (gp.systemMessage != null && !gp.systemMessage.isEmpty()) {
            g2.setColor(Color.RED);
            g2.setFont(Assets.PIXEL_FONT != null ? Assets.PIXEL_FONT.deriveFont(24f) : new Font("Arial", Font.BOLD, 22));
            String msg = gp.systemMessage;
            g2.drawString(msg, w - g2.getFontMetrics().stringWidth(msg) - 20, h - 20);
        }
    }
    
    private void drawSaveSlot(Graphics2D g2, int slot, Rectangle b, Rectangle d) {
        g2.setColor(b.contains(gp.inputHandler.mouseX, gp.inputHandler.mouseY) ? new Color(60, 60, 70) : new Color(40, 40, 50)); 
        g2.fillRoundRect(b.x, b.y, b.width, b.height, 20, 20); 
        g2.setColor(Color.GRAY); 
        g2.drawRoundRect(b.x, b.y, b.width, b.height, 20, 20);

        SaveInfo info = gp.getSaveInfo(slot);
        
        // Always draw thumbnail and date if they exist, even in edit mode
        if (info.exists) {
            if (info.thumbnail != null) g2.drawImage(info.thumbnail, b.x + 10, b.y + 10, 130, 80, null);
            g2.setColor(Color.LIGHT_GRAY); 
            g2.setFont(Assets.PIXEL_FONT != null ? Assets.PIXEL_FONT.deriveFont(18f) : new Font("Arial", Font.PLAIN, 16)); 
            g2.drawString(info.date, b.x + 160, b.y + 70); 
            
            // Draw Delete Button using new style
            boolean dHover = d.contains(gp.inputHandler.mouseX, gp.inputHandler.mouseY);
            boolean dPressed = dHover && gp.inputHandler.isMousePressed;
            
            // Force a square button centered in the delete slot area
            int btnSize = 60;
            int bx = d.x + (d.width - btnSize) / 2;
            int by = d.y + (d.height - btnSize) / 2;
            
            // Background
            if (Assets.UI_BTN_BG != null) {
                 g2.drawImage(Assets.UI_BTN_BG, bx, by, btnSize, btnSize, null);
            } else {
                 g2.setColor(new Color(40, 30, 20)); g2.fillRoundRect(bx, by, btnSize, btnSize, 10, 10);
            }

            // Icon
            java.awt.image.BufferedImage dImg = Assets.UI_BTN_DELETE;
            if (dPressed) dImg = Assets.UI_BTN_DELETE_PR;
            
            if (dImg != null) {
                int iconSize = 40; // Larger icon for larger button
                int ix = bx + (btnSize - iconSize) / 2;
                int iy = by + (btnSize - iconSize) / 2 + 3;
                g2.drawImage(dImg, ix, iy, iconSize, iconSize, null);
            } else {
                g2.setColor(new Color(150, 0, 0)); 
                g2.fillRoundRect(d.x, d.y, d.width, d.height, 10, 10); 
                g2.setColor(Color.WHITE); 
                g2.drawString("X", d.x + 18, d.y + 60); 
            }
        }

        if (gp.editingSaveSlot == slot) {
            // Editing Mode - Overlay Text Input
            g2.setColor(Color.YELLOW);
            g2.setFont(Assets.PIXEL_FONT != null ? Assets.PIXEL_FONT.deriveFont(24f) : new Font("Arial", Font.BOLD, 22));
            String txt = gp.currentTypingText.toString() + ((gp.tickCounter / 30) % 2 == 0 ? "|" : "");
            g2.drawString(txt, b.x + 160, b.y + 40);
            
            // Instructions
            g2.setFont(Assets.PIXEL_FONT != null ? Assets.PIXEL_FONT.deriveFont(16f) : new Font("Arial", Font.ITALIC, 14));
            g2.setColor(Color.GREEN);
            // If overwriting (info.exists), show instruction at default position. If new save (!info.exists), show at date position.
            if (info.exists) g2.drawString("Type Name & Enter", b.x + 350, b.y + 70);
            else g2.drawString("Type Name & Enter", b.x + 160, b.y + 70);
        } else {
            // Normal Mode
            if (info.exists) { 
                g2.setColor(Color.WHITE); 
                g2.setFont(Assets.PIXEL_FONT != null ? Assets.PIXEL_FONT.deriveFont(24f) : new Font("Arial", Font.BOLD, 22)); 
                g2.drawString(info.name, b.x + 160, b.y + 40); 
            } else { 
                g2.setColor(Color.GRAY); 
                g2.setFont(Assets.PIXEL_FONT != null ? Assets.PIXEL_FONT.deriveFont(26f) : new Font("Arial", Font.ITALIC, 24)); 
                g2.drawString("Empty Slot", b.x + 250, b.y + 60); 
            }
        }
    }

    public void drawShopMenu(Graphics2D g2, Player p) {        
        int w = gp.getWidth(), h = gp.getHeight(), cx = w/2, cy = h/2, pw = 900, ph = 550, px = cx - pw/2, py = cy - ph/2;
        g2.setColor(new Color(40, 35, 30)); g2.fillRoundRect(px, py, pw, ph, 20, 20); g2.setColor(new Color(150, 120, 50)); g2.setStroke(new BasicStroke(4)); g2.drawRoundRect(px, py, pw, ph, 20, 20);
        g2.setColor(Color.WHITE); g2.setFont(Assets.PIXEL_FONT != null ? Assets.PIXEL_FONT.deriveFont(32f) : new Font("Arial", Font.BOLD, 30)); g2.drawString("TRADING POST", px + 350, py + 40);
        IconRenderer.drawGoldCoin(g2, px + pw - 140, py + 40, 40); g2.drawString(String.valueOf(p.getGold()), px + pw - 95, py + 68); drawCloseButton(g2, px + ph - 50, py + 10);
        if (shopBuyRects == null || shopBuyRects.length != gp.merchantStock.size()) shopBuyRects = new Rectangle[gp.merchantStock.size()];
        for(int i=0; i<gp.merchantStock.size(); i++) {
            Item it = gp.merchantStock.get(i); int cost = it.specificType == Item.Specific.HEALTH_POTION ? 50 : it.value * 5; 
            if(shopBuyRects[i] == null) shopBuyRects[i] = new Rectangle(); shopBuyRects[i].setBounds(px + 40, py + 100 + i*70, 380, 60); Rectangle r = shopBuyRects[i]; g2.setColor(r.contains(gp.inputHandler.mouseX, gp.inputHandler.mouseY) ? new Color(70, 60, 50) : new Color(50, 45, 40)); g2.fillRoundRect(r.x, r.y, r.width, r.height, 10, 10);
            drawItemIcon(g2, it, r.x + 5, r.y + 5, 50); g2.setColor(Color.WHITE); g2.setFont(Assets.PIXEL_FONT != null ? Assets.PIXEL_FONT.deriveFont(18f) : new Font("Arial", Font.BOLD, 16)); g2.drawString(it.name, r.x + 65, r.y + 25);
            g2.setColor(p.getGold() >= cost ? Color.GREEN : Color.RED); g2.drawString(cost + " G", r.x + 300, r.y + 35);
        }
        if (shopSellRects == null) shopSellRects = new Rectangle[20];
        for(int i=0; i<20; i++) {
            int col = i % 4, row = i / 4, sx = px + 480 + col * 95, sy = py + 100 + row * 80; if(shopSellRects[i] == null) shopSellRects[i] = new Rectangle(); shopSellRects[i].setBounds(sx, sy, 70, 70);
            Item it = (i < 15) ? p.backpack[i] : p.inventory[i-15]; g2.setColor(shopSellRects[i].contains(gp.inputHandler.mouseX, gp.inputHandler.mouseY) ? new Color(80, 80, 90) : new Color(30, 30, 35)); g2.fillRoundRect(sx, sy, 70, 70, 10, 10);
            if (it != null) { drawItemIcon(g2, it, sx+5, sy+5, 60); drawItemQuantity(g2, it, sx+5, sy+5, 60); int val = it.specificType == Item.Specific.WOOD || it.specificType == Item.Specific.GRAIN ? 2 : (it.specificType == Item.Specific.STONE ? 3 : (it.specificType == Item.Specific.BREAD ? 5 : (it.specificType == Item.Specific.HEALTH_POTION ? 15 : it.value * 4))); g2.setColor(new Color(100, 255, 100)); g2.drawString("+"+val, sx + 35 - g2.getFontMetrics().stringWidth("+"+val)/2, sy + 65); }
        }
    }
    
    public void drawMapScreen(Graphics2D g2, WorldMap m, Player p) {
        int w = gp.getWidth(), h = gp.getHeight(), sc = 6, mw = m.cols * sc, mh = m.rows * sc, sx = w / 2 - mw / 2, sy = h / 2 - mh / 2;
        g2.setColor(new Color(0, 0, 0, 200)); g2.fillRect(0, 0, w, h); g2.setColor(new Color(30, 30, 35)); g2.fillRect(sx - 10, sy - 10, mw + 20, mh + 20); drawCloseButton(g2, sx + mw - 30, sy - 50);
        for (int r = 0; r < m.rows; r++) for (int c = 0; c < m.cols; c++) {
            int dx = sx + c * sc, dy = sy + r * sc; if (!m.explored[r][c]) { g2.setColor(Color.BLACK); g2.fillRect(dx, dy, sc, sc); } else {
                Object e = m.grid[r][c]; g2.setColor((r + c) % 2 == 0 ? Assets.GRASS_1 : Assets.GRASS_2); g2.fillRect(dx, dy, sc, sc);
                if ("WATER".equals(e)) IconRenderer.renderWaterTile(g2, dx, dy, sc, gp.tickCounter); else if (e instanceof Tree) IconRenderer.drawTree(g2, dx, dy, sc, ((Tree)e).getQuality()); else if (e instanceof Rock) IconRenderer.drawRock(g2, dx, dy, sc, ((Rock)e).getQuality()); else if (e instanceof Vendor) IconRenderer.drawVendor(g2, dx, dy, sc);
            }
        }
        int px = sx + p.x * sc, py = sy + p.y * sc; g2.setColor(Assets.SKIN); g2.fillOval(px - 2, py - 2, sc + 4, sc + 4);
    }

    public void drawEditorSelectMenu(Graphics2D g2) {
        int w = gp.getWidth(), h = gp.getHeight(); g2.setPaint(new GradientPaint(0, 0, new Color(20, 10, 30), 0, h, new Color(40, 20, 60))); g2.fillRect(0, 0, w, h);
        int mx = w/2, my = h/2;
        g2.setColor(Color.WHITE); g2.setFont(Assets.PIXEL_FONT != null ? Assets.PIXEL_FONT.deriveFont(44f) : new Font("Arial", Font.BOLD, 40)); g2.drawString("MAP EDITOR", w/2 - g2.getFontMetrics().stringWidth("MAP EDITOR")/2, 80);
        drawCloseButton(g2, w - 60, 20); if (gp.mapFiles == null) return;
        if (playMapBtns == null || playMapBtns.length < gp.mapFiles.size()) { playMapBtns = new Rectangle[gp.mapFiles.size()]; editMapBtns = new Rectangle[gp.mapFiles.size()]; delMapBtns = new Rectangle[gp.mapFiles.size()]; }
        Shape oldClip = g2.getClip(); g2.setClip(0, gp.LIST_VIEW_Y, w, gp.LIST_VIEW_H);
        
        int extraY = 0;
        if (gp.isCreatingMap) {
            extraY = 60;
            int y = gp.LIST_VIEW_Y + 10 - gp.listScrollY;
            int x = w/2 - 300; 
            g2.setColor(new Color(50, 50, 60)); 
            g2.fillRoundRect(x, y, 600, 50, 10, 10);
            g2.setColor(Color.YELLOW); 
            g2.drawRoundRect(x, y, 600, 50, 10, 10);
            g2.setFont(Assets.PIXEL_FONT != null ? Assets.PIXEL_FONT.deriveFont(20f) : new Font("Arial", Font.BOLD, 18)); 
            String txt = gp.currentTypingText.toString() + ((gp.tickCounter / 30) % 2 == 0 ? "|" : "");
            g2.drawString(txt, x + 20, y + 32);
            
            g2.setColor(Color.LIGHT_GRAY);
            g2.setFont(Assets.PIXEL_FONT != null ? Assets.PIXEL_FONT.deriveFont(14f) : new Font("Arial", Font.ITALIC, 12));
            g2.drawString("Press ENTER to create, ESC to cancel", x + 400, y + 32);
        }

        for(int i=0; i<gp.mapFiles.size(); i++) {
            int y = gp.LIST_VIEW_Y + 10 + i * 60 - gp.listScrollY + extraY; 
            int x = w/2 - 300; g2.setColor(new Color(0, 0, 0, 100)); g2.fillRoundRect(x, y, 600, 50, 10, 10);
            g2.setColor(Color.WHITE); g2.setFont(Assets.PIXEL_FONT != null ? Assets.PIXEL_FONT.deriveFont(20f) : new Font("Arial", Font.BOLD, 18)); g2.drawString(gp.mapFiles.get(i).getName(), x + 20, y + 32);
            playMapBtns[i] = new Rectangle(x + 350, y + 5, 70, 40); editMapBtns[i] = new Rectangle(x + 430, y + 5, 70, 40); delMapBtns[i] = new Rectangle(x + 510, y + 5, 70, 40);
            drawSimpleButton(g2, playMapBtns[i], "PLAY", Color.GRAY); drawSimpleButton(g2, editMapBtns[i], "EDIT", Color.GRAY); drawSimpleButton(g2, delMapBtns[i], "DEL", Color.RED);
        }
        g2.setClip(oldClip); drawMenuButton(g2, btnCreateMap, "CREATE NEW MAP", Assets.UI_BTN_BLUE, Assets.UI_BTN_BLUE_PR, true);
        if (gp.showDeleteConfirm) {
            g2.setColor(new Color(0, 0, 0, 200)); g2.fillRect(0, 0, w, h);
            g2.setColor(new Color(40, 40, 50)); g2.fillRoundRect(mx - 200, my - 100, 400, 200, 20, 20);
            g2.setColor(Color.WHITE); g2.setFont(Assets.PIXEL_FONT != null ? Assets.PIXEL_FONT.deriveFont(28f) : new Font("Arial", Font.BOLD, 24));
            String mt = "DELETE THIS MAP?"; g2.drawString(mt, mx - g2.getFontMetrics().stringWidth(mt)/2, my - 20);
            btnConfirmYes.setBounds(mx - 110, my + 30, 100, 50); btnConfirmNo.setBounds(mx + 10, my + 30, 100, 50);
            drawMenuButton(g2, btnConfirmYes, "YES", Assets.UI_BTN_BLUE, Assets.UI_BTN_BLUE_PR, true); 
            drawMenuButton(g2, btnConfirmNo, "NO", Assets.UI_BTN_RED, Assets.UI_BTN_RED_PR, true);
        }
    }

    public void drawLoadingScreen(Graphics2D g2) {
        int w = gp.getWidth(), h = gp.getHeight(); g2.setColor(new Color(15, 15, 20)); g2.fillRect(0, 0, w, h);
        g2.setColor(Color.WHITE); g2.setFont(Assets.PIXEL_FONT != null ? Assets.PIXEL_FONT.deriveFont(54f) : new Font("Arial", Font.BOLD, 50)); g2.drawString("LOADING...", w/2 - g2.getFontMetrics().stringWidth("LOADING...")/2, h/2 - 120);
        g2.setColor(new Color(40, 40, 45)); g2.fillRoundRect(w/2 - 250, h/2 - 60, 500, 25, 10, 10); g2.setColor(new Color(0, 180, 255)); g2.fillRoundRect(w/2 - 250, h/2 - 60, (int)(gp.loadingProgress * 5), 25, 10, 10);
        
        if (gp.loadingPlayer != null) {
            int px = (int)gp.loadingPlayer.visualX, py = (int)gp.loadingPlayer.visualY; 
            IconRenderer.drawKnight(g2, px, py, gp.TILE_SIZE, gp.loadingPlayer);
        }
        if (gp.loadingEnemy != null) {
            int ex = (int)gp.loadingEnemy.visualX, ey = (int)gp.loadingEnemy.visualY; 
            Graphics2D ge = (Graphics2D) g2.create(); if (gp.loadingEnemy.getHealth() <= 0) ge.rotate(Math.toRadians(90), ex + 30, ey + 30);
            IconRenderer.renderLoadingEnemy(ge, gp.loadingEnemy, ex, ey, 60, gp.loadingEnemy.walkAnim); ge.dispose();
        }
        if (gp.loadingTip != null) { g2.setColor(new Color(180, 180, 190)); g2.setFont(Assets.PIXEL_FONT != null ? Assets.PIXEL_FONT.deriveFont(20f) : new Font("Arial", Font.ITALIC, 18)); g2.drawString(gp.loadingTip, w/2 - g2.getFontMetrics().stringWidth(gp.loadingTip)/2, h - 100); }
    }

    public void drawEditorInterface(Graphics2D g2) {
        if (gp.editorMap == null) return; AffineTransform old = g2.getTransform(); g2.scale(gp.editorScale, gp.editorScale); g2.translate(-gp.editorCamX, -gp.editorCamY);
        int rows = gp.editorMap.rows, cols = gp.editorMap.cols;
        for (int r = -10; r < rows + 10; r++) {
            for (int c = -10; c < cols + 10; c++) {
                int px = c * gp.TILE_SIZE, py = r * gp.TILE_SIZE;
                if (c >= 0 && c < cols && r >= 0 && r < rows) {
                    g2.setColor((r + c) % 2 == 0 ? Assets.GRASS_1 : Assets.GRASS_2); g2.fillRect(px, py, gp.TILE_SIZE, gp.TILE_SIZE);
                    Object e = gp.editorMap.grid[r][c];
                    if ("WATER".equals(e)) IconRenderer.renderWaterTile(g2, px, py, gp.TILE_SIZE, gp.tickCounter);
                    else if (e instanceof Tree) IconRenderer.drawTree(g2, px, py, gp.TILE_SIZE, ((Tree)e).getQuality());
                    else if (e instanceof Rock) IconRenderer.drawRock(g2, px, py, gp.TILE_SIZE, ((Rock)e).getQuality());
                    else if (e instanceof Grain) IconRenderer.renderGrain(g2, px, py, gp.TILE_SIZE, ((Grain)e).getQuality());
                    else if (e instanceof Building) IconRenderer.drawBuilding(g2, (Building)e, px, py, gp.TILE_SIZE);
                    else if (e instanceof Vendor) IconRenderer.drawVendor(g2, px, py, gp.TILE_SIZE);
                    else if (e instanceof Enemy) IconRenderer.renderEnemy(g2, (Enemy)e, px, py, gp.TILE_SIZE);
                    else if (e instanceof WorldMap.Campfire) IconRenderer.drawCampfire(g2, px, py, gp.TILE_SIZE);
                    else if (e instanceof WorldMap.Tent) IconRenderer.drawTent(g2, px, py, gp.TILE_SIZE);
                    else if (e instanceof WorldMap.PlayerStart) {
                        Player dummy = new Player("Start", 0, 0);
                        IconRenderer.drawKnight(g2, px, py, gp.TILE_SIZE, dummy);
                    }
                } else { IconRenderer.renderWaterTile(g2, px, py, gp.TILE_SIZE, gp.tickCounter); }
            }
        }
        g2.setTransform(old); g2.setColor(new Color(0, 0, 0, 180)); g2.fillRect(0, gp.getHeight() - 120, gp.getWidth(), 120);
        if (paletteRects == null) { paletteRects = new Rectangle[gp.editorPaletteObjects.length]; for(int i=0; i<paletteRects.length; i++) paletteRects[i] = new Rectangle(20 + i*68, gp.getHeight() - 95, 60, 60); }
        for(int i=0; i<paletteRects.length; i++) {
            Rectangle r = paletteRects[i]; g2.setColor(gp.editorSelectedTileIndex == i ? new Color(255, 215, 0, 100) : new Color(255, 255, 255, 20)); g2.fillRoundRect(r.x, r.y, r.width, r.height, 10, 10);
            Object obj = gp.editorPaletteObjects[i]; String name = "OBJ";
            if ("ERASER".equals(obj)) { name="ERASER"; g2.setColor(Color.RED); g2.drawLine(r.x+10, r.y+10, r.x+50, r.y+50); g2.drawLine(r.x+50, r.y+10, r.x+10, r.y+50); }
            else if ("WATER".equals(obj)) { name="WATER"; IconRenderer.renderWaterIcon(g2, r.x, r.y, r.width, gp.tickCounter); }
            else if (obj instanceof WorldMap.PlayerStart) { name="SPAWN"; IconRenderer.drawSpawnIcon(g2, r.x, r.y, r.width); }
            else if (obj instanceof Tree) { name="TREE"; IconRenderer.drawTree(g2, r.x, r.y, r.width, Enums.Quality.COMMON); }
            else if (obj instanceof Rock) { name="ROCK"; IconRenderer.drawRock(g2, r.x, r.y, r.width, Enums.Quality.COMMON); }
            else if (obj instanceof Grain) { name="GRAIN"; IconRenderer.renderGrain(g2, r.x, r.y, r.width, Enums.Quality.COMMON); }
            else if (obj instanceof Building) { name = ((Building)obj).type.name(); IconRenderer.drawBuilding(g2, (Building)obj, r.x, r.y, r.width); }
            else if (obj instanceof Vendor) { name="VENDOR"; IconRenderer.drawVendor(g2, r.x, r.y, r.width); }
            else if (obj instanceof Enemy) { name = ((Enemy)obj).type.name(); IconRenderer.renderEnemy(g2, (Enemy)obj, r.x, r.y, r.width); }
            else if (obj instanceof WorldMap.Campfire) { name="FIRE"; IconRenderer.drawCampfire(g2, r.x, r.y, r.width); }
            else if (obj instanceof WorldMap.Tent) { name="TENT"; IconRenderer.drawTent(g2, r.x, r.y, r.width); }
            g2.setColor(Color.WHITE); g2.setFont(Assets.PIXEL_FONT != null ? Assets.PIXEL_FONT.deriveFont(14f) : new Font("Arial", Font.BOLD, 12));
            g2.drawString(name, r.x + r.width/2 - g2.getFontMetrics().stringWidth(name)/2, r.y + r.height + 18);
        }
        if (btnSaveMap != null) drawMenuButton(g2, btnSaveMap, "SAVE", Assets.UI_BTN_BLUE, Assets.UI_BTN_BLUE_PR, true); drawCloseButton(g2, gp.getWidth() - 60, 20);
        
        if (gp.showExitEditorConfirm) {
            int mx = gp.getWidth()/2, my = gp.getHeight()/2;
            g2.setColor(new Color(0, 0, 0, 200)); g2.fillRect(0, 0, gp.getWidth(), gp.getHeight());
            g2.setColor(new Color(40, 40, 50)); g2.fillRoundRect(mx - 250, my - 150, 500, 300, 20, 20);
            g2.setColor(Color.WHITE); g2.drawRoundRect(mx - 250, my - 150, 500, 300, 20, 20);
            g2.setFont(Assets.PIXEL_FONT != null ? Assets.PIXEL_FONT.deriveFont(28f) : new Font("Arial", Font.BOLD, 24));
            String msg = "Unsaved Changes! Exit?";
            g2.drawString(msg, mx - g2.getFontMetrics().stringWidth(msg)/2, my - 80);
            
            btnConfirmYes.setBounds(mx - 220, my + 20, 100, 50);
            btnConfirmNo.setBounds(mx - 100, my + 20, 100, 50);
            btnSaveAndExit.setBounds(mx + 20, my + 20, 200, 50);
            
            drawMenuButton(g2, btnConfirmYes, "YES", Assets.UI_BTN_RED, Assets.UI_BTN_RED_PR, true);
            drawMenuButton(g2, btnConfirmNo, "NO", Assets.UI_BTN_BLUE, Assets.UI_BTN_BLUE_PR, true);
            drawMenuButton(g2, btnSaveAndExit, "SAVE & EXIT", Assets.UI_BTN_BLUE, Assets.UI_BTN_BLUE_PR, true);
        }
    }

    public void drawItemIcon(Graphics2D g2, Item it, int x, int y, int s) { if(it.specificType == Item.Specific.SWORD) IconRenderer.drawSword(g2, x, y, s); else if(it.specificType == Item.Specific.AXE) IconRenderer.drawAxe(g2, x, y, s); else if(it.specificType == Item.Specific.PICKAXE) IconRenderer.drawPickaxe(g2, x, y, s); else if(it.specificType == Item.Specific.WOOD) IconRenderer.drawWoodIcon(g2, x, y, s); else if(it.specificType == Item.Specific.STONE) IconRenderer.drawStoneIcon(g2, x, y, s); else if(it.specificType == Item.Specific.BREAD) IconRenderer.drawBread(g2, x, y, s); else if(it.specificType == Item.Specific.HEALTH_POTION) IconRenderer.drawPotion(g2, x, y, s); }
    public void drawItemQuantity(Graphics2D g2, Item it, int x, int y, int s) { if(it.quantity > 1) { g2.setColor(Color.WHITE); g2.setFont(Assets.PIXEL_FONT != null ? Assets.PIXEL_FONT.deriveFont(16f) : new Font("Arial", Font.BOLD, 14)); g2.drawString(String.valueOf(it.quantity), x + s - 20, y + s - 5); } }
    public void drawHotbar(Graphics2D g2, Player p) {
        int hbW = 350, hbH = 70, hbX = gp.getWidth()/2 - hbW/2, hbY = gp.getHeight() - hbH - 10;
        g2.setColor(new Color(0,0,0,150)); g2.fillRoundRect(hbX, hbY, hbW, hbH, 10, 10);
        for(int i=0; i<5; i++) {
            hotbarRects[i] = new Rectangle(hbX + 10 + i*68, hbY + 5, 60, 60); Rectangle r = hotbarRects[i];
            if (p.selectedSlot == i) { g2.setColor(new Color(255, 215, 0, 100)); g2.fillRoundRect(r.x, r.y, r.width, r.height, 10, 10); g2.setColor(Color.YELLOW); }
            else { g2.setColor(new Color(0,0,0,50)); g2.fillRoundRect(r.x, r.y, r.width, r.height, 10, 10); g2.setColor(Color.DARK_GRAY); }
            g2.setStroke(new BasicStroke(2)); g2.drawRoundRect(r.x, r.y, r.width, r.height, 10, 10);
            Item it = p.inventory[i];
            if (it != null && it != gp.draggingItem) { drawItemIcon(g2, it, r.x+5, r.y+5, 50); drawItemQuantity(g2, it, r.x+5, r.y+5, 50); }
            g2.setColor(Color.WHITE); g2.setFont(Assets.PIXEL_FONT != null ? Assets.PIXEL_FONT.deriveFont(14f) : new Font("Arial", Font.BOLD, 12));
            g2.drawString(String.valueOf(i+1), r.x+5, r.y+15);
        }
    }
    private boolean isMouseOnEditorUI(int x, int y) { if (gp.showExitEditorConfirm) return true; if (y > gp.getHeight() - 120) return true; Point p = new Point(x, y); return (btnSaveMap != null && btnSaveMap.contains(p)) || (btnCloseWindow != null && btnCloseWindow.contains(p)); }

    public BufferedImage getDesiredCursor() {
        int mx = gp.inputHandler.mouseX, my = gp.inputHandler.mouseY;
        Point p = new Point(mx, my);

        if (gp.currentState == GamePanel.GameState.MENU) {
            if (btnStartGame.contains(p) || btnLoadGame.contains(p) || btnEditor.contains(p) || btnExitMenu.contains(p)) return Assets.CURSOR_POINTER;
            if (btnContinue.contains(p)) return gp.hasSaveFile ? Assets.CURSOR_POINTER : Assets.CURSOR_DISABLED;
        } else if (gp.currentState == GamePanel.GameState.PAUSED) {
            if (btnResume.contains(p) || btnSaveMenu.contains(p) || btnLoadMenu.contains(p) || btnMainMenu.contains(p) || btnExitPause.contains(p)) return Assets.CURSOR_POINTER;
        } else if (gp.currentState == GamePanel.GameState.GAMEOVER) {
            if (btnRestart.contains(p) || btnMainMenu.contains(p)) return Assets.CURSOR_POINTER;
        } else if (gp.currentState == GamePanel.GameState.INVENTORY) {
            if (btnCloseWindow != null && btnCloseWindow.contains(p)) return Assets.CURSOR_POINTER;
            for (Rectangle r : armorRects) if (r != null && r.contains(p)) return Assets.CURSOR_POINTER;
            for (Rectangle r : backpackRects) if (r != null && r.contains(p)) return Assets.CURSOR_POINTER;
            for (Rectangle r : hotbarRects) if (r != null && r.contains(p)) return Assets.CURSOR_POINTER;
        } else if (gp.currentState == GamePanel.GameState.CRAFTING) {
            if (btnCloseWindow != null && btnCloseWindow.contains(p)) return Assets.CURSOR_POINTER;
            for (CraftingButton cb : craftButtons) {
                if (cb.rect != null && cb.rect.contains(p)) {
                    boolean canCraft = gp.player.hasItem(Item.Specific.WOOD, cb.woodCost) && gp.player.hasItem(Item.Specific.STONE, cb.stoneCost) && gp.player.hasItem(Item.Specific.GRAIN, cb.grainCost);
                    return canCraft ? Assets.CURSOR_POINTER : Assets.CURSOR_DISABLED;
                }
            }
        } else if (gp.currentState == GamePanel.GameState.SHOP) {
            if (btnCloseWindow != null && btnCloseWindow.contains(p)) return Assets.CURSOR_POINTER;
            if (shopBuyRects != null) {
                for (int i = 0; i < shopBuyRects.length; i++) {
                    if (shopBuyRects[i] != null && shopBuyRects[i].contains(p)) {
                        Item it = gp.merchantStock.get(i);
                        int cost = it.specificType == Item.Specific.HEALTH_POTION ? 50 : it.value * 5;
                        return gp.player.getGold() >= cost ? Assets.CURSOR_POINTER : Assets.CURSOR_DISABLED;
                    }
                }
            }
            if (shopSellRects != null) {
                for (Rectangle r : shopSellRects) if (r != null && r.contains(p)) return Assets.CURSOR_POINTER;
            }
        } else if (gp.currentState == GamePanel.GameState.MAP) {
            if (btnCloseWindow != null && btnCloseWindow.contains(p)) return Assets.CURSOR_POINTER;
        } else if (gp.currentState == GamePanel.GameState.EDITOR_SELECT) {
            if (gp.isCreatingMap) return Assets.CURSOR_DEFAULT; // Or maybe pointer if over buttons
            if (btnCloseWindow != null && btnCloseWindow.contains(p)) return Assets.CURSOR_POINTER;
            if (btnCreateMap.contains(p)) return Assets.CURSOR_POINTER;
            if (playMapBtns != null) {
                for (int i = 0; i < gp.mapFiles.size(); i++) {
                    if (playMapBtns[i] != null && playMapBtns[i].contains(p)) return Assets.CURSOR_POINTER;
                    if (editMapBtns[i] != null && editMapBtns[i].contains(p)) return Assets.CURSOR_POINTER;
                    if (delMapBtns[i] != null && delMapBtns[i].contains(p)) return Assets.CURSOR_POINTER;
                }
            }
            if (gp.showDeleteConfirm) {
                if (btnConfirmYes.contains(p) || btnConfirmNo.contains(p)) return Assets.CURSOR_POINTER;
            }
        } else if (gp.currentState == GamePanel.GameState.EDITOR_EDIT) {
            if (gp.showExitEditorConfirm) {
                if (btnConfirmYes.contains(p) || btnConfirmNo.contains(p) || btnSaveAndExit.contains(p)) return Assets.CURSOR_POINTER;
                return Assets.CURSOR_DEFAULT;
            }
            if (btnSaveMap.contains(p) || btnCloseWindow.contains(p)) return Assets.CURSOR_POINTER;
            if (paletteRects != null) {
                for (Rectangle r : paletteRects) if (r != null && r.contains(p)) return Assets.CURSOR_POINTER;
            }
        } else if (gp.currentState == GamePanel.GameState.MENU_LOAD || gp.currentState == GamePanel.GameState.PAUSE_SAVE || gp.currentState == GamePanel.GameState.PAUSE_LOAD) {
            if (btnCloseWindow != null && btnCloseWindow.contains(p)) return Assets.CURSOR_POINTER;
            if (gp.showSaveDeleteConfirm || gp.showLoadConfirm) {
                if (btnConfirmYes.contains(p) || btnConfirmNo.contains(p)) return Assets.CURSOR_POINTER;
                return Assets.CURSOR_DEFAULT;
            }
            if (btnSlot1.contains(p) || btnSlot2.contains(p) || btnSlot3.contains(p) || btnBack.contains(p)) return Assets.CURSOR_POINTER;
            if (btnDelete1.contains(p) || btnDelete2.contains(p) || btnDelete3.contains(p)) return Assets.CURSOR_POINTER;
        } else if (gp.currentState == GamePanel.GameState.EXIT_CONFIRM) {
            if (btnExitYes.contains(p) || btnExitNo.contains(p)) return Assets.CURSOR_POINTER;
        } else if (gp.currentState == GamePanel.GameState.EXIT_GAME_CONFIRM) {
            if (btnExitGameConfirmYes.contains(p) || btnExitGameConfirmNo.contains(p)) return Assets.CURSOR_POINTER;
        } else if (gp.currentState == GamePanel.GameState.CHARACTER_SELECT) {
            if (btnCloseWindow.contains(p)) return Assets.CURSOR_POINTER;
            if (charButtons != null) for(Rectangle r : charButtons) if(r != null && r.contains(p)) return Assets.CURSOR_POINTER;
            if (btnRibbon != null && btnRibbon.contains(p)) return Assets.CURSOR_POINTER;
            if (ribbonDropdownRects != null) for(Rectangle r : ribbonDropdownRects) if(r != null && r.contains(p)) return Assets.CURSOR_POINTER;
        } else if (gp.currentState == GamePanel.GameState.PLAYING) {
            if (btnMapOpen.contains(p) || btnCraftingOpen.contains(p) || btnInventoryOpen.contains(p)) return Assets.CURSOR_POINTER;
            for (Rectangle r : hotbarRects) if (r != null && r.contains(p)) return Assets.CURSOR_POINTER;
        }

        return Assets.CURSOR_DEFAULT;
    }

    public static class CraftingButton { public String name, desc; public int woodCost, stoneCost, grainCost; public Item.Specific type; public Rectangle rect; public CraftingButton(String n, String d, int w, int s, int g, Item.Specific t) { name=n; desc=d; woodCost=w; stoneCost=s; grainCost=g; type=t; } }
    public static class ShopButton { public String name, desc; public int cost; public Item item; public Rectangle rect; public ShopButton(String n, String d, int c, Item i) { name=n; desc=d; cost=c; item=i; } }
}