/*
 * Project: Joc Survival 2D
 * Author:  Tudor Baranga(Tudorrr323)
 * Date:    Jan 23, 2026
 *
 * Copyright (c) 2026 Tudor Baranga(Tudorrr323). All rights reserved.
 * This code is proprietary software. Use is strictly prohibited without permission.
 */
package engine;

import javax.swing.JPanel;
import javax.swing.Timer;
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
    
    public enum GameState { MENU, PLAYING, CRAFTING, INVENTORY, SHOP, PAUSED, GAMEOVER, MAP, EDITOR_SELECT, EDITOR_EDIT, MENU_LOAD, PAUSE_SAVE, PAUSE_LOAD, LOADING, EXIT_CONFIRM, EXIT_GAME_CONFIRM, CHARACTER_SELECT }
    public GameState currentState = GameState.MENU;

    public WorldMap map;
    public Player player;
    private Timer gameLoop;
    
    public InputHandler inputHandler;
    public UIRenderer uiRenderer;
    
    // MENU SIMULATION VARIABLES
    public WorldMap menuMap;
    public Player menuPlayer;
    public List<Enemy> menuEnemies;
    public float menuCamX, menuCamY, menuCamTargetX, menuCamTargetY;
    
    // GAMEPLAY VARIABLES
    public float playerDashX = 0, playerDashY = 0;
    
    // LOADING VARIABLES
    public int loadingProgress = 0;
    public String loadingTip = "";
    public Player loadingPlayer;
    public int loadingScenario = 0;
    public Enemy loadingEnemy;
    public float loadingVy = 0;
    public boolean loadingDead = false;
    private final String[] TIPS = {
        "Tip: Bread heals 30 HP. Use it wisely.",
        "Tip: Monuments give you permanent damage boosts.",
        "Tip: Enemies can drop rare armor pieces.",
        "Tip: You can sell useless items to the Vendor.",
        "Tip: Build Fountains to heal fully.",
        "Tip: Golden Tools last longer and work faster.",
        "Tip: Explore the map to find hidden resources.",
        "Tip: Press 'M' to view the world map."
    };
    
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
    public float editorScale = 1.0f; 
    public int editorSelectedTileIndex = 0;
    public String currentMapName = "Untitled";
    public boolean editorMapModified = false;
    
    public boolean showDeleteConfirm = false;
    public int mapToDeleteIndex = -1;
    public boolean showSaveDeleteConfirm = false;
    public boolean showExitEditorConfirm = false;
    public int saveToDeleteSlot = -1;
    public boolean showLoadConfirm = false;
    public int loadTargetSlot = -1;
    
    public String menuMessage = "";
    public int menuMessageTimer = 0;
    
    // System Message (Bottom Right)
    public String systemMessage = "";
    public int systemMessageTimer = 0;

    public List<Item> merchantStock; 
    
    public File selectedMapFile = null;
    
    // CHARACTER SELECTION VARIABLES
    public int selectedCharIndex = 0; // 0=Knight, 1=Lancer, 2=Archer, 3=Pawn
    public int selectedColorIndex = 0; // 0=Blue, 1=Red, 2=Yellow, 3=Purple, 4=Black
    
    // Dragging
    public Item draggingItem = null;
    public int dragSourceIndex = -1;
    public int dragSourceType = 0; // 0=Backpack, 1=Hotbar, 2=Armor
    
    public Object[] editorPaletteObjects;
    public Rectangle pressedButton = null;

    // Text Input State
    public int editingSaveSlot = -1;
    public boolean isCreatingMap = false;
    public StringBuilder currentTypingText = new StringBuilder();

    public GamePanel() {
        this.setFocusable(true);
        this.setBackground(new Color(10, 10, 15));
        Assets.loadAssets();
        this.inputHandler = new InputHandler(this);
        this.addKeyListener(inputHandler);
        this.addMouseListener(inputHandler);
        this.addMouseMotionListener(inputHandler);
        this.addMouseWheelListener(inputHandler);
        this.addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentResized(java.awt.event.ComponentEvent e) { if(uiRenderer != null) uiRenderer.initUI(); }
        });
        uiRenderer = new UIRenderer(this);
        effects = new ArrayList<>();
        
        // Hide native cursor
        BufferedImage cursorImg = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
        Cursor blankCursor = Toolkit.getDefaultToolkit().createCustomCursor(cursorImg, new Point(0, 0), "blank cursor");
        setCursor(blankCursor);
        
        initEditorPalette(); refreshMapList();
        initMenuSimulation();
        hasSaveFile = checkSaveExists(1) || checkSaveExists(2) || checkSaveExists(3);
        gameLoop = new Timer(16, e -> update());
        gameLoop.start();
    }
    
    private void initMenuSimulation() {
        menuMap = new WorldMap(60, 60); // Smaller map for menu
        // Simple generation: fill with grass, some trees/rocks
        for(int r=0; r<menuMap.rows; r++) {
            for(int c=0; c<menuMap.cols; c++) {
                if (Math.random() < 0.1) menuMap.grid[r][c] = new Tree(Enums.Quality.COMMON);
                else if (Math.random() < 0.05) menuMap.grid[r][c] = new Rock(Enums.Quality.COMMON);
                else if (Math.random() < 0.02) menuMap.grid[r][c] = "WATER";
            }
        }
        
        // Ensure player start is clear
        menuMap.grid[30][30] = null;
        menuPlayer = new Player("Hero", 30, 30);
        menuPlayer.visualX = 30 * TILE_SIZE;
        menuPlayer.visualY = 30 * TILE_SIZE;
        
        menuEnemies = new ArrayList<>();
        for(int i=0; i<15; i++) {
            Enemy.Type t = Enemy.Type.ZOMBIE;
            double rand = Math.random();
            if(rand < 0.3) t = Enemy.Type.SKELETON;
            else if(rand < 0.6) t = Enemy.Type.HUNTER;
            
            int ex, ey;
            // Find valid spawn point
            do {
                ex = (int)(Math.random() * 60);
                ey = (int)(Math.random() * 60);
            } while (menuMap.grid[ey][ex] != null || (ex == 30 && ey == 30));
            
            Enemy en = new Enemy(t, ex, ey);
            en.visualX = ex * TILE_SIZE;
            en.visualY = ey * TILE_SIZE;
            menuEnemies.add(en);
        }
        
        menuCamX = menuPlayer.visualX - getWidth()/2;
        menuCamY = menuPlayer.visualY - getHeight()/2;
        menuCamTargetX = menuCamX;
        menuCamTargetY = menuCamY;
    }

    private void updateMenuSimulation() {
        // Move player randomly
        if (tickCounter % 60 == 0) {
            int dir = (int)(Math.random() * 4);
            int mx = 0, my = 0;
            if(dir == 0) my = -1; else if(dir == 1) my = 1; else if(dir == 2) mx = -1; else if(dir == 3) mx = 1;
            
            int nx = menuPlayer.x + mx;
            int ny = menuPlayer.y + my;
            
            // Check collision with map bounds AND objects (null means grass/empty)
            if(nx >= 0 && nx < menuMap.cols && ny >= 0 && ny < menuMap.rows && menuMap.grid[ny][nx] == null) {
                menuPlayer.x = nx;
                menuPlayer.y = ny;
            }
        }
        menuPlayer.updateVisuals(TILE_SIZE, false);
        
        // Move enemies randomly
        if (tickCounter % 4 == 0) {
            for(Enemy e : menuEnemies) {
                if (Math.random() < 0.05) {
                    int dir = (int)(Math.random() * 4);
                    int mx = 0, my = 0;
                    if(dir == 0) my = -1; else if(dir == 1) my = 1; else if(dir == 2) mx = -1; else if(dir == 3) mx = 1;
                    int nx = e.x + mx; int ny = e.y + my;
                    // Check collision
                    if(nx >= 0 && nx < menuMap.cols && ny >= 0 && ny < menuMap.rows && menuMap.grid[ny][nx] == null) { 
                        e.x = nx; e.y = ny; 
                    }
                }
                e.updateVisuals(TILE_SIZE);
            }
        }
        
        // Update Camera to move randomly
        if (tickCounter % 300 == 0 || Math.abs(menuCamX - menuCamTargetX) < 10 && Math.abs(menuCamY - menuCamTargetY) < 10) {
            menuCamTargetX = (float)(Math.random() * (menuMap.cols * TILE_SIZE - getWidth()));
            menuCamTargetY = (float)(Math.random() * (menuMap.rows * TILE_SIZE - getHeight()));
        }
        
        menuCamX += (menuCamTargetX - menuCamX) * 0.005f; // Very slow cinematic movement
        menuCamY += (menuCamTargetY - menuCamY) * 0.005f;
    }

    public boolean checkSaveExists(int slot) { return new File("saves/savegame_" + slot + ".dat").exists(); }
    
    public void saveGame(int slot, String name) {
        if (name == null || name.trim().isEmpty()) return;
        try {
            File folder = new File("saves"); if (!folder.exists()) folder.mkdir();
            FileOutputStream fos = new FileOutputStream("saves/savegame_" + slot + ".dat"); ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(player); oos.writeObject(map); oos.writeObject(activeEnemies); oos.close(); fos.close();
            PrintWriter pw = new PrintWriter("saves/savegame_" + slot + ".info"); pw.println(name); SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); pw.println(sdf.format(new Date())); pw.close();
            captureSaveThumbnail(slot); menuMessage = "GAME SAVED!"; menuMessageTimer = 60; hasSaveFile = true;
        } catch (Exception e) { e.printStackTrace(); }
    }
    
    private void captureSaveThumbnail(int slot) {
        BufferedImage image = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_RGB); Graphics2D g2 = image.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON); g2.setColor(new Color(10, 10, 15)); g2.fillRect(0, 0, getWidth(), getHeight());
        g2.translate(-camX, -camY); drawWorld(g2); drawPlayer(g2); g2.dispose();
        try { File folder = new File("saves"); if (!folder.exists()) folder.mkdir(); ImageIO.write(image, "png", new File("saves/savegame_" + slot + ".png")); } catch (IOException e) { e.printStackTrace(); }
    }
    
    public void deleteSave(int slot) {
        new File("saves/savegame_" + slot + ".dat").delete(); new File("saves/savegame_" + slot + ".info").delete(); new File("saves/savegame_" + slot + ".png").delete();
        hasSaveFile = checkSaveExists(1) || checkSaveExists(2) || checkSaveExists(3);
    }
    
    public SaveInfo getSaveInfo(int slot) {
        File dat = new File("saves/savegame_" + slot + ".dat"); if (!dat.exists()) return new SaveInfo(slot, "Empty Slot", "", null, false);
        String name = "Unknown Save"; String date = ""; BufferedImage thumb = null;
        try {
            File info = new File("saves/savegame_" + slot + ".info"); if (info.exists()) { BufferedReader br = new BufferedReader(new FileReader(info)); name = br.readLine(); date = br.readLine(); br.close(); }
            File img = new File("saves/savegame_" + slot + ".png"); if (img.exists()) thumb = ImageIO.read(img);
        } catch (Exception e) { e.printStackTrace(); }
        return new SaveInfo(slot, name, date, thumb, true);
    }

    public void loadGame(int slot) {
        if (!checkSaveExists(slot)) { if (currentState == GameState.MENU_LOAD || currentState == GameState.PAUSE_LOAD) { menuMessage = "EMPTY SLOT!"; menuMessageTimer = 60; } else effects.add(new VisualEffect(getWidth()/2, getHeight()/2, "EMPTY SLOT!", Color.RED, 40)); return; }
        startLoading(() -> {
            try { FileInputStream fis = new FileInputStream("saves/savegame_" + slot + ".dat"); ObjectInputStream ois = new ObjectInputStream(fis); player = (Player) ois.readObject(); map = (WorldMap) ois.readObject(); activeEnemies = (List<Enemy>) ois.readObject(); ois.close(); fis.close(); camX = player.visualX - getWidth()/2; camY = player.visualY - getHeight()/2; } catch (Exception e) { e.printStackTrace(); }
        });
    }
    
    private void initEditorPalette() { editorPaletteObjects = new Object[] { "ERASER", new WorldMap.PlayerStart(), "WATER", new Tree(Enums.Quality.COMMON), new Rock(Enums.Quality.COMMON), new Grain(Enums.Quality.COMMON), new Building(Building.Type.FOUNTAIN), new Building(Building.Type.MONUMENT), new Enemy(Enemy.Type.ZOMBIE, 0, 0), new Enemy(Enemy.Type.SKELETON, 0, 0), new Enemy(Enemy.Type.HUNTER, 0, 0), new Vendor(), new WorldMap.Campfire(), new WorldMap.Tent() }; }
    
    public void startNewGame() {
        startLoading(() -> {
            Assets.updatePlayerSprites(selectedCharIndex, selectedColorIndex);
            if (selectedMapFile != null) {
                try {
                    FileInputStream fis = new FileInputStream(selectedMapFile); ObjectInputStream ois = new ObjectInputStream(fis); WorldMap m = (WorldMap) ois.readObject(); ois.close(); map = m;
                    int sx = map.cols/2, sy = map.rows/2; for(int r=0; r<map.rows; r++) for(int c=0; c<map.cols; c++) if (map.grid[r][c] instanceof WorldMap.PlayerStart) { sx=c; sy=r; map.grid[r][c]=null; break; }
                    player = new Player("Hero", sx, sy); map.updateExploration(sx, sy, 10);
                } catch (Exception e) { e.printStackTrace(); }
                selectedMapFile = null;
            } else {
                map = new WorldMap(100, 100); map.clearCenter(); int cx = map.cols / 2; int cy = map.rows / 2; player = new Player("Hero", cx, cy);
                map.updateExploration(cx, cy, 10);
            }
            
            player.inventory[0] = new Item("Iron Sword", Item.Type.WEAPON, Item.Specific.SWORD, 30);
            player.inventory[1] = new Item("Iron Axe", Item.Type.TOOL, Item.Specific.AXE, 10);
            player.inventory[2] = new Item("Iron Pickaxe", Item.Type.TOOL, Item.Specific.PICKAXE, 10);
            
            effects = new ArrayList<>(); activeEnemies = new ArrayList<>();
            for(int r=0; r<map.rows; r++) { for(int c=0; c<map.cols; c++) { Object o = map.getEntityAt(c, r); if (o instanceof Enemy) { Enemy e = (Enemy)o; e.setPos(c, r); activeEnemies.add(e); } } }
            generateShopStock(); camX = player.visualX - getWidth()/2; camY = player.visualY - getHeight()/2;
        });
    }

    private void update() {
        if (currentState == GameState.LOADING) {
            if (loadingPlayer != null) {
                int animSpeed = 10; loadingPlayer.walkAnim++; if(loadingPlayer.walkAnim > animSpeed * 4) loadingPlayer.walkAnim = 0;
                if (loadingScenario == 0) { loadingPlayer.visualX += 3; if (loadingPlayer.visualX > getWidth() + 50) loadingPlayer.visualX = -50; } 
                else {
                    float dist = loadingEnemy.visualX - loadingPlayer.visualX;
                    if (!loadingDead) {
                        if (dist > 60) { loadingPlayer.visualX += 3; loadingEnemy.visualX -= 3; loadingEnemy.walkAnim++; if(loadingEnemy.walkAnim > animSpeed * 4) loadingEnemy.walkAnim = 0; }
                        else { 
                            if (attackAnimFrame == 0) {
                                attackAnimFrame = 20; 
                                loadingPlayer.animState = Player.AnimState.ATTACK_1;
                                loadingPlayer.actionLocked = true;
                                loadingPlayer.animFrame = 0;
                            }
                            if (tickCounter % 30 == 0) { loadingEnemy.takeDamage(50); effects.add(new VisualEffect(loadingEnemy.visualX, loadingEnemy.visualY, "HIT!", Color.WHITE, 30)); if (loadingEnemy.getHealth() <= 0) { loadingDead = true; loadingVy = -12; } } 
                        }
                    } else {
                        float groundY = getHeight() / 2 + 70; if (loadingVy != 0) loadingEnemy.visualX += 6; 
                        loadingEnemy.visualY += loadingVy; loadingVy += 0.8f; if (loadingEnemy.visualY >= groundY) { loadingEnemy.visualY = groundY; loadingVy = 0; }
                        loadingPlayer.visualX += 3;
                    }
                    if (loadingPlayer.visualX > getWidth() + 50) { loadingPlayer.visualX = -50; loadingEnemy.visualX = getWidth() + 50; loadingEnemy.visualY = getHeight() / 2 + 70; loadingEnemy.healFull(); loadingDead = false; loadingVy = 0; }
                }
                loadingPlayer.updateVisuals(TILE_SIZE, false);
            }
            repaint(); return;
        }
        if (menuMessageTimer > 0) { menuMessageTimer--; if (menuMessageTimer == 0) menuMessage = ""; }
        if (systemMessageTimer > 0) { systemMessageTimer--; if (systemMessageTimer == 0) systemMessage = ""; }
        
        if (currentState == GameState.MENU || currentState == GameState.EXIT_CONFIRM || currentState == GameState.MENU_LOAD || currentState == GameState.CHARACTER_SELECT) {
            tickCounter++;
            updateMenuSimulation();
            repaint();
            return;
        }

        if (currentState == GameState.EDITOR_SELECT || currentState == GameState.EDITOR_EDIT) {
            for (int i = 0; i < effects.size(); i++) { if (!effects.get(i).update()) { effects.remove(i); i--; } }
            if (currentState == GameState.EDITOR_EDIT) {
                 int speed = inputHandler.keyShift ? 30 : 15; if (inputHandler.keyW) editorCamY -= speed; if (inputHandler.keyS) editorCamY += speed; if (inputHandler.keyA) editorCamX -= speed; if (inputHandler.keyD) editorCamX += speed;
                 if (inputHandler.isMousePressed && !isMouseOnEditorUI(inputHandler.mouseX, inputHandler.mouseY)) placeEditorTile(inputHandler.mouseX, inputHandler.mouseY);
            }
            repaint(); return;
        }
        if (currentState != GameState.PLAYING && currentState != GameState.INVENTORY && currentState != GameState.CRAFTING && currentState != GameState.SHOP) { repaint(); return; }
        if (player == null) return;
        if (interactionCooldown > 0) interactionCooldown--;
        tickCounter++; if (attackAnimFrame > 0) attackAnimFrame--;
        if(currentState == GameState.PLAYING) {
            updatePlayerMovement();
            
            // Process Dash
            if (Math.abs(playerDashX) > 0.1f || Math.abs(playerDashY) > 0.1f) {
                player.visualX += playerDashX;
                player.visualY += playerDashY;
                playerDashX *= 0.8f; // Friction
                playerDashY *= 0.8f;
                
                // Update grid pos when visual is close enough to center of next tile
                // Or just snap at end. For now visual dash effect.
                // To keep grid synced, we should update player.x/y when dash completes or passes threshold.
                // Simple approach: When dash starts we verified next tile is free.
                // Let's actually move the player grid position at start of dash if valid?
                // Or just visual offset.
                // Better: Let visual offset slide, and snap x/y when done?
                // Issue: If visualX moves 60px, player.x must update.
                
                // Let's update player.x/y when the dash "Crosses" the boundary
                // Current simple: Visual only for smoothness, snap back if needed? 
                // No, we want actual movement.
                // We checked 1 tile ahead.
                // If velocity is high, we are moving.
                // Let's rely on standard visual interpolation in `player.updateVisuals`?
                // No, `player.updateVisuals` pulls visual towards x/y.
                // If we manually change visualX, `updateVisuals` will fight it.
                // So we should move `player.x/y` and let `updateVisuals` handle the slide, BUT `updateVisuals` is too slow (0.2f factor).
                // So we manually drive visualX/Y and prevent updateVisuals from overwriting it during dash?
                // Or temporarily increase lerp speed?
                
                // My choice: Add dash offset to visualX, but update logical X/Y immediately if dash started.
                // In handleMousePressed I didn't update player.x/y. Let's do it there?
                // But we want smooth slide.
                // Let's move player.x/y in handleMousePressed (if valid), and here we just handle the visual slide "overshoot" or custom movement.
                
                // Actually, let's keep it simple:
                // Dash just adds a temporary visual velocity that decays. 
                // AND we move the player logical position 1 tile forward in `handleMousePressed`.
                // Then `player.updateVisuals` will handle the rest, but we add an extra "kick" here.
            }
            
            processCombat();
        }
        
        // Disable standard visual interpolation if dashing to avoid conflict/jitter?
        // Actually, player.updateVisuals pulls visual -> grid. 
        // If we move grid, visual lags behind.
        // Dash velocity PUSHES visual towards grid (or past it).
        // Let's try combining them.
        
        player.updateVisuals(TILE_SIZE, inputHandler.keySpace);
        
        // Add dash velocity effect to visuals AFTER updateVisuals
        // (This makes it feel like a force is pushing the sprite)
        // Note: We need to set `player.x/y` in mouse pressed for this to make sense as movement.
        // I'll update mouse pressed logic next.
        
        if (player.getHealth() <= 0) { currentState = GameState.GAMEOVER; repaint(); return; }
        if (player.levelUpAnim == 59) effects.add(new VisualEffect(player.visualX, player.visualY - 40, "LEVEL UP!", Color.YELLOW, 60));
        if (tickCounter % 2 == 0) { 
            int pX = player.x, pY = player.y;
            for(Enemy en : activeEnemies) { 
                if (Math.abs(en.x - pX) > 18 || Math.abs(en.y - pY) > 14) continue; 
                if (en.updateAI(player, map)) {
                    boolean blocked = false;
                    if (player.isBlocking()) {
                        // Check block direction
                        int dx = en.x - pX;
                        int dy = en.y - pY;
                        
                        // Player facing needs to match enemy direction (roughly)
                        // If player faces right (attackDirX = 1), enemy must be to the right (dx > 0)
                        // If player faces up (attackDirY = -1), enemy must be above (dy < 0)
                        
                        if (player.attackDirX == 1 && dx > 0 && Math.abs(dy) <= 1) blocked = true;
                        else if (player.attackDirX == -1 && dx < 0 && Math.abs(dy) <= 1) blocked = true;
                        else if (player.attackDirY == -1 && dy < 0 && Math.abs(dx) <= 1) blocked = true;
                        else if (player.attackDirY == 1 && dy > 0 && Math.abs(dx) <= 1) blocked = true;
                    }

                    if (blocked) {
                        effects.add(new VisualEffect(player.visualX, player.visualY - 20, "BLOCKED", Color.BLUE, 30));
                    } else {
                        // Apply damage manually if not blocked
                        int dmg = player.applyDamage(en.getAttack());
                        effects.add(new VisualEffect(player.visualX, player.visualY, "-" + dmg, Color.RED, 25));
                    }
                } 
            }
        }
        updateCamera(); for (int i = 0; i < effects.size(); i++) { if (!effects.get(i).update()) { effects.remove(i); i--; } } repaint();
    }

    private void updatePlayerMovement() {
        if (moveTimer > 0) { moveTimer--; return; }
        
        // Can't move while attacking/blocking
        if (player.actionLocked) return;
        
        // Handle Rotation immediately based on input
        if (inputHandler.keyA) { player.facingLeft = true; player.attackDirX = -1; player.attackDirY = 0; }
        if (inputHandler.keyD) { player.facingLeft = false; player.attackDirX = 1; player.attackDirY = 0; }
        if (inputHandler.keyW) { player.attackDirX = 0; player.attackDirY = -1; }
        if (inputHandler.keyS) { player.attackDirX = 0; player.attackDirY = 1; }
        
        int nx = player.x, ny = player.y; boolean move = false;
        if (inputHandler.keyW) { ny--; move = true; } else if (inputHandler.keyS) { ny++; move = true; } else if (inputHandler.keyA) { nx--; move = true; } else if (inputHandler.keyD) { nx++; move = true; }
        
        if (move && nx >= 0 && nx < map.cols && ny >= 0 && ny < map.rows) {
            Object t = map.getEntityAt(nx, ny);
            // Collision Check: Only move if tile is empty or "walkable" (like Tent or Campfire, assuming they are walkable or we treat them as obstacles)
            // For now, assume everything except null and WATER is an obstacle
            if("WATER".equals(t)) { 
                // Blocked
            } else if(t == null || t instanceof WorldMap.Tent || t instanceof WorldMap.Campfire) { 
                player.x = nx; player.y = ny; 
                moveTimer = MOVE_SPEED_DELAY; 
                map.updateExploration(nx, ny, 8); 
                
                // Pick up buildings if walking over them? (Maybe keep E key for that)
            } 
            else if (t instanceof Building) {
                Building b = (Building)t;
                if (b.isReady()) { // Auto-use fountains/monuments when walking into them? Or just block?
                    // Let's block movement but allow use if walking into it? 
                    // User asked to remove bump interaction. So we just BLOCK.
                }
            }
            else {
                // Blocked by Enemy, Resource, Vendor
            }
        }
    }

    private void processCombat() {
        if (player.actionLocked && (player.animState == Player.AnimState.ATTACK_1 || player.animState == Player.AnimState.ATTACK_2)) {
            // Trigger hit on frame 3 (middle of swing)
            if (player.animFrame == 3 && !player.hitTriggered) {
                player.hitTriggered = true;
                
                int tx = player.x + player.attackDirX;
                int ty = player.y + player.attackDirY;
                
                if (tx >= 0 && tx < map.cols && ty >= 0 && ty < map.rows) {
                    Object target = map.getEntityAt(tx, ty);
                    if (target != null) {
                        // Bonus damage for heavy attack (Attack 2)
                        if (player.animState == Player.AnimState.ATTACK_2) {
                            player.damageBonus += 10; // Temporary bonus
                            interactWith(target, tx, ty);
                            player.damageBonus -= 10; // Remove bonus
                        } else {
                            interactWith(target, tx, ty);
                        }
                    }
                }
            }
        }
    }

    private void interactWith(Object t, int nx, int ny) {
    	if (interactionCooldown > 0) return;
    	Item item = player.getSelectedItem();
        if (t instanceof Vendor) currentState = GameState.SHOP;
        else if (t instanceof Building) {
            Building b = (Building) t;
            if (b.isReady()) { if (b.type == Building.Type.FOUNTAIN) { player.healFull(); effects.add(new VisualEffect(player.visualX, player.visualY, "HEALED!", Color.GREEN, 30)); } else if (b.type == Building.Type.MONUMENT) { player.addPermanentAttack(5); effects.add(new VisualEffect(player.visualX, player.visualY, "+5 DMG!", Color.ORANGE, 30)); } b.use(); } 
            else effects.add(new VisualEffect(player.visualX, player.visualY, "Wait " + b.getSecondsLeft() + "s", Color.YELLOW, 20));
        } 
        else if (t instanceof Enemy) {
            // attackAnimFrame = 10; // Old anim logic removed
            Enemy en = (Enemy)t; player.damage(en); effects.add(new VisualEffect(nx*TILE_SIZE, ny*TILE_SIZE, "HIT", Color.WHITE, 20));
            if(!en.isAlive()) { 
            	map.setEntityAt(nx, ny, en.getSavedTile()); activeEnemies.remove(en); player.addXp(50); effects.add(new VisualEffect(nx*TILE_SIZE, ny*TILE_SIZE, "+50 XP", Color.YELLOW, 30)); 
                int gold = 5 + (int)(Math.random() * 11); player.addGold(gold); effects.add(new VisualEffect(nx*TILE_SIZE, ny*TILE_SIZE - 20, "+" + gold + " G", new Color(255, 215, 0), 40));
                if(en.type == Enemy.Type.HUNTER && Math.random() < 0.35) {
                    Item drop = null; double r = Math.random();
                    if(r < 0.25) drop = new Item("Iron Helm", Item.Type.ARMOR, Item.Specific.HELMET, 2); else if(r < 0.5) drop = new Item("Iron Chest", Item.Type.ARMOR, Item.Specific.CHESTPLATE, 5); else if(r < 0.75) drop = new Item("Iron Legs", Item.Type.ARMOR, Item.Specific.PANTS, 3); else drop = new Item("Iron Boots", Item.Type.ARMOR, Item.Specific.BOOTS, 1);
                    if(player.addItem(drop)) effects.add(new VisualEffect(player.visualX, player.visualY - 40, "LEGENDARY DROP!", new Color(255, 215, 0), 60));
                }
            }
        } 
        else if (t instanceof ResourceEntity) {
             if (t instanceof Tree && (item == null || item.specificType != Item.Specific.AXE)) { effects.add(new VisualEffect(player.visualX, player.visualY, "Need AXE!", Color.RED, 20)); return; }
             if (t instanceof Rock && (item == null || item.specificType != Item.Specific.PICKAXE)) { effects.add(new VisualEffect(player.visualX, player.visualY, "Need PICKAXE!", Color.RED, 20)); return; }
             ResourceEntity res = (ResourceEntity)t; int collected = player.collectResource(res);
             if (collected > res.getQuantity()) effects.add(new VisualEffect(player.visualX, player.visualY - 40, "LUCKY DROP!", Color.YELLOW, 40));
             if (res.getQuality() == Enums.Quality.EPIC) effects.add(new VisualEffect(player.visualX, player.visualY - 60, "EPIC DROP!", new Color(148, 0, 211), 50));
             effects.add(new VisualEffect(player.visualX, player.visualY - 20, "+" + collected + " " + res.getResourceType(), Color.WHITE, 30));
             map.removeEntityAt(nx, ny); 
        }
    }

    public void useCurrentItem(int x, int y) {
        Object foot = map.getEntityAt(x, y);
        if (foot instanceof Building) {
            Building b = (Building) foot; Item buildItem = new Item(b.type == Building.Type.FOUNTAIN ? "Fountain" : "Monument", Item.Type.BUILDING, b.type == Building.Type.FOUNTAIN ? Item.Specific.FOUNTAIN : Item.Specific.MONUMENT, 0);
            buildItem.savedLastUsedTime = b.lastUsedTime;
            if (player.addItem(buildItem)) { map.removeEntityAt(x, y); effects.add(new VisualEffect(player.visualX, player.visualY, "Picked Up!", Color.GREEN, 30)); }
            else effects.add(new VisualEffect(player.visualX, player.visualY, "Full!", Color.RED, 30));
            return;
        }
        Item item = player.getSelectedItem(); if(item == null) return;
        if(item.type == Item.Type.BUILDING && map.getEntityAt(x, y) == null) {
            Building newB = new Building(item.specificType == Item.Specific.FOUNTAIN ? Building.Type.FOUNTAIN : Building.Type.MONUMENT);
            if (item.savedLastUsedTime > 0) newB.lastUsedTime = item.savedLastUsedTime;
            map.setEntityAt(x, y, newB); player.removeItem(player.selectedSlot); effects.add(new VisualEffect(x*TILE_SIZE, y*TILE_SIZE, "Built!", Color.GREEN, 30));
        } else if (item.type == Item.Type.CONSUMABLE && player.getHealth() < player.getMaxHealth()) {
            player.heal(item.value); player.removeItem(player.selectedSlot); effects.add(new VisualEffect(player.visualX, player.visualY, "+" + item.value + " HP", Color.GREEN, 30));
        }
    }

    private void updateCamera() {
        float tx = player.visualX - getWidth() / 2 + TILE_SIZE / 2, ty = player.visualY - getHeight() / 2 + TILE_SIZE / 2;
        camX += (tx - camX) * 0.08f; camY += (ty - camY) * 0.08f;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g); Graphics2D g2 = (Graphics2D) g; g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        if (currentState == GameState.LOADING) { uiRenderer.drawLoadingScreen(g2); drawCursor(g2); return; }
        
        // Draw Menu Background Simulation
        if (currentState == GameState.MENU || currentState == GameState.EXIT_CONFIRM || currentState == GameState.MENU_LOAD || currentState == GameState.CHARACTER_SELECT) {
            g2.setColor(Assets.WATER); g2.fillRect(0, 0, getWidth(), getHeight());
            if (menuMap != null) {
                g2.translate(-menuCamX, -menuCamY);
                drawMenuWorld(g2);
                g2.translate(menuCamX, menuCamY);
            }
        }

        if (currentState == GameState.MENU) { uiRenderer.drawModernMenu(g2, getWidth(), getHeight()); drawCursor(g2); return; }
        if (currentState == GameState.CHARACTER_SELECT) { uiRenderer.drawModernMenu(g2, getWidth(), getHeight()); uiRenderer.drawCharacterSelection(g2); drawCursor(g2); return; }
        if (currentState == GameState.EXIT_CONFIRM) { uiRenderer.drawModernMenu(g2, getWidth(), getHeight()); uiRenderer.drawExitConfirmation(g2); drawCursor(g2); return; }
        // EXIT_GAME_CONFIRM handled below to show game world
        if (currentState == GameState.MENU_LOAD || currentState == GameState.PAUSE_SAVE || currentState == GameState.PAUSE_LOAD) { uiRenderer.drawSlotSelectionMenu(g2); drawCursor(g2); return; }
        if (currentState == GameState.EDITOR_SELECT) { uiRenderer.drawEditorSelectMenu(g2); for(VisualEffect ve : effects) ve.draw(g2); drawCursor(g2); return; }
        if (currentState == GameState.EDITOR_EDIT) { uiRenderer.drawEditorInterface(g2); for(VisualEffect ve : effects) ve.draw(g2); drawCursor(g2); return; }
        if (map == null || player == null) { drawCursor(g2); return; }
        
        g2.setColor(Assets.WATER); g2.fillRect(0, 0, getWidth(), getHeight());
        g2.translate(-camX, -camY); drawWorld(g2); drawPlayer(g2); for(VisualEffect ve : effects) ve.draw(g2); g2.translate(camX, camY);
        if (currentState != GameState.MENU) uiRenderer.drawHUD(g2, player); 
        if (currentState == GameState.CRAFTING) uiRenderer.drawCraftingMenu(g2, player); if (currentState == GameState.SHOP) uiRenderer.drawShopMenu(g2, player); if (currentState == GameState.INVENTORY) uiRenderer.drawInventoryMenu(g2, player); if (currentState == GameState.PAUSED) uiRenderer.drawPauseMenu(g2); 
        if (currentState == GameState.EXIT_GAME_CONFIRM) { uiRenderer.drawPauseMenu(g2); uiRenderer.drawExitGameConfirmation(g2); }
        if (currentState == GameState.GAMEOVER) uiRenderer.drawGameOverScreen(g2); if (currentState == GameState.MAP) uiRenderer.drawMapScreen(g2, map, player);
        if (draggingItem != null) { uiRenderer.drawItemIcon(g2, draggingItem, inputHandler.mouseX - 30, inputHandler.mouseY - 30, 60); uiRenderer.drawItemQuantity(g2, draggingItem, inputHandler.mouseX - 30, inputHandler.mouseY - 30, 60); }
        
        drawCursor(g2);
    }

    private void drawCursor(Graphics2D g2) {
        if (uiRenderer != null) {
            BufferedImage cursorImg = uiRenderer.getDesiredCursor();
            if (cursorImg != null) {
                int size = 96; 
                int offX = 0;
                int offY = 0;

                // Tiny Swords cursors have padding. We offset the drawing to align the "tip"
                if (cursorImg == Assets.CURSOR_DEFAULT) {
                    offX = -34; // Adjusted to align visual tip with mouse coords
                    offY = -28; 
                } else if (cursorImg == Assets.CURSOR_POINTER) {
                    offX = -34; 
                    offY = -28;
                } else if (cursorImg == Assets.CURSOR_DISABLED) {
                    offX = -size / 2; // Centrează iconița de "interzis"
                    offY = -size / 2;
                }

                g2.drawImage(cursorImg, inputHandler.mouseX + offX, inputHandler.mouseY + offY, size, size, null);
            }
        }
    }

    private void drawMenuWorld(Graphics2D g2) {
        int sc = (int)Math.floor(menuCamX/TILE_SIZE), sr = (int)Math.floor(menuCamY/TILE_SIZE), ec = sc+getWidth()/TILE_SIZE+2, er = sr+getHeight()/TILE_SIZE+2;
        for (int r = sr; r < er; r++) for (int c = sc; c < ec; c++) {
            int px = c*TILE_SIZE, py = r*TILE_SIZE;
            if (c >= 0 && c < menuMap.cols && r >= 0 && r < menuMap.rows) {
                g2.setColor((r+c)%2==0 ? Assets.GRASS_1 : Assets.GRASS_2); g2.fillRect(px, py, TILE_SIZE, TILE_SIZE); 
                Object ent = menuMap.getEntityAt(c, r); 
                if (ent == null) continue;
                if ("WATER".equals(ent)) IconRenderer.renderWaterTile(g2, px, py, TILE_SIZE, tickCounter); 
                else if (ent instanceof Tree) IconRenderer.drawTree(g2, px, py, TILE_SIZE, ((Tree)ent).getQuality()); 
                else if (ent instanceof Rock) IconRenderer.drawRock(g2, px, py, TILE_SIZE, ((Rock)ent).getQuality());
            }
        }
        // Draw Enemies
        for(Enemy e : menuEnemies) {
            IconRenderer.renderEnemy(g2, e, (int)e.visualX, (int)e.visualY, TILE_SIZE);
        }
        // Draw Player
        IconRenderer.drawKnight(g2, (int)menuPlayer.visualX, (int)menuPlayer.visualY, TILE_SIZE, menuPlayer.animState, menuPlayer.animFrame, menuPlayer.facingLeft);
    }

    private void drawWorld(Graphics2D g2) {
        int sc = (int)Math.floor(camX/TILE_SIZE), sr = (int)Math.floor(camY/TILE_SIZE), ec = sc+getWidth()/TILE_SIZE+2, er = sr+getHeight()/TILE_SIZE+2;
        for (int r = sr; r < er; r++) for (int c = sc; c < ec; c++) {
            int px = c*TILE_SIZE, py = r*TILE_SIZE;
            if (c >= 0 && c < map.cols && r >= 0 && r < map.rows) {
                g2.setColor((r+c)%2==0 ? Assets.GRASS_1 : Assets.GRASS_2); g2.fillRect(px, py, TILE_SIZE, TILE_SIZE); Object ent = map.getEntityAt(c, r); if (ent == null) continue;
                if (ent instanceof WorldMap.Campfire) IconRenderer.drawCampfire(g2, px, py, TILE_SIZE); else if (ent instanceof WorldMap.Tent) IconRenderer.drawTent(g2, px, py, TILE_SIZE); else if ("WATER".equals(ent)) IconRenderer.renderWaterTile(g2, px, py, TILE_SIZE, tickCounter); else if (ent instanceof Tree) IconRenderer.drawTree(g2, px, py, TILE_SIZE, ((Tree)ent).getQuality()); else if (ent instanceof Rock) IconRenderer.drawRock(g2, px, py, TILE_SIZE, ((Rock)ent).getQuality()); else if (ent instanceof Grain) IconRenderer.renderGrain(g2, px, py, TILE_SIZE, ((Grain)ent).getQuality()); else if (ent instanceof Building) IconRenderer.drawBuilding(g2, (Building)ent, px, py, TILE_SIZE); else if (ent instanceof Vendor) IconRenderer.drawVendor(g2, px, py, TILE_SIZE); else if (ent instanceof Enemy) IconRenderer.renderEnemy(g2, (Enemy)ent, px, py, TILE_SIZE);
            } else IconRenderer.renderWaterTile(g2, px, py, TILE_SIZE, tickCounter);
        }
    }

        private void drawPlayer(Graphics2D g2) {

            if(!player.isAlive()) return; 

            int x = (int)player.visualX, y = (int)player.visualY;

            

            IconRenderer.drawKnight(g2, x, y, TILE_SIZE, player.animState, player.animFrame, player.facingLeft);

    

            if (player.levelUpAnim > 0) { float alpha = (float)player.levelUpAnim / 60.0f; g2.setColor(new Color(1f, 0.8f, 0f, alpha * 0.5f)); g2.fillOval(x-10, y-10, 80, 80); g2.setColor(new Color(1f, 1f, 0.5f, alpha)); g2.setStroke(new BasicStroke(3)); g2.drawOval(x-10, y-10, 80, 80); g2.setStroke(new BasicStroke(1)); }

            

            g2.setColor(Color.WHITE); g2.setFont(Assets.PIXEL_FONT != null ? Assets.PIXEL_FONT.deriveFont(14f) : new Font("Arial", Font.BOLD, 14));

            String lv = "Lv." + player.getLevel(); g2.drawString(lv, x+30-g2.getFontMetrics().stringWidth(lv)/2, y-5);

        }
    
    public void handleKeyTyped(char c) {
        if (editingSaveSlot != -1 || isCreatingMap) {
            if (c == KeyEvent.VK_BACK_SPACE) {
                if (currentTypingText.length() > 0) currentTypingText.deleteCharAt(currentTypingText.length() - 1);
            } else if (c != KeyEvent.VK_ENTER && c != KeyEvent.VK_ESCAPE && !Character.isISOControl(c)) {
                if (currentTypingText.length() < 20) currentTypingText.append(c);
            }
            repaint();
        }
    }
    
    public void handleKeyPressed(int k) {
        if (editingSaveSlot != -1) {
            if (k == KeyEvent.VK_ENTER) {
                if (currentTypingText.length() == 0) {
                    systemMessage = "Please enter a name!";
                    systemMessageTimer = 120;
                } else {
                    saveGame(editingSaveSlot, currentTypingText.toString());
                    editingSaveSlot = -1;
                }
            } else if (k == KeyEvent.VK_ESCAPE) {
                editingSaveSlot = -1;
            }
            repaint();
            return;
        }
        if (isCreatingMap) {
            if (k == KeyEvent.VK_ENTER) {
                String name = currentTypingText.toString();
                if (!name.trim().isEmpty()) {
                    currentMapName = name;
                    editorMap = new WorldMap(100, 100);
                    for(int r=0; r<100; r++) for(int c=0; c<100; c++) editorMap.grid[r][c] = null;
                    currentState = GameState.EDITOR_EDIT;
                    isCreatingMap = false;
                    editorMapModified = false;
                    showExitEditorConfirm = false;
                } else {
                    systemMessage = "Please enter a name!";
                    systemMessageTimer = 120;
                }
            } else if (k == KeyEvent.VK_ESCAPE) {
                isCreatingMap = false;
            }
            repaint();
            return;
        }

        if (k == KeyEvent.VK_ESCAPE) {
            if (currentState == GameState.PLAYING) { currentState = GameState.PAUSED; inputHandler.keyW = inputHandler.keyA = inputHandler.keyS = inputHandler.keyD = false; }
            else if (currentState == GameState.PAUSED) currentState = GameState.PLAYING;
            else if (currentState == GameState.INVENTORY || currentState == GameState.CRAFTING || currentState == GameState.SHOP || currentState == GameState.MAP) currentState = GameState.PLAYING;
            else if (currentState == GameState.MENU_LOAD) { currentState = GameState.MENU; editingSaveSlot = -1; }
            else if (currentState == GameState.EXIT_CONFIRM) { currentState = GameState.MENU; }
            else if (currentState == GameState.CHARACTER_SELECT) { currentState = GameState.MENU; }
            else if (currentState == GameState.EXIT_GAME_CONFIRM) { currentState = GameState.PAUSED; }
            else if (currentState == GameState.PAUSE_SAVE || currentState == GameState.PAUSE_LOAD) { currentState = GameState.PAUSED; editingSaveSlot = -1; }
            return;
        }
        if (currentState == GameState.MENU) { if (k == KeyEvent.VK_ENTER) startNewGame(); }
        else if (currentState == GameState.PLAYING) {
             if (k == KeyEvent.VK_W || k == KeyEvent.VK_UP) inputHandler.keyW = true; if (k == KeyEvent.VK_S || k == KeyEvent.VK_DOWN) inputHandler.keyS = true; if (k == KeyEvent.VK_A || k == KeyEvent.VK_LEFT) inputHandler.keyA = true; if (k == KeyEvent.VK_D || k == KeyEvent.VK_RIGHT) inputHandler.keyD = true;
             if (k == KeyEvent.VK_M) currentState = GameState.MAP; 
                          if (k >= KeyEvent.VK_1 && k <= KeyEvent.VK_5) player.selectedSlot = k - KeyEvent.VK_1; 
                          
                          if (k == KeyEvent.VK_E) useCurrentItem(player.x, player.y); // Moved Item Use to E
                          if (k == KeyEvent.VK_I) currentState = GameState.INVENTORY; if (k == KeyEvent.VK_C) currentState = GameState.CRAFTING;
        } else if (currentState == GameState.INVENTORY || currentState == GameState.CRAFTING) { if (k == KeyEvent.VK_I || k == KeyEvent.VK_C) currentState = GameState.PLAYING; }
        else if (currentState == GameState.MAP) { if (k == KeyEvent.VK_M) currentState = GameState.PLAYING; }
        else if (currentState == GameState.EDITOR_EDIT) { if (k == KeyEvent.VK_W || k == KeyEvent.VK_UP) inputHandler.keyW = true; if (k == KeyEvent.VK_S || k == KeyEvent.VK_DOWN) inputHandler.keyS = true; if (k == KeyEvent.VK_A || k == KeyEvent.VK_LEFT) inputHandler.keyA = true; if (k == KeyEvent.VK_D || k == KeyEvent.VK_RIGHT) inputHandler.keyD = true; }
    }
    
    public void handleMousePressed(int x, int y, int button) {
        Point p = new Point(x, y); pressedButton = null;
        
        // Handle Attack in PLAYING state
        if (currentState == GameState.PLAYING) {
            // Check if UI element clicked first (Hotbar, Buttons)
            boolean uiClicked = false;
            if (uiRenderer.btnMapOpen.contains(p) || uiRenderer.btnCraftingOpen.contains(p) || uiRenderer.btnInventoryOpen.contains(p)) uiClicked = true;
            for(Rectangle r : uiRenderer.hotbarRects) if(r != null && r.contains(p)) uiClicked = true;
            
            if (!uiClicked && !player.actionLocked) {
                // Calculate Attack Direction
                float px = player.visualX - camX + TILE_SIZE/2;
                float py = player.visualY - camY + TILE_SIZE/2;
                float dx = x - px;
                float dy = y - py;
                
                if (Math.abs(dx) > Math.abs(dy)) {
                    // Horizontal
                    player.attackDirX = dx > 0 ? 1 : -1;
                    player.attackDirY = 0;
                    player.facingLeft = dx < 0; // Visuals follow horizontal aim
                } else {
                    // Vertical
                    player.attackDirX = 0;
                    player.attackDirY = dy > 0 ? 1 : -1;
                    // Keep existing facingLeft for vertical attacks? Or flip?
                    // Usually vertical attacks in 2D side-view flip based on horizontal offset anyway.
                    // If dx is small but non-zero, use it. If 0, keep same.
                    if (Math.abs(dx) > 1) player.facingLeft = dx < 0; 
                }

                if (button == MouseEvent.BUTTON1) {
                    player.animState = Player.AnimState.ATTACK_1;
                    player.actionLocked = true;
                    player.animFrame = 0;
                    player.animTick = 0;
                } else if (button == MouseEvent.BUTTON3) {
                    player.animState = Player.AnimState.ATTACK_2;
                    player.actionLocked = true;
                    player.animFrame = 0;
                    player.animTick = 0;
                    
                    // Dash Logic: Check if space is free immediately in front
                    int dashTargetX = player.x + player.attackDirX;
                    int dashTargetY = player.y + player.attackDirY;
                    
                    if (dashTargetX >= 0 && dashTargetX < map.cols && dashTargetY >= 0 && dashTargetY < map.rows) {
                        Object t = map.getEntityAt(dashTargetX, dashTargetY);
                        // Only dash if tile is walkable (null, Tent, Campfire)
                        if (t == null || t instanceof WorldMap.Tent || t instanceof WorldMap.Campfire) {
                            // Move player grid position
                            player.x = dashTargetX;
                            player.y = dashTargetY;
                            map.updateExploration(dashTargetX, dashTargetY, 8);
                            
                            // Add visual burst
                            // Since player.x changed, visualX is now "behind". 
                            // updateVisuals will pull it.
                            // We can add a bit of "overshoot" or just let the fast update handle it?
                            // User wants a "dash/charge". 
                            // Let's set a high visual velocity impulse.
                            // We don't really use velocity in Player class, we use lerp.
                            // But in GamePanel update we added `playerDashX/Y`.
                            // Let's give it a kick.
                            playerDashX = player.attackDirX * 15.0f; 
                            playerDashY = player.attackDirY * 15.0f;
                        }
                    }
                }
                return; // Attack handled, don't do UI click logic
            }
        }

        if (uiRenderer.btnCloseWindow != null && uiRenderer.btnCloseWindow.contains(p) && (currentState == GameState.INVENTORY || currentState == GameState.CRAFTING || currentState == GameState.SHOP || currentState == GameState.MAP || currentState == GameState.EDITOR_SELECT || currentState == GameState.EDITOR_EDIT || currentState == GameState.MENU_LOAD || currentState == GameState.PAUSE_SAVE || currentState == GameState.PAUSE_LOAD || currentState == GameState.CHARACTER_SELECT)) { pressedButton = uiRenderer.btnCloseWindow; return; }
        if (currentState == GameState.MENU) {
            if (uiRenderer.btnStartGame.contains(p)) pressedButton = uiRenderer.btnStartGame; else if (uiRenderer.btnContinue.contains(p) && hasSaveFile) pressedButton = uiRenderer.btnContinue; else if (uiRenderer.btnLoadGame.contains(p)) pressedButton = uiRenderer.btnLoadGame; else if (uiRenderer.btnEditor.contains(p)) pressedButton = uiRenderer.btnEditor; else if (uiRenderer.btnExitMenu.contains(p)) pressedButton = uiRenderer.btnExitMenu;
        } else if (currentState == GameState.EXIT_CONFIRM) {
            if (uiRenderer.btnExitYes.contains(p)) pressedButton = uiRenderer.btnExitYes; else if (uiRenderer.btnExitNo.contains(p)) pressedButton = uiRenderer.btnExitNo;
        } else if (currentState == GameState.CHARACTER_SELECT) {
            if (uiRenderer.charButtons != null) {
                for(int i=0; i<uiRenderer.charButtons.length; i++) {
                    if (uiRenderer.charButtons[i] != null && uiRenderer.charButtons[i].contains(p)) {
                        pressedButton = uiRenderer.charButtons[i];
                        selectedCharIndex = i;
                        return;
                    }
                }
            }
            if (uiRenderer.btnCharStart != null && uiRenderer.btnCharStart.contains(p)) pressedButton = uiRenderer.btnCharStart;
            if (uiRenderer.ribbonDropdownRects != null) {
                for(int i=0; i<uiRenderer.ribbonDropdownRects.length; i++) {
                     if (uiRenderer.ribbonDropdownRects[i] != null && uiRenderer.ribbonDropdownRects[i].contains(p)) {
                         pressedButton = uiRenderer.ribbonDropdownRects[i];
                         return;
                     }
                }
            }
            if (uiRenderer.btnRibbon != null && uiRenderer.btnRibbon.contains(p)) pressedButton = uiRenderer.btnRibbon;
        } else if (currentState == GameState.EXIT_GAME_CONFIRM) {
            if (uiRenderer.btnExitGameConfirmYes.contains(p)) pressedButton = uiRenderer.btnExitGameConfirmYes; else if (uiRenderer.btnExitGameConfirmNo.contains(p)) pressedButton = uiRenderer.btnExitGameConfirmNo;
        } else if (currentState == GameState.PAUSED) {
            if (uiRenderer.btnResume.contains(p)) pressedButton = uiRenderer.btnResume; else if (uiRenderer.btnSaveMenu.contains(p)) pressedButton = uiRenderer.btnSaveMenu; else if (uiRenderer.btnLoadMenu.contains(p)) pressedButton = uiRenderer.btnLoadMenu; else if (uiRenderer.btnMainMenu.contains(p)) pressedButton = uiRenderer.btnMainMenu; else if (uiRenderer.btnExitPause.contains(p)) pressedButton = uiRenderer.btnExitPause;
        } else if (currentState == GameState.GAMEOVER) {
            if (uiRenderer.btnRestart.contains(p)) pressedButton = uiRenderer.btnRestart; 
            else if (uiRenderer.btnMainMenu.contains(p)) pressedButton = uiRenderer.btnMainMenu;
        } else if (currentState == GameState.INVENTORY) {
            for(int i=0; i<4; i++) if(uiRenderer.armorRects[i].contains(p) && player.armor[i] != null) { draggingItem = player.armor[i]; player.armor[i] = null; dragSourceIndex = i; dragSourceType = 2; return; }
            for(int i=0; i<16; i++) if(uiRenderer.backpackRects[i].contains(p) && player.backpack[i] != null) { draggingItem = player.backpack[i]; player.backpack[i] = null; dragSourceIndex = i; dragSourceType = 0; return; }
            for(int i=0; i<5; i++) if(uiRenderer.hotbarRects[i].contains(p) && player.inventory[i] != null) { draggingItem = player.inventory[i]; player.inventory[i] = null; dragSourceIndex = i; dragSourceType = 1; return; }
        } else if (currentState == GameState.SHOP) {
            if (uiRenderer.shopBuyRects != null) for(int i=0; i<uiRenderer.shopBuyRects.length; i++) if(uiRenderer.shopBuyRects[i] != null && uiRenderer.shopBuyRects[i].contains(p)) { pressedButton = uiRenderer.shopBuyRects[i]; return; }
            if (uiRenderer.shopSellRects != null) for(int i=0; i<20; i++) if(uiRenderer.shopSellRects[i] != null && uiRenderer.shopSellRects[i].contains(p)) { pressedButton = uiRenderer.shopSellRects[i]; return; }
        } else if (currentState == GameState.CRAFTING) {
            for(int i=0; i<uiRenderer.craftButtons.size(); i++) if(uiRenderer.craftButtons.get(i).rect != null && uiRenderer.craftButtons.get(i).rect.contains(p)) { pressedButton = uiRenderer.craftButtons.get(i).rect; return; }
        } else if (currentState == GameState.EDITOR_SELECT) {
            if (uiRenderer.btnCreateMap.contains(p)) pressedButton = uiRenderer.btnCreateMap;
            if (uiRenderer.playMapBtns != null) {
                for(int i=0; i<mapFiles.size(); i++) {
                    if(uiRenderer.playMapBtns[i] != null && uiRenderer.playMapBtns[i].contains(p)) { pressedButton = uiRenderer.playMapBtns[i]; return; }
                    if(uiRenderer.editMapBtns[i] != null && uiRenderer.editMapBtns[i].contains(p)) { pressedButton = uiRenderer.editMapBtns[i]; return; }
                    if(uiRenderer.delMapBtns[i] != null && uiRenderer.delMapBtns[i].contains(p)) { pressedButton = uiRenderer.delMapBtns[i]; return; }
                }
            }
            if (uiRenderer.btnConfirmYes != null && uiRenderer.btnConfirmYes.contains(p)) pressedButton = uiRenderer.btnConfirmYes; else if (uiRenderer.btnConfirmNo != null && uiRenderer.btnConfirmNo.contains(p)) pressedButton = uiRenderer.btnConfirmNo;
        } else if (currentState == GameState.EDITOR_EDIT) {
            if (showExitEditorConfirm) {
                if (uiRenderer.btnConfirmYes != null && uiRenderer.btnConfirmYes.contains(p)) pressedButton = uiRenderer.btnConfirmYes;
                else if (uiRenderer.btnConfirmNo != null && uiRenderer.btnConfirmNo.contains(p)) pressedButton = uiRenderer.btnConfirmNo;
                else if (uiRenderer.btnSaveAndExit != null && uiRenderer.btnSaveAndExit.contains(p)) pressedButton = uiRenderer.btnSaveAndExit;
                return;
            }
            if (uiRenderer.btnSaveMap.contains(p)) pressedButton = uiRenderer.btnSaveMap;
            if (uiRenderer.paletteRects != null) { for(int i=0; i<uiRenderer.paletteRects.length; i++) if(uiRenderer.paletteRects[i].contains(p)) { editorSelectedTileIndex = i; return; } }
        } else if (currentState == GameState.MENU_LOAD || currentState == GameState.PAUSE_SAVE || currentState == GameState.PAUSE_LOAD) {
            if (showSaveDeleteConfirm || showLoadConfirm) {
                 if (uiRenderer.btnConfirmYes != null && uiRenderer.btnConfirmYes.contains(p)) pressedButton = uiRenderer.btnConfirmYes; 
                 else if (uiRenderer.btnConfirmNo != null && uiRenderer.btnConfirmNo.contains(p)) pressedButton = uiRenderer.btnConfirmNo;
                 return;
            }
            if (uiRenderer.btnSlot1.contains(p)) pressedButton = uiRenderer.btnSlot1; else if (uiRenderer.btnSlot2.contains(p)) pressedButton = uiRenderer.btnSlot2; else if (uiRenderer.btnSlot3.contains(p)) pressedButton = uiRenderer.btnSlot3; else if (uiRenderer.btnBack.contains(p)) pressedButton = uiRenderer.btnBack; else if (uiRenderer.btnDelete1.contains(p)) pressedButton = uiRenderer.btnDelete1; else if (uiRenderer.btnDelete2.contains(p)) pressedButton = uiRenderer.btnDelete2; else if (uiRenderer.btnDelete3.contains(p)) pressedButton = uiRenderer.btnDelete3;
        }
    }
    
            public void handleMouseReleased(int x, int y) {
    
                Point p = new Point(x, y); inputHandler.isMousePressed = false;
    
                if (draggingItem != null) {
    
                    boolean placed = false;
    
                    for(int i=0; i<4; i++) if(uiRenderer.armorRects[i].contains(p)) { 
    
                        boolean v = false; if(i==0 && draggingItem.specificType == Item.Specific.HELMET) v=true; if(i==1 && draggingItem.specificType == Item.Specific.CHESTPLATE) v=true; if(i==2 && draggingItem.specificType == Item.Specific.PANTS) v=true; if(i==3 && draggingItem.specificType == Item.Specific.BOOTS) v=true; 
    
                        if(v) { Item t = player.armor[i]; player.armor[i] = draggingItem; draggingItem = t; placed = true; break; } 
    
                    }
    
                    if(!placed) for(int i=0; i<16; i++) if(uiRenderer.backpackRects[i].contains(p)) { Item t = player.backpack[i]; player.backpack[i] = draggingItem; draggingItem = t; placed = true; break; }
    
                    if(!placed) for(int i=0; i<5; i++) if(uiRenderer.hotbarRects[i].contains(p)) { Item t = player.inventory[i]; player.inventory[i] = draggingItem; draggingItem = t; placed = true; break; }
    
                    if(draggingItem != null) { if(dragSourceType == 0) player.backpack[dragSourceIndex] = draggingItem; else if(dragSourceType == 1) player.inventory[dragSourceIndex] = draggingItem; else if(dragSourceType == 2) player.armor[dragSourceIndex] = draggingItem; }
    
                    draggingItem = null; repaint(); return;
    
                }
    
                Rectangle clicked = (pressedButton != null && pressedButton.contains(p)) ? pressedButton : null; pressedButton = null;
    
                if (clicked == null) { if (currentState == GameState.PLAYING) for(int i=0; i<5; i++) if(uiRenderer.hotbarRects[i].contains(p)) player.selectedSlot = i; return; }
    
                
    
                if (uiRenderer.btnCloseWindow != null && clicked.equals(uiRenderer.btnCloseWindow)) { 
    
                    if (currentState == GameState.EDITOR_EDIT && editorMapModified) {
    
                        showExitEditorConfirm = true;
    
                        return;
    
                    }
    
                    if (currentState == GameState.MENU_LOAD) { currentState = GameState.MENU; }
    
                    else if (currentState == GameState.PAUSE_SAVE || currentState == GameState.PAUSE_LOAD) { currentState = GameState.PAUSED; }
    
                    else if (currentState == GameState.EDITOR_SELECT) { currentState = GameState.MENU; }
    
                    else if (currentState == GameState.EDITOR_EDIT) { currentState = GameState.EDITOR_SELECT; }
    
                    else if (currentState == GameState.CHARACTER_SELECT) { currentState = GameState.MENU; }
    
                    else { currentState = GameState.PLAYING; }
    
                    
    
                    isCreatingMap = false; editingSaveSlot = -1;
    
                    if(currentState == GameState.MENU || currentState == GameState.EDITOR_SELECT) refreshMapList(); return; 
    
                }
    
        
    
                if (currentState == GameState.EDITOR_EDIT) {
    
                    if (showExitEditorConfirm) {
    
                        if (uiRenderer.btnConfirmYes.equals(clicked)) { 
    
                            currentState = GameState.EDITOR_SELECT; refreshMapList();
    
                            showExitEditorConfirm = false; editorMapModified = false;
    
                        } else if (uiRenderer.btnConfirmNo.equals(clicked)) { 
    
                            showExitEditorConfirm = false;
    
                        } else if (uiRenderer.btnSaveAndExit.equals(clicked)) { 
    
                            saveMap(currentMapName);
    
                            currentState = GameState.EDITOR_SELECT; refreshMapList();
    
                            showExitEditorConfirm = false; editorMapModified = false;
    
                        }
    
                        return;
    
                    }
    
                    if (uiRenderer.btnSaveMap.equals(clicked)) saveMap(currentMapName);
    
                    if (uiRenderer.paletteRects != null) { for(int i=0; i<uiRenderer.paletteRects.length; i++) if(uiRenderer.paletteRects[i].contains(p)) { editorSelectedTileIndex = i; return; } }
    
                }
    
                else if (currentState == GameState.MENU) { 
    
                    if (uiRenderer.btnStartGame.equals(clicked)) currentState = GameState.CHARACTER_SELECT; 
    
                    else if (uiRenderer.btnContinue.equals(clicked)) loadGame(1); 
    
                    else if (uiRenderer.btnLoadGame.equals(clicked)) currentState = GameState.MENU_LOAD; 
    
                    else if (uiRenderer.btnEditor.equals(clicked)) { currentState = GameState.EDITOR_SELECT; refreshMapList(); } 
    
                    else if (uiRenderer.btnExitMenu.equals(clicked)) currentState = GameState.EXIT_CONFIRM; 
    
                }
    
                else if (currentState == GameState.CHARACTER_SELECT) {
    
                    Point relP = new Point(x, y);
    
                    boolean actionTaken = false;
    
                    
    
                    if (uiRenderer.charButtons != null) {
    
                        for(int i=0; i<uiRenderer.charButtons.length; i++) {
    
                            if (uiRenderer.charButtons[i] != null && uiRenderer.charButtons[i].contains(relP)) {
    
                                selectedCharIndex = i;
    
                                return;
    
                            }
    
                        }
    
                    }
    
                    
    
                                if (uiRenderer.btnCharStart != null && uiRenderer.btnCharStart.contains(relP)) {
    
                    
    
                                    startNewGame();
    
                    
    
                                    return;
    
                    
    
                                }
    
                    
    
                                
    
                    
    
                                // Logic for "Always Open" list
    
                    
    
                                if (uiRenderer.ribbonDropdownRects != null) {
    
                    
    
                                     for(int i=0; i<uiRenderer.ribbonDropdownRects.length; i++) {
    
                    
    
                                         if (uiRenderer.ribbonDropdownRects[i] != null && uiRenderer.ribbonDropdownRects[i].contains(relP)) {
    
                    
    
                                             uiRenderer.selectedRibbonIndex = i;
    
                    
    
                                             selectedColorIndex = i;
    
                    
    
                                             Assets.updateAvatars(selectedColorIndex);
    
                    
    
                                             // Keep list open
    
                    
    
                                             return; 
    
                    
    
                                         }
    
                    
    
                                     }
    
                    
    
                                }
    
                }
    
                else if (currentState == GameState.EXIT_CONFIRM) { 
    
                    if (uiRenderer.btnExitYes.equals(clicked)) System.exit(0); 
    
                    else if (uiRenderer.btnExitNo.equals(clicked)) currentState = GameState.MENU; 
    
                }
    
                else if (currentState == GameState.EXIT_GAME_CONFIRM) { 
    
                    if (uiRenderer.btnExitGameConfirmYes.equals(clicked)) System.exit(0); 
    
                    else if (uiRenderer.btnExitGameConfirmNo.equals(clicked)) currentState = GameState.PAUSED; 
    
                }
    
                else if (currentState == GameState.PAUSED) { 
    
                    if (uiRenderer.btnResume.equals(clicked)) currentState = GameState.PLAYING; 
    
                    else if (uiRenderer.btnSaveMenu.equals(clicked)) { currentState = GameState.PAUSE_SAVE; editingSaveSlot = -1; } 
    
                    else if (uiRenderer.btnLoadMenu.equals(clicked)) { currentState = GameState.PAUSE_LOAD; editingSaveSlot = -1; } 
    
                    else if (uiRenderer.btnMainMenu.equals(clicked)) currentState = GameState.MENU; 
    
                    else if (uiRenderer.btnExitPause.equals(clicked)) currentState = GameState.EXIT_GAME_CONFIRM; 
    
                }
    
                else if (currentState == GameState.GAMEOVER) { 
    
                    if (uiRenderer.btnRestart.equals(clicked)) startNewGame(); 
    
                    else if (uiRenderer.btnMainMenu.equals(clicked)) currentState = GameState.MENU; 
    
                }
    
                else if (currentState == GameState.SHOP) {
    
                    for(int i=0; i<merchantStock.size(); i++) if(clicked.equals(uiRenderer.shopBuyRects[i])) { Item it = merchantStock.get(i); int cost = it.specificType == Item.Specific.HEALTH_POTION ? 50 : it.value * 5; if(player.getGold() >= cost) { if(player.addItem(new Item(it.name, it.type, it.specificType, it.value))) { player.addGold(-cost); effects.add(new VisualEffect(player.visualX, player.visualY, "BOUGHT!", Color.GREEN, 30)); } else effects.add(new VisualEffect(player.visualX, player.visualY, "FULL!", Color.RED, 30)); } else effects.add(new VisualEffect(player.visualX, player.visualY, "NEED GOLD!", Color.RED, 30)); return; }
    
                    for(int i=0; i<20; i++) if(clicked.equals(uiRenderer.shopSellRects[i])) { Item it = (i < 15) ? player.backpack[i] : player.inventory[i-15]; if(it != null) { int val = it.specificType == Item.Specific.WOOD || it.specificType == Item.Specific.GRAIN ? 2 : (it.specificType == Item.Specific.STONE ? 3 : (it.specificType == Item.Specific.BREAD ? 5 : (it.specificType == Item.Specific.HEALTH_POTION ? 15 : it.value * 4))); player.addGold(val); if(i < 15) player.backpack[i] = null; else player.inventory[i-15] = null; effects.add(new VisualEffect(player.visualX, player.visualY, "+" + val + " G", Color.YELLOW, 30)); } return; }
    
                } 
    
                else if (currentState == GameState.CRAFTING) {
    
                    for(int i=0; i<uiRenderer.craftButtons.size(); i++) if(clicked.equals(uiRenderer.craftButtons.get(i).rect)) { UIRenderer.CraftingButton cb = uiRenderer.craftButtons.get(i); if(player.hasItem(Item.Specific.WOOD, cb.woodCost) && player.hasItem(Item.Specific.STONE, cb.stoneCost) && player.hasItem(Item.Specific.GRAIN, cb.grainCost)) { Item res = new Item(cb.name, cb.type == Item.Specific.BREAD ? Item.Type.CONSUMABLE : Item.Type.BUILDING, cb.type, cb.type == Item.Specific.BREAD ? 30 : 0); if(player.addItem(res)) { player.consumeItems(Item.Specific.WOOD, cb.woodCost); player.consumeItems(Item.Specific.STONE, cb.stoneCost); player.consumeItems(Item.Specific.GRAIN, cb.grainCost); effects.add(new VisualEffect(player.visualX, player.visualY, "CRAFTED!", Color.GREEN, 30)); } else effects.add(new VisualEffect(player.visualX, player.visualY, "FULL!", Color.RED, 30)); } return; }
    
                } 
    
                else if (currentState == GameState.EDITOR_SELECT) {
    
                    if (showDeleteConfirm) { if (uiRenderer.btnConfirmYes.equals(clicked)) { if (mapToDeleteIndex >= 0) mapFiles.get(mapToDeleteIndex).delete(); refreshMapList(); showDeleteConfirm = false; } else if (uiRenderer.btnConfirmNo.equals(clicked)) showDeleteConfirm = false; return; }
    
                    if (uiRenderer.btnCreateMap.equals(clicked)) { 
    
                        isCreatingMap = true; 
    
                        currentTypingText.setLength(0); 
    
                        repaint(); 
    
                    }
    
                    else if (uiRenderer.playMapBtns != null) {
    
                        for(int i=0; i<mapFiles.size(); i++) {
    
                            if(clicked.equals(uiRenderer.playMapBtns[i])) { loadMapAndPlay(mapFiles.get(i)); return; }
    
                            if(clicked.equals(uiRenderer.editMapBtns[i])) { loadMapForEditing(mapFiles.get(i)); return; }
    
                            if(clicked.equals(uiRenderer.delMapBtns[i])) { showDeleteConfirm = true; mapToDeleteIndex = i; return; }
    
                        }
    
                    }
    
                } 
    
                else if (currentState == GameState.MENU_LOAD || currentState == GameState.PAUSE_SAVE || currentState == GameState.PAUSE_LOAD) {
    
                    if (showSaveDeleteConfirm) {
    
                        if (uiRenderer.btnConfirmYes.equals(clicked)) { 
    
                            if (saveToDeleteSlot != -1) deleteSave(saveToDeleteSlot); 
    
                            showSaveDeleteConfirm = false; 
    
                        } 
    
                        else if (uiRenderer.btnConfirmNo.equals(clicked)) showSaveDeleteConfirm = false; 
    
                        return; 
    
                    }
    
                    if (showLoadConfirm) {
    
                        if (uiRenderer.btnConfirmYes.equals(clicked)) {
    
                            if (loadTargetSlot != -1) loadGame(loadTargetSlot);
    
                            showLoadConfirm = false;
    
                        }
    
                        else if (uiRenderer.btnConfirmNo.equals(clicked)) showLoadConfirm = false;
    
                        return;
    
                    }
    
                    if (uiRenderer.btnSlot1.equals(clicked)) { 
    
                        if (currentState == GameState.PAUSE_SAVE) { editingSaveSlot = 1; currentTypingText.setLength(0); if(checkSaveExists(1)) currentTypingText.append(getSaveInfo(1).name); repaint(); }
    
                        else if (checkSaveExists(1)) { showLoadConfirm = true; loadTargetSlot = 1; } 
    
                        else { menuMessage = "EMPTY SLOT!"; menuMessageTimer = 60; } 
    
                    }
    
                    else if (uiRenderer.btnDelete1.equals(clicked)) { showSaveDeleteConfirm = true; saveToDeleteSlot = 1; }
    
                    else if (uiRenderer.btnSlot2.equals(clicked)) { 
    
                        if (currentState == GameState.PAUSE_SAVE) { editingSaveSlot = 2; currentTypingText.setLength(0); if(checkSaveExists(2)) currentTypingText.append(getSaveInfo(2).name); repaint(); }
    
                        else if (checkSaveExists(2)) { showLoadConfirm = true; loadTargetSlot = 2; } 
    
                        else { menuMessage = "EMPTY SLOT!"; menuMessageTimer = 60; } 
    
                    }
    
                    else if (uiRenderer.btnDelete2.equals(clicked)) { showSaveDeleteConfirm = true; saveToDeleteSlot = 2; }
    
                    else if (uiRenderer.btnSlot3.equals(clicked)) { 
    
                        if (currentState == GameState.PAUSE_SAVE) { editingSaveSlot = 3; currentTypingText.setLength(0); if(checkSaveExists(3)) currentTypingText.append(getSaveInfo(3).name); repaint(); }
    
                        else if (checkSaveExists(3)) { showLoadConfirm = true; loadTargetSlot = 3; } 
    
                        else { menuMessage = "EMPTY SLOT!"; menuMessageTimer = 60; } 
    
                    }
    
                    else if (uiRenderer.btnDelete3.equals(clicked)) { showSaveDeleteConfirm = true; saveToDeleteSlot = 3; }
    
                    else if (uiRenderer.btnBack.equals(clicked)) {
    
                        currentState = (currentState == GameState.MENU_LOAD) ? GameState.MENU : GameState.PAUSED;
    
                        editingSaveSlot = -1;
    
                    }
    
                }         
            }
    public void handleMouseWheel(int rotation) {
        if (currentState == GameState.EDITOR_SELECT && mapFiles != null) {
            int itemsH = mapFiles.size() * 60; if (itemsH > LIST_VIEW_H) { listScrollY += rotation * 30; if (listScrollY < 0) listScrollY = 0; if (listScrollY > itemsH - LIST_VIEW_H) listScrollY = itemsH - LIST_VIEW_H; repaint(); }
        } else if (currentState == GameState.EDITOR_EDIT) { if (rotation < 0) editorScale = Math.min(editorScale + 0.1f, 2.0f); else editorScale = Math.max(editorScale - 0.1f, 0.5f); repaint(); }
    }

    public void generateShopStock() {
        merchantStock = new ArrayList<>(); merchantStock.add(new Item("Health Potion", Item.Type.CONSUMABLE, Item.Specific.HEALTH_POTION, 100));
        List<Item> pool = new ArrayList<>(); pool.add(new Item("Iron Sword", Item.Type.WEAPON, Item.Specific.SWORD, 70)); pool.add(new Item("Iron Helm", Item.Type.ARMOR, Item.Specific.HELMET, 5)); pool.add(new Item("Iron Chest", Item.Type.ARMOR, Item.Specific.CHESTPLATE, 8)); pool.add(new Item("Iron Pants", Item.Type.ARMOR, Item.Specific.PANTS, 4)); pool.add(new Item("Iron Boots", Item.Type.ARMOR, Item.Specific.BOOTS, 3)); pool.add(new Item("Golden Axe", Item.Type.TOOL, Item.Specific.AXE, 15, 1)); pool.add(new Item("Golden Pick", Item.Type.TOOL, Item.Specific.PICKAXE, 12, 1));
        java.util.Collections.shuffle(pool);
        for(int i=0; i<pool.size(); i++) if(pool.get(i).type == Item.Type.ARMOR) { merchantStock.add(pool.get(i)); pool.remove(i); break; }
        for(int i=0; i<pool.size(); i++) if(pool.get(i).type == Item.Type.TOOL || pool.get(i).type == Item.Type.WEAPON) { merchantStock.add(pool.get(i)); pool.remove(i); break; }
        int needed = 4 + (int)(Math.random() * 2) - merchantStock.size() + 1; for(int i=0; i<needed && !pool.isEmpty(); i++) merchantStock.add(pool.remove(0));
    }
    
    public void refreshMapList() { File f = new File("maps"); if (!f.exists()) f.mkdir(); File[] fs = f.listFiles((dir, name) -> name.endsWith(".map")); mapFiles = (fs != null) ? new ArrayList<>(Arrays.asList(fs)) : new ArrayList<>(); }
    
    private void loadMapAndPlay(File file) {
        selectedMapFile = file;
        currentState = GameState.CHARACTER_SELECT;
    }
    
    private void loadMapForEditing(File file) { try { FileInputStream fis = new FileInputStream(file); ObjectInputStream ois = new ObjectInputStream(fis); editorMap = (WorldMap) ois.readObject(); ois.close(); currentMapName = file.getName().replace(".map", ""); currentState = GameState.EDITOR_EDIT; editorCamX = 0; editorCamY = 0; editorMapModified = false; } catch (Exception e) { e.printStackTrace(); } }

    private void saveMap(String name) { try { File f = new File("maps"); if (!f.exists()) f.mkdir(); FileOutputStream fos = new FileOutputStream("maps/"+name+".map"); ObjectOutputStream oos = new ObjectOutputStream(fos); oos.writeObject(editorMap); oos.close(); refreshMapList(); effects.add(new VisualEffect(getWidth()-20, getHeight()-140, "MAP SAVED: " + name)); editorMapModified = false; } catch (Exception e) { e.printStackTrace(); } }

    private void startLoading(Runnable loadTask) {
        currentState = GameState.LOADING; repaint(); loadingProgress = 0; tickCounter = 0; loadingTip = TIPS[(int)(Math.random() * TIPS.length)];
        int rand = (int)(Math.random() * 6);
        if (rand == 0) { loadingScenario = 0; loadingEnemy = null; }
        else {
            loadingScenario = 1; Enemy.Type type = Enemy.Type.ZOMBIE;
            switch(rand) { case 1: type = Enemy.Type.ZOMBIE; break; case 2: type = Enemy.Type.SKELETON; break; case 3: type = Enemy.Type.RAT; break; case 4: type = Enemy.Type.WITCH; break; case 5: type = Enemy.Type.HUNTER; break; }
            loadingEnemy = new Enemy(type, 0, 0); loadingEnemy.visualX = getWidth() + 50; loadingEnemy.visualY = getHeight() / 2 + 70;
        }
        loadingDead = false; loadingVy = 0; loadingPlayer = new Player("Loading...", 0, 0); loadingPlayer.visualX = -100; loadingPlayer.visualY = getHeight() / 2 + 70; 
        loadingPlayer.armor[0] = new Item("Iron Helm", Item.Type.ARMOR, Item.Specific.HELMET, 0); loadingPlayer.armor[1] = new Item("Iron Chest", Item.Type.ARMOR, Item.Specific.CHESTPLATE, 0); loadingPlayer.armor[2] = new Item("Iron Legs", Item.Type.ARMOR, Item.Specific.PANTS, 0); loadingPlayer.armor[3] = new Item("Iron Boots", Item.Type.ARMOR, Item.Specific.BOOTS, 0);
        Item sword = new Item("Golden Sword", Item.Type.WEAPON, Item.Specific.SWORD, 0); sword.rarityBonus = 1; loadingPlayer.inventory[0] = sword; loadingPlayer.selectedSlot = 0;
        new Thread(() -> { try { for (int i = 0; i <= 40; i++) { loadingProgress = i; Thread.sleep(40 + (int)(Math.random() * 20)); } loadTask.run(); for (int i = 41; i <= 100; i++) { loadingProgress = i; Thread.sleep(20); } while (loadingDead) Thread.sleep(100); Thread.sleep(500); currentState = GameState.PLAYING; } catch (Exception e) { e.printStackTrace(); } }).start();
    }

    private void placeEditorTile(int x, int y) {
        if (editorMap == null) return; int wx = (int)(x / editorScale + editorCamX), wy = (int)(y / editorScale + editorCamY), c = wx / TILE_SIZE, r = wy / TILE_SIZE;
        if(c >= 0 && c < editorMap.cols && r >= 0 && r < editorMap.rows) {
            Object obj = editorPaletteObjects[editorSelectedTileIndex];
            if ("ERASER".equals(obj)) editorMap.grid[r][c] = null; else if ("WATER".equals(obj)) editorMap.grid[r][c] = "WATER"; else if (obj instanceof WorldMap.PlayerStart) { for(int yy=0; yy<editorMap.rows; yy++) for(int xx=0; xx<editorMap.cols; xx++) if(editorMap.grid[yy][xx] instanceof WorldMap.PlayerStart) editorMap.grid[yy][xx] = null; editorMap.grid[r][c] = new WorldMap.PlayerStart(); } 
            else if (obj instanceof Tree) editorMap.grid[r][c] = new Tree(Enums.Quality.COMMON); else if (obj instanceof Rock) editorMap.grid[r][c] = new Rock(Enums.Quality.COMMON); else if (obj instanceof Grain) editorMap.grid[r][c] = new Grain(Enums.Quality.COMMON); else if (obj instanceof Building) editorMap.grid[r][c] = new Building(((Building)obj).type); else if (obj instanceof Enemy) editorMap.grid[r][c] = new Enemy(((Enemy)obj).type, c, r); else if (obj instanceof Vendor) editorMap.grid[r][c] = new Vendor(); else if (obj instanceof WorldMap.Campfire) editorMap.grid[r][c] = new WorldMap.Campfire(); else if (obj instanceof WorldMap.Tent) editorMap.grid[r][c] = new WorldMap.Tent();
            editorMapModified = true;
        }
    }
    
    private boolean isMouseOnEditorUI(int x, int y) { 
        if (showExitEditorConfirm) return true;
        if (y > getHeight() - 120) return true; 
        Point p = new Point(x, y); 
        return (uiRenderer.btnSaveMap != null && uiRenderer.btnSaveMap.contains(p)) || (uiRenderer.btnCloseWindow != null && uiRenderer.btnCloseWindow.contains(p)); 
    }
}