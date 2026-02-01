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

public class VisualEffect {
    public float x, y;
    public float velY;
    public String text;
    public Color color;
    public int life; // Frames to live
    public float size;

    public boolean isNotification = false;

    public VisualEffect(float x, float y, String text, Color color, float size) {
        this.x = x;
        this.y = y;
        this.text = text;
        this.color = color;
        this.size = size;
        this.life = 60; // 1 secunda (la 60fps)
        this.velY = -1.0f; // Se ridica in sus
    }
    
    // Constructor for Notification (Bottom-Right, Static)
    public VisualEffect(float x, float y, String text) {
        this.x = x;
        this.y = y;
        this.text = text;
        this.color = Color.WHITE;
        this.size = 20;
        this.life = 120; // 2 seconds
        this.velY = 0;
        this.isNotification = true;
    }

    public boolean update() {
        y += velY;
        life--;
        return life > 0;
    }

    public void draw(Graphics2D g2) {
        int alpha = 255;

        if (isNotification) {
            // Fade Logic (Life starts at 120)
            int maxLife = 120;
            int fadeDuration = 20;
            
            if (life > maxLife - fadeDuration) {
                // Fade In
                float progress = (maxLife - life) / (float)fadeDuration;
                alpha = (int)(progress * 255);
            } else if (life < fadeDuration) {
                // Fade Out
                float progress = life / (float)fadeDuration;
                alpha = (int)(progress * 255);
            } else {
                alpha = 255;
            }
            
            // Clamp alpha
            if (alpha > 255) alpha = 255;
            if (alpha < 0) alpha = 0;

            if (utils.Assets.PIXEL_FONT != null) g2.setFont(utils.Assets.PIXEL_FONT.deriveFont(18f));
            else g2.setFont(new Font("Arial", Font.BOLD, 16));
            
            FontMetrics fm = g2.getFontMetrics();
            int tw = fm.stringWidth(text);
            int th = fm.getAscent(); // Use ascent for better vertical centering logic
            
            int paddingH = 20;
            int paddingV = 10;
            int boxW = tw + paddingH * 2;
            int boxH = th + paddingV * 2;
            
            int boxX = (int)x - boxW; // Anchor bottom-right
            int boxY = (int)y - boxH;
            
            // Background Box
            g2.setColor(new Color(20, 20, 25, (int)(alpha * 0.8))); // Dark background, max 80% opacity
            g2.fillRoundRect(boxX, boxY, boxW, boxH, 15, 15);
            
            // Border
            g2.setColor(new Color(100, 100, 100, alpha));
            g2.setStroke(new BasicStroke(1));
            g2.drawRoundRect(boxX, boxY, boxW, boxH, 15, 15);
            
            // Text
            g2.setColor(new Color(255, 255, 255, alpha));
            g2.drawString(text, boxX + paddingH, boxY + paddingV + th - 2); 
            
        } else {
            // Normal Floating Text Logic
            alpha = (int)((life / 60.0f) * 255);
            if (alpha > 255) alpha = 255;
            if (alpha < 0) alpha = 0;
            
            g2.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), alpha));
            if (utils.Assets.PIXEL_FONT != null) g2.setFont(utils.Assets.PIXEL_FONT.deriveFont(size));
            else g2.setFont(new Font("Arial", Font.BOLD, (int)size));
            g2.drawString(text, x, y);
        }
    }
}
