import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;
import java.io.*;
import java.util.Arrays;

public class GamePanel extends JPanel implements KeyListener, ActionListener, MouseListener, MouseMotionListener, MouseWheelListener {
    
	private enum GameState { MENU, PLAYING, CRAFTING, INVENTORY, SHOP, PAUSED, GAMEOVER, MAP, EDITOR_SELECT, EDITOR_EDIT} // Adaugat MAP
	private GameState currentState = GameState.MENU;

    private WorldMap map;
    private Player player;
    private Timer gameLoop;
    
    // SCROLL VARIABLES
    private int listScrollY = 0;
    private final int LIST_VIEW_Y = 150;
    private final int LIST_VIEW_H = 500;
    
    private List<VisualEffect> effects;
    private List<Enemy> activeEnemies;
    
    private final int TILE_SIZE = 60;
    private float camX, camY;
    private int attackAnimFrame = 0;
    private int tickCounter = 0;
    
    private boolean keyW, keyA, keyS, keyD;
    private int moveTimer = 0; 
    private final int MOVE_SPEED_DELAY = 6; 
    private int interactionCooldown = 0; // NEW: Prevent instant re-interaction
    
     // EDITOR VARIABLES
     private List<String> createdMaps = new ArrayList<>(); // Lista numelor hartilor (simulat)
     private Rectangle btnCreateMap, btnLoadMap, btnSaveMap;
     private List<File> mapFiles = new ArrayList<>(); // Lista fisierelor gasite
     private WorldMap editorMap; // Harta pe care o editam
     private float editorCamX, editorCamY;
     private int editorSelectedTileIndex = 0; // Ce obiect punem jos
     
     // DELETE CONFIRMATION POPUP
     private boolean showDeleteConfirm = false;
     private int mapToDeleteIndex = -1;
     private Rectangle btnConfirmYes, btnConfirmNo;
     
     // Rectangles pentru lista de harti (Play, Edit, Delete pentru fiecare)
     private Rectangle[] playMapBtns;
     private Rectangle[] editMapBtns;
     private Rectangle[] delMapBtns;
     
         // EDITOR CAMERA CONTROLS
         private Rectangle btnCamUp, btnCamDown, btnCamLeft, btnCamRight;
         private boolean isMousePressed = false;
         
         // Paleta de obiecte pentru editor (Iconite)
         private Object[] editorPaletteObjects; 
         private Rectangle[] paletteRects;
         
         private String currentMapName = "Untitled"; // Numele hartii curente      
         
         // UI Elements
    private Rectangle btnCraftingOpen, btnInventoryOpen, btnMapOpen, btnCloseWindow; // Adaugat btnMapOpen
    private Rectangle btnStartGame, btnExitMenu, btnRestart, btnEditor;
    private Rectangle btnResume, btnMainMenu, btnExitPause;
    
    private List<CraftingButton> craftButtons;
    private List<ShopButton> shopButtons; // Butoane Shop
 // SHOP VARIABLES
    private List<Item> merchantStock; // Stocul generat random al negustorului
    private Rectangle[] shopBuyRects; // Zonele de click pentru cumparare (stanga)
    private Rectangle[] shopSellRects; // Zonele de click pentru vanzare (dreapta - inventarul tau)
    
    private Rectangle[] hotbarRects, backpackRects, armorRects;
    
    private Item draggingItem = null;
    private int dragSourceIndex = -1;
    private int dragSourceType = 0; 
    private boolean dragFromHotbar = false;
    private int mouseX, mouseY;

    public GamePanel() {
        this.setFocusable(true); 
        this.addKeyListener(this); 
        this.addMouseListener(this); 
        this.addMouseMotionListener(this);
        this.addMouseWheelListener(this); // NOU: Scroll Support
        
        this.setBackground(new Color(10, 10, 15)); 
        effects = new ArrayList<>(); // Initialize effects list here
        initUI();
        gameLoop = new Timer(16, this); gameLoop.start();
    }
    
    private void startNewGame() {
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
        keyW = keyA = keyS = keyD = false; 
        currentState = GameState.PLAYING;
        
    }

    private void initUI() {
        hotbarRects = new Rectangle[5];
        backpackRects = new Rectangle[15];
        armorRects = new Rectangle[4];
       
        // Crafting & Shop Items (Pastram codul tau)
        craftButtons = new ArrayList<>();
        craftButtons.add(new CraftingButton("Fountain", "HP Restore", 5, 3, 0, Item.Specific.FOUNTAIN));
        craftButtons.add(new CraftingButton("Monument", "+5 Dmg", 10, 5, 0, Item.Specific.MONUMENT));
        craftButtons.add(new CraftingButton("Bread", "Heal 30HP", 0, 0, 3, Item.Specific.BREAD));
       
        shopButtons = new ArrayList<>();
        shopButtons.add(new ShopButton("Iron Sword", "70 DMG", 150, new Item("Iron Sword", Item.Type.WEAPON, Item.Specific.SWORD, 70)));
        shopButtons.add(new ShopButton("Golden Axe", "Epic Drop++", 200, new Item("Golden Axe", Item.Type.TOOL, Item.Specific.AXE, 15, 1)));
        shopButtons.add(new ShopButton("Golden Pick", "Epic Drop++", 200, new Item("Golden Pick", Item.Type.TOOL, Item.Specific.PICKAXE, 12, 1)));
        shopButtons.add(new ShopButton("Health Potion", "Heal 100", 50, new Item("Potion", Item.Type.CONSUMABLE, Item.Specific.HEALTH_POTION, 100)));
        shopButtons.add(new ShopButton("Iron Helm", "DEF +5", 100, new Item("Iron Helm", Item.Type.ARMOR, Item.Specific.HELMET, 5)));
       
        // --- MENU BUTTONS ---
        int menuCenterX = 1024 / 2 - 120; 
        int menuStartY = 768 / 2;         
        
        btnStartGame = new Rectangle(menuCenterX, menuStartY, 240, 60);
        btnEditor    = new Rectangle(menuCenterX, menuStartY + 80, 240, 60);
        btnExitMenu  = new Rectangle(menuCenterX, menuStartY + 160, 240, 60);
        
        // --- EDITOR BUTTONS (AICI ERA PROBLEMA - LIPSEAU) ---
        // Butonul Create Map centrat jos
        btnCreateMap = new Rectangle(menuCenterX, 768 - 100, 240, 60); 
        // Buton Save Map dreapta sus
        btnSaveMap   = new Rectangle(750, 20, 140, 40);
        
        // --- OTHER UI ---
        btnRestart = new Rectangle(0, 0, 200, 50);
        btnResume = new Rectangle(0, 0, 250, 50);
        btnMainMenu = new Rectangle(0, 0, 250, 50);
        btnExitPause = new Rectangle(0, 0, 250, 50);

        // HUD Buttons
        int uiX = 1024 - 140; 
        int uiY = 768;        
        btnMapOpen = new Rectangle(uiX, uiY - 190, 120, 50);      
        btnCraftingOpen = new Rectangle(uiX, uiY - 130, 120, 50); 
        btnInventoryOpen = new Rectangle(uiX, uiY - 70, 120, 50); 
        
        // Close Button Holder
        btnCloseWindow = new Rectangle(0, 0, 40, 40);

        initEditorPalette();
        refreshMapList(); 
    }
    private void initEditorPalette() {
        editorPaletteObjects = new Object[] {
            "ERASER", 
            new WorldMap.PlayerStart(), // NOU: Spawn Point (index 1)
            "WATER",  
            new Tree(Enums.Quality.COMMON), 
            new Rock(Enums.Quality.COMMON), 
            new Grain(Enums.Quality.COMMON), 
            new Building(Building.Type.FOUNTAIN), 
            new Building(Building.Type.MONUMENT), 
            new Enemy(Enemy.Type.ZOMBIE, 0, 0), 
            new Enemy(Enemy.Type.SKELETON, 0, 0), 
            new Enemy(Enemy.Type.HUNTER, 0, 0), 
            new Vendor(), // Added Vendor
            new WorldMap.Campfire(), 
            new WorldMap.Tent() 
        };
        
        paletteRects = new Rectangle[editorPaletteObjects.length];
        int startX = 50;
        int startY = 768 - 90;
        for(int i=0; i<editorPaletteObjects.length; i++) {
            paletteRects[i] = new Rectangle(startX + i*65, startY, 55, 55); // Putin mai mici sa incapa
        }
    }
    @Override
    public void actionPerformed(ActionEvent e) {
         if (currentState == GameState.EDITOR_EDIT) {
            // Init buttons if needed
            int w = getWidth(); int h = getHeight();
            if (btnCamUp == null) {
                btnCamUp = new Rectangle(w/2 - 25, 10, 50, 40);
                btnCamDown = new Rectangle(w/2 - 25, h - 150, 50, 40); // Above palette
                btnCamLeft = new Rectangle(10, h/2 - 25, 40, 50);
                btnCamRight = new Rectangle(w - 50, h/2 - 25, 40, 50);
            }

            // Keyboard Movement
            if (keyW) editorCamY -= 15;
            if (keyS) editorCamY += 15;
            if (keyA) editorCamX -= 15;
            if (keyD) editorCamX += 15;
            
            // Mouse Button Movement (Click & Hold)
            if (isMousePressed) {
                if (btnCamUp.contains(mouseX, mouseY)) editorCamY -= 15;
                if (btnCamDown.contains(mouseX, mouseY)) editorCamY += 15;
                if (btnCamLeft.contains(mouseX, mouseY)) editorCamX -= 15;
                if (btnCamRight.contains(mouseX, mouseY)) editorCamX += 15;
            }

            // Update effects in editor too
            for (int i = 0; i < effects.size(); i++) { if (!effects.get(i).update()) { effects.remove(i); i--; } }

            repaint();
            return;
        }
        
        if (currentState == GameState.EDITOR_SELECT) {
             for (int i = 0; i < effects.size(); i++) { 
                 if (!effects.get(i).update()) { 
                     effects.remove(i); 
                     i--; 
                 } 
             }
             repaint();
             return;
        }
         
        if (currentState != GameState.PLAYING && currentState != GameState.INVENTORY && currentState != GameState.CRAFTING) { 
        	repaint(); 
        	return; 
        	}

        if (player == null) 
        	return;
        
        if (interactionCooldown > 0) 
        	interactionCooldown--; // NEW: Decrease cooldown
        
        tickCounter++; 
        if (attackAnimFrame > 0) 
        	attackAnimFrame--;
        if(currentState == GameState.PLAYING) 
        	updatePlayerMovement(); // Miscare doar in playing
        player.updateVisuals(TILE_SIZE);
        if (player.getHealth() <= 0) { 
        	currentState = GameState.GAMEOVER; 
        	repaint(); 
        	return; 
        	}
        
        // Level UP Anim
        if (player.levelUpAnim == 59) effects.add(new VisualEffect(player.visualX, player.visualY - 40, "LEVEL UP!", Color.YELLOW, 60));
        
        if (tickCounter % 2 == 0) { 
            int pX = player.x; int pY = player.y;
            for(int i=0; i<activeEnemies.size(); i++) {
                Enemy en = activeEnemies.get(i);
                if (Math.abs(en.x - pX) > 18 || Math.abs(en.y - pY) > 14) continue; 
                boolean attacked = en.updateAI(player, map);
                if (attacked) effects.add(new VisualEffect(player.visualX, player.visualY, "-" + (Math.max(1, en.attack - player.getDefense())), Color.RED, 25));
            }
        }
        updateCamera();
        for (int i = 0; i < effects.size(); i++) { if (!effects.get(i).update()) { effects.remove(i); i--; } }
        repaint();
    }
    
    // --- 1. LOGICA JOCULUI (CONTINUARE) ---

    private void updatePlayerMovement() {
        if (moveTimer > 0) { moveTimer--; return; }
        
        int nx = player.x; 
        int ny = player.y; 
        boolean tryingToMove = false;

        if (keyW) { ny--; tryingToMove = true; }
        else if (keyS) { ny++; tryingToMove = true; }
        else if (keyA) { nx--; tryingToMove = true; }
        else if (keyD) { nx++; tryingToMove = true; }

        if (tryingToMove) {
            if(nx >= 0 && nx < map.cols && ny >= 0 && ny < map.rows) {
                Object t = map.getEntityAt(nx, ny);
                
                if("WATER".equals(t)) { 
                    // Blocat de apa
                } 
                else if(t == null || t instanceof WorldMap.Tent) { 
                    // Miscare libera
                    player.x = nx; 
                    player.y = ny; 
                    moveTimer = MOVE_SPEED_DELAY; 
                    
                    map.updateExploration(nx, ny, 8); // NOU: Descoperă raza de 8 pătrate
                } 
                else { 
                    // Interactiune
                    interactWith(t, nx, ny); 
                    moveTimer = 15; 
                }
            }
        }
    }

    private void interactWith(Object t, int nx, int ny) {
    	if (interactionCooldown > 0) return; // NEW: Stop if cooling down
    	
    	Item item = player.getSelectedItem();
        
        if (t instanceof Vendor) {
            currentState = GameState.SHOP; // Deschide Shop
        }
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
                
                // --- GOLD DROP ---
                int goldDrop = 5 + (int)(Math.random() * 11);
                player.addGold(goldDrop);
                effects.add(new VisualEffect(nx*TILE_SIZE, ny*TILE_SIZE - 20, "+" + goldDrop + " G", new Color(255, 215, 0), 40));

                // --- ITEM DROP (Rare/Legendary) ---
                if(en.type == Enemy.Type.HUNTER && Math.random() < 0.35) { // 35% sansa
                    Item drop = null;
                    double r = Math.random();
                    if(r < 0.25) drop = new Item("Iron Helm", Item.Type.ARMOR, Item.Specific.HELMET, 2);
                    else if(r < 0.5) drop = new Item("Iron Chest", Item.Type.ARMOR, Item.Specific.CHESTPLATE, 5);
                    else if(r < 0.75) drop = new Item("Iron Legs", Item.Type.ARMOR, Item.Specific.PANTS, 3);
                    else drop = new Item("Iron Boots", Item.Type.ARMOR, Item.Specific.BOOTS, 1);
                    
                    if(player.addItem(drop)) {
                        effects.add(new VisualEffect(player.visualX, player.visualY - 40, "LEGENDARY DROP!", new Color(255, 215, 0), 60));
                    }
                }
                player.x = nx; player.y = ny; 
            }
        } 
        else if (t instanceof ResourceEntity) {
             if (t instanceof Tree && (item == null || item.specificType != Item.Specific.AXE)) { effects.add(new VisualEffect(player.visualX, player.visualY, "Need AXE!", Color.RED, 20)); return; }
             if (t instanceof Rock && (item == null || item.specificType != Item.Specific.PICKAXE)) { effects.add(new VisualEffect(player.visualX, player.visualY, "Need PICKAXE!", Color.RED, 20)); return; }
             
             ResourceEntity res = (ResourceEntity)t; 
             player.collectResource(res);
             
             String txt = "+" + res.getQuantity(); 
             Color c = Color.GREEN;
             if(res.getQuality() == Enums.Quality.RARE) { txt += " RARE"; c = Color.CYAN; } 
             if(res.getQuality() == Enums.Quality.EPIC) { txt += " EPIC"; c = Color.MAGENTA; }
             effects.add(new VisualEffect(nx*TILE_SIZE, ny*TILE_SIZE, txt, c, 30));
             
             map.removeEntityAt(nx, ny); 
             player.x = nx; player.y = ny; 
             attackAnimFrame = 10;
        }
    }

    private void useCurrentItem(int x, int y) {
        // --- 1. RIDICARE CLADIRE (Transfer Cooldown -> Item) ---
        Object entityUnderfoot = map.getEntityAt(x, y);
        
        if (entityUnderfoot instanceof Building) {
            Building b = (Building) entityUnderfoot;
            
            Item buildingItem;
            if (b.type == Building.Type.FOUNTAIN) buildingItem = new Item("Fountain", Item.Type.BUILDING, Item.Specific.FOUNTAIN, 0);
            else buildingItem = new Item("Monument", Item.Type.BUILDING, Item.Specific.MONUMENT, 0);
            
            // TRANSFERAM TIMPUL
            buildingItem.savedLastUsedTime = b.lastUsedTime;
            
            if (player.addItem(buildingItem)) {
                map.removeEntityAt(x, y);
                effects.add(new VisualEffect(player.visualX, player.visualY, "Picked Up!", Color.GREEN, 30));
            } else {
                effects.add(new VisualEffect(player.visualX, player.visualY, "Full!", Color.RED, 30));
            }
            return;
        }

        // --- 2. PLASARE ITEM (Transfer Cooldown -> Cladire) ---
        Item item = player.getSelectedItem();
        if(item == null) return;
        
        if(item.type == Item.Type.BUILDING) {
            if(map.getEntityAt(x, y) == null) {
                Building.Type bType = (item.specificType == Item.Specific.FOUNTAIN) ? Building.Type.FOUNTAIN : Building.Type.MONUMENT;
                Building newB = new Building(bType);
                
                // RESTAURAM TIMPUL
                if (item.savedLastUsedTime > 0) {
                    newB.lastUsedTime = item.savedLastUsedTime;
                }
                
                map.setEntityAt(x, y, newB);
                player.removeItem(player.selectedSlot); 
                effects.add(new VisualEffect(x*TILE_SIZE, y*TILE_SIZE, "Built!", Color.GREEN, 30));
            } else { 
                effects.add(new VisualEffect(x*TILE_SIZE, y*TILE_SIZE, "Blocked!", Color.RED, 20)); 
            }
        }
        else if (item.type == Item.Type.CONSUMABLE) {
            if (item.specificType == Item.Specific.BREAD || item.specificType == Item.Specific.HEALTH_POTION) {
                if (player.getHealth() < player.getMaxHealth()) {
                    player.heal(item.value); 
                    player.removeItem(player.selectedSlot);
                    effects.add(new VisualEffect(player.visualX, player.visualY, "+" + item.value + " HP", Color.GREEN, 30));
                } else {
                    effects.add(new VisualEffect(player.visualX, player.visualY, "Health Full!", Color.WHITE, 20));
                }
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

    // --- 2. DESENARE (PAINT COMPONENT) ---

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g); 
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        // 1. Daca suntem in MENIU
        if (currentState == GameState.MENU) { 
            drawModernMenu(g2); 
            return; // STOP AICI
        }

        // 2. Daca suntem in EDITOR SELECT (Aici era problema ta)
        if (currentState == GameState.EDITOR_SELECT) {
            drawEditorSelectMenu(g2);
            return; // STOP AICI - Nu mai incerca sa desenezi lumea jocului!
        }

        // 3. Daca suntem in EDITOR EDIT (Plasare obiecte)
        if (currentState == GameState.EDITOR_EDIT) {
            drawEditorInterface(g2);
            return; // STOP AICI
        }

        // --- DE AICI IN JOS E DOAR PENTRU JOCUL PROPRIU-ZIS ---
        // Daca harta sau jucatorul sunt null (nu s-a dat Start Game), nu desenam nimic ca sa evitam eroarea
        if (map == null || player == null) return;

        g2.translate(-camX, -camY);
        drawWorld(g2); // Aici dadea eroare pentru ca ajungea aici fara sa trebuiasca
        drawPlayer(g2);
        for(VisualEffect ve : effects) ve.draw(g2);
        g2.translate(camX, camY);
        
        if (currentState != GameState.MENU) drawHUD(g2); 

        // Meniuri suprapuse peste joc
        if (currentState == GameState.CRAFTING) drawCraftingMenu(g2);
        if (currentState == GameState.SHOP) drawShopMenu(g2); 
        if (currentState == GameState.INVENTORY) drawInventoryMenu(g2);
        if (currentState == GameState.MAP) drawMapScreen(g2);
        if (currentState == GameState.PAUSED) drawPauseMenu(g2);
        if (currentState == GameState.GAMEOVER) drawGameOverScreen(g2);
        
        if (draggingItem != null) {
            drawItemIcon(g2, draggingItem, mouseX - 30, mouseY - 30, 60);
            drawItemQuantity(g2, draggingItem, mouseX - 30, mouseY - 30, 60);
        }

        if (currentState != GameState.MENU && currentState != GameState.GAMEOVER) {
            drawHoverTooltip(g2);
        }
    }
    // --- 3. METODELE DE DESENARE UI (AICI ERA PROBLEMA) ---

    private void drawCraftingMenu(Graphics2D g2) {
        int w = getWidth(); int h = getHeight();
        g2.setColor(new Color(0,0,0,200)); g2.fillRect(0,0,w,h);
        
        int cx = w/2 - 250; int cy = h/2 - 200;
        g2.setColor(new Color(30, 30, 35)); g2.fillRoundRect(cx, cy, 500, 400, 20, 20);
        g2.setColor(new Color(80, 80, 100)); g2.setStroke(new BasicStroke(3)); g2.drawRoundRect(cx, cy, 500, 400, 20, 20);
        g2.setColor(Color.WHITE); g2.setFont(new Font("Arial", Font.BOLD, 24)); g2.drawString("CRAFTING STATION", cx+130, cy+40);
        drawCloseButton(g2, cx, cy);

        int yOff = 80;
        for(CraftingButton btn : craftButtons) {
            btn.rect = new Rectangle(cx+50, cy+yOff, 400, 80);
            if(btn.rect.contains(mouseX, mouseY)) g2.setColor(new Color(60,60,70)); else g2.setColor(new Color(40,40,45));
            g2.fillRoundRect(btn.rect.x, btn.rect.y, btn.rect.width, btn.rect.height, 10, 10);
            
            if(btn.type == Item.Specific.FOUNTAIN) IconRenderer.drawFountain(g2, btn.rect.x+10, btn.rect.y+10, 60);
            else if(btn.type == Item.Specific.MONUMENT) IconRenderer.drawMonument(g2, btn.rect.x+10, btn.rect.y+10, 60);
            else if(btn.type == Item.Specific.BREAD) IconRenderer.drawBread(g2, btn.rect.x+10, btn.rect.y+10, 60);
            
            g2.setColor(Color.ORANGE); g2.setFont(new Font("Arial", Font.BOLD, 18)); g2.drawString(btn.name, btn.rect.x+80, btn.rect.y+30);
            g2.setColor(Color.LIGHT_GRAY); g2.setFont(new Font("Arial", Font.PLAIN, 14)); g2.drawString(btn.desc, btn.rect.x+80, btn.rect.y+50);
            
            boolean affordable = player.countItem(Item.Specific.WOOD) >= btn.woodCost && player.countItem(Item.Specific.STONE) >= btn.stoneCost && player.countItem(Item.Specific.GRAIN) >= btn.grainCost;
            if(affordable) g2.setColor(Assets.TEXT_GREEN); else g2.setColor(Assets.TEXT_RED);
            
            String cost = "Cost: ";
            if(btn.woodCost > 0) cost += btn.woodCost + " Wood  ";
            if(btn.stoneCost > 0) cost += btn.stoneCost + " Stone  ";
            if(btn.grainCost > 0) cost += btn.grainCost + " Grain";
            g2.drawString(cost, btn.rect.x+80, btn.rect.y+70);
            yOff += 90;
        }
    }

    private void drawShopMenu(Graphics2D g2) {
        int w = getWidth(); int h = getHeight();
        g2.setColor(new Color(0,0,0,220)); g2.fillRect(0,0,w,h);
        
        int cx = w/2; int cy = h/2;
        int panelW = 900; int panelH = 550;
        int px = cx - panelW/2; int py = cy - panelH/2;
        
        // Fundal General
        g2.setColor(new Color(40, 35, 30)); 
        g2.fillRoundRect(px, py, panelW, panelH, 20, 20);
        g2.setColor(new Color(150, 120, 50)); // Rama Aurie
        g2.setStroke(new BasicStroke(4));
        g2.drawRoundRect(px, py, panelW, panelH, 20, 20);
        
        // Titlu si Bani
        g2.setColor(Color.WHITE); g2.setFont(new Font("Arial", Font.BOLD, 30));
        g2.drawString("TRADING POST", px + 350, py + 40);
        
        g2.setColor(new Color(255, 215, 0));
        g2.setFont(new Font("Arial", Font.BOLD, 22));
        String goldTxt = "Gold: " + player.getGold();
        int goldW = g2.getFontMetrics().stringWidth(goldTxt);
        g2.drawString(goldTxt, px + panelW - goldW - 40, py + 50);
        
        drawCloseButton(g2, px + panelW/2 - 25, py);

        // --- COLOANA STANGA (MERCHANT - BUY) ---
        g2.setColor(Color.LIGHT_GRAY); g2.setFont(new Font("Arial", Font.BOLD, 18));
        g2.drawString("MERCHANT STOCK (BUY)", px + 80, py + 80);
        
        int startY = py + 100;
        for(int i=0; i<merchantStock.size(); i++) {
            Item item = merchantStock.get(i);
            int itemCost = item.value * 5; // Pret cumparare (mai scump decat vanzarea)
            if (item.specificType == Item.Specific.HEALTH_POTION) itemCost = 50; // Pret fix potiune
            
            shopBuyRects[i] = new Rectangle(px + 40, startY + i*70, 380, 60);
            Rectangle r = shopBuyRects[i];
            
            // Hover effect
            if(r.contains(mouseX, mouseY)) g2.setColor(new Color(70, 60, 50)); 
            else g2.setColor(new Color(50, 45, 40));
            g2.fillRoundRect(r.x, r.y, r.width, r.height, 10, 10);
            g2.setColor(Color.GRAY); g2.drawRoundRect(r.x, r.y, r.width, r.height, 10, 10);
            
            drawItemIcon(g2, item, r.x + 5, r.y + 5, 50);
            
            g2.setColor(Color.WHITE); g2.setFont(new Font("Arial", Font.BOLD, 16));
            g2.drawString(item.name, r.x + 65, r.y + 25);
            
            // Pret
            if (player.getGold() >= itemCost) g2.setColor(Color.GREEN); else g2.setColor(Color.RED);
            g2.drawString(itemCost + " G", r.x + 300, r.y + 35);
        }

        // --- COLOANA DREAPTA (PLAYER - SELL) ---
        g2.setColor(Color.LIGHT_GRAY); g2.setFont(new Font("Arial", Font.BOLD, 18));
        g2.drawString("YOUR INVENTORY (SELL)", px + 500, py + 80);
        
        // Desenam inventarul ca o grila (Backpack + Hotbar)
        shopSellRects = new Rectangle[20]; // 15 backpack + 5 hotbar
        int gridStartX = px + 480;
        int gridStartY = py + 100;
        
        for(int i=0; i<20; i++) {
            int col = i % 4; 
            int row = i / 4;
            int slotX = gridStartX + col * 95;
            int slotY = gridStartY + row * 80;
            
            shopSellRects[i] = new Rectangle(slotX, slotY, 70, 70);
            
            // Itemul curent (din backpack sau hotbar)
            Item it = (i < 15) ? player.backpack[i] : player.inventory[i-15];
            
            if (shopSellRects[i].contains(mouseX, mouseY)) g2.setColor(new Color(80, 80, 90));
            else g2.setColor(new Color(30, 30, 35));
            g2.fillRoundRect(slotX, slotY, 70, 70, 10, 10);
            g2.setColor(Color.GRAY); g2.drawRoundRect(slotX, slotY, 70, 70, 10, 10);
            
            if (it != null) {
                drawItemIcon(g2, it, slotX+5, slotY+5, 60);
                drawItemQuantity(g2, it, slotX+5, slotY+5, 60);
                
                // Pret Vanzare (mic text verde)
                int sellPrice = getItemSellPrice(it);
                g2.setColor(new Color(100, 255, 100)); g2.setFont(new Font("Arial", Font.BOLD, 12));
                String priceTxt = "+" + sellPrice;
                g2.drawString(priceTxt, slotX + 35 - g2.getFontMetrics().stringWidth(priceTxt)/2, slotY + 65);
            }
        }
    }
    private void drawInventoryMenu(Graphics2D g2) {
        int w = getWidth(); int h = getHeight();
        g2.setColor(new Color(0,0,0,200)); g2.fillRect(0, 0, w, h); // Dark Overlay
       
        int cx = w/2 - 250; int cy = h/2 - 250;
        g2.setColor(new Color(40, 40, 45)); g2.fillRoundRect(cx, cy, 500, 450, 20, 20);
        g2.setColor(new Color(100, 100, 120)); g2.setStroke(new BasicStroke(3)); g2.drawRoundRect(cx, cy, 500, 450, 20, 20);
        g2.setColor(Color.WHITE); g2.setFont(new Font("Arial", Font.BOLD, 24)); g2.drawString("INVENTORY", cx+180, cy+40);
        drawCloseButton(g2, cx, cy);

        // --- ARMOR SLOTS (FIXED) ---
        int ax = cx + 30; int ay = cy + 80;
        String[] labels = {"Head", "Body", "Legs", "Feet"};
        
        for(int i=0; i<4; i++) {
            armorRects[i] = new Rectangle(ax, ay + i*85, 70, 70);
            
            // Background
            g2.setColor(new Color(30, 30, 35)); 
            g2.fillRoundRect(armorRects[i].x, armorRects[i].y, 70, 70, 10, 10);
            g2.setColor(Color.DARK_GRAY); 
            g2.drawRoundRect(armorRects[i].x, armorRects[i].y, 70, 70, 10, 10);
            
            // Logic for Empty Slot (Placeholder + Text)
            if (player.armor[i] == null) {
                // Draw outline/shadow
                IconRenderer.drawArmorPlaceholder(g2, armorRects[i].x, armorRects[i].y, 70, i);
                
                // Draw text ONLY if slot is empty
                g2.setColor(Color.GRAY); 
                g2.setFont(new Font("Arial", Font.PLAIN, 10)); 
                g2.drawString(labels[i], armorRects[i].x+20, armorRects[i].y+65);
            }

            // Logic for Drawing the Item
            // FIX: We only skip drawing if we are dragging FROM armor (type 2) AND indices match
            boolean isDraggingThisSlot = (draggingItem != null && dragSourceType == 2 && dragSourceIndex == i);
            
            if (!isDraggingThisSlot && player.armor[i] != null) {
                drawItemIcon(g2, player.armor[i], armorRects[i].x+5, armorRects[i].y+5, 60);
            }
        }

        // --- BACKPACK SLOTS ---
        int startX = cx + 130; int startY = cy + 80;
        for(int i=0; i<15; i++) {
            int col = i % 4; int row = i / 4;
            backpackRects[i] = new Rectangle(startX + col*85, startY + row*85, 70, 70);
            g2.setColor(new Color(20, 20, 20)); g2.fillRoundRect(backpackRects[i].x, backpackRects[i].y, 70, 70, 10, 10);
            g2.setColor(Color.GRAY); g2.setStroke(new BasicStroke(1)); g2.drawRoundRect(backpackRects[i].x, backpackRects[i].y, 70, 70, 10, 10);
            
            // Fix drag visibility logic here too
            boolean isDraggingThisSlot = (draggingItem != null && dragSourceType == 0 && dragSourceIndex == i);
            
            if (!isDraggingThisSlot) {
                Item item = player.backpack[i];
                if (item != null) { 
                    drawItemIcon(g2, item, backpackRects[i].x+5, backpackRects[i].y+5, 60); 
                    drawItemQuantity(g2, item, backpackRects[i].x+5, backpackRects[i].y+5, 60); 
                }
            }
        }
       
        // --- HOTBAR SLOTS ---
        int hbStartX = w/2 - 175; int hbStartY = h - 80;
        for(int i=0; i<5; i++) {
            Rectangle rect = new Rectangle(hbStartX + i*75, hbStartY, 70, 70);
            if (player.selectedSlot == i) { g2.setColor(new Color(255, 255, 0, 80)); g2.fillRoundRect(rect.x-3, rect.y-3, 76, 76, 12, 12); g2.setColor(Color.YELLOW); g2.setStroke(new BasicStroke(3)); } 
            else { g2.setColor(Assets.UI_DARK); g2.setStroke(new BasicStroke(1)); }
            g2.fillRoundRect(rect.x, rect.y, 70, 70, 10, 10); g2.setColor(Color.WHITE); g2.drawRoundRect(rect.x, rect.y, 70, 70, 10, 10);
           
            // Fix drag visibility logic here too
            boolean isDraggingThisSlot = (draggingItem != null && dragFromHotbar && dragSourceIndex == i);

            if (!isDraggingThisSlot) {
                Item item = player.inventory[i];
                if (item != null) { 
                    drawItemIcon(g2, item, rect.x+5, rect.y+5, 60); 
                    drawItemQuantity(g2, item, rect.x+5, rect.y+5, 60); 
                }
            }
            g2.setFont(new Font("Arial", Font.BOLD, 12)); IconRenderer.drawTextWithShadow(g2, String.valueOf(i+1), rect.x+5, rect.y+15);
        }
    }
    // --- RESTUL METODELOR DE DESENARE ---

    private void drawWorld(Graphics2D g2) {
        int startCol = Math.max(0, (int)(camX / TILE_SIZE)); int startRow = Math.max(0, (int)(camY / TILE_SIZE));
        int endCol = Math.min(map.cols, startCol + getWidth()/TILE_SIZE + 2); int endRow = Math.min(map.rows, startRow + getHeight()/TILE_SIZE + 2);
        for (int r = startRow; r < endRow; r++) {
            for (int c = startCol; c < endCol; c++) {
                int px = c * TILE_SIZE; int py = r * TILE_SIZE;
                if ((r+c)%2==0) g2.setColor(Assets.GRASS_1); else g2.setColor(Assets.GRASS_2); g2.fillRect(px, py, TILE_SIZE, TILE_SIZE);
                Object ent = map.getEntityAt(c, r);
                if (ent == null) 
                	continue;
                if (ent instanceof WorldMap.Campfire) {
                    IconRenderer.drawCampfire(g2, px, py, TILE_SIZE);
                }
                else if (ent instanceof WorldMap.Tent) {
                    IconRenderer.drawTent(g2, px, py, TILE_SIZE);
                }
                if ("WATER".equals(ent)) {
                    GradientPaint waterGp = new GradientPaint(px, py, Assets.WATER_LIGHT, px+TILE_SIZE, py+TILE_SIZE, Assets.WATER_DEEP); g2.setPaint(waterGp); g2.fillRect(px, py, TILE_SIZE, TILE_SIZE);
                    g2.setColor(new Color(255,255,255,40)); int wO = (int)(Math.sin((tickCounter + px)*0.1)*5); g2.drawLine(px+10, py+20+wO, px+30, py+20+wO);
                }
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
        Item item = player.getSelectedItem(); if (item != null) drawItemIcon(arm, item, x+30, y+15, 40, false); arm.dispose();
    }

    private void drawHUD(Graphics2D g2) {
        int w = getWidth(); 
        int h = getHeight(); 
        g2.setColor(Assets.UI_DARK); 
        g2.fillRoundRect(10, 10, 280, 100, 20, 20);
        g2.setColor(Assets.UI_BORDER); 
        g2.setStroke(new BasicStroke(2)); 
        g2.drawRoundRect(10, 10, 280, 100, 20, 20);
        g2.setFont(new Font("Arial", Font.BOLD, 16)); 
        IconRenderer.drawTextWithShadow(g2, "Level " + player.getLevel(), 25, 35);
        g2.setColor(Color.DARK_GRAY); 
        g2.fillRect(100, 22, 160, 12); 
        g2.setColor(Color.GREEN); 
        int xpW = (int)(160 * ((double)player.getXp() / player.getMaxXp())); 
        g2.fillRect(100, 22, xpW, 12);
        IconRenderer.drawHeart(g2, 25, 50, 28); 
        g2.setFont(new Font("Arial", Font.BOLD, 22)); 
        IconRenderer.drawTextWithShadow(g2, player.getHealth() + " / " + player.getMaxHealth(), 65, 72);
        g2.setFont(new Font("Arial", Font.BOLD, 16));
        g2.setColor(Color.ORANGE); 
        IconRenderer.drawTextWithShadow(g2, "DMG: " + player.getTotalDamage(), 25, 95);
        g2.setColor(Color.CYAN); 
        IconRenderer.drawTextWithShadow(g2, "DEF: " + player.getDefense(), 110, 95);
        g2.setColor(new Color(255, 215, 0)); 
        IconRenderer.drawTextWithShadow(g2, "$ " + player.getGold(), 190, 95);
        
        btnCraftingOpen = new Rectangle(w-140, h-130, 120, 50); drawSimpleButton(g2, btnCraftingOpen, "CRAFT [C]", (currentState==GameState.CRAFTING));
        btnInventoryOpen = new Rectangle(w-140, h-70, 120, 50); drawSimpleButton(g2, btnInventoryOpen, "INVENTORY [I]", (currentState==GameState.INVENTORY));
        btnMapOpen.x = w - 140; 
        btnMapOpen.y = h - 190;
        drawSimpleButton(g2, btnMapOpen, "MAP [M]", (currentState==GameState.MAP)); // NOU
        
        int startX = w/2 - 175; int startY = h - 80;
        for(int i=0; i<5; i++) {
            hotbarRects[i] = new Rectangle(startX + i*75, startY, 70, 70);
            if (player.selectedSlot == i) { g2.setColor(new Color(255, 255, 0, 80)); g2.fillRoundRect(hotbarRects[i].x-3, hotbarRects[i].y-3, 76, 76, 12, 12); g2.setColor(Color.YELLOW); g2.setStroke(new BasicStroke(3)); } 
            else { g2.setColor(Assets.UI_DARK); g2.setStroke(new BasicStroke(1)); }
            g2.fillRoundRect(hotbarRects[i].x, hotbarRects[i].y, 70, 70, 10, 10); g2.setColor(Color.WHITE); g2.drawRoundRect(hotbarRects[i].x, hotbarRects[i].y, 70, 70, 10, 10);
            if (draggingItem != null && dragFromHotbar && dragSourceIndex == i) continue;
            Item item = player.inventory[i];
            if (item != null) { drawItemIcon(g2, item, hotbarRects[i].x+5, hotbarRects[i].y+5, 60); drawItemQuantity(g2, item, hotbarRects[i].x+5, hotbarRects[i].y+5, 60); }
            g2.setFont(new Font("Arial", Font.BOLD, 12)); IconRenderer.drawTextWithShadow(g2, String.valueOf(i+1), hotbarRects[i].x+5, hotbarRects[i].y+15);
        }
    }

    private void drawModernMenu(Graphics2D g2) {
        int w = getWidth();
        int h = getHeight();

        // 1. Fundal
        GradientPaint gp = new GradientPaint(0, 0, new Color(15, 20, 40), 0, h, new Color(50, 30, 70));
        g2.setPaint(gp);
        g2.fillRect(0, 0, w, h);

        // 2. Titlu
        g2.setFont(new Font("Arial Black", Font.BOLD, 60));
        String t = "ULTIMATE SURVIVAL";
        
        // Umbra titlu
        g2.setColor(new Color(0, 0, 0, 100));
        g2.drawString(t, w / 2 - 350 + 5, h / 3 + 5);
        
        // Titlu principal
        g2.setColor(Color.CYAN);
        g2.drawString(t, w / 2 - 350, h / 3);

        // --- AICI ERA PROBLEMA: AM SCOS LINIILE CARE RESETAU POZITIA ---
        // (Liniile cu btnStartGame.x = ... si btnStartGame.y = ... au fost sterse)
        // Pozitia este setata o singura data in initUI() si ramane asa.

        // 3. Desenam butoanele (folosind pozitiile din initUI)
        drawMenuButton(g2, btnStartGame, "START GAME", new Color(0, 180, 80));
        drawMenuButton(g2, btnEditor, "MAP EDITOR", new Color(70, 70, 150));
        drawMenuButton(g2, btnExitMenu, "EXIT GAME", new Color(180, 50, 50));

        // 4. Versiune
        g2.setFont(new Font("Arial", Font.PLAIN, 12));
        g2.setColor(Color.GRAY);
        g2.drawString("v15.0 Complete", 10, h - 10);
    }
    private void drawPauseMenu(Graphics2D g2) { int w = getWidth(); int h = getHeight(); g2.setColor(new Color(0, 0, 0, 150)); g2.fillRect(0, 0, w, h); int boxW = 400; int boxH = 400; int cx = w/2 - boxW/2; int cy = h/2 - boxH/2; g2.setColor(Assets.UI_DARK); g2.fillRoundRect(cx, cy, boxW, boxH, 20, 20); g2.setColor(Assets.UI_BORDER); g2.setStroke(new BasicStroke(3)); g2.drawRoundRect(cx, cy, boxW, boxH, 20, 20); g2.setColor(Color.WHITE); g2.setFont(new Font("Arial", Font.BOLD, 40)); String t = "PAUSED"; g2.drawString(t, w/2 - g2.getFontMetrics().stringWidth(t)/2, cy + 60); btnResume.x = w/2 - 125; btnResume.y = cy + 120; btnMainMenu.x = w/2 - 125; btnMainMenu.y = cy + 200; btnExitPause.x = w/2 - 125; btnExitPause.y = cy + 280; drawMenuButton(g2, btnResume, "CONTINUE", new Color(50, 150, 200)); drawMenuButton(g2, btnMainMenu, "MAIN MENU", new Color(200, 150, 50)); drawMenuButton(g2, btnExitPause, "EXIT GAME", new Color(180, 50, 50)); }
    private void drawMenuButton(Graphics2D g2, Rectangle rect, String text, Color baseColor) {
        // 1. Hover Effect (Daca mouse-ul e peste, il facem mai deschis)
        Color primaryColor = baseColor;
        if (rect.contains(mouseX, mouseY)) {
            primaryColor = baseColor.brighter();
        }

        // 2. Umbra Butonului (Shadow) - Deseneaza o forma neagra putin decalata
        g2.setColor(new Color(0, 0, 0, 100));
        g2.fillRoundRect(rect.x + 5, rect.y + 5, rect.width, rect.height, 30, 30);

        // 3. Conturul Auriu (Border)
        g2.setStroke(new BasicStroke(3));
        g2.setColor(new Color(255, 215, 0)); // Auriu (Gold)
        g2.drawRoundRect(rect.x, rect.y, rect.width, rect.height, 30, 30);
        g2.setStroke(new BasicStroke(1)); // Resetam grosimea liniei

        // 4. Gradientul de Fundal (Efect 3D)
        // Face o trecere de la culoarea deschisa (sus) la inchisa (jos)
        GradientPaint gp = new GradientPaint(
            rect.x, rect.y, primaryColor.brighter(),
            rect.x, rect.y + rect.height, primaryColor.darker()
        );
        g2.setPaint(gp);
        g2.fillRoundRect(rect.x, rect.y, rect.width, rect.height, 30, 30);

        // 5. Efectul de "Sticla" (Shine) - Un oval alb transparent in partea de sus
        GradientPaint shine = new GradientPaint(
            rect.x, rect.y, new Color(255, 255, 255, 100),
            rect.x, rect.y + rect.height / 2, new Color(255, 255, 255, 0)
        );
        g2.setPaint(shine);
        g2.fillRoundRect(rect.x + 3, rect.y + 3, rect.width - 6, rect.height / 2, 20, 20);

        // 6. Textul Centrat cu Umbra
        g2.setFont(new Font("Arial", Font.BOLD, 22));
        FontMetrics fm = g2.getFontMetrics();
        int textX = rect.x + (rect.width - fm.stringWidth(text)) / 2;
        int textY = rect.y + (rect.height - fm.getHeight()) / 2 + fm.getAscent();

        // Umbra textului (Negru)
        g2.setColor(new Color(0, 0, 0, 150));
        g2.drawString(text, textX + 2, textY + 2);

        // Textul propriu-zis (Alb)
        g2.setColor(Color.WHITE);
        g2.drawString(text, textX, textY);
    }
    private void drawSimpleButton(Graphics2D g2, Rectangle rect, String text, boolean active) { 
        if(rect.contains(mouseX, mouseY) || active) g2.setColor(new Color(100,100,120)); 
        else g2.setColor(Assets.UI_DARK); 
        g2.fillRoundRect(rect.x, rect.y, rect.width, rect.height, 15, 15); 
        g2.setColor(Color.WHITE); 
        g2.setStroke(new BasicStroke(1)); 
        g2.drawRoundRect(rect.x, rect.y, rect.width, rect.height, 15, 15); 
        
        g2.setFont(new Font("Arial", Font.BOLD, 14)); 
        FontMetrics fm = g2.getFontMetrics();
        int tx = rect.x + (rect.width - fm.stringWidth(text)) / 2;
        int ty = rect.y + (rect.height - fm.getHeight()) / 2 + fm.getAscent();
        
        IconRenderer.drawTextWithShadow(g2, text, tx, ty); 
    }
    private void drawCloseButton(Graphics2D g2, int cx, int cy) { btnCloseWindow = new Rectangle(cx + 450, cy + 10, 40, 40); if(btnCloseWindow.contains(mouseX, mouseY)) g2.setColor(Color.RED); else g2.setColor(new Color(150, 0, 0)); g2.fillRoundRect(btnCloseWindow.x, btnCloseWindow.y, btnCloseWindow.width, btnCloseWindow.height, 10, 10); g2.setColor(Color.WHITE); g2.setFont(new Font("Arial", Font.BOLD, 20)); g2.drawString("X", btnCloseWindow.x + 13, btnCloseWindow.y + 27); }
    private void drawItemIcon(Graphics2D g2, Item item, int x, int y, int size) {
        drawItemIcon(g2, item, x, y, size, true);
    }

    private void drawItemIcon(Graphics2D g2, Item item, int x, int y, int size, boolean drawBorder) {
        // 1. Efect de stralucire (GLOW)
        boolean isPowerful = (item.type == Item.Type.WEAPON && item.value > 35) || 
                             (item.type == Item.Type.ARMOR && item.value > 3);
                             
        if (item.name.contains("Golden") || item.rarityBonus > 0 || item.name.contains("Epic") || isPowerful) {
            long time = System.currentTimeMillis();
            int glowSize = (int)(Math.sin(time * 0.005) * 5); 
            
            RadialGradientPaint p = new RadialGradientPaint(
                new Point(x + size/2, y + size/2), 
                size/2 + 10,
                new float[] { 0.0f, 1.0f },
                new Color[] { new Color(255, 215, 0, 150), new Color(255, 215, 0, 0) }
            );
            g2.setPaint(p);
            g2.fillOval(x - 5 - glowSize, y - 5 - glowSize, size + 10 + glowSize*2, size + 10 + glowSize*2);
            
            if (drawBorder) {
                g2.setColor(Color.YELLOW);
                g2.setStroke(new BasicStroke(1));
                g2.drawRoundRect(x-2, y-2, size+4, size+4, 10, 10);
            }
        }

        // 2. Desenarea propriu-zisa a iconitei
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

        // --- 3. NOU: COOLDOWN OVERLAY (Vizualizare timp ramas) ---
        if (item.type == Item.Type.BUILDING && item.savedLastUsedTime > 0) {
            long now = System.currentTimeMillis();
            // Determinam durata totala (trebuie sa fie la fel ca in Building.java)
            long duration = (item.specificType == Item.Specific.FOUNTAIN) ? 30000 : 60000;
            long elapsed = now - item.savedLastUsedTime;
            
            if (elapsed < duration) {
                // Calculam cat la suta a mai ramas
                float percentLeft = 1.0f - ((float)elapsed / duration); 
                int angle = (int)(360 * percentLeft);
                
                // Desenam un arc intunecat peste iconita
                g2.setColor(new Color(0, 0, 0, 180));
                // Incepem de la ora 12 (90 grade)
                g2.fillArc(x, y, size, size, 90, angle);
                
                // Desenam secundele ramase
                int secondsLeft = (int)((duration - elapsed) / 1000) + 1;
                String secText = String.valueOf(secondsLeft);
                
                g2.setColor(Color.WHITE);
                g2.setFont(new Font("Arial", Font.BOLD, 20));
                int sw = g2.getFontMetrics().stringWidth(secText);
                
                // Umbra text
                g2.setColor(Color.BLACK);
                g2.drawString(secText, x + size/2 - sw/2 + 1, y + size/2 + 8 + 1);
                // Text alb
                g2.setColor(Color.WHITE);
                g2.drawString(secText, x + size/2 - sw/2, y + size/2 + 8);
            } else {
                // Daca timpul a expirat, resetam variabila ca sa nu mai calculam degeaba
                item.savedLastUsedTime = 0;
            }
        }
    }
    private void drawGameOverScreen(Graphics2D g2) { int w = getWidth(); int h = getHeight(); g2.setColor(new Color(20, 0, 0, 220)); g2.fillRect(0, 0, w, h); g2.setColor(Color.RED); g2.setFont(new Font("Arial Black", Font.BOLD, 70)); g2.drawString("YOU DIED", w/2 - 200, h/3); btnRestart.x = w/2 - 100; btnRestart.y = h/2; drawMenuButton(g2, btnRestart, "RESTART", Color.DARK_GRAY); }
    @Override
    public void keyPressed(KeyEvent e) {
        int k = e.getKeyCode();

        // 1. MENU START
        if (currentState == GameState.MENU) {
            if (k == KeyEvent.VK_ENTER) startNewGame();
            return;
        }

        // 2. GAME OVER
        if (currentState == GameState.GAMEOVER) {
            if (k == KeyEvent.VK_ENTER) startNewGame();
            return;
        }

        // 3. GLOBAL ESCAPE (Pauza sau Inapoi in joc)
        if (k == KeyEvent.VK_ESCAPE) {
            if (currentState == GameState.PLAYING) {
                currentState = GameState.PAUSED;
                keyW = keyA = keyS = keyD = false; // Oprim miscarea
            } else if (currentState == GameState.PAUSED) {
                currentState = GameState.PLAYING;
            } else {
                // Iesim din orice alt meniu (Shop, Inventory, Map, Crafting) inapoi in joc
                currentState = GameState.PLAYING;
            }
            return;
        }

        // 4. PLAYING & EDITOR (Movement)
        if (currentState == GameState.PLAYING || currentState == GameState.EDITOR_EDIT) {
            // Miscare (W/A/S/D or Arrows)
            if (k == KeyEvent.VK_W || k == KeyEvent.VK_UP) keyW = true;
            if (k == KeyEvent.VK_S || k == KeyEvent.VK_DOWN) keyS = true;
            if (k == KeyEvent.VK_A || k == KeyEvent.VK_LEFT) keyA = true;
            if (k == KeyEvent.VK_D || k == KeyEvent.VK_RIGHT) keyD = true;
            
            if (currentState == GameState.PLAYING) {
                // ... (restul controalelelor specifice jocului: M, 1-5, Space, I, C) ...
                if (k == KeyEvent.VK_M) { currentState = GameState.MAP; keyW=keyA=keyS=keyD=false; }
                if (k >= KeyEvent.VK_1 && k <= KeyEvent.VK_5) player.selectedSlot = k - KeyEvent.VK_1;
                if (k == KeyEvent.VK_SPACE) useCurrentItem(player.x, player.y);
                if (k == KeyEvent.VK_I) { currentState = GameState.INVENTORY; keyW=keyA=keyS=keyD=false; }
                if (k == KeyEvent.VK_C) { currentState = GameState.CRAFTING; keyW=keyA=keyS=keyD=false; }
            }
        }
        
        // 5. MAP STATE (Inchidere cu M)
        else if (currentState == GameState.MAP) {
            if (k == KeyEvent.VK_M) currentState = GameState.PLAYING;
        }
        
        // 6. MENU STATES (Inchidere cu tasta specifica)
        else if (currentState == GameState.INVENTORY || currentState == GameState.CRAFTING || currentState == GameState.SHOP) {
            if (k == KeyEvent.VK_I || k == KeyEvent.VK_C) currentState = GameState.PLAYING;
        }
        
        // 7. EDITOR EDIT (Miscare Camera - Daca ai implementat editorul)
        // Daca nu ai editorul inca, poti sterge acest bloc else-if
        else if (currentState == GameState.EDITOR_EDIT) {
            if (k == KeyEvent.VK_W) keyW = true; // Folosim aceleasi variabile pentru camera
            if (k == KeyEvent.VK_S) keyS = true;
            if (k == KeyEvent.VK_A) keyA = true;
            if (k == KeyEvent.VK_D) keyD = true;
        }

        repaint();
    }
    @Override 
    public void keyReleased(KeyEvent e) { 
        int k = e.getKeyCode(); 
        if (currentState == GameState.PLAYING || currentState == GameState.EDITOR_EDIT) { 
            if(k == KeyEvent.VK_W || k == KeyEvent.VK_UP) keyW = false; 
            if(k == KeyEvent.VK_S || k == KeyEvent.VK_DOWN) keyS = false; 
            if(k == KeyEvent.VK_A || k == KeyEvent.VK_LEFT) keyA = false; 
            if(k == KeyEvent.VK_D || k == KeyEvent.VK_RIGHT) keyD = false; 
        } 
    }
    @Override public void keyTyped(KeyEvent e) {}
    @Override public void mouseMoved(MouseEvent e) { mouseX = e.getX(); mouseY = e.getY(); repaint(); }
    @Override public void mouseDragged(MouseEvent e) { mouseX = e.getX(); mouseY = e.getY(); repaint(); }
    @Override public void mouseClicked(MouseEvent e) {}
    @Override public void mouseEntered(MouseEvent e) {}
    @Override public void mouseExited(MouseEvent e) {}
    @Override
    public void mousePressed(MouseEvent e) {
        this.requestFocusInWindow();
        isMousePressed = true;
        Point p = e.getPoint();

        // --- 1. MENU STATE ---
     // --- 1. MENU STATE ---
        if (currentState == GameState.MENU) {
            if (btnStartGame.contains(p)) startNewGame();
            
            // MODIFICAT: Acum intram in meniul de selectie editor
            if (btnEditor.contains(p)) {
                currentState = GameState.EDITOR_SELECT;
                refreshMapList();
                // Asigura-te ca lista de harti e initializata (ar trebui sa fie din declararea variabilei)
            }
            
            if (btnExitMenu.contains(p)) System.exit(0);
            return;
        }

        // --- 2. GAMEOVER STATE ---
        if (currentState == GameState.GAMEOVER) {
            if (btnRestart.contains(p)) startNewGame();
            return;
        }

        // --- 3. PAUSED STATE ---
        if (currentState == GameState.PAUSED) {
            if (btnResume.contains(p)) currentState = GameState.PLAYING;
            if (btnMainMenu.contains(p)) currentState = GameState.MENU;
            if (btnExitPause.contains(p)) System.exit(0);
            return;
        }

        // --- 4. PLAYING STATE (HUD Buttons) ---
        if(currentState == GameState.PLAYING) {
            if(btnMapOpen.contains(p)) currentState = GameState.MAP;
            if(btnCraftingOpen.contains(p)) currentState = GameState.CRAFTING;
            if(btnInventoryOpen.contains(p)) currentState = GameState.INVENTORY;
            
            for(int i=0; i<5; i++) 
                if(hotbarRects[i].contains(p)) player.selectedSlot = i;
        } 
        
        // --- 5. MAP STATE ---
        else if(currentState == GameState.MAP) {
            if (btnCloseWindow.contains(p)) currentState = GameState.PLAYING;
        }
        
        // --- 6. INVENTORY STATE ---
        else if(currentState == GameState.INVENTORY) {
            if (btnCloseWindow.contains(p)) { currentState = GameState.PLAYING; return; }
            
            // Armor
            for(int i=0; i<4; i++) {
                if(armorRects[i].contains(p) && player.armor[i] != null) {
                    draggingItem = player.armor[i]; dragSourceIndex = i; dragSourceType = 2; return;
                }
            }
            // Backpack
            for(int i=0; i<15; i++) {
                if(backpackRects[i].contains(p) && player.backpack[i] != null) {
                    draggingItem = player.backpack[i]; dragSourceIndex = i; dragSourceType = 0; return;
                }
            }
            // Hotbar inside Inventory
            for(int i=0; i<5; i++) {
                if(hotbarRects[i].contains(p) && player.inventory[i] != null) {
                    draggingItem = player.inventory[i]; dragSourceIndex = i; dragSourceType = 1; dragFromHotbar=true; return;
                }
            }
        } 
        
        // --- 7. SHOP STATE ---
        else if (currentState == GameState.SHOP) {
            if (btnCloseWindow.contains(p)) { 
                currentState = GameState.PLAYING; 
                interactionCooldown = 30; 
                keyW = keyA = keyS = keyD = false; 
                return; 
            }
         // LOGICA CUMPARARE (Coloana Stanga)
            for(int i=0; i<merchantStock.size(); i++) {
                if(shopBuyRects[i] != null && shopBuyRects[i].contains(p)) {
                    Item itemToBuy = merchantStock.get(i);
                    int cost = itemToBuy.value * 5;
                    if(itemToBuy.specificType == Item.Specific.HEALTH_POTION) cost = 50;

                    if(player.getGold() >= cost) {
                        // Cream o copie a itemului
                        Item newItem = new Item(itemToBuy.name, itemToBuy.type, itemToBuy.specificType, itemToBuy.value, itemToBuy.rarityBonus);
                        if(player.addItem(newItem)) {
                            player.removeGold(cost);
                            effects.add(new VisualEffect(player.visualX, player.visualY, "BOUGHT -" + cost, Color.GREEN, 30));
                        } else {
                            effects.add(new VisualEffect(player.visualX, player.visualY, "FULL!", Color.RED, 30));
                        }
                    } else {
                        effects.add(new VisualEffect(player.visualX, player.visualY, "NO GOLD!", Color.RED, 30));
                    }
                }
            }
            
            // LOGICA VANZARE (Coloana Dreapta)
            for(int i=0; i<20; i++) {
                if(shopSellRects[i] != null && shopSellRects[i].contains(p)) {
                    Item it = (i < 15) ? player.backpack[i] : player.inventory[i-15];
                    if (it != null) {
                        int sellVal = getItemSellPrice(it);
                        player.addGold(sellVal);
                        
                        // Scadem cantitatea sau stergem itemul
                        if (it.quantity > 1) it.quantity--;
                        else {
                            if (i < 15) player.backpack[i] = null;
                            else player.inventory[i-15] = null;
                        }
                        effects.add(new VisualEffect(player.visualX, player.visualY, "SOLD +" + sellVal, Color.YELLOW, 30));
                    }
                }
            }
        }
        
        
        // --- 8. CRAFTING STATE ---
        else if (currentState == GameState.CRAFTING) {
            if (btnCloseWindow.contains(p)) { currentState = GameState.PLAYING; return; }
            
            for(CraftingButton btn : craftButtons) {
                if(btn.rect.contains(p)) {
                    // Verificare resurse si craftare
                    if(player.consumeItems(Item.Specific.WOOD, btn.woodCost) && 
                       player.consumeItems(Item.Specific.STONE, btn.stoneCost) && 
                       player.consumeItems(Item.Specific.GRAIN, btn.grainCost)) {
                        
                        Item newItem = new Item(btn.name, btn.type == Item.Specific.BREAD ? Item.Type.CONSUMABLE : Item.Type.BUILDING, btn.type, btn.type == Item.Specific.BREAD ? 30 : 0);
                        if(player.addItem(newItem)) 
                            effects.add(new VisualEffect(player.visualX, player.visualY, "Crafted!", Color.GREEN, 25));
                        else 
                            effects.add(new VisualEffect(player.visualX, player.visualY, "Full!", Color.RED, 25));
                    }
                }
            }
        }
        
     // LOGICA EDITOR SELECT
        if (currentState == GameState.EDITOR_SELECT) {
            // 1. DELETE POPUP LOGIC
            if (showDeleteConfirm) {
                int cx = getWidth() / 2;
                int cy = getHeight() / 2;
                if (btnConfirmYes == null) {
                    btnConfirmYes = new Rectangle(cx - 110, cy + 20, 100, 50);
                    btnConfirmNo = new Rectangle(cx + 10, cy + 20, 100, 50);
                }
                
                if (btnConfirmYes.contains(p)) {
                    if (mapToDeleteIndex >= 0 && mapToDeleteIndex < mapFiles.size()) {
                        String deletedName = mapFiles.get(mapToDeleteIndex).getName().replace(".map", "");
                        mapFiles.get(mapToDeleteIndex).delete();
                        refreshMapList();
                        // Mesaj Pop-up Stergere
                        effects.add(new VisualEffect(getWidth()/2 - 150, getHeight()/2, "Deleted: " + deletedName, Color.RED, 30));
                    }
                    showDeleteConfirm = false;
                    mapToDeleteIndex = -1;
                } else if (btnConfirmNo.contains(p)) {
                    showDeleteConfirm = false;
                    mapToDeleteIndex = -1;
                }
                return; // Block other interactions
            }

            if (btnCloseWindow.contains(p)) currentState = GameState.MENU;
            
            // Buton Create
            if (btnCreateMap.contains(p)) {
                String name = javax.swing.JOptionPane.showInputDialog("Map Name:");
                if(name != null && !name.trim().isEmpty()) {
                    currentMapName = name;
                    editorMap = new WorldMap(100, 100); // Harta 100x100 (la fel ca in joc)
                    // O golim complet
                    for(int r=0; r<100; r++) for(int c=0; c<100; c++) editorMap.grid[r][c] = null;
                    currentState = GameState.EDITOR_EDIT;
                }
            }
            
            // Butoane Lista (Play, Edit, Delete)
            if (mapFiles != null && p.y > LIST_VIEW_Y && p.y < LIST_VIEW_Y + LIST_VIEW_H) {
                for(int i=0; i<mapFiles.size(); i++) {
                    if (playMapBtns[i] != null && playMapBtns[i].contains(p)) loadMapAndPlay(mapFiles.get(i));
                    if (editMapBtns[i] != null && editMapBtns[i].contains(p)) loadMapForEditing(mapFiles.get(i));
                    if (delMapBtns[i] != null && delMapBtns[i].contains(p)) {
                        mapToDeleteIndex = i;
                        showDeleteConfirm = true;
                        // Force init buttons rects relative to screen center
                        int cx = getWidth() / 2;
                        int cy = getHeight() / 2;
                        btnConfirmYes = new Rectangle(cx - 110, cy + 20, 100, 50);
                        btnConfirmNo = new Rectangle(cx + 10, cy + 20, 100, 50);
                    }
                }
            }
            return;
        }
     // 3. EDITOR EDIT (Plasare)
        if (currentState == GameState.EDITOR_EDIT) {
            if (btnCloseWindow.contains(p)) {
                currentState = GameState.EDITOR_SELECT;
                refreshMapList();
                return;
            }
            
            if (btnSaveMap.contains(p)) {
                saveMap(currentMapName);
                // Pop-up central
                effects.add(new VisualEffect(getWidth()/2 - 150, getHeight()/2, "Map Saved: " + currentMapName, Color.GREEN, 30));
                return;
            }
            
            // Selectie din Paleta
            for(int i=0; i<paletteRects.length; i++) {
                if(paletteRects[i].contains(p)) {
                    editorSelectedTileIndex = i;
                    return;
                }
            }
            
            // Plasare pe Harta (click in lume)
            int mx = (int)(mouseX + editorCamX);
            int my = (int)(mouseY + editorCamY);
            int c = mx / TILE_SIZE;
            int r = my / TILE_SIZE;
            
            if(c >= 0 && c < editorMap.cols && r >= 0 && r < editorMap.rows) {
                Object objToPlace = editorPaletteObjects[editorSelectedTileIndex];
                
                if ("ERASER".equals(objToPlace)) {
                    editorMap.grid[r][c] = null;
                } else if ("WATER".equals(objToPlace)) {
                    editorMap.grid[r][c] = "WATER";
                } else if (objToPlace instanceof WorldMap.PlayerStart) {
                    // Stergem alte spawn points ca sa fie doar unul
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
                else if (objToPlace instanceof Vendor) editorMap.grid[r][c] = new Vendor();
                else if (objToPlace instanceof WorldMap.Campfire) editorMap.grid[r][c] = new WorldMap.Campfire();
                else if (objToPlace instanceof WorldMap.Tent) editorMap.grid[r][c] = new WorldMap.Tent();
            }
            return;
        }
        
        repaint();
    }
    @Override public void mouseReleased(MouseEvent e) { isMousePressed = false; if (draggingItem != null && currentState == GameState.INVENTORY) { Point p = e.getPoint(); boolean placed = false; if (draggingItem.type == Item.Type.ARMOR) { for(int i=0; i<4; i++) { if(armorRects[i].contains(p)) { boolean valid = false; if(i==0 && draggingItem.specificType == Item.Specific.HELMET) valid=true; if(i==1 && draggingItem.specificType == Item.Specific.CHESTPLATE) valid=true; if(i==2 && draggingItem.specificType == Item.Specific.PANTS) valid=true; if(i==3 && draggingItem.specificType == Item.Specific.BOOTS) valid=true; if(valid) { Item temp = player.armor[i]; player.armor[i] = draggingItem; returnItemToSource(temp); placed = true; } } } } if(!placed) { for(int i=0; i<15; i++) { if(backpackRects[i].contains(p)) { Item temp = player.backpack[i]; player.backpack[i] = draggingItem; returnItemToSource(temp); placed = true; break; } } } if(!placed) { for(int i=0; i<5; i++) { if(hotbarRects[i].contains(p)) { Item temp = player.inventory[i]; player.inventory[i] = draggingItem; returnItemToSource(temp); placed = true; break; } } } if(!placed) returnItemToSource(draggingItem); } draggingItem = null; dragSourceIndex = -1; dragFromHotbar = false; repaint(); }
    private void returnItemToSource(Item item) {
        // REMOVED: if(item == null) return;  <-- This was causing the duplication!
        // We WANT to be able to set the source slot to null (clearing it).
        
        if(dragSourceType == 0) {
            player.backpack[dragSourceIndex] = item;
        } 
        else if(dragSourceType == 1) {
            player.inventory[dragSourceIndex] = item;
        } 
        else if(dragSourceType == 2) {
            player.armor[dragSourceIndex] = item;
        }
    }
    
    private void drawMapScreen(Graphics2D g2) {
        int w = getWidth(); int h = getHeight();
        g2.setColor(new Color(0, 0, 0, 200)); 
        g2.fillRect(0, 0, w, h); // Fundal intunecat

        // Calculam dimensiunea hartii pe ecran
        // Harta are 100x100. Daca punem 6 pixeli per patrat => 600x600 pixeli
        int scale = 6; 
        int mapW = map.cols * scale;
        int mapH = map.rows * scale;
        int startX = w / 2 - mapW / 2;
        int startY = h / 2 - mapH / 2;

        // Chenar
        g2.setColor(new Color(30, 30, 35));
        g2.fillRect(startX - 10, startY - 10, mapW + 20, mapH + 20);
        g2.setColor(Color.WHITE);
        g2.setStroke(new BasicStroke(2));
        g2.drawRect(startX - 10, startY - 10, mapW + 20, mapH + 20);
        
        // Titlu
        g2.setFont(new Font("Arial", Font.BOLD, 20));
        g2.drawString("WORLD MAP", startX, startY - 20);
        drawCloseButton(g2, startX + mapW/2 - 250, startY + mapH/2 - 250); // Ajusteaza pozitia X-ului relativ la centru

        // Desenare grid
        for (int r = 0; r < map.rows; r++) {
            for (int c = 0; c < map.cols; c++) {
                int dx = startX + c * scale;
                int dy = startY + r * scale;

                if (!map.explored[r][c]) {
                    // Zona neexplorata (Fog of War)
                    g2.setColor(Color.BLACK);
                    g2.fillRect(dx, dy, scale, scale);
                } else {
                    // Zona explorata - Desenam culori simple
                    Object e = map.grid[r][c];
                    
                    // Teren de baza
                    if ((r + c) % 2 == 0) g2.setColor(Assets.GRASS_1); 
                    else g2.setColor(Assets.GRASS_2);
                    
                    // Entitati
                    if ("WATER".equals(e)) g2.setColor(Color.BLUE);
                    else if (e instanceof Tree) g2.setColor(new Color(0, 100, 0)); // Verde inchis
                    else if (e instanceof Rock) g2.setColor(Color.GRAY);
                    else if (e instanceof Building) g2.setColor(Color.ORANGE);
                    else if (e instanceof Vendor) g2.setColor(Color.MAGENTA);
                    
                    g2.fillRect(dx, dy, scale, scale);
                }
            }
        }

        // Desenare Jucator
        int px = startX + player.x * scale;
        int py = startY + player.y * scale;
        int headSize = scale + 4; 
        g2.setColor(Assets.SKIN);
        g2.fillOval(px - 2, py - 2, headSize, headSize);
        
        // Parul (Maro inchis)
        g2.setColor(new Color(50, 30, 10)); 
        g2.fillArc(px - 2, py - 4, headSize, headSize - 2, 0, 180);
        g2.fillArc(px - 2, py - 4, 3, 6, 180, 90); // Perciune stanga
        g2.fillArc(px + headSize - 3, py - 4, 3, 6, 270, 90); // Perciune dreapta
        
        // Contur (pentru vizibilitate)
        g2.setColor(Color.WHITE);
        g2.setStroke(new BasicStroke(1));
        g2.drawOval(px - 2, py - 2, headSize, headSize);
        
        // Legenda (Optional)
        g2.setFont(new Font("Arial", Font.PLAIN, 12));
        g2.setColor(Color.WHITE);
        g2.drawString("You are here", px + 10, py + 5);
    }
 // --- ARMOR PLACEHOLDERS (Add this inside IconRenderer class) ---
    public static void drawArmorPlaceholder(Graphics2D g2, int x, int y, int size, int type) {
        g2.setColor(new Color(255, 255, 255, 20)); // Faint white transparency
        g2.setStroke(new BasicStroke(2));

        // Type: 0=Head, 1=Body, 2=Legs, 3=Feet
        if (type == 0) { // Helmet shape
            g2.drawArc(x + 15, y + 15, size - 30, size - 30, 0, 180);
            g2.drawRect(x + 15, y + size / 2, 6, 10);
            g2.drawRect(x + size - 21, y + size / 2, 6, 10);
        } else if (type == 1) { // Chest shape
            g2.drawRoundRect(x + 15, y + 15, size - 30, size - 30, 10, 10);
            g2.drawLine(x + 15, y + 15 + (size-30), x + size - 15, y + 15 + (size-30));
        } else if (type == 2) { // Pants shape
            g2.drawRect(x + 20, y + 15, size - 40, 10);
            g2.drawRect(x + 20, y + 25, 10, 20); // Left leg
            g2.drawRect(x + size - 30, y + 25, 10, 20); // Right leg
        } else if (type == 3) { // Boots shape
            g2.drawRect(x + 15, y + 30, 12, 10);
            g2.drawRect(x + 15, y + 40, 18, 8);
            g2.drawRect(x + size - 27, y + 30, 12, 10);
            g2.drawRect(x + size - 33, y + 40, 18, 8);
        }
        g2.setStroke(new BasicStroke(1));
    }
    private void drawHoverTooltip(Graphics2D g2) {
        String tooltipText = null;
        
        // 1. Hover in SHOP
        if (currentState == GameState.SHOP) {
            // Check Buy List
            if (merchantStock != null) {
                for(int i=0; i<merchantStock.size(); i++) {
                    if(shopBuyRects[i] != null && shopBuyRects[i].contains(mouseX, mouseY)) {
                        tooltipText = merchantStock.get(i).name;
                    }
                }
            }
            // Check Sell List
            if (shopSellRects != null) {
                for(int i=0; i<20; i++) {
                    if(shopSellRects[i] != null && shopSellRects[i].contains(mouseX, mouseY)) {
                        Item it = (i < 15) ? player.backpack[i] : player.inventory[i-15];
                        if(it != null) tooltipText = it.name;
                    }
                }
            }
        }

        // 2. Hover in INVENTORY/PLAYING (Hotbar & Backpack)
        if (currentState == GameState.INVENTORY || currentState == GameState.PLAYING) {
             for(int i=0; i<5; i++) { // Hotbar
                if(hotbarRects[i] != null && hotbarRects[i].contains(mouseX, mouseY) && player.inventory[i] != null) 
                    tooltipText = player.inventory[i].name;
            }
        }
        if (currentState == GameState.INVENTORY) {
            for(int i=0; i<15; i++) { // Backpack
                if(backpackRects[i] != null && backpackRects[i].contains(mouseX, mouseY) && player.backpack[i] != null) 
                    tooltipText = player.backpack[i].name;
            }
            for(int i=0; i<4; i++) { // Armor
                if(armorRects[i] != null && armorRects[i].contains(mouseX, mouseY) && player.armor[i] != null) 
                    tooltipText = player.armor[i].name;
            }
        }

        // --- DESENARE ---
        if (tooltipText != null) {
            // ... (codul de desenare chenar negru ramane la fel) ...
            g2.setFont(new Font("Arial", Font.BOLD, 14));
            FontMetrics fm = g2.getFontMetrics();
            int textW = fm.stringWidth(tooltipText);
            int textH = fm.getHeight();
            int tx = mouseX + 15; int ty = mouseY + 15;
            if (tx + textW + 10 > getWidth()) tx = mouseX - textW - 15;
            if (ty + textH + 10 > getHeight()) ty = mouseY - textH - 15;
            g2.setColor(new Color(0, 0, 0, 220));
            g2.fillRoundRect(tx, ty, textW + 10, textH + 6, 8, 8);
            g2.setColor(Color.WHITE);
            g2.setStroke(new BasicStroke(1));
            g2.drawRoundRect(tx, ty, textW + 10, textH + 6, 8, 8);
            g2.drawString(tooltipText, tx + 5, ty + textH - 3);
        }
    }
 // Calculeaza pretul unui item (pentru vanzare)
    private int getItemSellPrice(Item item) {
        if (item == null) return 0;
        
        // 1. Resurse (Ieftine)
        if (item.specificType == Item.Specific.WOOD) return 2;
        if (item.specificType == Item.Specific.STONE) return 3;
        if (item.specificType == Item.Specific.GRAIN) return 2;
        
        // 2. Consumabile
        if (item.specificType == Item.Specific.BREAD) return 5;
        if (item.specificType == Item.Specific.HEALTH_POTION) return 15;
        
        // 3. Echipament (Bazat pe puterea lor - value)
        // De ex: O sabie de 70 dmg va costa 70 * 4 = 280 gold la vanzare
        return item.value * 4;
    }
    
    private void generateShopStock() {
        merchantStock = new ArrayList<>();
        shopBuyRects = new Rectangle[6]; // Maxim 6 iteme
        
        // 1. OBLIGATORIU: Health Potion (Mereu primul loc)
        merchantStock.add(new Item("Health Potion", Item.Type.CONSUMABLE, Item.Specific.HEALTH_POTION, 100));
        
        // 2. Creăm un "pool" cu toate itemele posibile (în afară de poțiune)
        List<Item> pool = new ArrayList<>();
        pool.add(new Item("Iron Sword", Item.Type.WEAPON, Item.Specific.SWORD, 70));
        pool.add(new Item("Iron Helm", Item.Type.ARMOR, Item.Specific.HELMET, 5));
        pool.add(new Item("Iron Chest", Item.Type.ARMOR, Item.Specific.CHESTPLATE, 8));
        pool.add(new Item("Iron Pants", Item.Type.ARMOR, Item.Specific.PANTS, 4));
        pool.add(new Item("Iron Boots", Item.Type.ARMOR, Item.Specific.BOOTS, 3));
        pool.add(new Item("Golden Axe", Item.Type.TOOL, Item.Specific.AXE, 15, 1));
        pool.add(new Item("Golden Pick", Item.Type.TOOL, Item.Specific.PICKAXE, 12, 1));
        
        // Amestecăm lista ca să fie random
        java.util.Collections.shuffle(pool);
        
        // 3. GARANTAM categoriile cerute
        
        // A. Cautam si adaugam minim o ARMURA
        for(int i=0; i<pool.size(); i++) {
            if(pool.get(i).type == Item.Type.ARMOR) {
                merchantStock.add(pool.get(i));
                pool.remove(i); // Scoatem din pool ca sa nu o mai alegem o data
                break;
            }
        }
        
        // B. Cautam si adaugam minim o UNEALTA sau ARMA
        for(int i=0; i<pool.size(); i++) {
            if(pool.get(i).type == Item.Type.TOOL || pool.get(i).type == Item.Type.WEAPON) {
                merchantStock.add(pool.get(i));
                pool.remove(i);
                break;
            }
        }
        
        // 4. UMPLEM restul locurilor (până la 5 sau 6 iteme total)
        // Mai avem nevoie de inca 2-3 iteme random din ce a ramas in pool
        int itemsNeeded = 4 + (int)(Math.random() * 2) - merchantStock.size() + 1; // +1 pt potiune
        
        for(int i=0; i<itemsNeeded && !pool.isEmpty(); i++) {
            merchantStock.add(pool.get(0)); // Luam mereu primul din lista amestecata
            pool.remove(0);
        }
    }
    
    private void drawEditorSelectMenu(Graphics2D g2) {
        // 1. Fundal
        GradientPaint gp = new GradientPaint(0, 0, new Color(20, 10, 30), 0, getHeight(), new Color(40, 20, 60));
        g2.setPaint(gp); 
        g2.fillRect(0, 0, getWidth(), getHeight());
        
        // 2. Titlu
        g2.setColor(Color.WHITE); 
        g2.setFont(new Font("Arial", Font.BOLD, 40));
        String title = "MAP EDITOR";
        g2.drawString(title, getWidth()/2 - g2.getFontMetrics().stringWidth(title)/2, 80);

        // 3. Lista de Harti (SCROLLABLE AREA)
        if (mapFiles == null) mapFiles = new ArrayList<>();

        if (mapFiles.isEmpty()) {
            String msg = "No maps found.";
            g2.setColor(Color.GRAY); 
            g2.setFont(new Font("Arial", Font.PLAIN, 20));
            g2.drawString(msg, getWidth()/2 - g2.getFontMetrics().stringWidth(msg)/2, 200);
        } else {
            // Resize arrays if needed
            if (playMapBtns == null || playMapBtns.length < mapFiles.size()) {
                 playMapBtns = new Rectangle[mapFiles.size()]; 
                 editMapBtns = new Rectangle[mapFiles.size()]; 
                 delMapBtns = new Rectangle[mapFiles.size()];
            }

            // Set Clipping Area
            Shape originalClip = g2.getClip();
            g2.setClip(0, LIST_VIEW_Y, getWidth(), LIST_VIEW_H);

            int startY = LIST_VIEW_Y + 10; // Padding inside clip

            for(int i=0; i<mapFiles.size(); i++) {
                if(mapFiles.get(i) == null) continue;

                int y = startY + i * 60 - listScrollY;
                
                // Optimization: Don't draw if completely out of view
                if (y + 50 < LIST_VIEW_Y || y > LIST_VIEW_Y + LIST_VIEW_H) {
                    // Still update rects for logic consistency, but maybe move them off-screen or mark invalid?
                    // Actually, keeping them updated is fine, collision check will handle bounds.
                    // We'll update coordinates but skip drawing heavy stuff.
                }

                int x = getWidth()/2 - 300;
                
                // Draw Entry Background
                g2.setColor(new Color(0, 0, 0, 100));
                g2.fillRoundRect(x, y, 600, 50, 10, 10);
                
                // Map Name
                String fileName = mapFiles.get(i).getName();
                String name = fileName.contains(".") ? fileName.substring(0, fileName.lastIndexOf('.')) : fileName;
                
                g2.setColor(Color.WHITE); 
                g2.setFont(new Font("Arial", Font.BOLD, 18));
                g2.drawString(name, x + 20, y + 32);
                
                // Buttons
                playMapBtns[i] = new Rectangle(x + 350, y + 5, 70, 40);
                editMapBtns[i] = new Rectangle(x + 430, y + 5, 70, 40);
                delMapBtns[i]  = new Rectangle(x + 510, y + 5, 70, 40);
                
                drawSimpleButton(g2, playMapBtns[i], "PLAY", false);
                drawSimpleButton(g2, editMapBtns[i], "EDIT", false);
                
                g2.setColor(new Color(150, 0, 0));
                g2.fillRoundRect(delMapBtns[i].x, delMapBtns[i].y, 70, 40, 10, 10);
                g2.setColor(Color.WHITE); 
                g2.drawString("DEL", delMapBtns[i].x + 20, delMapBtns[i].y + 25);
            }
            
            // Restore Clip
            g2.setClip(originalClip);
            
            // Draw Scrollbar
            int totalH = mapFiles.size() * 60;
            if (totalH > LIST_VIEW_H) {
                int sbH = (int)((float)LIST_VIEW_H / totalH * LIST_VIEW_H);
                int sbY = LIST_VIEW_Y + (int)((float)listScrollY / totalH * LIST_VIEW_H);
                g2.setColor(new Color(100, 100, 100));
                g2.fillRoundRect(getWidth()/2 + 310, sbY, 8, sbH, 4, 4);
            }
        }
        
        // 4. Buton Create
        if (btnCreateMap != null) {
            drawMenuButton(g2, btnCreateMap, "CREATE NEW MAP", new Color(0, 150, 200));
        }
        
        // 5. Buton X
        btnCloseWindow = new Rectangle(getWidth() - 60, 20, 40, 40);
        g2.setColor(new Color(150, 0, 0));
        g2.fillRoundRect(btnCloseWindow.x, btnCloseWindow.y, 40, 40, 10, 10);
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Arial", Font.BOLD, 20));
        g2.drawString("X", btnCloseWindow.x + 13, btnCloseWindow.y + 27);

        // 6. DELETE POPUP (DRAW)
        if (showDeleteConfirm) {
            // Dark Overlay
            g2.setColor(new Color(0, 0, 0, 200));
            g2.fillRect(0, 0, getWidth(), getHeight());
            
            // Dialog Box
            int cx = getWidth() / 2;
            int cy = getHeight() / 2;
            g2.setColor(new Color(40, 40, 50));
            g2.fillRoundRect(cx - 200, cy - 100, 400, 200, 20, 20);
            g2.setColor(Color.WHITE);
            g2.setStroke(new BasicStroke(2));
            g2.drawRoundRect(cx - 200, cy - 100, 400, 200, 20, 20);
            
            // Text
            g2.setFont(new Font("Arial", Font.BOLD, 20));
            String msg1 = "Are you sure you want to";
            String msg2 = "delete this map?";
            g2.drawString(msg1, cx - g2.getFontMetrics().stringWidth(msg1)/2, cy - 40);
            g2.drawString(msg2, cx - g2.getFontMetrics().stringWidth(msg2)/2, cy - 10);
            
            // Buttons
            if (btnConfirmYes != null) {
                drawMenuButton(g2, btnConfirmYes, "YES", new Color(0, 150, 0));
                drawMenuButton(g2, btnConfirmNo, "NO", new Color(150, 0, 0));
            }
        }
        
        // 7. EFFECTS
        for(int i=0; i<effects.size(); i++) effects.get(i).draw(g2);
    }
    // --- METODA 2: Deseneaza Interfata de Editare (Harta + Paleta) ---
    private void drawEditorInterface(Graphics2D g2) {
        // A. Harta
        g2.translate(-editorCamX, -editorCamY);
        // ... (Codul tau de desenare grid si obiecte ramane la fel, e bun) ...
        // Poti pastra bucla for existenta aici
        
        // Voi rescrie doar bucla pentru siguranta, dar poti folosi ce aveai daca mergea desenarea
        int startCol = Math.max(0, (int)(editorCamX / TILE_SIZE));
        int startRow = Math.max(0, (int)(editorCamY / TILE_SIZE));
        int endCol = Math.min(editorMap.cols, startCol + getWidth()/TILE_SIZE + 2);
        int endRow = Math.min(editorMap.rows, startRow + getHeight()/TILE_SIZE + 2);

        for (int r = startRow; r < endRow; r++) {
            for (int c = startCol; c < endCol; c++) {
                int px = c * TILE_SIZE; int py = r * TILE_SIZE;
                g2.setColor(new Color(20, 100, 20)); g2.fillRect(px, py, TILE_SIZE, TILE_SIZE);
                g2.setColor(new Color(0, 0, 0, 50)); g2.drawRect(px, py, TILE_SIZE, TILE_SIZE);
                
                Object ent = editorMap.getEntityAt(c, r);
                if ("WATER".equals(ent)) { 
                    IconRenderer.drawWaterTile(g2, px, py, TILE_SIZE, tickCounter); 
                }
                else if (ent instanceof WorldMap.PlayerStart) {
                    IconRenderer.drawSpawnIcon(g2, px, py, TILE_SIZE);
                }
                else if (ent instanceof Tree) IconRenderer.drawTree(g2, px, py, TILE_SIZE, Enums.Quality.COMMON);
                else if (ent instanceof Rock) IconRenderer.drawRock(g2, px, py, TILE_SIZE, Enums.Quality.COMMON);
                else if (ent instanceof Grain) IconRenderer.drawGrain(g2, px, py, TILE_SIZE, Enums.Quality.COMMON);
                else if (ent instanceof Building) IconRenderer.drawBuilding(g2, (Building)ent, px, py, TILE_SIZE);
                else if (ent instanceof Enemy) IconRenderer.drawEnemy(g2, (Enemy)ent, px, py, TILE_SIZE);
                else if (ent instanceof WorldMap.Campfire) IconRenderer.drawCampfire(g2, px, py, TILE_SIZE);
                else if (ent instanceof WorldMap.Tent) IconRenderer.drawTent(g2, px, py, TILE_SIZE);
                else if (ent instanceof Vendor) IconRenderer.drawVendor(g2, px, py, TILE_SIZE);
            }
        }
        g2.translate(editorCamX, editorCamY);

        // B. HUD
        g2.setColor(new Color(0, 0, 0, 180));
        g2.fillRect(0, getHeight() - 120, getWidth(), 120); // Taller HUD to fit text on top
        
        for(int i=0; i<paletteRects.length; i++) {
            Rectangle r = paletteRects[i];
            if (i == editorSelectedTileIndex) { g2.setColor(Color.YELLOW); g2.fillRoundRect(r.x-2, r.y-2, r.width+4, r.height+4, 10, 10); } 
            g2.setColor(Color.DARK_GRAY); g2.fillRoundRect(r.x, r.y, r.width, r.height, 5, 5);
            
            Object obj = editorPaletteObjects[i];
            if ("ERASER".equals(obj)) { g2.setColor(Color.RED); g2.setFont(new Font("Arial", Font.BOLD, 30)); g2.drawString("X", r.x+15, r.y+40); }
            else if (obj instanceof WorldMap.PlayerStart) { IconRenderer.drawSpawnIcon(g2, r.x, r.y, 50); }
            else if ("WATER".equals(obj)) { IconRenderer.drawWaterIcon(g2, r.x, r.y, 50, tickCounter); }
            else if (obj instanceof Tree) IconRenderer.drawTree(g2, r.x, r.y, 50, Enums.Quality.COMMON);
            else if (obj instanceof Rock) IconRenderer.drawRock(g2, r.x, r.y, 50, Enums.Quality.COMMON);
            else if (obj instanceof Grain) IconRenderer.drawGrain(g2, r.x, r.y, 50, Enums.Quality.COMMON);
            else if (obj instanceof Building) { if (((Building)obj).type == Building.Type.FOUNTAIN) IconRenderer.drawFountain(g2, r.x, r.y, 50); else IconRenderer.drawMonument(g2, r.x, r.y, 50); }
            else if (obj instanceof Enemy) IconRenderer.drawEnemy(g2, (Enemy)obj, r.x, r.y, 50);
            else if (obj instanceof WorldMap.Campfire) IconRenderer.drawCampfire(g2, r.x, r.y, 50);
            else if (obj instanceof WorldMap.Tent) IconRenderer.drawTent(g2, r.x, r.y, 50);
            else if (obj instanceof Vendor) IconRenderer.drawVendor(g2, r.x, r.y, 50);
            
            // Draw Name Tag (ABOVE)
            String name = "";
            if ("ERASER".equals(obj)) name = "Eraser";
            else if (obj instanceof WorldMap.PlayerStart) name = "Spawn";
            else if ("WATER".equals(obj)) name = "Water";
            else if (obj instanceof Tree) name = "Tree";
            else if (obj instanceof Rock) name = "Rock";
            else if (obj instanceof Grain) name = "Grain";
            else if (obj instanceof Building) {
                if (((Building)obj).type == Building.Type.FOUNTAIN) name = "Fount.";
                else name = "Monum.";
            }
            else if (obj instanceof Enemy) {
                Enemy.Type t = ((Enemy)obj).type;
                if (t == Enemy.Type.ZOMBIE) name = "Zombie";
                else if (t == Enemy.Type.SKELETON) name = "Skeleton";
                else if (t == Enemy.Type.HUNTER) name = "Hunter";
            }
            else if (obj instanceof WorldMap.Campfire) name = "Camp";
            else if (obj instanceof WorldMap.Tent) name = "Tent";
            else if (obj instanceof Vendor) name = "Vendor";
            
            g2.setColor(Color.YELLOW);
            g2.setFont(new Font("Arial", Font.BOLD, 11));
            int tx = r.x + (r.width - g2.getFontMetrics().stringWidth(name)) / 2;
            g2.drawString(name, tx, r.y - 8);
        }
        
        // D. Effects
        for(VisualEffect ve : effects) ve.draw(g2);
        
        // C. Camera Controls
        if (btnCamUp != null) {
            g2.setColor(new Color(0,0,0,100));
            g2.fill(btnCamUp); g2.fill(btnCamDown); g2.fill(btnCamLeft); g2.fill(btnCamRight);
            
            g2.setColor(Color.WHITE);
            // Up Arrow
            g2.fillPolygon(new int[]{btnCamUp.x+10, btnCamUp.x+25, btnCamUp.x+40}, new int[]{btnCamUp.y+30, btnCamUp.y+10, btnCamUp.y+30}, 3);
            // Down Arrow
            g2.fillPolygon(new int[]{btnCamDown.x+10, btnCamDown.x+25, btnCamDown.x+40}, new int[]{btnCamDown.y+10, btnCamDown.y+30, btnCamDown.y+10}, 3);
            // Left Arrow
            g2.fillPolygon(new int[]{btnCamLeft.x+30, btnCamLeft.x+10, btnCamLeft.x+30}, new int[]{btnCamLeft.y+10, btnCamLeft.y+25, btnCamLeft.y+40}, 3);
            // Right Arrow
            g2.fillPolygon(new int[]{btnCamRight.x+10, btnCamRight.x+30, btnCamRight.x+10}, new int[]{btnCamRight.y+10, btnCamRight.y+25, btnCamRight.y+40}, 3);
        }

        // Buton SAVE
        if (btnSaveMap != null) drawMenuButton(g2, btnSaveMap, "SAVE", new Color(0, 150, 0));
        
        // Buton X (Manual)
        btnCloseWindow = new Rectangle(getWidth() - 60, 20, 40, 40);
        g2.setColor(new Color(150, 0, 0));
        g2.fillRoundRect(btnCloseWindow.x, btnCloseWindow.y, 40, 40, 10, 10);
        g2.setColor(Color.WHITE); g2.setFont(new Font("Arial", Font.BOLD, 20));
        g2.drawString("X", btnCloseWindow.x + 13, btnCloseWindow.y + 27);
    }
    private void drawItemQuantity(Graphics2D g2, Item item, int x, int y, int size) {
        if(item.quantity > 1) {
            g2.setFont(new Font("Arial", Font.BOLD, 14));
            String qty = String.valueOf(item.quantity);
            
            // Umbra neagra (ca sa se vada pe fundal deschis)
            g2.setColor(Color.BLACK);
            g2.drawString(qty, x + size - 18, y + size - 5);
            
            // Text alb
            g2.setColor(Color.WHITE);
            g2.drawString(qty, x + size - 20, y + size - 7);
        }
    }
    
 // --- FILE SYSTEM ---
    private void refreshMapList() {
        File folder = new File("maps");
        if (!folder.exists()) folder.mkdir();
        
        File[] files = folder.listFiles((dir, name) -> name.endsWith(".map"));
        if (files != null) {
            mapFiles = new ArrayList<>(Arrays.asList(files));
        } else {
            mapFiles = new ArrayList<>();
        }
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
            System.out.println("Map saved: " + name);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadMapAndPlay(File file) {
        try {
            FileInputStream fis = new FileInputStream(file);
            ObjectInputStream ois = new ObjectInputStream(fis);
            WorldMap loadedMap = (WorldMap) ois.readObject();
            ois.close();
            
            this.map = loadedMap;
            
            // Cautam Spawn Point
            int startX = map.cols / 2;
            int startY = map.rows / 2;
            
            for(int r=0; r<map.rows; r++) {
                for(int c=0; c<map.cols; c++) {
                    if (map.grid[r][c] instanceof WorldMap.PlayerStart) {
                        startX = c;
                        startY = r;
                        map.grid[r][c] = null; // Stergem markerul vizual din joc
                        break;
                    }
                }
            }
            
            // Init Player
            player = new Player("Hero", startX, startY);
            map.updateExploration(startX, startY, 10);
            
            // Resetam listele
            activeEnemies = new ArrayList<>();
            effects = new ArrayList<>();
            generateShopStock(); // Ensure shop is initialized
            
            // Re-populam inamicii existenti pe harta salvata
            for(int r=0; r<map.rows; r++) {
                for(int c=0; c<map.cols; c++) {
                    Object o = map.getEntityAt(c, r);
                    if (o instanceof Enemy) {
                        Enemy e = (Enemy)o;
                        e.setPos(c, r); // Refresh position memory
                        activeEnemies.add(e);
                    }
                }
            }
            
            camX = player.visualX - getWidth()/2; camY = player.visualY - getHeight()/2;
            keyW = keyA = keyS = keyD = false; 
            currentState = GameState.PLAYING;
            
        } catch (Exception e) {
            e.printStackTrace();
        }
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
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {
        if (currentState == GameState.EDITOR_SELECT && mapFiles != null) {
            int itemsH = mapFiles.size() * 60;
            if (itemsH > LIST_VIEW_H) { 
                listScrollY += e.getWheelRotation() * 30; // Scroll speed
                if (listScrollY < 0) listScrollY = 0;
                if (listScrollY > itemsH - LIST_VIEW_H) listScrollY = itemsH - LIST_VIEW_H;
                repaint();
            }
        }
    }
    
    // INNER CLASSES
    class CraftingButton { String name, desc; int woodCost, stoneCost, grainCost; Item.Specific type; Rectangle rect; public CraftingButton(String n, String d, int w, int s, int g, Item.Specific t) { name=n; desc=d; woodCost=w; stoneCost=s; grainCost=g; type=t; } }
    class ShopButton { String name, desc; int cost; Item item; Rectangle rect; public ShopButton(String n, String d, int c, Item i) { name=n; desc=d; cost=c; item=i; } }
}