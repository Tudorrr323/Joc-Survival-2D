import java.awt.*;
import java.awt.geom.*;

public class IconRenderer {

    public static void drawTextWithShadow(Graphics2D g2, String text, int x, int y) {
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2.setColor(new Color(0,0,0,220)); g2.drawString(text, x + 2, y + 2); g2.drawString(text, x - 1, y - 1);
        g2.setColor(Color.WHITE); g2.drawString(text, x, y);
    }

    public static void drawVendor(Graphics2D g2, int x, int y, int size) {
        // Scalable Medieval Merchant & Cart
        double s = size / 60.0;
        int merchX = x;
        int merchY = y + (int)(10*s);
        
        // Merchant Body
        g2.setColor(new Color(60, 40, 100)); 
        int[] px = {merchX + (int)(5*s), merchX + (int)(25*s), merchX + (int)(30*s), merchX};
        int[] py = {merchY + (int)(15*s), merchY + (int)(15*s), merchY + (int)(45*s), merchY + (int)(45*s)};
        g2.fillPolygon(px, py, 4);
        
        // Merchant Head
        g2.setColor(Assets.SKIN);
        g2.fillOval(merchX + (int)(8*s), merchY + (int)(5*s), (int)(14*s), (int)(14*s));
        
        // Merchant Hat (Medieval Hood/Cap)
        g2.setColor(new Color(50, 30, 80));
        g2.fillArc(merchX + (int)(6*s), merchY + (int)(2*s), (int)(18*s), (int)(15*s), 0, 180);
        
        // Merchant Beard
        g2.setColor(Color.WHITE);
        g2.fillArc(merchX + (int)(8*s), merchY + (int)(12*s), (int)(14*s), (int)(10*s), 180, 180);

        // Cart
        int cartX = x + (int)(25*s); 
        g2.setColor(new Color(40, 20, 10)); // Wheels
        g2.fillOval(cartX + (int)(5*s), y + size - (int)(20*s), (int)(18*s), (int)(18*s));
        // Removed right wheel per request

        g2.setColor(new Color(120, 80, 40)); // Body
        g2.fillRect(cartX, y + (int)(25*s), size - (int)(20*s), (int)(25*s));
        
        // Canopy
        GradientPaint canopy = new GradientPaint(cartX, y, new Color(200, 180, 150), cartX, y + (int)(20*s), new Color(160, 140, 110));
        g2.setPaint(canopy);
        g2.fillArc(cartX - (int)(5*s), y + (int)(10*s), size - (int)(10*s), (int)(40*s), 0, 180);
    }
    
    public static void drawGoldCoin(Graphics2D g2, int x, int y, int size) {
        g2.setColor(new Color(255, 215, 0)); // Gold
        g2.fillOval(x+10, y+10, size-20, size-20);
        g2.setColor(new Color(218, 165, 32)); // Dark Gold Outline
        g2.setStroke(new BasicStroke(2));
        g2.drawOval(x+10, y+10, size-20, size-20);
        g2.setFont(new Font("Arial", Font.BOLD, 14));
        g2.drawString("$", x+size/2-4, y+size/2+6);
    }
    
    public static void drawPotion(Graphics2D g2, int x, int y, int size) {
        g2.setColor(Color.WHITE); 
        g2.drawRoundRect(x+20, y+15, 20, 30, 5, 5); // Sticla
        g2.fillRect(x+25, y+10, 10, 5); // Dop
        g2.setColor(Color.RED); 
        g2.fillRoundRect(x+22, y+25, 16, 18, 5, 5); // Lichid
        g2.setColor(new Color(255,255,255,100));
        g2.fillOval(x+25, y+28, 5, 5); // Reflexie
    }

    public static void drawWaterIcon(Graphics2D g2, int x, int y, int size, int tick) {
        GradientPaint waterGp = new GradientPaint(x, y, Assets.WATER_LIGHT, x + size, y + size, Assets.WATER_DEEP);
        g2.setPaint(waterGp);
        g2.fillRoundRect(x + 5, y + 5, size - 10, size - 10, 10, 10);
        g2.setColor(new Color(255, 255, 255, 60));
        int wO = (int)(Math.sin((tick + x) * 0.1) * (size / 12.0));
        g2.drawLine(x + 10, y + size / 2 + wO, x + size - 10, y + size / 2 + wO);
    }

    public static void drawWaterTile(Graphics2D g2, int x, int y, int size, int tick) {
        GradientPaint waterGp = new GradientPaint(x, y, Assets.WATER_LIGHT, x + size, y + size, Assets.WATER_DEEP);
        g2.setPaint(waterGp);
        g2.fillRect(x, y, size, size); // Full fill, no padding
        g2.setColor(new Color(255, 255, 255, 40));
        int wO = (int)(Math.sin((tick + x) * 0.1) * 5);
        g2.drawLine(x, y + 20 + wO, x + size, y + 20 + wO);
    }

    public static void drawSpawnIcon(Graphics2D g2, int x, int y, int size) {
        // Modern Portal/Spawn look
        g2.setColor(new Color(0, 150, 255, 80));
        g2.fillOval(x + 5, y + 5, size - 10, size - 10);
        g2.setStroke(new BasicStroke(3));
        g2.setColor(new Color(0, 200, 255));
        g2.drawOval(x + 10, y + 10, size - 20, size - 20);
        g2.setColor(Color.WHITE);
        int cx = x + size / 2;
        int cy = y + size / 2;
        g2.drawLine(cx, cy - 10, cx, cy + 10);
        g2.drawLine(cx - 5, cy - 5, cx, cy - 10);
        g2.drawLine(cx + 5, cy - 5, cx, cy - 10);
        g2.setStroke(new BasicStroke(1));
    }

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
        g2.setColor(new Color(40, 40, 45, 100)); g2.drawPolygon(px, py, 5); g2.setColor(new Color(0,0,0,60)); g2.drawLine(x+20, y+20, x+30, y+35);
    }

    public static void drawGrain(Graphics2D g2, int x, int y, int size, Enums.Quality q) {
        g2.setColor(new Color(200, 160, 0)); g2.setStroke(new BasicStroke(2));
        g2.drawLine(x + 15, y + size-5, x + 10, y + 15); g2.drawLine(x + size/2, y + size-5, x + size/2, y + 10); g2.drawLine(x + size-15, y + size-5, x + size-10, y + 20);
        g2.setStroke(new BasicStroke(1)); g2.setColor(new Color(255, 215, 0));
        g2.fillOval(x + 8, y + 10, 6, 12); g2.fillOval(x + size/2 - 3, y + 5, 6, 14); g2.fillOval(x + size-13, y + 15, 6, 12);
    }

    public static void drawEnemy(Graphics2D g2, Enemy e, int x, int y, int size) {
        if (size > 55) {
            g2.setColor(new Color(0,0,0,150)); g2.fillRoundRect(x+10, y-8, size-20, 5, 2, 2);
            g2.setColor(Color.RED); float hpPercent = (float)e.health / e.maxHealth; if(hpPercent < 0) hpPercent = 0;
            g2.fillRoundRect(x+10, y-8, (int)((size-20) * hpPercent), 5, 2, 2);
        }
        switch(e.type) {
            case ZOMBIE: drawZombie(g2, x, y, size); break;
            case SKELETON: drawSkeleton(g2, x, y, size); break;
            case RAT: drawRat(g2, x, y, size); break;
            case HUNTER: drawHunter(g2, x, y, size); break;
        }
    }

    private static void drawZombie(Graphics2D g2, int x, int y, int size) { 
        double s = size / 60.0;
        GradientPaint skin = new GradientPaint(x, y, new Color(100, 140, 60), x+size, y+size, new Color(40, 60, 20)); 
        g2.setPaint(skin); 
        g2.fillRoundRect(x + (int)(15*s), y + (int)(10*s), size - (int)(30*s), (int)(40*s), (int)(8*s), (int)(8*s)); 
        g2.setColor(new Color(60, 80, 100)); 
        g2.fillRect(x + (int)(15*s), y + (int)(35*s), size - (int)(30*s), (int)(15*s)); 
        g2.setColor(new Color(80, 100, 120)); 
        g2.fillRect(x + (int)(15*s), y + (int)(15*s), (int)(30*s), (int)(20*s)); 
        g2.setPaint(skin); 
        g2.fillRect(x + (int)(5*s), y + (int)(25*s), (int)(10*s), (int)(8*s)); 
        g2.fillRect(x + size - (int)(15*s), y + (int)(25*s), (int)(10*s), (int)(8*s)); 
        g2.setColor(Color.RED); 
        g2.fillOval(x + (int)(20*s), y + (int)(18*s), (int)(6*s), (int)(6*s)); 
        g2.fillOval(x + (int)(34*s), y + (int)(18*s), (int)(6*s), (int)(6*s)); 
    }

    private static void drawSkeleton(Graphics2D g2, int x, int y, int size) { 
        double s = size / 60.0;
        g2.setColor(new Color(220, 220, 220)); 
        g2.fillOval(x + (int)(20*s), y + (int)(8*s), (int)(20*s), (int)(22*s)); 
        g2.fillRect(x + (int)(28*s), y + (int)(30*s), (int)(4*s), (int)(15*s)); 
        g2.setColor(Color.WHITE); 
        g2.drawArc(x + (int)(20*s), y + (int)(32*s), (int)(20*s), (int)(10*s), 0, 180); 
        g2.drawArc(x + (int)(20*s), y + (int)(38*s), (int)(20*s), (int)(10*s), 0, 180); 
        g2.setColor(Color.BLACK); 
        g2.fillOval(x + (int)(24*s), y + (int)(14*s), (int)(5*s), (int)(5*s)); 
        g2.fillOval(x + (int)(31*s), y + (int)(14*s), (int)(5*s), (int)(5*s)); 
    }

    private static void drawRat(Graphics2D g2, int x, int y, int size) { 
        double s = size / 60.0;
        g2.setColor(Color.DARK_GRAY); 
        g2.fillOval(x + (int)(15*s), y + (int)(35*s), (int)(30*s), (int)(15*s)); 
        g2.setColor(Color.GRAY); 
        g2.fillOval(x + (int)(12*s), y + (int)(32*s), (int)(8*s), (int)(8*s)); 
        g2.fillOval(x + (int)(32*s), y + (int)(32*s), (int)(8*s), (int)(8*s)); 
        g2.setColor(Color.PINK); 
        g2.drawArc(x + (int)(40*s), y + (int)(30*s), (int)(15*s), (int)(20*s), 180, 180); 
    }

    private static void drawHunter(Graphics2D g2, int x, int y, int size) { 
        double s = size / 60.0;
        g2.setColor(new Color(90, 70, 50)); 
        g2.fillRoundRect(x + (int)(15*s), y + (int)(15*s), (int)(30*s), (int)(35*s), (int)(10*s), (int)(10*s)); 
        g2.setColor(new Color(40, 30, 20)); 
        int[] px = {x + (int)(20*s), x + (int)(40*s), x + (int)(30*s)}; 
        int[] py = {y + (int)(15*s), y + (int)(15*s), y}; 
        g2.fillPolygon(px, py, 3); 
        g2.setColor(Color.YELLOW); 
        g2.fillOval(x + (int)(24*s), y + (int)(18*s), (int)(4*s), (int)(4*s)); 
        g2.fillOval(x + (int)(32*s), y + (int)(18*s), (int)(4*s), (int)(4*s)); 
    }

    public static void drawWoodIcon(Graphics2D g2, int x, int y, int size) { drawWood(g2, x, y, size); }
    public static void drawStoneIcon(Graphics2D g2, int x, int y, int size) { drawStone(g2, x, y, size); }
    
    public static void drawWood(Graphics2D g2, int x, int y, int size) { g2.setColor(new Color(90, 60, 30)); int[] px = {x+5, x+size-15, x+size-5, x+15}; int[] py = {y+15, y+5, y+size-15, y+size-5}; g2.fillPolygon(px, py, 4); g2.setColor(new Color(160, 120, 60)); g2.fillOval(x+12, y+size-18, 12, 8); g2.setColor(new Color(120, 90, 40)); g2.drawOval(x+12, y+size-18, 12, 8); }
    public static void drawStone(Graphics2D g2, int x, int y, int size) { g2.setColor(new Color(120, 120, 125)); int[] px = {x+10, x+size-10, x+size-5, x+15}; int[] py = {y+15, y+10, y+size-10, y+size-5}; g2.fillPolygon(px, py, 4); g2.setColor(Color.WHITE); g2.drawLine(x+15, y+15, x+25, y+15); }
    public static void drawBread(Graphics2D g2, int x, int y, int size) { g2.setColor(Assets.BREAD_COLOR); g2.fillRoundRect(x+5, y+15, size-10, size-25, 15, 15); g2.setColor(Assets.BREAD_CRUST); g2.setStroke(new BasicStroke(2)); g2.drawRoundRect(x+5, y+15, size-10, size-25, 15, 15); g2.drawLine(x+15, y+20, x+18, y+size-15); g2.drawLine(x+25, y+20, x+28, y+size-15); g2.setStroke(new BasicStroke(1)); }
    public static void drawHelmet(Graphics2D g2, int x, int y, int size) { g2.setColor(Color.GRAY); g2.fillArc(x+10, y+10, size-20, size-20, 0, 180); g2.fillRect(x+15, y+size/2, 6, 15); g2.fillRect(x+size-21, y+size/2, 6, 15); g2.setColor(Color.LIGHT_GRAY); g2.fillRect(x+22, y+15, 4, 20); }
    public static void drawChestplate(Graphics2D g2, int x, int y, int size) { g2.setColor(Color.GRAY); int[] bx = {x + 15, x + size - 15, x + size - 10, x + 10}; int[] by = {y + 10, y + 10, y + size - 10, y + size - 10}; g2.fillPolygon(bx, by, 4); g2.setColor(new Color(110, 110, 110)); g2.fillOval(x + 2, y + 5, 18, 18); g2.fillOval(x + size - 20, y + 5, 18, 18); g2.setColor(Color.DARK_GRAY); g2.drawOval(x + 2, y + 5, 18, 18); g2.drawOval(x + size - 20, y + 5, 18, 18); g2.setColor(Color.GRAY); g2.fillRect(x + 5, y + 20, 8, 20); g2.fillRect(x + size - 13, y + 20, 8, 20); g2.setColor(new Color(160, 160, 170)); g2.fillRect(x + 20, y + 15, size - 40, 5); g2.fillRect(x + 25, y + 25, 4, 15); }
    public static void drawArmorPlaceholder(Graphics2D g2, int x, int y, int size, int type) { g2.setColor(new Color(255, 255, 255, 30)); g2.setStroke(new BasicStroke(2)); if (type == 0) { g2.drawRect(x + 15, y + 10, size - 30, size - 25); g2.drawLine(x + 15, y + 10, x + size/2, y + 5); g2.drawLine(x + size/2, y + 5, x + size - 15, y + 10); g2.drawLine(x + 15, y + 25, x + size - 15, y + 25); g2.drawLine(x + size/2, y + 10, x + size/2, y + size - 15); g2.drawOval(x + 20, y + 35, 2, 2); g2.drawOval(x + size - 22, y + 35, 2, 2); } else if (type == 1) { g2.drawRect(x + 20, y + 15, size - 40, size - 30); g2.drawArc(x + 5, y + 10, 15, 20, 90, 180); g2.drawArc(x + size - 20, y + 10, 15, 20, 270, 180); g2.drawLine(x + 10, y + 30, x + 10, y + 50); g2.drawLine(x + size - 10, y + 30, x + size - 10, y + 50); } else if (type == 2) { g2.drawRect(x + 15, y + 15, size - 30, 8); g2.drawRect(x + 18, y + 23, 12, 25); g2.drawRect(x + size - 30, y + 23, 12, 25); g2.drawOval(x + 17, y + 35, 14, 6); g2.drawOval(x + size - 31, y + 35, 14, 6); } else if (type == 3) { g2.drawRect(x + 15, y + 35, 12, 12); g2.fillRoundRect(x + 12, y + 47, 18, 8, 5, 5); g2.drawRect(x + size - 27, y + 35, 12, 12); g2.fillRoundRect(x + size - 30, y + 47, 18, 8, 5, 5); } g2.setStroke(new BasicStroke(1)); }
    public static void drawPants(Graphics2D g2, int x, int y, int size) { g2.setColor(Color.GRAY); g2.fillRect(x+15, y+10, size-30, 15); g2.fillRect(x+15, y+25, 12, 20); g2.fillRect(x+size-27, y+25, 12, 20); }
    public static void drawBoots(Graphics2D g2, int x, int y, int size) { g2.setColor(Color.GRAY); g2.fillRoundRect(x+10, y+25, 12, 15, 5, 5); g2.fillRect(x+10, y+35, 18, 10); g2.fillRoundRect(x+size-22, y+25, 12, 15, 5, 5); g2.fillRect(x+size-22, y+35, 18, 10); }
    public static void drawHeart(Graphics2D g2, int x, int y, int size) { GradientPaint hp = new GradientPaint(x, y, new Color(255, 50, 50), x, y+size, new Color(150, 0, 0)); g2.setPaint(hp); int[] px = {x + size/2, x + size, x + size/2, x}; int[] py = {y + size, y + size/2 - 5, y + 5, y + size/2 - 5}; g2.fillPolygon(px, py, 4); g2.setColor(Color.WHITE); g2.fillOval(x+size/2+2, y+10, 4, 4); }
    public static void drawBuilding(Graphics2D g2, Building b, int x, int y, int size) { if (b.type == Building.Type.FOUNTAIN) drawFountain(g2, x, y, size); else drawMonument(g2, x, y, size); if (!b.isReady()) { g2.setFont(new Font("Arial", Font.BOLD, 16)); String time = b.getSecondsLeft() + "s"; int sw = g2.getFontMetrics().stringWidth(time); g2.setColor(new Color(0,0,0,180)); g2.fillRoundRect(x + size/2 - sw/2 - 3, y - 15, sw + 6, 18, 5, 5); g2.setColor(Color.YELLOW); g2.drawString(time, x + size/2 - sw/2, y); } }
    public static void drawFountain(Graphics2D g2, int x, int y, int size) { g2.setColor(Assets.STONE_BASE); g2.fillOval(x+5, y+10, size-10, size-20); g2.setColor(new Color(60, 60, 70)); g2.drawOval(x+5, y+10, size-10, size-20); GradientPaint water = new GradientPaint(x, y, Assets.MAGIC_WATER, x+size, y+size, new Color(0, 100, 200)); g2.setPaint(water); g2.fillOval(x+12, y+18, size-24, size-36); g2.setColor(new Color(255, 255, 255, 150)); long time = System.currentTimeMillis() / 200; if(time % 2 == 0) g2.fillOval(x+size/2 - 2, y+size/2 - 5, 4, 4); else g2.fillOval(x+size/2 + 5, y+size/2 - 2, 3, 3); }
    public static void drawMonument(Graphics2D g2, int x, int y, int size) { g2.setColor(new Color(80, 80, 80)); int[] px = {x+10, x+size-10, x+size-5, x+5}; int[] py = {y+size-15, y+size-15, y+size-5, y+size-5}; g2.fillPolygon(px, py, 4); g2.setColor(new Color(100, 100, 100)); int[] ox = {x+15, x+size-15, x+size/2}; int[] oy = {y+size-15, y+size-15, y-5}; g2.fillPolygon(ox, oy, 3); g2.setColor(new Color(255, 100, 0, 200)); g2.fillOval(x+size/2-3, y+size/2, 6, 6); }
    public static void drawSword(Graphics2D g2, int x, int y, int size) { g2.setColor(new Color(220, 220, 230)); g2.fillRect(x+size/2-2, y+5, 4, size-20); g2.setColor(new Color(180, 140, 20)); g2.fillRect(x+size/2-8, y+size-15, 16, 4); g2.setColor(new Color(80, 40, 10)); g2.fillRect(x+size/2-2, y+size-12, 4, 8); }
    public static void drawAxe(Graphics2D g2, int x, int y, int size) { g2.setColor(new Color(100, 60, 20)); g2.fillRect(x+size/2-2, y+5, 4, size-10); g2.setColor(Color.LIGHT_GRAY); g2.fillArc(x+size/2-2, y+5, 18, 18, 90, 180); }
    public static void drawPickaxe(Graphics2D g2, int x, int y, int size) { g2.setColor(new Color(100, 60, 20)); g2.fillRect(x+size/2-2, y+5, 4, size-10); g2.setColor(Color.GRAY); g2.setStroke(new BasicStroke(3)); g2.drawArc(x+size/2-12, y+5, 30, 15, 0, 180); g2.setStroke(new BasicStroke(1)); }
    public static void drawCampfire(Graphics2D g2, int x, int y, int size) { g2.setColor(new Color(60, 40, 20)); g2.setStroke(new BasicStroke(3)); g2.drawLine(x + 15, y + size - 15, x + size - 15, y + size - 25); g2.drawLine(x + size - 15, y + size - 15, x + 15, y + size - 25); g2.setStroke(new BasicStroke(1)); long time = System.currentTimeMillis(); double flicker = Math.sin(time * 0.02) * 3; int fireH = size / 2 + (int)flicker; int fireY = y + size - 20 - fireH; g2.setColor(Assets.FIRE_ORANGE); g2.fillOval(x + 15 - (int)flicker/2, fireY, 30 + (int)flicker, fireH); g2.setColor(Assets.FIRE_YELLOW); g2.fillOval(x + 22, fireY + 10, 16, fireH - 15); g2.setColor(new Color(100, 100, 100, 150)); int smokeOffset = (int)((time / 50) % 40); g2.fillOval(x + size/2, y + size/2 - 20 - smokeOffset, 4, 4); }
    public static void drawTent(Graphics2D g2, int x, int y, int size) { int[] px = {x + 10, x + size/2, x + size - 10}; int[] py = {y + size - 10, y + 10, y + size - 10}; g2.setColor(Assets.TENT_BASE); g2.fillPolygon(px, py, 3); int[] ox = {x + size/2 - 5, x + size/2, x + size/2 + 5}; int[] oy = {y + size - 10, y + size/2 + 10, y + size - 10}; g2.setColor(new Color(60, 50, 40)); g2.fillPolygon(ox, oy, 3); g2.setColor(Assets.TENT_DARK); g2.drawPolygon(px, py, 3); }
}