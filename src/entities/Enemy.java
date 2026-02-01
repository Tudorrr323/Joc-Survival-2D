/*
 * Project: Joc Survival 2D
 * Author:  Tudor Baranga(Tudorrr323)
 * Date:    Jan 23, 2026
 *
 * Copyright (c) 2026 Tudor Baranga(Tudorrr323). All rights reserved.
 * This code is proprietary software. Use is strictly prohibited without permission.
 */
package entities;

import utils.Enums;
import world.WorldMap;

public class Enemy extends GameCharacter {
    public enum Type { ZOMBIE, SKELETON, RAT, HUNTER, WITCH }
    
    public Type type;
    public int x, y;
    
    private int moveTimer = 0;
    private int moveSpeedThreshold;
    private int attackCooldown = 0;

    // NOU: Inamicul tine minte pe ce sta (ca sa nu stearga corturile/cerealele cand merge)
    private Object savedTile = null; 

    public Enemy(Type type, int startX, int startY) {
        super(type.toString(), 50, 10, 2);
        this.type = type;
        this.x = startX;
        this.y = startY;
        
        switch(type) {
            case ZOMBIE:
                this.maxHealth = 80; this.attack = 15; this.moveSpeedThreshold = 80; break;
            case SKELETON:
                this.maxHealth = 50; this.attack = 10; this.moveSpeedThreshold = 50; break;
            case RAT:
                this.maxHealth = 20; this.attack = 5; this.moveSpeedThreshold = 25; break;
            case HUNTER:
                this.maxHealth = 60; this.attack = 20; this.moveSpeedThreshold = 45; break;
            case WITCH:
                this.maxHealth = 40; this.attack = 25; this.moveSpeedThreshold = 60; break;
        }
        this.health = this.maxHealth;
    }

    public boolean updateAI(Player p, WorldMap map) {
        if (status == Enums.Status.DEAD) return false;
        
        if (attackCooldown > 0) attackCooldown--;

        // Distanta
        int diffX = p.x - this.x;
        int diffY = p.y - this.y;
        int distSq = diffX*diffX + diffY*diffY;

        // 1. Anti-Lag: Ignora daca e departe (marit putin raza pentru hunterii din tabere)
        if (distSq > 150) return false; 

        // 2. Atac
        if (distSq <= 1) { 
            if (attackCooldown <= 0) {
                // p.takeDamage(this.attack); // Removed: GamePanel handles damage/block logic now
                attackCooldown = 60; 
                return true; 
            }
            return false;
        }

        // 3. Miscare
        moveTimer++;
        if (moveTimer > moveSpeedThreshold) { 
            moveTimer = 0;
            
            int moveX = 0;
            int moveY = 0;
            
            // Logica: Ne miscam pe axa unde distanta e mai mare
            if (Math.abs(diffX) > Math.abs(diffY)) {
                if (diffX > 0) moveX = 1; else moveX = -1;
            } else {
                if (diffY > 0) moveY = 1; else moveY = -1;
            }
            
            // Calculam coordonatele viitoare
            int nextX = this.x + moveX;
            int nextY = this.y + moveY;
            
            // Verificam ce se afla la destinatie
            Object target = map.getEntityAt(nextX, nextY);

            // MODIFICAT: Acum permitem trecerea prin NULL, CORTURI sau CEREALE
            if (target == null || target instanceof WorldMap.Tent || target instanceof Grain) {
                
                // A. Punem inapoi ce aveam sub noi la pozitia veche
                map.setEntityAt(this.x, this.y, savedTile);

                // B. Salvam ce se afla la noua pozitie (ca sa nu distrugem cortul)
                savedTile = target;

                // C. Mutam inamicul
                this.x = nextX;
                this.y = nextY;
                map.setEntityAt(this.x, this.y, this);
            }
        }
        return false;
    }
    
    // Cand setam pozitia manual (la spawn), resetam memoria tile-ului
    public void setPos(int x, int y) { 
        this.x = x; 
        this.y = y; 
        this.savedTile = null; 
    }
    
    @Override public void damage(GameCharacter target) { target.takeDamage(this.attack); }
    @Override public void die() { this.status = Enums.Status.DEAD; }
    
    // Metoda ajutatoare pentru a recupera tile-ul de sub inamic cand moare (pt GamePanel)
    public Object getSavedTile() { return savedTile; }
    
    // --- VISUALS & LOGIC FOR LOADING SCREEN ---
    public float visualX, visualY;
    public int walkAnim = 0;
    
    public void updateVisuals(int tileSize) {
        // Simple interpolation for smooth movement
        // For actual grid movement, this follows x/y. 
        // For loading screen, visualX/Y are manipulated directly, so this method might fight it?
        // Ah, in Loading Screen we manipulate visualX directly and DON'T change x/y grid pos.
        // So this method might overwrite visualX with 0 if x is 0.
        // We should only interpolate if we are actually using grid movement.
        // But for consistency with Player, we can keep it.
        // However, in loading screen, we are NOT setting 'x' and 'y'. We are setting 'visualX'.
        // So calling updateVisuals() will try to move visualX towards x*60 (which is 0).
        // FIX: Only update if x/y are relevant? Or just don't call updateVisuals in Loading Screen for Enemy?
        // Player's updateVisuals is called in Loading Screen. Player x is 0. 
        // So Player's visualX should move towards 0?
        // In GamePanel update:
        // loadingPlayer.updateVisuals(TILE_SIZE);
        // loadingPlayer.visualX += 3;
        // The += 3 fights the interpolation!
        // If x=0, visualX wants to go to 0. 
        // If we want free movement, we shouldn't call updateVisuals OR we should update x/y to match visualX/tileSize.
        // For Loading Screen, simpler to NOT call updateVisuals() if we move manually.
        // I will remove the call to updateVisuals from GamePanel for loading entities later.
        // But I still need the fields.
        
        visualX += (x * tileSize - visualX) * 0.2f; 
        visualY += (y * tileSize - visualY) * 0.2f; 
    }
    
    public void healFull() {
        health = maxHealth;
        status = Enums.Status.ALIVE;
    }
}
