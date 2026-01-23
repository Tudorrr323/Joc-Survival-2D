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
import java.util.ArrayList;
import java.util.List;

import engine.GamePanel;
import entities.Player;
import entities.Building;
import items.Item;
import utils.Assets;
import utils.Enums;
import utils.SaveInfo;
import world.WorldMap;

public class UIRenderer {

    private GamePanel gp;

    // UI Rectangles (Moved from GamePanel)
    public Rectangle btnCraftingOpen, btnInventoryOpen, btnMapOpen, btnCloseWindow;
    public Rectangle btnStartGame, btnContinue, btnLoadGame, btnExitMenu, btnRestart, btnEditor;
    public Rectangle btnResume, btnSaveMenu, btnLoadMenu, btnMainMenu, btnExitPause;
    
    // Slot UI
    public Rectangle btnSlot1, btnSlot2, btnSlot3, btnBack;
    public Rectangle btnDelete1, btnDelete2, btnDelete3;
    
    // Editor UI
    public Rectangle btnCreateMap, btnLoadMap, btnSaveMap;
    public Rectangle btnConfirmYes, btnConfirmNo;
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
        // --- MENU BUTTONS ---
        int menuCenterX = 1024 / 2 - 120; 
        int menuStartY = 300;         
        
        btnStartGame = new Rectangle(menuCenterX, menuStartY, 240, 50);
        btnContinue  = new Rectangle(menuCenterX, menuStartY + 60, 240, 50);
        btnLoadGame  = new Rectangle(menuCenterX, menuStartY + 120, 240, 50);
        btnEditor    = new Rectangle(menuCenterX, menuStartY + 180, 240, 50);
        btnExitMenu  = new Rectangle(menuCenterX, menuStartY + 240, 240, 50);
        
        // Slot Buttons - REDESIGNED
        int slotW = 600;
        int slotH = 100;
        int slotX = 1024 / 2 - slotW / 2;
        
        btnSlot1 = new Rectangle(slotX, 200, slotW, slotH);
        btnDelete1 = new Rectangle(slotX + slotW + 10, 200, 50, slotH);
        
        btnSlot2 = new Rectangle(slotX, 320, slotW, slotH);
        btnDelete2 = new Rectangle(slotX + slotW + 10, 320, 50, slotH);
        
        btnSlot3 = new Rectangle(slotX, 440, slotW, slotH);
        btnDelete3 = new Rectangle(slotX + slotW + 10, 440, 50, slotH);
        
        btnBack  = new Rectangle(menuCenterX, 600, 240, 50);
        
        // Editor Buttons
        btnCreateMap = new Rectangle(menuCenterX, 768 - 100, 240, 60); 
        btnSaveMap   = new Rectangle(750, 20, 140, 40);
        
        // Other UI
        btnRestart = new Rectangle(0, 0, 200, 50);
        btnResume = new Rectangle(0, 0, 250, 50);
        btnSaveMenu = new Rectangle(0, 0, 250, 50);
        btnLoadMenu = new Rectangle(0, 0, 250, 50);
        btnMainMenu = new Rectangle(0, 0, 250, 50);
        btnExitPause = new Rectangle(0, 0, 250, 50);

        // HUD Buttons
        int uiX = 1024 - 140; 
        int uiY = 768;        
        btnMapOpen = new Rectangle(uiX, uiY - 190, 120, 50);      
        btnCraftingOpen = new Rectangle(uiX, uiY - 130, 120, 50); 
        btnInventoryOpen = new Rectangle(uiX, uiY - 70, 120, 50); 
        
        btnCloseWindow = new Rectangle(0, 0, 40, 40);
        
        initCraftingButtons();
    }
    
    private void initCraftingButtons() {
        craftButtons = new ArrayList<>();
        craftButtons.add(new CraftingButton("Fountain", "HP Restore", 5, 3, 0, Item.Specific.FOUNTAIN));
        craftButtons.add(new CraftingButton("Monument", "+5 Dmg", 10, 5, 0, Item.Specific.MONUMENT));
        craftButtons.add(new CraftingButton("Bread", "Heal 30HP", 0, 0, 3, Item.Specific.BREAD));
    }
    
    // --- DRAWING METHODS ---

    public void drawModernMenu(Graphics2D g2, int w, int h) {
        GradientPaint gpPaint = new GradientPaint(0, 0, new Color(20, 20, 30), 0, h, new Color(50, 30, 20));
        g2.setPaint(gpPaint);
        g2.fillRect(0, 0, w, h);
        
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Arial", Font.BOLD, 60));
        String title = "SURVIVAL 2D";
        g2.drawString(title, w/2 - g2.getFontMetrics().stringWidth(title)/2, 200);
        
        drawMenuButton(g2, btnStartGame, "NEW GAME", new Color(0, 150, 0));
        
        if (gp.hasSaveFile) {
            drawMenuButton(g2, btnContinue, "CONTINUE", new Color(0, 150, 200));
        } else {
            // Disabled Look
            g2.setColor(new Color(100, 100, 100, 100));
            g2.fillRoundRect(btnContinue.x, btnContinue.y, btnContinue.width, btnContinue.height, 15, 15);
            g2.setColor(new Color(150, 150, 150, 100));
            g2.setStroke(new BasicStroke(2));
            g2.drawRoundRect(btnContinue.x, btnContinue.y, btnContinue.width, btnContinue.height, 15, 15);
            
            g2.setColor(new Color(200, 200, 200, 100));
            g2.setFont(new Font("Arial", Font.BOLD, 20));
            String text = "CONTINUE";
            int tw = g2.getFontMetrics().stringWidth(text);
            g2.drawString(text, btnContinue.x + btnContinue.width/2 - tw/2, btnContinue.y + btnContinue.height/2 + 7);
        }
        
        drawMenuButton(g2, btnLoadGame, "LOAD GAME", new Color(150, 100, 0));
        drawMenuButton(g2, btnEditor, "MAP EDITOR", new Color(0, 100, 150));
        drawMenuButton(g2, btnExitMenu, "EXIT", new Color(150, 0, 0));
    }
    
    public void drawHUD(Graphics2D g2, Player player) {
        // Hotbar
        drawHotbar(g2, player);
        
        // Status Bars
        g2.setColor(Color.RED); g2.fillRect(20, 20, 200, 20);
        g2.setColor(new Color(0, 100, 0)); g2.fillRect(20, 20, (int)(200 * ((double)player.getHealth()/player.getMaxHealth())), 20);
        g2.setColor(Color.WHITE); g2.drawRect(20, 20, 200, 20);
        g2.setFont(new Font("Arial", Font.BOLD, 12)); g2.drawString("HP: " + player.getHealth() + "/" + player.getMaxHealth(), 30, 35);
    }

    public void drawCraftingMenu(Graphics2D g2, Player player) {
        int w = gp.getWidth(); int h = gp.getHeight();
        g2.setColor(new Color(0, 0, 0, 200)); g2.fillRect(0, 0, w, h);
        
        int boxW = 500; int boxH = 400; int cx = w/2 - boxW/2; int cy = h/2 - boxH/2;
        g2.setColor(Assets.UI_DARK); g2.fillRoundRect(cx, cy, boxW, boxH, 20, 20);
        g2.setColor(Assets.UI_BORDER); g2.setStroke(new BasicStroke(3)); g2.drawRoundRect(cx, cy, boxW, boxH, 20, 20);
        
        g2.setColor(Color.WHITE); g2.setFont(new Font("Arial", Font.BOLD, 30));
        g2.drawString("CRAFTING", cx + 20, cy + 40);
        
        drawCloseButton(g2, cx + boxW - 50, cy + 10);
        
        int startY = cy + 60;
        for(int i=0; i<craftButtons.size(); i++) {
            CraftingButton cb = craftButtons.get(i);
            int by = startY + i * 80;
            cb.rect = new Rectangle(cx + 20, by, boxW - 40, 70);
            
            boolean canAfford = player.hasItem(Item.Specific.WOOD, cb.woodCost) && player.hasItem(Item.Specific.STONE, cb.stoneCost) && player.hasItem(Item.Specific.GRAIN, cb.grainCost);
            
            if (cb.rect.contains(gp.inputHandler.mouseX, gp.inputHandler.mouseY)) g2.setColor(new Color(80, 80, 100)); else g2.setColor(new Color(60, 60, 70));
            g2.fillRoundRect(cb.rect.x, cb.rect.y, cb.rect.width, cb.rect.height, 10, 10);
            if (canAfford) g2.setColor(Color.GREEN); else g2.setColor(Color.RED);
            g2.drawRoundRect(cb.rect.x, cb.rect.y, cb.rect.width, cb.rect.height, 10, 10);
            
            Item dummyItem = new Item(cb.name, cb.type == Item.Specific.BREAD ? Item.Type.CONSUMABLE : Item.Type.BUILDING, cb.type, 0);
            drawItemIcon(g2, dummyItem, cb.rect.x + 10, cb.rect.y + 10, 50);

            g2.setColor(Color.WHITE);
            g2.setFont(new Font("Arial", Font.BOLD, 20)); g2.drawString(cb.name, cb.rect.x + 70, cb.rect.y + 25);
            g2.setFont(new Font("Arial", Font.PLAIN, 14)); g2.drawString(cb.desc, cb.rect.x + 70, cb.rect.y + 45);
            
            // Draw Costs with Icons
            int costX = cb.rect.x + 230; 
            int costY = cb.rect.y + 17; // Centered vertically in 70px height
            g2.setFont(new Font("Arial", Font.BOLD, 18));
            
            if (cb.woodCost > 0) {
                g2.setColor(new Color(0,0,0,100)); g2.fillRoundRect(costX, costY, 36, 36, 5, 5);
                IconRenderer.drawWoodIcon(g2, costX, costY, 36);
                g2.setColor(Color.WHITE); g2.drawString(String.valueOf(cb.woodCost), costX + 42, costY + 25);
                costX += 90;
            }
            if (cb.stoneCost > 0) {
                g2.setColor(new Color(0,0,0,100)); g2.fillRoundRect(costX, costY, 36, 36, 5, 5);
                IconRenderer.drawStoneIcon(g2, costX, costY, 36);
                g2.setColor(Color.WHITE); g2.drawString(String.valueOf(cb.stoneCost), costX + 42, costY + 25);
                costX += 90;
            }
            if (cb.grainCost > 0) {
                g2.setColor(new Color(0,0,0,100)); g2.fillRoundRect(costX, costY, 36, 36, 5, 5);
                IconRenderer.drawGrain(g2, costX, costY, 36, Enums.Quality.COMMON);
                g2.setColor(Color.WHITE); g2.drawString(String.valueOf(cb.grainCost), costX + 42, costY + 25);
            }
        }
    }
    
    public void drawInventoryMenu(Graphics2D g2, Player player) {
        int w = gp.getWidth(); int h = gp.getHeight();
        g2.setColor(new Color(0, 0, 0, 200)); g2.fillRect(0, 0, w, h);
        
        int boxW = 600; int boxH = 500; int cx = w/2 - boxW/2; int cy = h/2 - boxH/2;
        g2.setColor(Assets.UI_DARK); g2.fillRoundRect(cx, cy, boxW, boxH, 20, 20);
        g2.setColor(Assets.UI_BORDER); g2.setStroke(new BasicStroke(3)); g2.drawRoundRect(cx, cy, boxW, boxH, 20, 20);
        
        g2.setColor(Color.WHITE); g2.setFont(new Font("Arial", Font.BOLD, 30));
        g2.drawString("INVENTORY", cx + 20, cy + 40);
        
        drawCloseButton(g2, cx + boxW - 50, cy + 10);
        
        // Armor Slots
        String[] armorLabels = {"HEAD", "BODY", "LEGS", "FEET"};
        for(int i=0; i<4; i++) {
            int ax = cx + 40; int ay = cy + 80 + i * 90;
            armorRects[i] = new Rectangle(ax, ay, 70, 70);
            g2.setColor(new Color(0,0,0,100)); g2.fillRoundRect(ax, ay, 70, 70, 10, 10);
            g2.setColor(Color.GRAY); g2.drawRoundRect(ax, ay, 70, 70, 10, 10);
            
            if (player.armor[i] != null) {
                drawItemIcon(g2, player.armor[i], ax+5, ay+5, 60);
            } else {
                IconRenderer.drawArmorPlaceholder(g2, ax, ay, 70, i);
            }
        }
        
        // Backpack Grid
        int gridX = cx + 200; int gridY = cy + 80;
        g2.setStroke(new BasicStroke(1));
        for(int i=0; i<16; i++) {
            int col = i % 4; int row = i / 4;
            int bx = gridX + col * 85; int by = gridY + row * 85;
            backpackRects[i] = new Rectangle(bx, by, 70, 70);
            
            g2.setColor(new Color(0,0,0,100)); g2.fillRoundRect(bx, by, 70, 70, 10, 10);
            g2.setColor(Color.GRAY); g2.drawRoundRect(bx, by, 70, 70, 10, 10);
            
            if (player.backpack[i] != null && player.backpack[i] != gp.draggingItem) {
                drawItemIcon(g2, player.backpack[i], bx+5, by+5, 60);
                drawItemQuantity(g2, player.backpack[i], bx+5, by+5, 60);
            }
        }
        
        // Stats
        int statY = cy + boxH - 40;
        
        // Attack (Sword) - Left
        IconRenderer.drawSword(g2, cx + 50, statY - 30, 40);
        g2.setColor(Color.WHITE); g2.setFont(new Font("Arial", Font.BOLD, 20));
        g2.drawString(String.valueOf(player.getTotalDamage()), cx + 95, statY);

        // Defense (Chestplate) - Center
        IconRenderer.drawChestplate(g2, cx + 240, statY - 30, 40);
        g2.setColor(Color.WHITE); g2.setFont(new Font("Arial", Font.BOLD, 20));
        g2.drawString(String.valueOf(player.getDefense()), cx + 285, statY);

        IconRenderer.drawGoldCoin(g2, cx + 430, statY - 30, 40);
        g2.setColor(Color.WHITE); g2.setFont(new Font("Arial", Font.BOLD, 20));
        g2.drawString(String.valueOf(player.getGold()), cx + 475, statY);
        
        // Draw Hotbar on top of inventory dim
        drawHotbar(g2, player);
    }
    
    public void drawMenuButton(Graphics2D g2, Rectangle r, String text, Color c) {
        if(r.contains(gp.inputHandler.mouseX, gp.inputHandler.mouseY)) g2.setColor(c.brighter()); else g2.setColor(c);
        g2.fillRoundRect(r.x, r.y, r.width, r.height, 15, 15);
        g2.setColor(Color.WHITE); g2.setStroke(new BasicStroke(2)); g2.drawRoundRect(r.x, r.y, r.width, r.height, 15, 15);
        g2.setFont(new Font("Arial", Font.BOLD, 20));
        int tw = g2.getFontMetrics().stringWidth(text);
        g2.drawString(text, r.x + r.width/2 - tw/2, r.y + r.height/2 + 7);
    }
    
    public void drawSimpleButton(Graphics2D g2, Rectangle r, String text, boolean active) {
        if(r.contains(gp.inputHandler.mouseX, gp.inputHandler.mouseY) || active) g2.setColor(Color.GRAY); else g2.setColor(Color.DARK_GRAY);
        g2.fillRoundRect(r.x, r.y, r.width, r.height, 10, 10);
        g2.setColor(Color.WHITE);
        g2.drawString(text, r.x + 10, r.y + 25);
    }
    
    public void drawCloseButton(Graphics2D g2, int x, int y) {
        btnCloseWindow = new Rectangle(x, y, 40, 40);
        if (btnCloseWindow.contains(gp.inputHandler.mouseX, gp.inputHandler.mouseY)) g2.setColor(Color.RED); else g2.setColor(new Color(150, 0, 0));
        g2.fillRoundRect(x, y, 40, 40, 10, 10);
        g2.setColor(Color.WHITE); g2.setFont(new Font("Arial", Font.BOLD, 20));
        g2.drawString("X", x + 13, y + 27);
    }

    public void drawGameOverScreen(Graphics2D g2) {
        int w = gp.getWidth(); int h = gp.getHeight();
        g2.setColor(new Color(0, 0, 0, 180));
        g2.fillRect(0, 0, w, h);
        
        String msg = "YOU DIED";
        g2.setFont(new Font("Arial", Font.BOLD, 70));
        g2.setColor(Color.RED);
        int tw = g2.getFontMetrics().stringWidth(msg);
        g2.drawString(msg, w/2 - tw/2, h/2 - 50);
        
        int bx = w/2 - 100;
        int by = h/2 + 50;
        
        btnRestart = new Rectangle(bx, by, 200, 50);
        btnMainMenu = new Rectangle(bx, by + 70, 200, 50);
        
        drawMenuButton(g2, btnRestart, "RESTART", new Color(0, 150, 0));
        drawMenuButton(g2, btnMainMenu, "MAIN MENU", new Color(200, 120, 0));
    }

    public void drawPauseMenu(Graphics2D g2) {
        int w = gp.getWidth(); int h = gp.getHeight(); 
        g2.setColor(new Color(0, 0, 0, 150)); g2.fillRect(0, 0, w, h); 
        int boxW = 400; int boxH = 500; 
        int cx = w/2 - boxW/2; int cy = h/2 - boxH/2; 
        g2.setColor(Assets.UI_DARK); g2.fillRoundRect(cx, cy, boxW, boxH, 20, 20); 
        g2.setColor(Assets.UI_BORDER); g2.setStroke(new BasicStroke(3)); g2.drawRoundRect(cx, cy, boxW, boxH, 20, 20); 
        g2.setColor(Color.WHITE); g2.setFont(new Font("Arial", Font.BOLD, 40)); 
        String t = "PAUSED"; g2.drawString(t, w/2 - g2.getFontMetrics().stringWidth(t)/2, cy + 60); 
        
        btnResume.setBounds(w/2 - 125, cy + 100, 250, 50);
        btnSaveMenu.setBounds(w/2 - 125, cy + 170, 250, 50);
        btnLoadMenu.setBounds(w/2 - 125, cy + 240, 250, 50);
        btnMainMenu.setBounds(w/2 - 125, cy + 310, 250, 50);
        btnExitPause.setBounds(w/2 - 125, cy + 380, 250, 50);
        
        drawMenuButton(g2, btnResume, "CONTINUE", new Color(50, 150, 200));
        drawMenuButton(g2, btnSaveMenu, "SAVE GAME", new Color(50, 200, 50));
        drawMenuButton(g2, btnLoadMenu, "LOAD GAME", new Color(200, 150, 0));
        drawMenuButton(g2, btnMainMenu, "MAIN MENU", new Color(200, 100, 50)); 
        drawMenuButton(g2, btnExitPause, "EXIT GAME", new Color(180, 50, 50)); 
    }
    
    public void drawSlotSelectionMenu(Graphics2D g2) {
        int w = gp.getWidth(); int h = gp.getHeight();
        
        // --- DELETE CONFIRMATION ---
        if (gp.showSaveDeleteConfirm) {
            // Background
            g2.setColor(new Color(0, 0, 0, 200)); g2.fillRect(0, 0, w, h);
            drawSaveSlot(g2, 1, btnSlot1, btnDelete1);
            drawSaveSlot(g2, 2, btnSlot2, btnDelete2);
            drawSaveSlot(g2, 3, btnSlot3, btnDelete3);
            
            g2.setColor(new Color(0, 0, 0, 150)); g2.fillRect(0, 0, w, h);
            
            int cx = w / 2; int cy = h / 2;
            int boxW = 400; int boxH = 200;
            g2.setColor(new Color(40, 40, 50)); g2.fillRoundRect(cx - boxW/2, cy - boxH/2, boxW, boxH, 20, 20);
            g2.setColor(Color.WHITE); g2.setStroke(new BasicStroke(2)); g2.drawRoundRect(cx - boxW/2, cy - boxH/2, boxW, boxH, 20, 20);
            
            g2.setFont(new Font("Arial", Font.BOLD, 25));
            String msg = "Delete this save?";
            g2.drawString(msg, cx - g2.getFontMetrics().stringWidth(msg)/2, cy - 20);
            
            if (btnConfirmYes == null) btnConfirmYes = new Rectangle(0,0,0,0);
            if (btnConfirmNo == null) btnConfirmNo = new Rectangle(0,0,0,0);
            
            btnConfirmYes.setBounds(cx - 110, cy + 30, 100, 50);
            btnConfirmNo.setBounds(cx + 10, cy + 30, 100, 50);
            
            drawMenuButton(g2, btnConfirmYes, "DELETE", new Color(200, 0, 0));
            drawMenuButton(g2, btnConfirmNo, "NO", new Color(100, 100, 100));
            return;
        }

        // --- LOAD CONFIRMATION ---
        if (gp.showLoadConfirm) {
            g2.setColor(new Color(0, 0, 0, 200)); g2.fillRect(0, 0, w, h);
            drawSaveSlot(g2, 1, btnSlot1, btnDelete1);
            drawSaveSlot(g2, 2, btnSlot2, btnDelete2);
            drawSaveSlot(g2, 3, btnSlot3, btnDelete3);
            
            g2.setColor(new Color(0, 0, 0, 150)); g2.fillRect(0, 0, w, h);
            
            int cx = w / 2; int cy = h / 2;
            int boxW = 400; int boxH = 200;
            g2.setColor(new Color(40, 40, 50)); g2.fillRoundRect(cx - boxW/2, cy - boxH/2, boxW, boxH, 20, 20);
            g2.setColor(Color.WHITE); g2.setStroke(new BasicStroke(2)); g2.drawRoundRect(cx - boxW/2, cy - boxH/2, boxW, boxH, 20, 20);
            
            g2.setFont(new Font("Arial", Font.BOLD, 25));
            String msg = "Load this game?";
            g2.drawString(msg, cx - g2.getFontMetrics().stringWidth(msg)/2, cy - 20);
            
            if (btnConfirmYes == null) btnConfirmYes = new Rectangle(0,0,0,0);
            if (btnConfirmNo == null) btnConfirmNo = new Rectangle(0,0,0,0);
            
            btnConfirmYes.setBounds(cx - 110, cy + 30, 100, 50);
            btnConfirmNo.setBounds(cx + 10, cy + 30, 100, 50);
            
            drawMenuButton(g2, btnConfirmYes, "YES", new Color(0, 150, 0));
            drawMenuButton(g2, btnConfirmNo, "NO", new Color(150, 0, 0));
            return;
        }

        g2.setColor(new Color(0, 0, 0, 200)); g2.fillRect(0, 0, w, h);
        
        String title = (gp.currentState == GamePanel.GameState.PAUSE_SAVE) ? "SAVE GAME" : "LOAD GAME";
        g2.setColor(Color.WHITE); g2.setFont(new Font("Arial", Font.BOLD, 50));
        g2.drawString(title, w/2 - g2.getFontMetrics().stringWidth(title)/2, 150);
        
        drawSaveSlot(g2, 1, btnSlot1, btnDelete1);
        drawSaveSlot(g2, 2, btnSlot2, btnDelete2);
        drawSaveSlot(g2, 3, btnSlot3, btnDelete3);
        
        drawMenuButton(g2, btnBack, "BACK", Color.GRAY);
        
        // Draw Menu Message (e.g. Empty Slot)
        if (gp.menuMessage != null && !gp.menuMessage.isEmpty()) {
            g2.setColor(Color.RED);
            g2.setFont(new Font("Arial", Font.BOLD, 40));
            String msg = gp.menuMessage;
            g2.drawString(msg, w/2 - g2.getFontMetrics().stringWidth(msg)/2, h/2);
        }
    }
    
    private void drawSaveSlot(Graphics2D g2, int slot, Rectangle btnRect, Rectangle delRect) {
        SaveInfo info = gp.getSaveInfo(slot);
        
        // Hover
        if (btnRect.contains(gp.inputHandler.mouseX, gp.inputHandler.mouseY)) g2.setColor(new Color(60, 60, 70)); 
        else g2.setColor(new Color(40, 40, 50));
        
        g2.fillRoundRect(btnRect.x, btnRect.y, btnRect.width, btnRect.height, 20, 20);
        g2.setColor(Color.GRAY); g2.setStroke(new BasicStroke(2));
        g2.drawRoundRect(btnRect.x, btnRect.y, btnRect.width, btnRect.height, 20, 20);
        
        if (info.exists) {
            // Draw Thumbnail
            if (info.thumbnail != null) {
                g2.drawImage(info.thumbnail, btnRect.x + 10, btnRect.y + 10, 130, 80, null);
                g2.setColor(Color.BLACK); g2.drawRect(btnRect.x + 10, btnRect.y + 10, 130, 80);
            } else {
                g2.setColor(Color.BLACK); g2.fillRect(btnRect.x + 10, btnRect.y + 10, 130, 80);
                g2.setColor(Color.DARK_GRAY); g2.drawString("No Image", btnRect.x + 40, btnRect.y + 50);
            }
            
            // Draw Text
            g2.setColor(Color.WHITE); 
            g2.setFont(new Font("Arial", Font.BOLD, 22));
            
            // Truncate Name
            String displayName = info.name;
            int maxW = 400; // Available width for text
            if (g2.getFontMetrics().stringWidth(displayName) > maxW) {
                while (g2.getFontMetrics().stringWidth(displayName + "...") > maxW && displayName.length() > 0) {
                    displayName = displayName.substring(0, displayName.length() - 1);
                }
                displayName += "...";
            }
            
            g2.drawString(displayName, btnRect.x + 160, btnRect.y + 40);
            
            g2.setColor(Color.LIGHT_GRAY); 
            g2.setFont(new Font("Arial", Font.PLAIN, 16));
            g2.drawString(info.date, btnRect.x + 160, btnRect.y + 70);
            
            // Draw Delete Button
            if (delRect.contains(gp.inputHandler.mouseX, gp.inputHandler.mouseY)) g2.setColor(new Color(200, 0, 0)); 
            else g2.setColor(new Color(150, 0, 0));
            
            g2.fillRoundRect(delRect.x, delRect.y, delRect.width, delRect.height, 10, 10);
            g2.setColor(Color.WHITE);
            g2.setFont(new Font("Arial", Font.BOLD, 20));
            g2.drawString("X", delRect.x + 18, delRect.y + 60); // Centered roughly
            
        } else {
            g2.setColor(Color.GRAY); 
            g2.setFont(new Font("Arial", Font.ITALIC, 24));
            g2.drawString("Empty Slot", btnRect.x + 250, btnRect.y + 60);
        }
    }

    public void drawShopMenu(Graphics2D g2, Player player) {        int w = gp.getWidth(); int h = gp.getHeight();
        g2.setColor(new Color(0,0,0,220)); g2.fillRect(0,0,w,h);
        
        int cx = w/2; int cy = h/2;
        int panelW = 900; int panelH = 550;
        int px = cx - panelW/2; int py = cy - panelH/2;
        
        g2.setColor(new Color(40, 35, 30)); g2.fillRoundRect(px, py, panelW, panelH, 20, 20);
        g2.setColor(new Color(150, 120, 50)); g2.setStroke(new BasicStroke(4)); g2.drawRoundRect(px, py, panelW, panelH, 20, 20);
        
        g2.setColor(Color.WHITE); g2.setFont(new Font("Arial", Font.BOLD, 30)); g2.drawString("TRADING POST", px + 350, py + 40);
        
        // Gold Display
        IconRenderer.drawGoldCoin(g2, px + panelW - 140, py + 40, 40);
        g2.setColor(Color.WHITE); g2.setFont(new Font("Arial", Font.BOLD, 22));
        g2.drawString(String.valueOf(player.getGold()), px + panelW - 95, py + 68);
        
        drawCloseButton(g2, px + panelW - 50, py + 10);

        // --- LEFT COLUMN (MERCHANT - BUY) ---
        g2.setColor(Color.LIGHT_GRAY); g2.setFont(new Font("Arial", Font.BOLD, 18));
        g2.drawString("MERCHANT STOCK (BUY)", px + 80, py + 80);
        
        int startY = py + 100;
        if (shopBuyRects == null || shopBuyRects.length != gp.merchantStock.size()) {
            shopBuyRects = new Rectangle[gp.merchantStock.size()];
        }
        
        int mx = gp.inputHandler.mouseX;
        int my = gp.inputHandler.mouseY;

        for(int i=0; i<gp.merchantStock.size(); i++) {
            Item item = gp.merchantStock.get(i);
            int itemCost = item.value * 5; 
            if (item.specificType == Item.Specific.HEALTH_POTION) itemCost = 50; 
            
            shopBuyRects[i] = new Rectangle(px + 40, startY + i*70, 380, 60);
            Rectangle r = shopBuyRects[i];
            
            if(r.contains(mx, my)) g2.setColor(new Color(70, 60, 50)); else g2.setColor(new Color(50, 45, 40));
            g2.fillRoundRect(r.x, r.y, r.width, r.height, 10, 10);
            g2.setColor(Color.GRAY); g2.drawRoundRect(r.x, r.y, r.width, r.height, 10, 10);
            
            drawItemIcon(g2, item, r.x + 5, r.y + 5, 50);
            
            g2.setColor(Color.WHITE); g2.setFont(new Font("Arial", Font.BOLD, 16)); g2.drawString(item.name, r.x + 65, r.y + 25);
            
            if (player.getGold() >= itemCost) g2.setColor(Color.GREEN); else g2.setColor(Color.RED);
            g2.drawString(itemCost + " G", r.x + 300, r.y + 35);
        }

        // --- RIGHT COLUMN (PLAYER - SELL) ---
        g2.setColor(Color.LIGHT_GRAY); g2.setFont(new Font("Arial", Font.BOLD, 18));
        g2.drawString("YOUR INVENTORY (SELL)", px + 500, py + 80);
        
        if (shopSellRects == null) shopSellRects = new Rectangle[20];
        int gridStartX = px + 480; int gridStartY = py + 100;
        
        for(int i=0; i<20; i++) {
            int col = i % 4; int row = i / 4;
            int slotX = gridStartX + col * 95;
            int slotY = gridStartY + row * 80;
            shopSellRects[i] = new Rectangle(slotX, slotY, 70, 70);
            
            Item it = (i < 15) ? player.backpack[i] : player.inventory[i-15];
            
            if (shopSellRects[i].contains(mx, my)) g2.setColor(new Color(80, 80, 90)); else g2.setColor(new Color(30, 30, 35));
            g2.fillRoundRect(slotX, slotY, 70, 70, 10, 10);
            g2.setColor(Color.GRAY); g2.drawRoundRect(slotX, slotY, 70, 70, 10, 10);
            
            if (it != null) {
                drawItemIcon(g2, it, slotX+5, slotY+5, 60);
                drawItemQuantity(g2, it, slotX+5, slotY+5, 60);
                
                int sellPrice = getItemSellPrice(it);
                g2.setColor(new Color(100, 255, 100)); g2.setFont(new Font("Arial", Font.BOLD, 12));
                String priceTxt = "+" + sellPrice;
                g2.drawString(priceTxt, slotX + 35 - g2.getFontMetrics().stringWidth(priceTxt)/2, slotY + 65);
            }
        }
    }
    
    private int getItemSellPrice(Item item) {
        if (item == null) return 0;
        if (item.specificType == Item.Specific.WOOD) return 2;
        if (item.specificType == Item.Specific.STONE) return 3;
        if (item.specificType == Item.Specific.GRAIN) return 2;
        if (item.specificType == Item.Specific.BREAD) return 5;
        if (item.specificType == Item.Specific.HEALTH_POTION) return 15;
        return item.value * 4;
    }

    public void drawMapScreen(Graphics2D g2, WorldMap map, Player player) {
        int w = gp.getWidth(); int h = gp.getHeight();
        g2.setColor(new Color(0, 0, 0, 200)); 
        g2.fillRect(0, 0, w, h);

        int scale = 6; 
        int mapW = map.cols * scale;
        int mapH = map.rows * scale;
        int startX = w / 2 - mapW / 2;
        int startY = h / 2 - mapH / 2;

        g2.setColor(new Color(30, 30, 35));
        g2.fillRect(startX - 10, startY - 10, mapW + 20, mapH + 20);
        g2.setColor(Color.WHITE);
        g2.setStroke(new BasicStroke(2));
        g2.drawRect(startX - 10, startY - 10, mapW + 20, mapH + 20);
        
        g2.setFont(new Font("Arial", Font.BOLD, 20));
        g2.drawString("WORLD MAP", startX, startY - 20);
        
        drawCloseButton(g2, startX + mapW - 30, startY - 50);

        for (int r = 0; r < map.rows; r++) {
            for (int c = 0; c < map.cols; c++) {
                int dx = startX + c * scale;
                int dy = startY + r * scale;

                if (!map.explored[r][c]) {
                    g2.setColor(Color.BLACK);
                    g2.fillRect(dx, dy, scale, scale);
                } else {
                    Object e = map.grid[r][c];
                    if ((r + c) % 2 == 0) g2.setColor(Assets.GRASS_1); else g2.setColor(Assets.GRASS_2);
                    
                    if ("WATER".equals(e)) g2.setColor(Assets.WATER);
                    else if (e instanceof entities.Tree) g2.setColor(new Color(0, 100, 0));
                    else if (e instanceof entities.Rock) g2.setColor(Color.GRAY);
                    else if (e instanceof Building) g2.setColor(Color.ORANGE);
                    else if (e instanceof entities.Vendor) g2.setColor(Color.MAGENTA);
                    
                    g2.fillRect(dx, dy, scale, scale);
                }
            }
        }

        int px = startX + player.x * scale;
        int py = startY + player.y * scale;
        int headSize = scale + 4; 
        
        // --- PLAYER HEAD ICON ---
        g2.setColor(Assets.SKIN);
        g2.fillOval(px - 2, py - 2, headSize, headSize);
        g2.setColor(new Color(50, 30, 10)); 
        g2.fillArc(px - 2, py - 4, headSize, headSize, 0, 180);
        
        g2.setColor(Color.WHITE);
        g2.setStroke(new BasicStroke(1));
        g2.drawOval(px - 2, py - 2, headSize, headSize);
        
        g2.setFont(new Font("Arial", Font.PLAIN, 12));
        g2.setColor(Color.WHITE);
        g2.drawString("You", px + 10, py + 5);
    }

    public void drawEditorSelectMenu(Graphics2D g2) {
        int w = gp.getWidth();
        int h = gp.getHeight();
        
        // 1. Background
        GradientPaint gpPaint = new GradientPaint(0, 0, new Color(20, 10, 30), 0, h, new Color(40, 20, 60));
        g2.setPaint(gpPaint); 
        g2.fillRect(0, 0, w, h);
        
        // 2. Title
        g2.setColor(Color.WHITE); 
        g2.setFont(new Font("Arial", Font.BOLD, 40));
        String title = "MAP EDITOR";
        g2.drawString(title, w/2 - g2.getFontMetrics().stringWidth(title)/2, 80);

        // 3. Close Button (Top Right)
        drawCloseButton(g2, w - 60, 20);

        // 4. Map List (Scrollable)
        if (gp.mapFiles == null) return;

        if (gp.mapFiles.isEmpty()) {
            String msg = "No maps found.";
            g2.setColor(Color.GRAY); 
            g2.setFont(new Font("Arial", Font.PLAIN, 20));
            g2.drawString(msg, w/2 - g2.getFontMetrics().stringWidth(msg)/2, 200);
        } else {
            // Resize arrays
            if (playMapBtns == null || playMapBtns.length < gp.mapFiles.size()) {
                 playMapBtns = new Rectangle[gp.mapFiles.size()]; 
                 editMapBtns = new Rectangle[gp.mapFiles.size()]; 
                 delMapBtns = new Rectangle[gp.mapFiles.size()];
            }

            // Clipping for Scroll
            Shape originalClip = g2.getClip();
            g2.setClip(0, gp.LIST_VIEW_Y, w, gp.LIST_VIEW_H);

            int startY = gp.LIST_VIEW_Y + 10;

            for(int i=0; i<gp.mapFiles.size(); i++) {
                if(gp.mapFiles.get(i) == null) continue;

                int y = startY + i * 60 - gp.listScrollY;
                int x = w/2 - 300;
                
                // Draw Entry Background
                g2.setColor(new Color(0, 0, 0, 100));
                g2.fillRoundRect(x, y, 600, 50, 10, 10);
                
                // Map Name
                String fileName = gp.mapFiles.get(i).getName();
                String name = fileName.contains(".") ? fileName.substring(0, fileName.lastIndexOf('.')) : fileName;
                
                g2.setColor(Color.WHITE); 
                g2.setFont(new Font("Arial", Font.BOLD, 18));
                g2.drawString(name, x + 20, y + 32);
                
                // Buttons Rects
                playMapBtns[i] = new Rectangle(x + 350, y + 5, 70, 40);
                editMapBtns[i] = new Rectangle(x + 430, y + 5, 70, 40);
                delMapBtns[i]  = new Rectangle(x + 510, y + 5, 70, 40);
                
                // Draw Buttons
                drawSimpleButton(g2, playMapBtns[i], "PLAY", false);
                drawSimpleButton(g2, editMapBtns[i], "EDIT", false);
                
                // Delete Button (Red)
                Rectangle del = delMapBtns[i];
                if(del.contains(gp.inputHandler.mouseX, gp.inputHandler.mouseY)) g2.setColor(new Color(200, 0, 0)); else g2.setColor(new Color(150, 0, 0));
                g2.fillRoundRect(del.x, del.y, 70, 40, 10, 10);
                g2.setColor(Color.WHITE); 
                g2.drawString("DEL", del.x + 20, del.y + 25);
            }
            
            g2.setClip(originalClip);
            
            // Scrollbar
            int totalH = gp.mapFiles.size() * 60;
            if (totalH > gp.LIST_VIEW_H) {
                int sbH = (int)((float)gp.LIST_VIEW_H / totalH * gp.LIST_VIEW_H);
                int sbY = gp.LIST_VIEW_Y + (int)((float)gp.listScrollY / totalH * gp.LIST_VIEW_H);
                g2.setColor(new Color(100, 100, 100));
                g2.fillRoundRect(w/2 + 310, sbY, 8, sbH, 4, 4);
            }
        }
        
        // 5. Create Button (Styled)
        if (btnCreateMap != null) {
            drawMenuButton(g2, btnCreateMap, "CREATE NEW MAP", new Color(0, 150, 200));
        }
        
        // 6. Delete Popup
        if (gp.showDeleteConfirm) {
            g2.setColor(new Color(0, 0, 0, 200)); g2.fillRect(0, 0, w, h);
            int cx = w / 2; int cy = h / 2;
            g2.setColor(new Color(40, 40, 50)); g2.fillRoundRect(cx - 200, cy - 100, 400, 200, 20, 20);
            g2.setColor(Color.WHITE); g2.setStroke(new BasicStroke(2)); g2.drawRoundRect(cx - 200, cy - 100, 400, 200, 20, 20);
            g2.setFont(new Font("Arial", Font.BOLD, 20));
            String msg1 = "Are you sure you want to"; String msg2 = "delete this map?";
            g2.drawString(msg1, cx - g2.getFontMetrics().stringWidth(msg1)/2, cy - 40);
            g2.drawString(msg2, cx - g2.getFontMetrics().stringWidth(msg2)/2, cy - 10);
            
            btnConfirmYes = new Rectangle(cx - 110, cy + 20, 100, 50);
            btnConfirmNo = new Rectangle(cx + 10, cy + 20, 100, 50);
            drawMenuButton(g2, btnConfirmYes, "YES", new Color(0, 150, 0));
            drawMenuButton(g2, btnConfirmNo, "NO", new Color(150, 0, 0));
        }
    }

    public void drawEditorInterface(Graphics2D g2) {
        // A. Draw Map Grid (SCALED)
        if (gp.editorMap == null) return;
        
        AffineTransform oldT = g2.getTransform(); // SAVE STATE
        g2.scale(gp.editorScale, gp.editorScale);
        g2.translate(-gp.editorCamX, -gp.editorCamY);
        
        int startCol = Math.max(0, (int)(gp.editorCamX / gp.TILE_SIZE));
        int startRow = Math.max(0, (int)(gp.editorCamY / gp.TILE_SIZE));
        
        // Adjusted visible range calculation for zoom
        int visibleCols = (int)(gp.getWidth() / (gp.TILE_SIZE * gp.editorScale)) + 2;
        int visibleRows = (int)(gp.getHeight() / (gp.TILE_SIZE * gp.editorScale)) + 2;
        
        int endCol = Math.min(gp.editorMap.cols, startCol + visibleCols);
        int endRow = Math.min(gp.editorMap.rows, startRow + visibleRows);

        for (int r = startRow; r < endRow; r++) {
            for (int c = startCol; c < endCol; c++) {
                int px = c * gp.TILE_SIZE; int py = r * gp.TILE_SIZE;
                g2.setColor(new Color(20, 100, 20)); g2.fillRect(px, py, gp.TILE_SIZE, gp.TILE_SIZE);
                g2.setColor(new Color(0, 0, 0, 50)); g2.drawRect(px, py, gp.TILE_SIZE, gp.TILE_SIZE);
                
                Object ent = gp.editorMap.getEntityAt(c, r);
                if ("WATER".equals(ent)) IconRenderer.drawWaterTile(g2, px, py, gp.TILE_SIZE, gp.tickCounter);
                else if (ent instanceof utils.Enums) { }
                else if (ent instanceof WorldMap.PlayerStart) IconRenderer.drawSpawnIcon(g2, px, py, gp.TILE_SIZE);
                else if (ent instanceof entities.Tree) IconRenderer.drawTree(g2, px, py, gp.TILE_SIZE, Enums.Quality.COMMON);
                else if (ent instanceof entities.Rock) IconRenderer.drawRock(g2, px, py, gp.TILE_SIZE, Enums.Quality.COMMON);
                else if (ent instanceof entities.Grain) IconRenderer.drawGrain(g2, px, py, gp.TILE_SIZE, Enums.Quality.COMMON);
                else if (ent instanceof Building) IconRenderer.drawBuilding(g2, (Building)ent, px, py, gp.TILE_SIZE);
                else if (ent instanceof entities.Enemy) IconRenderer.drawEnemy(g2, (entities.Enemy)ent, px, py, gp.TILE_SIZE);
                else if (ent instanceof WorldMap.Campfire) IconRenderer.drawCampfire(g2, px, py, gp.TILE_SIZE);
                else if (ent instanceof WorldMap.Tent) IconRenderer.drawTent(g2, px, py, gp.TILE_SIZE);
                else if (ent instanceof entities.Vendor) IconRenderer.drawVendor(g2, px, py, gp.TILE_SIZE);
            }
        }
        
        g2.setTransform(oldT); // RESTORE STATE (No scale/translate for HUD)

        // B. HUD (Palette)
        g2.setColor(new Color(0, 0, 0, 180));
        g2.fillRect(0, gp.getHeight() - 120, gp.getWidth(), 120);
        
        // Init Palette Rects if needed
        if (paletteRects == null || paletteRects.length != gp.editorPaletteObjects.length) {
            paletteRects = new Rectangle[gp.editorPaletteObjects.length];
            int startX = 50; int startY = gp.getHeight() - 90;
            for(int i=0; i<gp.editorPaletteObjects.length; i++) paletteRects[i] = new Rectangle(startX + i*65, startY, 55, 55);
        }
        
        for(int i=0; i<paletteRects.length; i++) {
            Rectangle r = paletteRects[i];
            if (i == gp.editorSelectedTileIndex) { g2.setColor(Color.YELLOW); g2.fillRoundRect(r.x-2, r.y-2, r.width+4, r.height+4, 10, 10); } 
            g2.setColor(Color.DARK_GRAY); g2.fillRoundRect(r.x, r.y, r.width, r.height, 5, 5);
            
            Object obj = gp.editorPaletteObjects[i];
            // Draw Icon based on type
            if ("ERASER".equals(obj)) { g2.setColor(Color.RED); g2.setFont(new Font("Arial", Font.BOLD, 30)); g2.drawString("X", r.x+15, r.y+40); }
            else if (obj instanceof WorldMap.PlayerStart) IconRenderer.drawSpawnIcon(g2, r.x, r.y, 50);
            else if ("WATER".equals(obj)) IconRenderer.drawWaterIcon(g2, r.x, r.y, 50, gp.tickCounter);
            else if (obj instanceof entities.Tree) IconRenderer.drawTree(g2, r.x, r.y, 50, Enums.Quality.COMMON);
            else if (obj instanceof entities.Rock) IconRenderer.drawRock(g2, r.x, r.y, 50, Enums.Quality.COMMON);
            else if (obj instanceof entities.Grain) IconRenderer.drawGrain(g2, r.x, r.y, 50, Enums.Quality.COMMON);
            else if (obj instanceof Building) { if (((Building)obj).type == Building.Type.FOUNTAIN) IconRenderer.drawFountain(g2, r.x, r.y, 50); else IconRenderer.drawMonument(g2, r.x, r.y, 50); }
            else if (obj instanceof entities.Enemy) IconRenderer.drawEnemy(g2, (entities.Enemy)obj, r.x, r.y, 50);
            else if (obj instanceof WorldMap.Campfire) IconRenderer.drawCampfire(g2, r.x, r.y, 50);
            else if (obj instanceof WorldMap.Tent) IconRenderer.drawTent(g2, r.x, r.y, 50);
            else if (obj instanceof entities.Vendor) IconRenderer.drawVendor(g2, r.x, r.y, 50);
            
            // Name Tag - FIXED
            String name = "";
            if ("ERASER".equals(obj)) name = "Eraser";
            else if (obj instanceof WorldMap.PlayerStart) name = "Spawn";
            else if ("WATER".equals(obj)) name = "Water";
            else if (obj instanceof entities.Tree) name = "Tree";
            else if (obj instanceof entities.Rock) name = "Rock";
            else if (obj instanceof entities.Grain) name = "Grain";
            else if (obj instanceof Building) name = ((Building)obj).type == Building.Type.FOUNTAIN ? "Fountain" : "Monument";
            else if (obj instanceof entities.Enemy) {
                String rawName = ((entities.Enemy)obj).type.toString();
                name = rawName.substring(0, 1).toUpperCase() + rawName.substring(1).toLowerCase();
            }
            else if (obj instanceof WorldMap.Campfire) name = "Campfire";
            else if (obj instanceof WorldMap.Tent) name = "Tent";
            else if (obj instanceof entities.Vendor) name = "Vendor";
            
            g2.setColor(Color.YELLOW); g2.setFont(new Font("Arial", Font.BOLD, 10));
            int tx = r.x + (r.width - g2.getFontMetrics().stringWidth(name)) / 2;
            g2.drawString(name, tx, r.y - 5);
        }
        
        // Save Button
        if (btnSaveMap != null) drawMenuButton(g2, btnSaveMap, "SAVE", new Color(0, 150, 0));
        
        // Close Button
        drawCloseButton(g2, gp.getWidth() - 60, 20);
        
        // --- DRAW CAMERA ARROWS ---
        int w = gp.getWidth(); int h = gp.getHeight();
        if (btnCamUp == null) {
            btnCamUp = new Rectangle(w/2 - 25, 10, 50, 40);
            btnCamDown = new Rectangle(w/2 - 25, h - 150, 50, 40);
            btnCamLeft = new Rectangle(10, h/2 - 25, 40, 50);
            btnCamRight = new Rectangle(w - 50, h/2 - 25, 40, 50);
        }
        
        g2.setColor(new Color(0,0,0,150));
        g2.fill(btnCamUp); g2.fill(btnCamDown); g2.fill(btnCamLeft); g2.fill(btnCamRight);
        g2.setColor(Color.WHITE);
        g2.fillPolygon(new int[]{btnCamUp.x+10, btnCamUp.x+25, btnCamUp.x+40}, new int[]{btnCamUp.y+30, btnCamUp.y+10, btnCamUp.y+30}, 3);
        g2.fillPolygon(new int[]{btnCamDown.x+10, btnCamDown.x+25, btnCamDown.x+40}, new int[]{btnCamDown.y+10, btnCamDown.y+30, btnCamDown.y+10}, 3);
        g2.fillPolygon(new int[]{btnCamLeft.x+30, btnCamLeft.x+10, btnCamLeft.x+30}, new int[]{btnCamLeft.y+10, btnCamLeft.y+25, btnCamLeft.y+40}, 3);
        g2.fillPolygon(new int[]{btnCamRight.x+10, btnCamRight.x+30, btnCamRight.x+10}, new int[]{btnCamRight.y+10, btnCamRight.y+25, btnCamRight.y+40}, 3);
    }

    public void drawItemIcon(Graphics2D g2, Item item, int x, int y, int size) {
        // Delegate to GamePanel's logic or implement here? 
        // The glow logic was in GamePanel. I'll implement it here using IconRenderer
        boolean isPowerful = (item.type == Item.Type.WEAPON && item.value > 35) || (item.type == Item.Type.ARMOR && item.value > 3);
        if (item.name.contains("Golden") || item.rarityBonus > 0 || item.name.contains("Epic") || isPowerful) {
            long time = System.currentTimeMillis();
            int glowSize = (int)(Math.sin(time * 0.005) * 5); 
            RadialGradientPaint p = new RadialGradientPaint(new Point(x + size/2, y + size/2), size/2 + 10, new float[] { 0.0f, 1.0f }, new Color[] { new Color(255, 215, 0, 150), new Color(255, 215, 0, 0) });
            g2.setPaint(p);
            g2.fillOval(x - 5 - glowSize, y - 5 - glowSize, size + 10 + glowSize*2, size + 10 + glowSize*2);
        }
        
        if(item.specificType == Item.Specific.SWORD) IconRenderer.drawSword(g2, x, y, size);
        else if(item.specificType == Item.Specific.AXE) IconRenderer.drawAxe(g2, x, y, size);
        else if(item.specificType == Item.Specific.PICKAXE) IconRenderer.drawPickaxe(g2, x, y, size);
        else if(item.specificType == Item.Specific.WOOD) IconRenderer.drawWoodIcon(g2, x, y, size);
        else if(item.specificType == Item.Specific.STONE) IconRenderer.drawStoneIcon(g2, x, y, size);
        else if(item.specificType == Item.Specific.GRAIN) IconRenderer.drawGrain(g2, x, y, size, Enums.Quality.COMMON);
        else if(item.specificType == Item.Specific.BREAD) IconRenderer.drawBread(g2, x, y, size);
        else if(item.specificType == Item.Specific.HELMET) IconRenderer.drawHelmet(g2, x, y, size);
        else if(item.specificType == Item.Specific.CHESTPLATE) IconRenderer.drawChestplate(g2, x, y, size);
        else if(item.specificType == Item.Specific.PANTS) IconRenderer.drawPants(g2, x, y, size);
        else if(item.specificType == Item.Specific.BOOTS) IconRenderer.drawBoots(g2, x, y, size);
        else if(item.type == Item.Type.BUILDING) {
            if(item.specificType == Item.Specific.FOUNTAIN) IconRenderer.drawFountain(g2, x, y, size);
            else IconRenderer.drawMonument(g2, x, y, size);
        }
        else if(item.specificType == Item.Specific.HEALTH_POTION) IconRenderer.drawPotion(g2, x, y, size);

        // Cooldown Overlay
        if (item.type == Item.Type.BUILDING && item.savedLastUsedTime > 0) {
            long now = System.currentTimeMillis();
            long duration = (item.specificType == Item.Specific.FOUNTAIN) ? 30000 : 60000;
            long elapsed = now - item.savedLastUsedTime;
            if (elapsed < duration) {
                float percentLeft = 1.0f - ((float)elapsed / duration); 
                int angle = (int)(360 * percentLeft);
                g2.setColor(new Color(0, 0, 0, 180));
                g2.fillArc(x, y, size, size, 90, angle);
                int secondsLeft = (int)((duration - elapsed) / 1000) + 1;
                String secText = String.valueOf(secondsLeft);
                g2.setColor(Color.WHITE); g2.setFont(new Font("Arial", Font.BOLD, 20));
                int sw = g2.getFontMetrics().stringWidth(secText);
                g2.setColor(Color.BLACK); g2.drawString(secText, x + size/2 - sw/2 + 1, y + size/2 + 8 + 1);
                g2.setColor(Color.WHITE); g2.drawString(secText, x + size/2 - sw/2, y + size/2 + 8);
            } else { item.savedLastUsedTime = 0; }
        }
    }
    
    public void drawItemQuantity(Graphics2D g2, Item item, int x, int y, int size) {
        if(item.quantity > 1) {
            g2.setFont(new Font("Arial", Font.BOLD, 14));
            String qty = String.valueOf(item.quantity);
            g2.setColor(Color.BLACK); g2.drawString(qty, x + size - 18, y + size - 5);
            g2.setColor(Color.WHITE); g2.drawString(qty, x + size - 20, y + size - 7);
        }
    }

    public void drawHotbar(Graphics2D g2, Player player) {
        int hbW = 350; int hbH = 70;
        int hbX = gp.getWidth()/2 - hbW/2; int hbY = gp.getHeight() - hbH - 10;
        
        // XP Bar
        int xpY = hbY - 15;
        g2.setColor(Color.BLACK); g2.fillRect(hbX, xpY, hbW, 10);
        int xpWidth = (int)((float)player.getXp() / player.getMaxXp() * hbW);
        g2.setColor(Color.CYAN); g2.fillRect(hbX, xpY, xpWidth, 10);
        g2.setColor(Color.WHITE); g2.setStroke(new BasicStroke(1)); g2.drawRect(hbX, xpY, hbW, 10);
        
        g2.setColor(new Color(0,0,0,150)); g2.fillRoundRect(hbX, hbY, hbW, hbH, 10, 10);
        
        for(int i=0; i<5; i++) {
            hotbarRects[i] = new Rectangle(hbX + 10 + i*68, hbY + 5, 60, 60);
            Rectangle r = hotbarRects[i];
            
            if (player.selectedSlot == i) { g2.setColor(new Color(255, 215, 0, 100)); g2.fillRoundRect(r.x, r.y, r.width, r.height, 10, 10); g2.setColor(Color.YELLOW); } 
            else { g2.setColor(new Color(0,0,0,50)); g2.fillRoundRect(r.x, r.y, r.width, r.height, 10, 10); g2.setColor(Color.DARK_GRAY); }
            g2.setStroke(new BasicStroke(2)); g2.drawRoundRect(r.x, r.y, r.width, r.height, 10, 10);
            
            Item it = player.inventory[i];
            if (it != null && it != gp.draggingItem) {
                drawItemIcon(g2, it, r.x+5, r.y+5, 50);
                drawItemQuantity(g2, it, r.x+5, r.y+5, 50);
            }
            g2.setColor(Color.WHITE); g2.setFont(new Font("Arial", Font.BOLD, 12)); g2.drawString(String.valueOf(i+1), r.x+5, r.y+15);
        }
    }

    // Inner Classes moved here
    public static class CraftingButton { 
        public String name, desc; 
        public int woodCost, stoneCost, grainCost; 
        public Item.Specific type; 
        public Rectangle rect; 
        public CraftingButton(String n, String d, int w, int s, int g, Item.Specific t) { name=n; desc=d; woodCost=w; stoneCost=s; grainCost=g; type=t; } 
    }
    
    public static class ShopButton { 
        public String name, desc; 
        public int cost; 
        public Item item; 
        public Rectangle rect; 
        public ShopButton(String n, String d, int c, Item i) { name=n; desc=d; cost=c; item=i; } 
    }
}
