/*
 * Project: Joc Survival 2D
 * Author:  Tudor Baranga(Tudorrr323)
 * Date:    Jan 23, 2026
 *
 * Copyright (c) 2026 Tudor Baranga(Tudorrr323). All rights reserved.
 * This code is proprietary software. Use is strictly prohibited without permission.
 */
package utils;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

public class Assets {
    // --- UI IMAGES ---
    public static BufferedImage UI_MENU_BG;
    public static BufferedImage UI_BUTTON;
    public static BufferedImage UI_BUTTON_HOVER;
    public static BufferedImage UI_RIBBON;
    public static BufferedImage UI_SPECIAL_PAPER;
    
    public static BufferedImage UI_WATER_BG;
    public static BufferedImage UI_PAPER;
    public static BufferedImage UI_BANNER;
    public static BufferedImage UI_MODAL;
    
    public static BufferedImage UI_BTN_BLUE, UI_BTN_BLUE_PR;
    public static BufferedImage UI_BTN_RED, UI_BTN_RED_PR;
    public static BufferedImage UI_BTN_YELLOW, UI_BTN_YELLOW_PR;
    public static BufferedImage UI_BTN_DISABLED;
    
    // --- CLOSE BUTTON ---
    public static BufferedImage UI_BTN_CLOSE;
    public static BufferedImage UI_BTN_CLOSE_HOVER;
    public static BufferedImage UI_BTN_CLOSE_PRESSED;
    
    public static BufferedImage UI_BTN_DELETE, UI_BTN_DELETE_PR;
    
    public static BufferedImage UI_BTN_BG;
    
    public static BufferedImage UI_AVATAR_01, UI_AVATAR_02, UI_AVATAR_03, UI_AVATAR_05;
    
    // RED
    public static BufferedImage UI_AVATAR_06, UI_AVATAR_07, UI_AVATAR_08, UI_AVATAR_10;
    // YELLOW
    public static BufferedImage UI_AVATAR_11, UI_AVATAR_12, UI_AVATAR_13, UI_AVATAR_15;
    // PURPLE
    public static BufferedImage UI_AVATAR_16, UI_AVATAR_17, UI_AVATAR_18, UI_AVATAR_20;
    // BLACK
    public static BufferedImage UI_AVATAR_21, UI_AVATAR_22, UI_AVATAR_23, UI_AVATAR_25;
    
    public static BufferedImage UI_RIBBONS_SMALL;
    
    public static java.util.List<BufferedImage> HERO_AVATARS = new java.util.ArrayList<>();
    public static java.util.List<BufferedImage> RIBBON_VARIANTS = new java.util.ArrayList<>();
    
    // --- UNITS ---
    public static BufferedImage WARRIOR_IDLE;
    public static BufferedImage WARRIOR_RUN;
    public static BufferedImage WARRIOR_ATTACK_1;
    public static BufferedImage WARRIOR_ATTACK_2;
    public static BufferedImage WARRIOR_GUARD;
    
    // --- CURSORS ---
    public static BufferedImage CURSOR_DEFAULT;
    public static BufferedImage CURSOR_POINTER;
    public static BufferedImage CURSOR_DISABLED;
    
    // --- FONTS ---
    public static Font PIXEL_FONT;

    public static void loadAssets() {
        try {
            UI_MENU_BG = ImageIO.read(new File("Tiny Swords (Free Pack)/UI Elements/UI Elements/Wood Table/WoodTable.png"));
            UI_BUTTON = ImageIO.read(new File("Tiny Swords (Update 010)/UI/Buttons/Button_Blue_3Slides.png"));
            UI_BUTTON_HOVER = ImageIO.read(new File("Tiny Swords (Update 010)/UI/Buttons/Button_Hover_3Slides.png"));
            UI_RIBBON = ImageIO.read(new File("Tiny Swords (Update 010)/UI/Ribbons/Ribbon_Red_3Slides.png"));
            
            BufferedImage rawSpecial = ImageIO.read(new File("Tiny Swords (Free Pack)/UI Elements/UI Elements/Papers/SpecialPaper.png"));
            if (rawSpecial != null) {
                UI_SPECIAL_PAPER = new BufferedImage(192, 192, BufferedImage.TYPE_INT_ARGB);
                Graphics2D g = UI_SPECIAL_PAPER.createGraphics();
                int[] srcOffsets = {0, 128, 256};
                int[] dstOffsets = {0, 64, 128};
                for(int r=0; r<3; r++) {
                    for(int c=0; c<3; c++) {
                        BufferedImage cell = rawSpecial.getSubimage(srcOffsets[c], srcOffsets[r], 64, 64);
                        g.drawImage(cell, dstOffsets[c], dstOffsets[r], null);
                    }
                }
                g.dispose();
            }
            
            UI_WATER_BG = ImageIO.read(new File("Tiny Swords (Free Pack)/Terrain/Tileset/Water Background color.png"));
            UI_PAPER = ImageIO.read(new File("Tiny Swords (Update 010)/UI/Banners/Carved_9Slides.png")); 
            UI_BANNER = ImageIO.read(new File("Tiny Swords (Update 010)/UI/Ribbons/Ribbon_Blue_3Slides.png"));
            UI_MODAL = ImageIO.read(new File("Tiny Swords (Update 010)/UI/Banners/Banner_Connection_Up.png"));
            
            UI_BTN_BLUE = ImageIO.read(new File("Tiny Swords (Update 010)/UI/Buttons/Button_Blue_3Slides.png"));
            UI_BTN_BLUE_PR = ImageIO.read(new File("Tiny Swords (Update 010)/UI/Buttons/Button_Blue_3Slides_Pressed.png"));
            UI_BTN_RED = ImageIO.read(new File("Tiny Swords (Update 010)/UI/Buttons/Button_Red_3Slides.png"));
            UI_BTN_RED_PR = ImageIO.read(new File("Tiny Swords (Update 010)/UI/Buttons/Button_Red_3Slides_Pressed.png"));
            UI_BTN_YELLOW = ImageIO.read(new File("Tiny Swords (Update 010)/UI/Buttons/Button_Hover_3Slides.png"));
            // Since there's no pressed state for hover, we'll use blue pressed or just offset
            UI_BTN_YELLOW_PR = UI_BTN_BLUE_PR; 
            UI_BTN_DISABLED = ImageIO.read(new File("Tiny Swords (Update 010)/UI/Buttons/Button_Disable_3Slides.png"));
            
            UI_BTN_CLOSE = ImageIO.read(new File("Tiny Swords (Update 010)/UI/Icons/Regular_01.png"));
            UI_BTN_CLOSE_HOVER = ImageIO.read(new File("Tiny Swords (Update 010)/UI/Icons/Disable_01.png"));
            UI_BTN_CLOSE_PRESSED = ImageIO.read(new File("Tiny Swords (Update 010)/UI/Icons/Pressed_01.png"));
            UI_BTN_DELETE = ImageIO.read(new File("Tiny Swords (Update 010)/UI/Icons/Regular_09.png"));
            UI_BTN_DELETE_PR = ImageIO.read(new File("Tiny Swords (Update 010)/UI/Icons/Pressed_09.png"));
            UI_BTN_BG = ImageIO.read(new File("Tiny Swords (Update 010)/UI/Banners/Carved_Regular.png"));
            
            // Blue
            UI_AVATAR_01 = ImageIO.read(new File("Tiny Swords (Free Pack)/UI Elements/UI Elements/Human Avatars/Avatars_01.png"));
            UI_AVATAR_02 = ImageIO.read(new File("Tiny Swords (Free Pack)/UI Elements/UI Elements/Human Avatars/Avatars_02.png"));
            UI_AVATAR_03 = ImageIO.read(new File("Tiny Swords (Free Pack)/UI Elements/UI Elements/Human Avatars/Avatars_03.png"));
            UI_AVATAR_05 = ImageIO.read(new File("Tiny Swords (Free Pack)/UI Elements/UI Elements/Human Avatars/Avatars_05.png"));
            
            // Red
            UI_AVATAR_06 = ImageIO.read(new File("Tiny Swords (Free Pack)/UI Elements/UI Elements/Human Avatars/Avatars_06.png"));
            UI_AVATAR_07 = ImageIO.read(new File("Tiny Swords (Free Pack)/UI Elements/UI Elements/Human Avatars/Avatars_07.png"));
            UI_AVATAR_08 = ImageIO.read(new File("Tiny Swords (Free Pack)/UI Elements/UI Elements/Human Avatars/Avatars_08.png"));
            UI_AVATAR_10 = ImageIO.read(new File("Tiny Swords (Free Pack)/UI Elements/UI Elements/Human Avatars/Avatars_10.png"));
            
            // Yellow
            UI_AVATAR_11 = ImageIO.read(new File("Tiny Swords (Free Pack)/UI Elements/UI Elements/Human Avatars/Avatars_11.png"));
            UI_AVATAR_12 = ImageIO.read(new File("Tiny Swords (Free Pack)/UI Elements/UI Elements/Human Avatars/Avatars_12.png"));
            UI_AVATAR_13 = ImageIO.read(new File("Tiny Swords (Free Pack)/UI Elements/UI Elements/Human Avatars/Avatars_13.png"));
            UI_AVATAR_15 = ImageIO.read(new File("Tiny Swords (Free Pack)/UI Elements/UI Elements/Human Avatars/Avatars_15.png"));
            
            // Purple
            UI_AVATAR_16 = ImageIO.read(new File("Tiny Swords (Free Pack)/UI Elements/UI Elements/Human Avatars/Avatars_16.png"));
            UI_AVATAR_17 = ImageIO.read(new File("Tiny Swords (Free Pack)/UI Elements/UI Elements/Human Avatars/Avatars_17.png"));
            UI_AVATAR_18 = ImageIO.read(new File("Tiny Swords (Free Pack)/UI Elements/UI Elements/Human Avatars/Avatars_18.png"));
            UI_AVATAR_20 = ImageIO.read(new File("Tiny Swords (Free Pack)/UI Elements/UI Elements/Human Avatars/Avatars_20.png"));
            
            // Black
            UI_AVATAR_21 = ImageIO.read(new File("Tiny Swords (Free Pack)/UI Elements/UI Elements/Human Avatars/Avatars_21.png"));
            UI_AVATAR_22 = ImageIO.read(new File("Tiny Swords (Free Pack)/UI Elements/UI Elements/Human Avatars/Avatars_22.png"));
            UI_AVATAR_23 = ImageIO.read(new File("Tiny Swords (Free Pack)/UI Elements/UI Elements/Human Avatars/Avatars_23.png"));
            UI_AVATAR_25 = ImageIO.read(new File("Tiny Swords (Free Pack)/UI Elements/UI Elements/Human Avatars/Avatars_25.png"));
            
            updateAvatars(0); // Default Blue
            
            UI_RIBBONS_SMALL = ImageIO.read(new File("Tiny Swords (Free Pack)/UI Elements/UI Elements/Swords/Swords.png"));
            
            RIBBON_VARIANTS.clear();
            if (UI_RIBBONS_SMALL != null) {
                int count = 5; 
                int rh = 128; // 640 / 5
                
                for(int i=0; i<count; i++) {
                    int y = i * rh;
                    
                    // Stitch Hilt(0-128), Middle(192-256) x3, Tip(320-448)
                    if (y + rh <= UI_RIBBONS_SMALL.getHeight()) {
                        BufferedImage combined = new BufferedImage(128 + 64*3 + 128, rh, BufferedImage.TYPE_INT_ARGB);
                        Graphics2D g = combined.createGraphics();
                        
                        BufferedImage part1 = UI_RIBBONS_SMALL.getSubimage(0, y, 128, rh);
                        BufferedImage part2 = UI_RIBBONS_SMALL.getSubimage(192, y, 64, rh);
                        BufferedImage part3 = UI_RIBBONS_SMALL.getSubimage(320, y, 128, rh);
                        
                        g.drawImage(part1, 0, 0, null);
                        g.drawImage(part2, 128, 0, null);
                        g.drawImage(part2, 128 + 64, 0, null);
                        g.drawImage(part2, 128 + 128, 0, null);
                        g.drawImage(part3, 128 + 192, 0, null);
                        g.dispose();
                        
                        RIBBON_VARIANTS.add(combined);
                    }
                }
            }
            
            WARRIOR_IDLE = ImageIO.read(new File("Tiny Swords (Free Pack)/Units/Blue Units/Warrior/Warrior_Idle.png"));
            WARRIOR_RUN = ImageIO.read(new File("Tiny Swords (Free Pack)/Units/Blue Units/Warrior/Warrior_Run.png"));
            WARRIOR_ATTACK_1 = ImageIO.read(new File("Tiny Swords (Free Pack)/Units/Blue Units/Warrior/Warrior_Attack1.png"));
            WARRIOR_ATTACK_2 = ImageIO.read(new File("Tiny Swords (Free Pack)/Units/Blue Units/Warrior/Warrior_Attack2.png"));
            WARRIOR_GUARD = ImageIO.read(new File("Tiny Swords (Free Pack)/Units/Blue Units/Warrior/Warrior_Guard.png"));
            
            // Load Cursors as BufferedImages
            CURSOR_DEFAULT = ImageIO.read(new File("Tiny Swords (Free Pack)/UI Elements/UI Elements/Cursors/Cursor_01.png"));
            CURSOR_POINTER = ImageIO.read(new File("Tiny Swords (Free Pack)/UI Elements/UI Elements/Cursors/Cursor_02.png"));
            CURSOR_DISABLED = ImageIO.read(new File("Tiny Swords (Free Pack)/UI Elements/UI Elements/Cursors/Cursor_03.png"));
            
            try {
                PIXEL_FONT = Font.createFont(Font.TRUETYPE_FONT, new File("Text Font/Pixel Game.otf")).deriveFont(24f);
                GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
                ge.registerFont(PIXEL_FONT);
            } catch (Exception e) {
                System.err.println("Could not load font, using fallback.");
                PIXEL_FONT = new Font("Arial", Font.BOLD, 24);
            }
            
            System.out.println("Assets loaded successfully!");
        } catch (IOException e) {
            System.err.println("Failed to load assets!");
            e.printStackTrace();
        }
    }

    private static BufferedImage toBufferedImage(Image img) {
        if (img instanceof BufferedImage) return (BufferedImage) img;
        BufferedImage bimage = new BufferedImage(img.getWidth(null), img.getHeight(null), BufferedImage.TYPE_INT_ARGB);
        Graphics2D bGr = bimage.createGraphics();
        bGr.drawImage(img, 0, 0, null);
        bGr.dispose();
        return bimage;
    }

    public static final Color UI_DARK = new Color(20, 20, 25, 240);
    public static final Color UI_BORDER = new Color(100, 100, 110);
    public static final Color TEXT_SHADOW = new Color(0, 0, 0, 200); 
    public static final Color TEXT_WHITE = new Color(255, 255, 255);
    public static final Color TEXT_GREEN = new Color(50, 255, 50);
    public static final Color TEXT_RED = new Color(255, 80, 80);

    public static final Color GRASS_1 = new Color(30, 100, 30);
    public static final Color GRASS_2 = new Color(25, 90, 25);
    public static final Color WATER = new Color(50, 100, 220); // Pt compatibilitate
    public static final Color WATER_DEEP = new Color(20, 60, 180);
    public static final Color WATER_LIGHT = new Color(60, 120, 250);
    
    public static final Color WOOD_DARK = new Color(60, 40, 10);
    public static final Color WOOD_LIGHT = new Color(140, 100, 50);
    public static final Color LEAF_DARK = new Color(0, 60, 0);
    public static final Color LEAF_LIGHT = new Color(60, 180, 40);
    
    public static final Color WOOD_ICON = new Color(120, 80, 40);
    public static final Color STONE_ICON = new Color(140, 140, 140);
    public static final Color BREAD_COLOR = new Color(210, 180, 140);
    public static final Color BREAD_CRUST = new Color(160, 100, 50);
    
    public static final Color SKIN = new Color(255, 210, 170);
    public static final Color ENEMY_RED = new Color(220, 50, 50);
    
    public static final Color ZOMBIE_DARK = new Color(60, 80, 20);
    public static final Color ZOMBIE_LIGHT = new Color(120, 160, 50);
    public static final Color SKELETON_WHITE = new Color(230, 230, 230);
    public static final Color SKELETON_GRAY = new Color(180, 180, 180);
    
    public static final Color STONE_BASE = new Color(80, 80, 80);
    public static final Color MAGIC_WATER = new Color(0, 255, 255);
    
 // --- CAMP COLORS ---
    public static final Color TENT_BASE = new Color(210, 190, 150); // Beige canvas
    public static final Color TENT_DARK = new Color(160, 140, 110);
    public static final Color FIRE_ORANGE = new Color(255, 100, 0);
    public static final Color FIRE_YELLOW = new Color(255, 220, 0);
    public static final Color ASH = new Color(50, 50, 50);
    
    // --- NEW ENEMY COLORS ---
    public static final Color RAT_DARK = new Color(60, 60, 60);
    public static final Color RAT_LIGHT = new Color(100, 100, 100);
    public static final Color HUNTER_CAMO = new Color(50, 70, 30);
    public static final Color WITCH_ROBE = new Color(70, 20, 90);
    public static final Color WITCH_SKIN = new Color(200, 200, 220);

    public static void updatePlayerSprites(int charIndex, int colorIndex) {
        String[] colors = {"Blue", "Red", "Yellow", "Purple", "Black"};
        String[] types = {"Warrior", "Lancer", "Archer", "Pawn"};
        
        if (colorIndex < 0 || colorIndex >= colors.length) colorIndex = 0;
        if (charIndex < 0 || charIndex >= types.length) charIndex = 0;
        
        String color = colors[colorIndex];
        String type = types[charIndex];
        
        String path = "Tiny Swords (Free Pack)/Units/" + color + " Units/" + type + "/";
        
        try {
            File fIdle = new File(path + type + "_Idle.png");
            if (fIdle.exists()) WARRIOR_IDLE = ImageIO.read(fIdle);
            
            File fRun = new File(path + type + "_Run.png");
            if (fRun.exists()) WARRIOR_RUN = ImageIO.read(fRun);
            
            if (type.equals("Warrior")) {
                File fA1 = new File(path + type + "_Attack1.png"); if(fA1.exists()) WARRIOR_ATTACK_1 = ImageIO.read(fA1);
                File fA2 = new File(path + type + "_Attack2.png"); if(fA2.exists()) WARRIOR_ATTACK_2 = ImageIO.read(fA2);
                File fG = new File(path + type + "_Guard.png"); if(fG.exists()) WARRIOR_GUARD = ImageIO.read(fG);
            } else if (type.equals("Archer")) {
                File fShoot = new File(path + type + "_Shoot.png");
                if(fShoot.exists()) {
                    WARRIOR_ATTACK_1 = ImageIO.read(fShoot);
                    WARRIOR_ATTACK_2 = WARRIOR_ATTACK_1;
                }
                WARRIOR_GUARD = WARRIOR_IDLE;
            } else if (type.equals("Lancer")) {
                File fAtt = new File(path + type + "_Right_Attack.png");
                if(fAtt.exists()) {
                    WARRIOR_ATTACK_1 = ImageIO.read(fAtt);
                    WARRIOR_ATTACK_2 = WARRIOR_ATTACK_1;
                }
                File fDef = new File(path + type + "_Right_Defence.png");
                if(fDef.exists()) WARRIOR_GUARD = ImageIO.read(fDef);
            } else if (type.equals("Pawn")) {
                File fA1 = new File(path + type + "_Interact Axe.png"); if(fA1.exists()) WARRIOR_ATTACK_1 = ImageIO.read(fA1);
                File fA2 = new File(path + type + "_Interact Pickaxe.png"); if(fA2.exists()) WARRIOR_ATTACK_2 = ImageIO.read(fA2);
                WARRIOR_GUARD = WARRIOR_IDLE;
            }
        } catch (IOException e) {
            System.err.println("Error updating sprites for " + color + " " + type);
            e.printStackTrace();
        }
    }

    public static void updateAvatars(int colorIndex) {
        HERO_AVATARS.clear();
        switch (colorIndex) {
            case 0: // Blue
                HERO_AVATARS.add(UI_AVATAR_01);
                HERO_AVATARS.add(UI_AVATAR_02);
                HERO_AVATARS.add(UI_AVATAR_03);
                HERO_AVATARS.add(UI_AVATAR_05);
                break;
            case 1: // Red
                HERO_AVATARS.add(UI_AVATAR_06);
                HERO_AVATARS.add(UI_AVATAR_07);
                HERO_AVATARS.add(UI_AVATAR_08);
                HERO_AVATARS.add(UI_AVATAR_10);
                break;
            case 2: // Yellow
                HERO_AVATARS.add(UI_AVATAR_11);
                HERO_AVATARS.add(UI_AVATAR_12);
                HERO_AVATARS.add(UI_AVATAR_13);
                HERO_AVATARS.add(UI_AVATAR_15);
                break;
            case 3: // Purple
                HERO_AVATARS.add(UI_AVATAR_16);
                HERO_AVATARS.add(UI_AVATAR_17);
                HERO_AVATARS.add(UI_AVATAR_18);
                HERO_AVATARS.add(UI_AVATAR_20);
                break;
            case 4: // Black
                HERO_AVATARS.add(UI_AVATAR_21);
                HERO_AVATARS.add(UI_AVATAR_22);
                HERO_AVATARS.add(UI_AVATAR_23);
                HERO_AVATARS.add(UI_AVATAR_25);
                break;
            default: // Fallback to Blue
                HERO_AVATARS.add(UI_AVATAR_01);
                HERO_AVATARS.add(UI_AVATAR_02);
                HERO_AVATARS.add(UI_AVATAR_03);
                HERO_AVATARS.add(UI_AVATAR_05);
                break;
        }
    }
}
