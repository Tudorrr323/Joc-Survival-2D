/*
 * Project: Joc Survival 2D
 * Author:  Tudor Baranga(Tudorrr323)
 * Date:    Jan 23, 2026
 *
 * Copyright (c) 2026 Tudor Baranga(Tudorrr323). All rights reserved.
 * This code is proprietary software. Use is strictly prohibited without permission.
 */
package entities;

import java.awt.Rectangle;

public class Projectile {
    public float x, y;
    public float vx, vy;
    public float rotation;
    public int life = 60; // Frames
    public int damage;
    public boolean active = true;
    public int shooterId; // 0 for player, 1 for enemy

    public Projectile(float x, float y, float vx, float vy, float rotation, int damage) {
        this.x = x;
        this.y = y;
        this.vx = vx;
        this.vy = vy;
        this.rotation = rotation;
        this.damage = damage;
    }

    public void update() {
        x += vx;
        y += vy;
        life--;
        if (life <= 0) active = false;
    }
    
    public Rectangle getBounds() {
        return new Rectangle((int)x - 5, (int)y - 5, 10, 10);
    }
}
