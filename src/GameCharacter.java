import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public abstract class GameCharacter implements Serializable {
    protected String name;
    protected int attack;
    protected int defense;
    protected int health;
    protected int maxHealth;
    protected Enums.Status status;
    protected List<Item> inventory;

    public GameCharacter(String name, int hp, int atk, int def) {
        this.name = name;
        this.maxHealth = hp;
        this.health = hp;
        this.attack = atk;
        this.defense = def;
        this.status = Enums.Status.ALIVE;
        this.inventory = new ArrayList<>();
    }

    public abstract void damage(GameCharacter target);
    
    public void takeDamage(int dmg) {
        int actualDmg = Math.max(0, dmg - this.defense);
        this.health -= actualDmg;
        if (this.health <= 0) {
            this.health = 0;
            die();
        }
    }

    public abstract void die();
    
    public boolean isAlive() { return status == Enums.Status.ALIVE; }
    public String getName() { return name; }
}