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

    public VisualEffect(float x, float y, String text, Color color, float size) {
        this.x = x;
        this.y = y;
        this.text = text;
        this.color = color;
        this.size = size;
        this.life = 60; // 1 secunda (la 60fps)
        this.velY = -1.0f; // Se ridica in sus
    }

    public boolean update() {
        y += velY;
        life--;
        return life > 0;
    }

    public void draw(Graphics2D g2) {
        // Fade out alpha
        int alpha = (int)((life / 60.0f) * 255);
        if (alpha > 255) alpha = 255;
        if (alpha < 0) alpha = 0;

        g2.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), alpha));
        g2.setFont(new Font("Segoe UI Emoji", Font.BOLD, (int)size));
        g2.drawString(text, x, y);
    }
}
