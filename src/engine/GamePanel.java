/*
 * Project: Joc Survival 2D
 * Author:  Tudor Baranga(Tudorrr323)
 * Date:    Jan 23, 2026
 *
 * Copyright (c) 2026 Tudor Baranga(Tudorrr323). All rights reserved.
 * This code is proprietary software. Use is strictly prohibited without permission.
 */
package engine;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;
import java.io.*;
import java.util.Arrays;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.text.SimpleDateFormat;
import java.util.Date;

import entities.*;
import world.WorldMap;
import items.Item;
import ui.*;
import utils.*;

public class GamePanel extends JPanel {
    
    public enum GameState { MENU, PLAYING, CRAFTING, INVENTORY, SHOP, PAUSED, GAMEOVER, MAP, EDITOR_SELECT, EDITOR_EDIT, MENU_LOAD, PAUSE_SAVE, PAUSE_LOAD }
    public GameState currentState = GameState.MENU;

    public WorldMap map;
    public Player player;
    private Timer gameLoop;
    
    public InputHandler inputHandler;
    public UIRenderer uiRenderer;
    
    // SCROLL VARIABLES
    public int listScrollY = 0;
    public final int LIST_VIEW_Y = 150;
    public final int LIST_VIEW_H = 500;
    
    public List<VisualEffect> effects;
    public List<Enemy> activeEnemies;
    
    public final int TILE_SIZE = 60;
    public float camX, camY;
    public int attackAnimFrame = 0;
    public int tickCounter = 0;
    
    public boolean hasSaveFile = false;
    
    // Logic Timers
    public int moveTimer = 0; 
    public final int MOVE_SPEED_DELAY = 6; 
    public int interactionCooldown = 0; 
    
    // EDITOR VARIABLES
    public List<File> mapFiles = new ArrayList<>();
    public WorldMap editorMap;
    public float editorCamX, editorCamY;
    public float editorScale = 1.0f; // ZOOM LEVEL
    public int editorSelectedTileIndex = 0;
    public String currentMapName = "Untitled";
    
    // DELETE CONFIRMATION
    public boolean showDeleteConfirm = false;
    public int mapToDeleteIndex = -1;
    
    // SAVE DELETE CONFIRM
    public boolean showSaveDeleteConfirm = false;
    public int saveToDeleteSlot = -1;
    
    // LOAD CONFIRMATION
    public boolean showLoadConfirm = false;
    public int loadTargetSlot = -1;
    
    // MENU MESSAGES
    public String menuMessage = "";
    public int menuMessageTimer = 0;

    // SHOP VARIABLES
    public List<Item> merchantStock; 
    
    // Dragging
    public Item draggingItem = null;
    public int dragSourceIndex = -1;
    public int dragSourceType = 0; // 0=Backpack, 1=Hotbar, 2=Armor
    public boolean dragFromHotbar = false;
    
    // Editor Palette (Objects for logic)
    public Object[] editorPaletteObjects;

    public GamePanel() {
        this.setFocusable(true);
        this.setBackground(new Color(10, 10, 15));
        
        // Init Sub-Systems
        this.inputHandler = new InputHandler(this);
        this.addKeyListener(inputHandler);
        this.addMouseListener(inputHandler);
        this.addMouseMotionListener(inputHandler);
        this.addMouseWheelListener(inputHandler);
        
        this.uiRenderer = new UIRenderer(this);
        
        effects = new ArrayList<>();
        initEditorPalette();
        refreshMapList();
        
        hasSaveFile = checkSaveExists(1) || checkSaveExists(2) || checkSaveExists(3);
        
        gameLoop = new Timer(16, e -> update());
        gameLoop.start();
    }
    
    public boolean checkSaveExists(int slot) {
        return new File("saves/savegame_" + slot + ".dat").exists();
    }
    
    public void saveGame(int slot) {
        String name = JOptionPane.showInputDialog(this, "Enter a name for this save:", "Save Game", JOptionPane.PLAIN_MESSAGE);
        if (name == null || name.trim().isEmpty()) {
            return;
        }

        try {
            File folder = new File("saves");
            if (!folder.exists()) folder.mkdir();

            // 1. Save Data
            FileOutputStream fos = new FileOutputStream("saves/savegame_" + slot + ".dat");
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(player);
            oos.writeObject(map);
            oos.writeObject(activeEnemies);
            oos.close();
            fos.close();
            
            // 2. Save Metadata (Name + Date)
            PrintWriter pw = new PrintWriter("saves/savegame_" + slot + ".info");
            pw.println(name);
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
            pw.println(sdf.format(new Date()));
            pw.close();
            
            // 3. Capture Screenshot
            captureSaveThumbnail(slot);

            effects.add(new VisualEffect(getWidth()/2, getHeight()/2, "GAME SAVED!", Color.GREEN, 60));
            hasSaveFile = true;
        } catch (Exception e) { e.printStackTrace(); }
    }
    
    private void captureSaveThumbnail(int slot) {
        // Create an image of the current game state (without UI)
        BufferedImage image = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = image.createGraphics();
        
        // Setup graphics
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        // Fill background
        g2.setColor(new Color(10, 10, 15));
        g2.fillRect(0, 0, getWidth(), getHeight());
        
        // Apply camera transform
        g2.translate(-camX, -camY);
        drawWorld(g2); 
        drawPlayer(g2);
        
        g2.dispose();
        
        try {
            File folder = new File("saves");
            if (!folder.exists()) folder.mkdir();
            ImageIO.write(image, "png", new File("saves/savegame_" + slot + ".png"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public void deleteSave(int slot) {
        new File("saves/savegame_" + slot + ".dat").delete();
        new File("saves/savegame_" + slot + ".info").delete();
        new File("saves/savegame_" + slot + ".png").delete();
        
        // Refresh 'hasSaveFile'
        hasSaveFile = checkSaveExists(1) || checkSaveExists(2) || checkSaveExists(3);
    }
    
    public SaveInfo getSaveInfo(int slot) {
        File dat = new File("saves/savegame_" + slot + ".dat");
        if (!dat.exists()) return new SaveInfo(slot, "Empty Slot", "", null, false);
        
        String name = "Unknown Save";
        String date = "";
        BufferedImage thumb = null;
        
        try {
            File info = new File("saves/savegame_" + slot + ".info");
            if (info.exists()) {
                BufferedReader br = new BufferedReader(new FileReader(info));
                name = br.readLine();
                date = br.readLine();
                br.close();
            }
            
            File img = new File("saves/savegame_" + slot + ".png");
            if (img.exists()) {
                thumb = ImageIO.read(img);
            }
        } catch (Exception e) { e.printStackTrace(); }
        
        return new SaveInfo(slot, name, date, thumb, true);
    }

    public void loadGame(int slot) {
        if (!checkSaveExists(slot)) {
            if (currentState == GameState.MENU_LOAD || currentState == GameState.PAUSE_LOAD) {
                menuMessage = "EMPTY SLOT!";
                menuMessageTimer = 60;
            } else {
                effects.add(new VisualEffect(getWidth()/2, getHeight()/2, "EMPTY SLOT!", Color.RED, 40));
            }
            return;
        }

        try {
            FileInputStream fis = new FileInputStream("saves/savegame_" + slot + ".dat");
            ObjectInputStream ois = new ObjectInputStream(fis);
            player = (Player) ois.readObject();
            map = (WorldMap) ois.readObject();
            activeEnemies = (List<Enemy>) ois.readObject();
            ois.close();
            fis.close();
            
            camX = player.visualX - getWidth()/2; 
            camY = player.visualY - getHeight()/2;
            currentState = GameState.PLAYING;
        } catch (Exception e) { e.printStackTrace(); }
    }
    
    private void initEditorPalette() {
        editorPaletteObjects = new Object[] {
            "ERASER", 
            new WorldMap.PlayerStart(),
            "WATER",  
            new Tree(Enums.Quality.COMMON), 
            new Rock(Enums.Quality.COMMON), 
            new Grain(Enums.Quality.COMMON), 
            new Building(Building.Type.FOUNTAIN), 
            new Building(Building.Type.MONUMENT), 
            new Enemy(Enemy.Type.ZOMBIE, 0, 0), 
            new Enemy(Enemy.Type.SKELETON, 0, 0), 
            new Enemy(Enemy.Type.HUNTER, 0, 0), 
            new Vendor(),
            new WorldMap.Campfire(), 
            new WorldMap.Tent() 
        };
    }
    
    public void startNewGame() {
        map = new WorldMap(100, 100); map.clearCenter(); 
        int cx = map.cols / 2; int cy = map.rows / 2;
        player = new Player("Hero", cx, cy);
        
        map.updateExploration(cx, cy, 10);
        effects = new ArrayList<>(); activeEnemies = new ArrayList<>();
        
        for(int r=0; r<map.rows; r++) {
            for(int c=0; c<map.cols; c++) {
                Object o = map.getEntityAt(c, r);
                if (o instanceof Enemy) { 
                	Enemy e = (Enemy)o; 
                	e.setPos(c, r); 
                	activeEnemies.add(e); 
                }
            }
        }
        generateShopStock();
        camX = player.visualX - getWidth()/2; 
        camY = player.visualY - getHeight()/2;
        currentState = GameState.PLAYING;
    }

    private void update() {
        if (menuMessageTimer > 0) {
            menuMessageTimer--;
            if (menuMessageTimer == 0) menuMessage = "";
        }

        if (currentState == GameState.EDITOR_EDIT || currentState == GameState.EDITOR_SELECT) {
            for (int i = 0; i < effects.size(); i++) { if (!effects.get(i).update()) { effects.remove(i); i--; } }
            
            // Editor Camera Movement handled in input/update
            if (currentState == GameState.EDITOR_EDIT) {
                 // SPRINT MODE logic
                 int speed = inputHandler.keyShift ? 30 : 15;
                 
                 // Keyboard Movement (WASD + Arrows)
                 if (inputHandler.keyW) editorCamY -= speed;
                 if (inputHandler.keyS) editorCamY += speed;
                 if (inputHandler.keyA) editorCamX -= speed;
                 if (inputHandler.keyD) editorCamX += speed;
                 
                 // Mouse Holding on Arrow Buttons
                 if (inputHandler.isMousePressed) {
                     Point p = new Point(inputHandler.mouseX, inputHandler.mouseY);
                     if (uiRenderer.btnCamUp != null && uiRenderer.btnCamUp.contains(p)) editorCamY -= speed;
                     if (uiRenderer.btnCamDown != null && uiRenderer.btnCamDown.contains(p)) editorCamY += speed;
                     if (uiRenderer.btnCamLeft != null && uiRenderer.btnCamLeft.contains(p)) editorCamX -= speed;
                     if (uiRenderer.btnCamRight != null && uiRenderer.btnCamRight.contains(p)) editorCamX += speed;
                 }
            }
            repaint();
            return;
        }
         
        if (currentState != GameState.PLAYING && currentState != GameState.INVENTORY && currentState != GameState.CRAFTING && currentState != GameState.SHOP) { 
        	repaint(); 
        	return; 
        }

        if (player == null) return;
        
        if (interactionCooldown > 0) interactionCooldown--;
        
        tickCounter++; 
        if (attackAnimFrame > 0) attackAnimFrame--;
        
        if(currentState == GameState.PLAYING) updatePlayerMovement();
        
        player.updateVisuals(TILE_SIZE);
        if (player.getHealth() <= 0) { currentState = GameState.GAMEOVER; repaint(); return; }
        
        if (player.levelUpAnim == 59) effects.add(new VisualEffect(player.visualX, player.visualY - 40, "LEVEL UP!", Color.YELLOW, 60));
        
        if (tickCounter % 2 == 0) { 
            int pX = player.x; int pY = player.y;
            for(int i=0; i<activeEnemies.size(); i++) {
                Enemy en = activeEnemies.get(i);
                if (Math.abs(en.x - pX) > 18 || Math.abs(en.y - pY) > 14) continue; 
                boolean attacked = en.updateAI(player, map);
                if (attacked) effects.add(new VisualEffect(player.visualX, player.visualY, "-" + (Math.max(1, en.getAttack() - player.getDefense())), Color.RED, 25));
            }
        }
        updateCamera();
        for (int i = 0; i < effects.size(); i++) { if (!effects.get(i).update()) { effects.remove(i); i--; } }
        repaint();
    }
    private void updatePlayerMovement() {
        if (moveTimer > 0) { moveTimer--; return; }
        
        int nx = player.x; 
        int ny = player.y; 
        boolean tryingToMove = false;

        if (inputHandler.keyW) { ny--; tryingToMove = true; }
        else if (inputHandler.keyS) { ny++; tryingToMove = true; }
        else if (inputHandler.keyA) { nx--; tryingToMove = true; }
        else if (inputHandler.keyD) { nx++; tryingToMove = true; }

        if (tryingToMove) {
            if(nx >= 0 && nx < map.cols && ny >= 0 && ny < map.rows) {
                Object t = map.getEntityAt(nx, ny);
                
                if("WATER".equals(t)) { /* Blocat */ } 
                else if(t == null || t instanceof WorldMap.Tent) { 
                    player.x = nx; player.y = ny; 
                    moveTimer = MOVE_SPEED_DELAY; 
                    map.updateExploration(nx, ny, 8);
                } 
                else { interactWith(t, nx, ny); moveTimer = 15; }
            }
        }
    }

    private void interactWith(Object t, int nx, int ny) {
    	if (interactionCooldown > 0) return;
    	Item item = player.getSelectedItem();
        
        if (t instanceof Vendor) { currentState = GameState.SHOP; }
        else if (t instanceof Building) {
            Building b = (Building) t;
            if (b.isReady()) {
                if (b.type == Building.Type.FOUNTAIN) { player.healFull(); effects.add(new VisualEffect(player.visualX, player.visualY, "HEALED!", Color.GREEN, 30)); b.use(); } 
                else if (b.type == Building.Type.MONUMENT) { player.addPermanentAttack(5); effects.add(new VisualEffect(player.visualX, player.visualY, "+5 DMG!", Color.ORANGE, 30)); b.use(); }
            } else { effects.add(new VisualEffect(player.visualX, player.visualY, "Wait " + b.getSecondsLeft() + "s", Color.YELLOW, 20)); }
            player.x = nx; player.y = ny; moveTimer = 15;
        } 
        else if (t instanceof Enemy) {
            attackAnimFrame = 10; 
            Enemy en = (Enemy)t; 
            player.damage(en); 
            effects.add(new VisualEffect(nx*TILE_SIZE, ny*TILE_SIZE, "HIT", Color.WHITE, 20));
            
            if(!en.isAlive()) { 
            	map.setEntityAt(nx, ny, en.getSavedTile()); 
                activeEnemies.remove(en); 
                player.addXp(50); 
                effects.add(new VisualEffect(nx*TILE_SIZE, ny*TILE_SIZE, "+50 XP", Color.YELLOW, 30)); 
                int goldDrop = 5 + (int)(Math.random() * 11);
                player.addGold(goldDrop);
                effects.add(new VisualEffect(nx*TILE_SIZE, ny*TILE_SIZE - 20, "+" + goldDrop + " G", new Color(255, 215, 0), 40));

                if(en.type == Enemy.Type.HUNTER && Math.random() < 0.35) {
                    Item drop = null; double r = Math.random();
                    if(r < 0.25) drop = new Item("Iron Helm", Item.Type.ARMOR, Item.Specific.HELMET, 2);
                    else if(r < 0.5) drop = new Item("Iron Chest", Item.Type.ARMOR, Item.Specific.CHESTPLATE, 5);
                    else if(r < 0.75) drop = new Item("Iron Legs", Item.Type.ARMOR, Item.Specific.PANTS, 3);
                    else drop = new Item("Iron Boots", Item.Type.ARMOR, Item.Specific.BOOTS, 1);
                    
                    if(player.addItem(drop)) effects.add(new VisualEffect(player.visualX, player.visualY - 40, "LEGENDARY DROP!", new Color(255, 215, 0), 60));
                }
                player.x = nx; player.y = ny; 
            }
        } 
        else if (t instanceof ResourceEntity) {
             if (t instanceof Tree && (item == null || item.specificType != Item.Specific.AXE)) { effects.add(new VisualEffect(player.visualX, player.visualY, "Need AXE!", Color.RED, 20)); return; }
             if (t instanceof Rock && (item == null || item.specificType != Item.Specific.PICKAXE)) { effects.add(new VisualEffect(player.visualX, player.visualY, "Need PICKAXE!", Color.RED, 20)); return; }
             
             ResourceEntity res = (ResourceEntity)t; 
             int collected = player.collectResource(res);
             
             if (collected > res.getQuantity()) {
                 effects.add(new VisualEffect(player.visualX, player.visualY - 40, "LUCKY DROP!", Color.YELLOW, 40));
             }
             if (res.getQuality() == Enums.Quality.EPIC) {
                 effects.add(new VisualEffect(player.visualX, player.visualY - 60, "EPIC DROP!", new Color(148, 0, 211), 50));
             }
             effects.add(new VisualEffect(player.visualX, player.visualY - 20, "+" + collected + " " + res.getResourceType(), Color.WHITE, 30));
             
             map.removeEntityAt(nx, ny); 
             player.x = nx; player.y = ny; attackAnimFrame = 10;
        }
    }

    public void useCurrentItem(int x, int y) {
        Object entityUnderfoot = map.getEntityAt(x, y);
        
        if (entityUnderfoot instanceof Building) {
            Building b = (Building) entityUnderfoot;
            Item buildingItem;
            if (b.type == Building.Type.FOUNTAIN) buildingItem = new Item("Fountain", Item.Type.BUILDING, Item.Specific.FOUNTAIN, 0);
            else buildingItem = new Item("Monument", Item.Type.BUILDING, Item.Specific.MONUMENT, 0);
            buildingItem.savedLastUsedTime = b.lastUsedTime;
            
            if (player.addItem(buildingItem)) { map.removeEntityAt(x, y); effects.add(new VisualEffect(player.visualX, player.visualY, "Picked Up!", Color.GREEN, 30)); }
            else { effects.add(new VisualEffect(player.visualX, player.visualY, "Full!", Color.RED, 30)); }
            return;
        }

        Item item = player.getSelectedItem();
        if(item == null) return;
        
        if(item.type == Item.Type.BUILDING) {
            if(map.getEntityAt(x, y) == null) {
                Building.Type bType = (item.specificType == Item.Specific.FOUNTAIN) ? Building.Type.FOUNTAIN : Building.Type.MONUMENT;
                Building newB = new Building(bType);
                if (item.savedLastUsedTime > 0) newB.lastUsedTime = item.savedLastUsedTime;
                map.setEntityAt(x, y, newB);
                player.removeItem(player.selectedSlot); 
                effects.add(new VisualEffect(x*TILE_SIZE, y*TILE_SIZE, "Built!", Color.GREEN, 30));
            } else { effects.add(new VisualEffect(x*TILE_SIZE, y*TILE_SIZE, "Blocked!", Color.RED, 20)); }
        }
        else if (item.type == Item.Type.CONSUMABLE) {
            if (item.specificType == Item.Specific.BREAD || item.specificType == Item.Specific.HEALTH_POTION) {
                if (player.getHealth() < player.getMaxHealth()) {
                    player.heal(item.value); player.removeItem(player.selectedSlot);
                    effects.add(new VisualEffect(player.visualX, player.visualY, "+" + item.value + " HP", Color.GREEN, 30));
                } else { effects.add(new VisualEffect(player.visualX, player.visualY, "Health Full!", Color.WHITE, 20)); }
            }
        }
    }

    private void updateCamera() {
        float tx = player.visualX - getWidth() / 2 + TILE_SIZE / 2;
        float ty = player.visualY - getHeight() / 2 + TILE_SIZE / 2;
        float mw = map.cols * TILE_SIZE;
        float mh = map.rows * TILE_SIZE;
        if (tx < 0) tx = 0; if (tx > mw - getWidth()) tx = mw - getWidth(); 
        if (ty < 0) ty = 0; if (ty > mh - getHeight()) ty = mh - getHeight();
        camX += (tx - camX) * 0.08f; camY += (ty - camY) * 0.08f;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g); 
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        if (currentState == GameState.MENU) { uiRenderer.drawModernMenu(g2, getWidth(), getHeight()); return; }
        
        if (currentState == GameState.MENU_LOAD || currentState == GameState.PAUSE_SAVE || currentState == GameState.PAUSE_LOAD) {
            uiRenderer.drawSlotSelectionMenu(g2);
            return;
        }
        
        if (currentState == GameState.EDITOR_SELECT) { 
             uiRenderer.drawEditorSelectMenu(g2);
             return; 
        }
        
        if (currentState == GameState.EDITOR_EDIT) {
            uiRenderer.drawEditorInterface(g2);
            return;
        }

        if (map == null || player == null) return;

        g2.translate(-camX, -camY);
        drawWorld(g2); 
        drawPlayer(g2);
        for(VisualEffect ve : effects) ve.draw(g2);
        g2.translate(camX, camY);
        
        if (currentState != GameState.MENU) uiRenderer.drawHUD(g2, player); 

        if (currentState == GameState.CRAFTING) uiRenderer.drawCraftingMenu(g2, player);
        if (currentState == GameState.SHOP) uiRenderer.drawShopMenu(g2, player);
        if (currentState == GameState.INVENTORY) uiRenderer.drawInventoryMenu(g2, player);
        if (currentState == GameState.PAUSED) uiRenderer.drawPauseMenu(g2);
        if (currentState == GameState.GAMEOVER) uiRenderer.drawGameOverScreen(g2);
        if (currentState == GameState.MAP) uiRenderer.drawMapScreen(g2, map, player);
        
        if (draggingItem != null) {
            uiRenderer.drawItemIcon(g2, draggingItem, inputHandler.mouseX - 30, inputHandler.mouseY - 30, 60);
            uiRenderer.drawItemQuantity(g2, draggingItem, inputHandler.mouseX - 30, inputHandler.mouseY - 30, 60);
        }
    }

    private void drawWorld(Graphics2D g2) {
        int startCol = Math.max(0, (int)(camX / TILE_SIZE)); int startRow = Math.max(0, (int)(camY / TILE_SIZE));
        int endCol = Math.min(map.cols, startCol + getWidth()/TILE_SIZE + 2); int endRow = Math.min(map.rows, startRow + getHeight()/TILE_SIZE + 2);
        for (int r = startRow; r < endRow; r++) {
            for (int c = startCol; c < endCol; c++) {
                int px = c * TILE_SIZE; int py = r * TILE_SIZE;
                if ((r+c)%2==0) g2.setColor(Assets.GRASS_1); else g2.setColor(Assets.GRASS_2); g2.fillRect(px, py, TILE_SIZE, TILE_SIZE);
                Object ent = map.getEntityAt(c, r);
                if (ent == null) continue;
                
                if (ent instanceof WorldMap.Campfire) IconRenderer.drawCampfire(g2, px, py, TILE_SIZE);
                else if (ent instanceof WorldMap.Tent) IconRenderer.drawTent(g2, px, py, TILE_SIZE);
                else if ("WATER".equals(ent)) IconRenderer.drawWaterTile(g2, px, py, TILE_SIZE, tickCounter);
                else if (ent instanceof Tree) IconRenderer.drawTree(g2, px, py, TILE_SIZE, ((Tree)ent).getQuality());
                else if (ent instanceof Rock) IconRenderer.drawRock(g2, px, py, TILE_SIZE, ((Rock)ent).getQuality());
                else if (ent instanceof Grain) IconRenderer.drawGrain(g2, px, py, TILE_SIZE, ((Grain)ent).getQuality());
                else if (ent instanceof Building) IconRenderer.drawBuilding(g2, (Building)ent, px, py, TILE_SIZE);
                else if (ent instanceof Vendor) IconRenderer.drawVendor(g2, px, py, TILE_SIZE);
                else if (ent instanceof Enemy) IconRenderer.drawEnemy(g2, (Enemy)ent, px, py, TILE_SIZE);
            }
        }
    }

    private void drawPlayer(Graphics2D g2) {
        if(!player.isAlive()) return;
        int x = (int)player.visualX; int y = (int)player.visualY;
        
        // --- RESTORED COMPLEX SKIN DRAWING ---
        if (player.levelUpAnim > 0) { float alpha = (float)player.levelUpAnim / 60.0f; g2.setColor(new Color(1f, 0.8f, 0f, alpha * 0.5f)); g2.fillOval(x - 10, y - 10, 80, 80); g2.setColor(new Color(1f, 1f, 0.5f, alpha)); g2.setStroke(new BasicStroke(3)); g2.drawOval(x - 10, y - 10, 80, 80); g2.setStroke(new BasicStroke(1)); }
        g2.setColor(new Color(0,0,0,80)); g2.fillOval(x+15, y+50, 30, 8);
        if (player.armor[3] != null) g2.setColor(new Color(120, 120, 130)); else g2.setColor(new Color(40, 40, 40)); g2.fillOval(x+15, y+45, 12, 10); g2.fillOval(x+33, y+45, 12, 10);
        if (player.armor[2] != null) { g2.setColor(new Color(100, 100, 110)); g2.fillRect(x+18, y+35, 10, 15); g2.fillRect(x+32, y+35, 10, 15); }
        g2.setColor(new Color(100, 70, 30)); g2.fillRoundRect(x+12, y+15, 36, 30, 5, 5); g2.setColor(new Color(130, 90, 40)); g2.fillRect(x+15, y+15, 4, 30); g2.fillRect(x+41, y+15, 4, 30);
        if (player.armor[1] != null) { GradientPaint chest = new GradientPaint(x, y, new Color(180, 180, 190), x+30, y+40, new Color(80, 80, 90)); g2.setPaint(chest); g2.fillRoundRect(x+13, y+20, 34, 28, 5, 5); g2.setColor(Color.DARK_GRAY); g2.drawRoundRect(x+13, y+20, 34, 28, 5, 5); } else { GradientPaint base = new GradientPaint(x, y, new Color(200, 200, 210), x+30, y+40, new Color(100, 100, 120)); g2.setPaint(base); g2.fillRoundRect(x+15, y+20, 30, 25, 10, 10); }
        g2.setColor(Assets.SKIN); g2.fillOval(x+18, y+5, 24, 24);
        if (player.armor[0] != null) { g2.setColor(Color.GRAY); g2.fillArc(x+16, y+3, 28, 28, 0, 180); g2.fillRect(x+17, y+15, 4, 12); g2.fillRect(x+39, y+15, 4, 12); g2.fillRect(x+28, y+5, 4, 20); } else { g2.setColor(new Color(50, 30, 10)); g2.fillArc(x+18, y+5, 24, 24, 0, 180); }
        g2.setColor(Color.BLACK); g2.fillOval(x+24, y+15, 3, 3); g2.fillOval(x+32, y+15, 3, 3);
        Graphics2D arm = (Graphics2D) g2.create(); arm.rotate(Math.toRadians(attackAnimFrame * -25), x+50, y+30); arm.setColor(Assets.SKIN); arm.fillOval(x+40, y+25, 10, 10);
        Item item = player.getSelectedItem(); if (item != null) uiRenderer.drawItemIcon(arm, item, x+30, y+15, 40); arm.dispose();
        
        // Draw Level
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Arial", Font.BOLD, 14));
        String lv = "Lv." + player.getLevel();
        int lw = g2.getFontMetrics().stringWidth(lv);
        g2.drawString(lv, x + 30 - lw/2, y - 5);
    }
    
    // INPUT DELEGATES
    public void handleKeyPressed(int k) {
        // --- GLOBAL KEYS ---
        if (k == KeyEvent.VK_ESCAPE) {
            if (currentState == GameState.PLAYING) {
                currentState = GameState.PAUSED;
                inputHandler.keyW = inputHandler.keyA = inputHandler.keyS = inputHandler.keyD = false;
            } else if (currentState == GameState.PAUSED) {
                currentState = GameState.PLAYING;
            } else if (currentState == GameState.INVENTORY || currentState == GameState.CRAFTING || currentState == GameState.SHOP || currentState == GameState.MAP) {
                currentState = GameState.PLAYING;
            }
            return;
        }

        if (currentState == GameState.MENU) {
            if (k == KeyEvent.VK_ENTER) startNewGame();
        } else if (currentState == GameState.PLAYING) {
             if (k == KeyEvent.VK_W || k == KeyEvent.VK_UP) inputHandler.keyW = true;
             if (k == KeyEvent.VK_S || k == KeyEvent.VK_DOWN) inputHandler.keyS = true;
             if (k == KeyEvent.VK_A || k == KeyEvent.VK_LEFT) inputHandler.keyA = true;
             if (k == KeyEvent.VK_D || k == KeyEvent.VK_RIGHT) inputHandler.keyD = true;
             
             if (k == KeyEvent.VK_M) currentState = GameState.MAP;
             if (k >= KeyEvent.VK_1 && k <= KeyEvent.VK_5) player.selectedSlot = k - KeyEvent.VK_1;
             if (k == KeyEvent.VK_SPACE) useCurrentItem(player.x, player.y);
             if (k == KeyEvent.VK_I) currentState = GameState.INVENTORY;
             if (k == KeyEvent.VK_C) currentState = GameState.CRAFTING;
        } else if (currentState == GameState.INVENTORY || currentState == GameState.CRAFTING) {
             if (k == KeyEvent.VK_I || k == KeyEvent.VK_C) currentState = GameState.PLAYING;
        } else if (currentState == GameState.MAP) {
             if (k == KeyEvent.VK_M) currentState = GameState.PLAYING;
        } else if (currentState == GameState.EDITOR_EDIT) {
             // Camera WASD / Arrow Keys logic
             if (k == KeyEvent.VK_W || k == KeyEvent.VK_UP) inputHandler.keyW = true;
             if (k == KeyEvent.VK_S || k == KeyEvent.VK_DOWN) inputHandler.keyS = true;
             if (k == KeyEvent.VK_A || k == KeyEvent.VK_LEFT) inputHandler.keyA = true;
             if (k == KeyEvent.VK_D || k == KeyEvent.VK_RIGHT) inputHandler.keyD = true;
        }
    }
    
    public void handleMousePressed(int x, int y) {
        Point p = new Point(x, y);
        if (currentState == GameState.MENU) {
            if (uiRenderer.btnStartGame.contains(p)) startNewGame();
            if (uiRenderer.btnContinue.contains(p) && hasSaveFile) loadGame(1);
            if (uiRenderer.btnLoadGame.contains(p)) currentState = GameState.MENU_LOAD;
            if (uiRenderer.btnEditor.contains(p)) {
                currentState = GameState.EDITOR_SELECT;
                refreshMapList();
            }
            if (uiRenderer.btnExitMenu.contains(p)) System.exit(0);
        } else if (currentState == GameState.MENU_LOAD) {
            if (showSaveDeleteConfirm) {
                if (uiRenderer.btnConfirmYes.contains(p)) {
                    deleteSave(saveToDeleteSlot);
                    showSaveDeleteConfirm = false;
                } else if (uiRenderer.btnConfirmNo.contains(p)) {
                    showSaveDeleteConfirm = false;
                }
                return;
            }

            if (showLoadConfirm) {
                if (uiRenderer.btnConfirmYes.contains(p)) {
                    loadGame(loadTargetSlot);
                    showLoadConfirm = false;
                } else if (uiRenderer.btnConfirmNo.contains(p)) {
                    showLoadConfirm = false;
                }
                return;
            }

            if (uiRenderer.btnDelete1.contains(p) && checkSaveExists(1)) { showSaveDeleteConfirm = true; saveToDeleteSlot = 1; return; }
            if (uiRenderer.btnDelete2.contains(p) && checkSaveExists(2)) { showSaveDeleteConfirm = true; saveToDeleteSlot = 2; return; }
            if (uiRenderer.btnDelete3.contains(p) && checkSaveExists(3)) { showSaveDeleteConfirm = true; saveToDeleteSlot = 3; return; }
            
            if (uiRenderer.btnSlot1.contains(p)) {
                if (checkSaveExists(1)) { showLoadConfirm = true; loadTargetSlot = 1; }
                else { menuMessage = "EMPTY SLOT!"; menuMessageTimer = 60; }
            }
            if (uiRenderer.btnSlot2.contains(p)) {
                 if (checkSaveExists(2)) { showLoadConfirm = true; loadTargetSlot = 2; }
                 else { menuMessage = "EMPTY SLOT!"; menuMessageTimer = 60; }
            }
            if (uiRenderer.btnSlot3.contains(p)) {
                 if (checkSaveExists(3)) { showLoadConfirm = true; loadTargetSlot = 3; }
                 else { menuMessage = "EMPTY SLOT!"; menuMessageTimer = 60; }
            }
            if (uiRenderer.btnBack.contains(p)) currentState = GameState.MENU;
        } else if (currentState == GameState.PLAYING) {
             for(int i=0; i<5; i++) if(uiRenderer.hotbarRects[i].contains(p)) player.selectedSlot = i;
        } else if (currentState == GameState.CRAFTING) {
            if (uiRenderer.btnCloseWindow.contains(p)) currentState = GameState.PLAYING;
            for(UIRenderer.CraftingButton btn : uiRenderer.craftButtons) {
                if(btn.rect.contains(p)) {
                    if(player.consumeItems(Item.Specific.WOOD, btn.woodCost) && player.consumeItems(Item.Specific.STONE, btn.stoneCost) && player.consumeItems(Item.Specific.GRAIN, btn.grainCost)) {
                        Item newItem = new Item(btn.name, btn.type == Item.Specific.BREAD ? Item.Type.CONSUMABLE : Item.Type.BUILDING, btn.type, btn.type == Item.Specific.BREAD ? 30 : 0);
                        player.addItem(newItem);
                    }
                }
            }
        } else if (currentState == GameState.SHOP) {
            if (uiRenderer.btnCloseWindow.contains(p)) { currentState = GameState.PLAYING; interactionCooldown = 30; inputHandler.keyW=inputHandler.keyA=inputHandler.keyS=inputHandler.keyD=false; return; }
            if(uiRenderer.shopBuyRects != null) {
                for(int i=0; i<merchantStock.size(); i++) {
                    if(uiRenderer.shopBuyRects[i] != null && uiRenderer.shopBuyRects[i].contains(p)) {
                        Item itemToBuy = merchantStock.get(i);
                        int cost = itemToBuy.value * 5; if(itemToBuy.specificType == Item.Specific.HEALTH_POTION) cost = 50;
                        if(player.getGold() >= cost) {
                            Item newItem = new Item(itemToBuy.name, itemToBuy.type, itemToBuy.specificType, itemToBuy.value, itemToBuy.rarityBonus);
                            if(player.addItem(newItem)) player.removeGold(cost);
                        }
                    }
                }
            }
            if(uiRenderer.shopSellRects != null) {
                for(int i=0; i<20; i++) {
                    if(uiRenderer.shopSellRects[i] != null && uiRenderer.shopSellRects[i].contains(p)) {
                        Item it = (i < 15) ? player.backpack[i] : player.inventory[i-15];
                        if (it != null) {
                            int sellVal = it.value * 4;
                            if (it.specificType == Item.Specific.WOOD || it.specificType == Item.Specific.GRAIN) sellVal = 2;
                            if (it.specificType == Item.Specific.STONE) sellVal = 3;
                            if (it.specificType == Item.Specific.BREAD) sellVal = 5;
                            if (it.specificType == Item.Specific.HEALTH_POTION) sellVal = 15;
                            player.addGold(sellVal);
                            if (it.quantity > 1) it.quantity--; else { if (i < 15) player.backpack[i] = null; else player.inventory[i-15] = null; }
                        }
                    }
                }
            }
        } else if (currentState == GameState.EDITOR_SELECT) {
            if (showDeleteConfirm) {
                if (uiRenderer.btnConfirmYes.contains(p)) {
                    if (mapToDeleteIndex >= 0 && mapToDeleteIndex < mapFiles.size()) {
                        mapFiles.get(mapToDeleteIndex).delete();
                        refreshMapList();
                    }
                    showDeleteConfirm = false; mapToDeleteIndex = -1;
                } else if (uiRenderer.btnConfirmNo.contains(p)) { showDeleteConfirm = false; mapToDeleteIndex = -1; }
                return;
            }
            
            if (uiRenderer.btnCloseWindow.contains(p)) currentState = GameState.MENU;
            if (uiRenderer.btnCreateMap.contains(p)) {
                String name = JOptionPane.showInputDialog("Map Name:");
                if(name != null && !name.trim().isEmpty()) {
                    currentMapName = name;
                    editorMap = new WorldMap(100, 100);
                    for(int r=0; r<100; r++) for(int c=0; c<100; c++) editorMap.grid[r][c] = null;
                    currentState = GameState.EDITOR_EDIT;
                }
            }
            // LIST LOGIC
            if (mapFiles != null && uiRenderer.playMapBtns != null) {
                for(int i=0; i<mapFiles.size(); i++) {
                    if (uiRenderer.playMapBtns[i] != null && uiRenderer.playMapBtns[i].contains(p)) loadMapAndPlay(mapFiles.get(i));
                    if (uiRenderer.editMapBtns[i] != null && uiRenderer.editMapBtns[i].contains(p)) loadMapForEditing(mapFiles.get(i));
                    if (uiRenderer.delMapBtns[i] != null && uiRenderer.delMapBtns[i].contains(p)) {
                        mapToDeleteIndex = i;
                        showDeleteConfirm = true;
                    }
                }
            }
        } else if (currentState == GameState.EDITOR_EDIT) {
            if (uiRenderer.btnCloseWindow.contains(p)) {
                currentState = GameState.EDITOR_SELECT;
                refreshMapList();
                return;
            }
            if (uiRenderer.btnSaveMap.contains(p)) {
                saveMap(currentMapName);
                effects.add(new VisualEffect(getWidth()/2 - 150, getHeight()/2, "ai salvat harta " + currentMapName, Color.GREEN, 40));
                return;
            }
            
            // Palette Selection
            if (uiRenderer.paletteRects != null) {
                for(int i=0; i<uiRenderer.paletteRects.length; i++) {
                    if(uiRenderer.paletteRects[i].contains(p)) {
                        editorSelectedTileIndex = i;
                        return;
                    }
                }
            }
            
            // Place on Map
            // LOGIC FOR ZOOM PLACEMENT
            // WorldX = (ScreenX - TranslationX) / Scale
            int worldX = (int)((x + editorCamX) / editorScale);
            int worldY = (int)((y + editorCamY) / editorScale);
            
            int c = worldX / TILE_SIZE;
            int r = worldY / TILE_SIZE;
            
            if(c >= 0 && c < editorMap.cols && r >= 0 && r < editorMap.rows) {
                Object objToPlace = editorPaletteObjects[editorSelectedTileIndex];
                
                if ("ERASER".equals(objToPlace)) {
                    editorMap.grid[r][c] = null;
                } else if ("WATER".equals(objToPlace)) {
                    editorMap.grid[r][c] = "WATER";
                } else if (objToPlace instanceof WorldMap.PlayerStart) {
                     for(int yy=0; yy<editorMap.rows; yy++) 
                         for(int xx=0; xx<editorMap.cols; xx++) 
                             if(editorMap.grid[yy][xx] instanceof WorldMap.PlayerStart) editorMap.grid[yy][xx] = null;
                    editorMap.grid[r][c] = new WorldMap.PlayerStart();
                } 
                else if (objToPlace instanceof Tree) editorMap.grid[r][c] = new Tree(Enums.Quality.COMMON);
                else if (objToPlace instanceof Rock) editorMap.grid[r][c] = new Rock(Enums.Quality.COMMON);
                else if (objToPlace instanceof Grain) editorMap.grid[r][c] = new Grain(Enums.Quality.COMMON);
                else if (objToPlace instanceof Building) editorMap.grid[r][c] = new Building(((Building)objToPlace).type);
                else if (objToPlace instanceof Enemy) editorMap.grid[r][c] = new Enemy(((Enemy)objToPlace).type, c, r);
                else if (objToPlace instanceof entities.Vendor) editorMap.grid[r][c] = new Vendor();
                else if (objToPlace instanceof WorldMap.Campfire) editorMap.grid[r][c] = new WorldMap.Campfire();
                else if (objToPlace instanceof WorldMap.Tent) editorMap.grid[r][c] = new WorldMap.Tent();
            }
        } else if (currentState == GameState.GAMEOVER) {
            if (uiRenderer.btnRestart.contains(p)) startNewGame();
            if (uiRenderer.btnMainMenu.contains(p)) currentState = GameState.MENU;
        } else if (currentState == GameState.PAUSED) {
            if (uiRenderer.btnResume.contains(p)) currentState = GameState.PLAYING;
            if (uiRenderer.btnSaveMenu.contains(p)) currentState = GameState.PAUSE_SAVE;
            if (uiRenderer.btnLoadMenu.contains(p)) currentState = GameState.PAUSE_LOAD;
            if (uiRenderer.btnMainMenu.contains(p)) currentState = GameState.MENU;
            if (uiRenderer.btnExitPause.contains(p)) System.exit(0);
        } else if (currentState == GameState.PAUSE_SAVE) {
            if (showSaveDeleteConfirm) {
                if (uiRenderer.btnConfirmYes.contains(p)) {
                    deleteSave(saveToDeleteSlot);
                    showSaveDeleteConfirm = false;
                } else if (uiRenderer.btnConfirmNo.contains(p)) {
                    showSaveDeleteConfirm = false;
                }
                return;
            }
            
            if (uiRenderer.btnDelete1.contains(p) && checkSaveExists(1)) { showSaveDeleteConfirm = true; saveToDeleteSlot = 1; return; }
            if (uiRenderer.btnDelete2.contains(p) && checkSaveExists(2)) { showSaveDeleteConfirm = true; saveToDeleteSlot = 2; return; }
            if (uiRenderer.btnDelete3.contains(p) && checkSaveExists(3)) { showSaveDeleteConfirm = true; saveToDeleteSlot = 3; return; }
            
            if (uiRenderer.btnSlot1.contains(p)) { saveGame(1); currentState = GameState.PAUSED; }
            if (uiRenderer.btnSlot2.contains(p)) { saveGame(2); currentState = GameState.PAUSED; }
            if (uiRenderer.btnSlot3.contains(p)) { saveGame(3); currentState = GameState.PAUSED; }
            if (uiRenderer.btnBack.contains(p)) currentState = GameState.PAUSED;
        } else if (currentState == GameState.PAUSE_LOAD) {
            if (showSaveDeleteConfirm) {
                if (uiRenderer.btnConfirmYes.contains(p)) {
                    deleteSave(saveToDeleteSlot);
                    showSaveDeleteConfirm = false;
                } else if (uiRenderer.btnConfirmNo.contains(p)) {
                    showSaveDeleteConfirm = false;
                }
                return;
            }

            if (showLoadConfirm) {
                if (uiRenderer.btnConfirmYes.contains(p)) {
                    loadGame(loadTargetSlot);
                    showLoadConfirm = false;
                } else if (uiRenderer.btnConfirmNo.contains(p)) {
                    showLoadConfirm = false;
                }
                return;
            }

            if (uiRenderer.btnDelete1.contains(p) && checkSaveExists(1)) { showSaveDeleteConfirm = true; saveToDeleteSlot = 1; return; }
            if (uiRenderer.btnDelete2.contains(p) && checkSaveExists(2)) { showSaveDeleteConfirm = true; saveToDeleteSlot = 2; return; }
            if (uiRenderer.btnDelete3.contains(p) && checkSaveExists(3)) { showSaveDeleteConfirm = true; saveToDeleteSlot = 3; return; }
            
            if (uiRenderer.btnSlot1.contains(p)) {
                 if (checkSaveExists(1)) { showLoadConfirm = true; loadTargetSlot = 1; }
                 else { menuMessage = "EMPTY SLOT!"; menuMessageTimer = 60; }
            }
            if (uiRenderer.btnSlot2.contains(p)) {
                 if (checkSaveExists(2)) { showLoadConfirm = true; loadTargetSlot = 2; }
                 else { menuMessage = "EMPTY SLOT!"; menuMessageTimer = 60; }
            }
            if (uiRenderer.btnSlot3.contains(p)) {
                 if (checkSaveExists(3)) { showLoadConfirm = true; loadTargetSlot = 3; }
                 else { menuMessage = "EMPTY SLOT!"; menuMessageTimer = 60; }
            }
            if (uiRenderer.btnBack.contains(p)) currentState = GameState.PAUSED;
        } else if (currentState == GameState.MAP) {
             if (uiRenderer.btnCloseWindow.contains(p)) currentState = GameState.PLAYING;
        } else if (currentState == GameState.INVENTORY) {
             if (uiRenderer.btnCloseWindow.contains(p)) currentState = GameState.PLAYING;
             for(int i=0; i<4; i++) { if(uiRenderer.armorRects[i].contains(p) && player.armor[i] != null) { draggingItem = player.armor[i]; dragSourceIndex = i; dragSourceType = 2; return; } }
             for(int i=0; i<15; i++) { if(uiRenderer.backpackRects[i].contains(p) && player.backpack[i] != null) { draggingItem = player.backpack[i]; dragSourceIndex = i; dragSourceType = 0; return; } }
             for(int i=0; i<5; i++) { if(uiRenderer.hotbarRects[i].contains(p) && player.inventory[i] != null) { draggingItem = player.inventory[i]; dragSourceIndex = i; dragSourceType = 1; dragFromHotbar=true; return; } }
        }
    }
    
    public void handleMouseReleased(int x, int y) {
        inputHandler.isMousePressed = false;
        if (draggingItem != null && currentState == GameState.INVENTORY) {
            Point p = new Point(x, y);
            boolean placed = false;
            if (draggingItem.type == Item.Type.ARMOR) {
                 for(int i=0; i<4; i++) {
                     if(uiRenderer.armorRects[i].contains(p)) {
                         boolean valid = false;
                         if(i==0 && draggingItem.specificType == Item.Specific.HELMET) valid=true;
                         if(i==1 && draggingItem.specificType == Item.Specific.CHESTPLATE) valid=true;
                         if(i==2 && draggingItem.specificType == Item.Specific.PANTS) valid=true;
                         if(i==3 && draggingItem.specificType == Item.Specific.BOOTS) valid=true;
                         if(valid) { Item temp = player.armor[i]; player.armor[i] = draggingItem; returnItemToSource(temp); placed = true; }
                     }
                 }
            }
            if(!placed) { for(int i=0; i<15; i++) { if(uiRenderer.backpackRects[i].contains(p)) { Item temp = player.backpack[i]; player.backpack[i] = draggingItem; returnItemToSource(temp); placed = true; break; } } }
            if(!placed) { for(int i=0; i<5; i++) { if(uiRenderer.hotbarRects[i].contains(p)) { Item temp = player.inventory[i]; player.inventory[i] = draggingItem; returnItemToSource(temp); placed = true; break; } } }
            if(!placed) returnItemToSource(draggingItem);
        }
        draggingItem = null; dragSourceIndex = -1; dragFromHotbar = false;
    }
    
    private void returnItemToSource(Item item) {
        if(dragSourceType == 0) player.backpack[dragSourceIndex] = item;
        else if(dragSourceType == 1) player.inventory[dragSourceIndex] = item;
        else if(dragSourceType == 2) player.armor[dragSourceIndex] = item;
    }

    public void handleMouseWheel(int rotation) {
        if (currentState == GameState.EDITOR_SELECT && mapFiles != null) {
            int itemsH = mapFiles.size() * 60;
            if (itemsH > LIST_VIEW_H) { 
                listScrollY += rotation * 30; 
                if (listScrollY < 0) listScrollY = 0;
                if (listScrollY > itemsH - LIST_VIEW_H) listScrollY = itemsH - LIST_VIEW_H;
                repaint();
            }
        }
        else if (currentState == GameState.EDITOR_EDIT) {
            // ZOOM LOGIC
            if (rotation < 0) editorScale = Math.min(editorScale + 0.1f, 2.0f);
            else editorScale = Math.max(editorScale - 0.1f, 0.5f);
            repaint();
        }
    }

    public void generateShopStock() {
        merchantStock = new ArrayList<>();
        merchantStock.add(new Item("Health Potion", Item.Type.CONSUMABLE, Item.Specific.HEALTH_POTION, 100));
        List<Item> pool = new ArrayList<>();
        pool.add(new Item("Iron Sword", Item.Type.WEAPON, Item.Specific.SWORD, 70));
        pool.add(new Item("Iron Helm", Item.Type.ARMOR, Item.Specific.HELMET, 5));
        pool.add(new Item("Iron Chest", Item.Type.ARMOR, Item.Specific.CHESTPLATE, 8));
        pool.add(new Item("Iron Pants", Item.Type.ARMOR, Item.Specific.PANTS, 4));
        pool.add(new Item("Iron Boots", Item.Type.ARMOR, Item.Specific.BOOTS, 3));
        pool.add(new Item("Golden Axe", Item.Type.TOOL, Item.Specific.AXE, 15, 1));
        pool.add(new Item("Golden Pick", Item.Type.TOOL, Item.Specific.PICKAXE, 12, 1));
        java.util.Collections.shuffle(pool);
        for(int i=0; i<pool.size(); i++) { if(pool.get(i).type == Item.Type.ARMOR) { merchantStock.add(pool.get(i)); pool.remove(i); break; } }
        for(int i=0; i<pool.size(); i++) { if(pool.get(i).type == Item.Type.TOOL || pool.get(i).type == Item.Type.WEAPON) { merchantStock.add(pool.get(i)); pool.remove(i); break; } }
        int itemsNeeded = 4 + (int)(Math.random() * 2) - merchantStock.size() + 1; 
        for(int i=0; i<itemsNeeded && !pool.isEmpty(); i++) { merchantStock.add(pool.get(0)); pool.remove(0); }
    }
    
    public void refreshMapList() {
        File folder = new File("maps");
        if (!folder.exists()) folder.mkdir();
        File[] files = folder.listFiles((dir, name) -> name.endsWith(".map"));
        if (files != null) mapFiles = new ArrayList<>(Arrays.asList(files)); else mapFiles = new ArrayList<>();
    }
    
    // --- LOAD/SAVE METHODS ---
    private void loadMapAndPlay(File file) {
        try {
            FileInputStream fis = new FileInputStream(file);
            ObjectInputStream ois = new ObjectInputStream(fis);
            WorldMap loadedMap = (WorldMap) ois.readObject();
            ois.close();
            this.map = loadedMap;
            int startX = map.cols / 2; int startY = map.rows / 2;
            for(int r=0; r<map.rows; r++) {
                for(int c=0; c<map.cols; c++) {
                    if (map.grid[r][c] instanceof WorldMap.PlayerStart) {
                        startX = c; startY = r; map.grid[r][c] = null; break;
                    }
                }
            }
            player = new Player("Hero", startX, startY);
            map.updateExploration(startX, startY, 10);
            activeEnemies = new ArrayList<>(); effects = new ArrayList<>(); generateShopStock();
            for(int r=0; r<map.rows; r++) {
                for(int c=0; c<map.cols; c++) {
                    Object o = map.getEntityAt(c, r);
                    if (o instanceof Enemy) { Enemy e = (Enemy)o; e.setPos(c, r); activeEnemies.add(e); }
                }
            }
            camX = player.visualX - getWidth()/2; camY = player.visualY - getHeight()/2;
            currentState = GameState.PLAYING;
        } catch (Exception e) { e.printStackTrace(); }
    }
    
    private void saveMap(String name) {
        try {
            File folder = new File("maps");
            if (!folder.exists()) folder.mkdir();
            FileOutputStream fos = new FileOutputStream("maps/" + name + ".map");
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(editorMap);
            oos.close();
            refreshMapList();
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void loadMapForEditing(File file) {
        try {
            FileInputStream fis = new FileInputStream(file);
            ObjectInputStream ois = new ObjectInputStream(fis);
            editorMap = (WorldMap) ois.readObject();
            ois.close();
            currentMapName = file.getName().replace(".map", "");
            currentState = GameState.EDITOR_EDIT;
            editorCamX = 0; editorCamY = 0;
        } catch (Exception e) { e.printStackTrace(); }
    }
}
