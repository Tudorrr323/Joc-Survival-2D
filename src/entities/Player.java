/*
 * Project: Joc Survival 2D
 * Author:  Tudor Baranga(Tudorrr323)
 * Date:    Jan 23, 2026
 *
 * Copyright (c) 2026 Tudor Baranga(Tudorrr323). All rights reserved.
 * This code is proprietary software. Use is strictly prohibited without permission.
 */
package entities;

import items.Item;
import utils.Enums;

public class Player extends GameCharacter {
    public int x, y;
    public float visualX, visualY;
    
    private int xp = 0;
    private int level = 1;
    private int nextLevelXp = 100;
    public int damageBonus = 0;
    
    // --- GOLD ---
    private int gold = 0;
    
    public int levelUpAnim = 0; 
    public Item[] inventory = new Item[5]; 
    public Item[] backpack = new Item[16];
    public Item[] armor = new Item[4];
    public int selectedSlot = 0; 

    public Player(String name, int startX, int startY) {
        super(name, 100, 0, 5);
        this.x = startX; this.y = startY;
        this.visualX = startX * 60; this.visualY = startY * 60;
        
        inventory[0] = new Item("Sword", Item.Type.WEAPON, Item.Specific.SWORD, 30);
        inventory[1] = new Item("Axe", Item.Type.TOOL, Item.Specific.AXE, 8);
        inventory[2] = new Item("Pickaxe", Item.Type.TOOL, Item.Specific.PICKAXE, 6);
    }
    
    // --- METODE GOLD ---
    public int getGold() { return gold; }
    public void addGold(int amount) { this.gold += amount; }
    public void removeGold(int amount) { this.gold -= amount; }

    public int getTotalDamage() {
        int dmg = 5; 
        Item item = getSelectedItem();
        if (item != null && (item.type == Item.Type.WEAPON || item.type == Item.Type.TOOL)) {
            dmg = item.value;
        }
        return dmg + damageBonus;
    }
    
    public int getDefense() {
        int def = 0; for(Item i : armor) if(i != null) def += i.value; return def;
    }
    
    @Override
    public void takeDamage(int dmg) {
        int finalDmg = dmg - getDefense();
        if (finalDmg < 1) finalDmg = 1; 
        health -= finalDmg; if (health < 0) health = 0; if (health == 0) die();
    }
    
    // --- COLECTARE IMBUNATATITA (RARITY CHECK) ---
    public int collectResource(ResourceEntity res) {
        String type = res.getResourceType();
        int qty = res.getQuantity();
        
        // Verificam daca avem unealta cu bonus
        Item tool = getSelectedItem();
        if (tool != null && tool.rarityBonus > 0) {
            // Daca unealta e "Golden" sau mai buna, creste sansa de a primi iteme in plus
            if (Math.random() < 0.3) { // 30% sansa pentru extra resurse
                qty += 2;
            }
        }

        Item newItem = null;
        if (type.equals("WOOD")) newItem = new Item("Wood", Item.Type.RESOURCE, Item.Specific.WOOD, 0);
        else if (type.equals("STONE")) newItem = new Item("Stone", Item.Type.RESOURCE, Item.Specific.STONE, 0);
        else if (type.equals("FOOD")) newItem = new Item("Grain", Item.Type.RESOURCE, Item.Specific.GRAIN, 0);
        
        if(newItem != null) {
            newItem.quantity = qty;
            addItem(newItem);
            addXp(5 * (res.getQuality() == Enums.Quality.EPIC ? 4 : 1));
            return qty;
        }
        return 0;
    }
    
    public boolean addItem(Item newItem) {
        if (newItem.type == Item.Type.RESOURCE || newItem.type == Item.Type.CONSUMABLE) {
            for (Item i : inventory) if (i != null && i.specificType == newItem.specificType) { i.quantity += newItem.quantity; return true; }
            for (Item i : backpack) if (i != null && i.specificType == newItem.specificType) { i.quantity += newItem.quantity; return true; }
        }
        for(int i=0; i<inventory.length; i++) if (inventory[i] == null) { inventory[i] = newItem; return true; }
        for(int i=0; i<backpack.length; i++) if (backpack[i] == null) { backpack[i] = newItem; return true; }
        return false; 
    }
    
    public void removeItem(int slot) { 
        if(slot >= 0 && slot < inventory.length && inventory[slot] != null) {
            if(inventory[slot].quantity > 1) inventory[slot].quantity--; else inventory[slot] = null;
        }
    }
    public Item getSelectedItem() { return inventory[selectedSlot]; }
    public void addXp(int amount) { this.xp += amount; if (this.xp >= nextLevelXp) { level++; xp -= nextLevelXp; nextLevelXp = (int)(nextLevelXp * 1.5); maxHealth += 20; health = maxHealth; damageBonus += 5; levelUpAnim = 60; } }
    public void updateVisuals(int tileSize) { visualX += (x * tileSize - visualX) * 0.2f; visualY += (y * tileSize - visualY) * 0.2f; if (levelUpAnim > 0) levelUpAnim--; }
    @Override public void damage(GameCharacter target) { target.takeDamage(getTotalDamage()); }
    public int countItem(Item.Specific type) { int count = 0; for(Item i : inventory) if(i != null && i.specificType == type) count += i.quantity; for(Item i : backpack) if(i != null && i.specificType == type) count += i.quantity; return count; }
    public boolean hasItem(Item.Specific type, int amount) { return countItem(type) >= amount; }
    public boolean consumeItems(Item.Specific type, int amount) { if(countItem(type) < amount) return false; int remaining = amount; for(int i=0; i<inventory.length; i++) { if(inventory[i] != null && inventory[i].specificType == type) { if(inventory[i].quantity > remaining) { inventory[i].quantity -= remaining; return true; } else { remaining -= inventory[i].quantity; inventory[i] = null; } } } if(remaining > 0) { for(int i=0; i<backpack.length; i++) { if(backpack[i] != null && backpack[i].specificType == type) { if(backpack[i].quantity > remaining) { backpack[i].quantity -= remaining; return true; } else { remaining -= backpack[i].quantity; backpack[i] = null; } } } } return true; }
    public void heal(int amount) { health += amount; if(health > maxHealth) health = maxHealth; }
    public void addPermanentAttack(int v) { damageBonus += v; }
    public void healFull() { health = maxHealth; }
    @Override public void die() { status = Enums.Status.DEAD; }
    public int getHealth() { return health; }
    public int getMaxHealth() { return maxHealth; }
    public int getLevel() { return level; }
    public int getXp() { return xp; }
    public int getMaxXp() { return nextLevelXp; }
}
