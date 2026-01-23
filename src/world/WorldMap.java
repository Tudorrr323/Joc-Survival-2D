/*
 * Project: Joc Survival 2D
 * Author:  Tudor Baranga(Tudorrr323)
 * Date:    Jan 23, 2026
 *
 * Copyright (c) 2026 Tudor Baranga(Tudorrr323). All rights reserved.
 * This code is proprietary software. Use is strictly prohibited without permission.
 */
package world;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import entities.*;
import utils.Enums;

public class WorldMap implements Serializable {
	private static final long serialVersionUID = 1L; // IMPORTANT pentru salvare
	
    public Object[][] grid;
    public boolean[][] explored; // Matrice pentru Fog of War
    public int rows, cols;
    
    // --- NOU: Clase pentru elementele taberei ---
    public static class Campfire implements Serializable {}
    public static class Tent implements Serializable {}
 // NOU: Clasa pentru punctul de spawn
    public static class PlayerStart implements Serializable {}

    public WorldMap(int rows, int cols) {
        this.rows = rows;
        this.cols = cols;
        this.grid = new Object[rows][cols];
        this.explored = new boolean[rows][cols]; // Inițial totul e false (neexplorat)
        
        generateDynamicMap();   // Genereaza natura si monstrii normali
        generateHunterCamps();  // NOU: Genereaza taberele speciale
        spawnVendor();          // Spawneaza negustorul
    }

    private void generateDynamicMap() {
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                double rand = Math.random();
                Enums.Quality q = Enums.Quality.COMMON;
                if(Math.random() > 0.95) q = Enums.Quality.EPIC; else if(Math.random() > 0.80) q = Enums.Quality.RARE;
                
                if (rand < 0.05) grid[r][c] = "WATER"; 
                else if (rand < 0.15) grid[r][c] = new Tree(q);
                else if (rand < 0.20) grid[r][c] = new Rock(q);
                else if (rand < 0.25) grid[r][c] = new Grain(q);
                else if (rand < 0.28) { 
                    // MODIFICAT: Aici spawnam doar monstrii "salbatici", FARA HUNTERI
                    double typeRand = Math.random();
                    if (typeRand < 0.4) grid[r][c] = new Enemy(Enemy.Type.ZOMBIE, c, r);
                    else if (typeRand < 0.7) grid[r][c] = new Enemy(Enemy.Type.SKELETON, c, r);
                    else grid[r][c] = new Enemy(Enemy.Type.RAT, c, r);
                    // Hunterii au fost scosi de aici
                }
                else grid[r][c] = null;
            }
        }
    }

    // --- NOU: Algoritm pentru generarea taberelor ---
    private void generateHunterCamps() {
        int cx = cols / 2;
        int cy = rows / 2;
        int numberOfCamps = 8; // Numarul de tabere dorite
        int attempts = 0;
        int campsPlaced = 0;

        while(campsPlaced < numberOfCamps && attempts < 2000) {
            attempts++;
            // Alegem o coordonata random, dar pastram o marja de siguranta fata de margini
            int r = (int)(Math.random() * (rows - 10)) + 5;
            int c = (int)(Math.random() * (cols - 10)) + 5;

            // 1. Verificare Distanta (Trebuie sa fie departe de centru/spawn)
            double dist = Math.sqrt(Math.pow(r - cy, 2) + Math.pow(c - cx, 2));
            if (dist < 35) continue; // Minimum 35 blocuri distanta de centru

            // 2. Verificam daca locul e liber (nu e apa sau altceva important)
            if (grid[r][c] != null) continue;

            // --- PLASARE TABARA ---
            
            // Pasul A: Punem Focul de tabara in centru
            grid[r][c] = new Campfire();

            // Pasul B: Identificam locurile libere din jur (vecinii)
            List<int[]> neighbors = new ArrayList<>();
            for(int ny = r-2; ny <= r+2; ny++) {
                for(int nx = c-2; nx <= c+2; nx++) {
                    if (nx == c && ny == r) continue; // Sarim peste foc
                    // Putem suprascrie copacii sau locurile goale, dar nu apa sau stancile
                    if (isValid(nx, ny)) {
                        Object obj = grid[ny][nx];
                        if (obj == null || obj instanceof Tree || obj instanceof Grain) {
                            neighbors.add(new int[]{nx, ny});
                        }
                    }
                }
            }
            Collections.shuffle(neighbors); // Amestecam pozitiile pentru aspect natural

            // Pasul C: Plasare Corturi (2 sau 3)
            int tentCount = 2 + (int)(Math.random() * 2);
            for(int i=0; i<tentCount && !neighbors.isEmpty(); i++) {
                int[] pos = neighbors.remove(0);
                grid[pos[1]][pos[0]] = new Tent();
            }

            // Pasul D: Plasare Hunteri (3 pana la 5)
            int hunterCount = 3 + (int)(Math.random() * 3);
            for(int i=0; i<hunterCount && !neighbors.isEmpty(); i++) {
                int[] pos = neighbors.remove(0);
                grid[pos[1]][pos[0]] = new Enemy(Enemy.Type.HUNTER, pos[0], pos[1]);
            }
            
            campsPlaced++;
        }
    }

    private void spawnVendor() {
        boolean placed = false;
        int attempts = 0;
        while(!placed && attempts < 100) {
            int r = (int)(Math.random() * rows);
            int c = (int)(Math.random() * cols);
            
            if (r < 5 || r > rows - 5 || c < 5 || c > cols - 5) {
                if (grid[r][c] == null) {
                    grid[r][c] = new Vendor();
                    placed = true;
                }
            }
            attempts++;
        }
    }

    public void clearCenter() {
        int cx = cols / 2; int cy = rows / 2;
        for(int y = cy-2; y <= cy+2; y++) { for(int x = cx-2; x <= cx+2; x++) { if(x>=0 && x<cols && y>=0 && y<rows) grid[y][x] = null; } }
    }
    
    // Metoda pentru Fog of War
    public void updateExploration(int px, int py, int radius) {
        for (int r = py - radius; r <= py + radius; r++) {
            for (int c = px - radius; c <= px + radius; c++) {
                if (isValid(c, r)) {
                    int dx = c - px;
                    int dy = r - py;
                    if (dx * dx + dy * dy < radius * radius) {
                        explored[r][c] = true;
                    }
                }
            }
        }
    }
    
    public Object getEntityAt(int x, int y) { if (isValid(x, y)) return grid[y][x]; return null; }
    public void setEntityAt(int x, int y, Object entity) { if (isValid(x, y)) grid[y][x] = entity; }
    public void removeEntityAt(int x, int y) { if (isValid(x, y)) grid[y][x] = null; }
    private boolean isValid(int x, int y) { return x >= 0 && x < cols && y >= 0 && y < rows; }
}
