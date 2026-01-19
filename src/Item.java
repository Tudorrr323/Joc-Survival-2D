public class Item {
    public enum Type { WEAPON, TOOL, BUILDING, RESOURCE, CONSUMABLE, ARMOR, CURRENCY }
    public enum Specific { 
        SWORD, AXE, PICKAXE, 
        FOUNTAIN, MONUMENT, 
        WOOD, STONE, GRAIN, BREAD, HEALTH_POTION,
        HELMET, CHESTPLATE, PANTS, BOOTS,
        GOLD_COIN
    }

    public String name;
    public Type type;
    public Specific specificType;
    public int value; // Damage/Heal/Armor/Price
    public int quantity;
    public int rarityBonus; // 0 = Normal, 1 = High Chance for Epic
    
 // NOU: Memorie pentru cooldown
    public long savedLastUsedTime = 0;

    public Item(String name, Type type, Specific specific, int value) {
        this.name = name;
        this.type = type;
        this.specificType = specific;
        this.value = value;
        this.quantity = 1;
        this.rarityBonus = 0;
     // Initializam timpul ca fiind 0 (gata de folosire)
        this.savedLastUsedTime = 0;
    }
    
    // Constructor pentru unelte speciale
    public Item(String name, Type type, Specific specific, int value, int rarityBonus) {
        this(name, type, specific, value);
        this.rarityBonus = rarityBonus;
    }
}