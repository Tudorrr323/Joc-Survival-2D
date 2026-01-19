import java.io.Serializable;

public class Building implements Serializable {
    // Adaugam serialVersionUID pentru compatibilitate la salvare
    private static final long serialVersionUID = 1L;

    public enum Type { FOUNTAIN, MONUMENT }
    
    public Type type;
    public String name;
    
    // NOU: Public, ca sa putem restaura timpul cand o punem jos din inventar
    public long lastUsedTime = 0; 
    
    // NOU: Nu mai e final, difera in functie de cladire
    public long cooldownDuration; 

    public Building(Type type) {
        this.type = type;
        this.name = (type == Type.FOUNTAIN) ? "Fountain" : "Monument";
        
        // Configuram cooldown diferit
        if (type == Type.FOUNTAIN) {
            this.cooldownDuration = 30000; // 30 secunde
        } else {
            this.cooldownDuration = 60000; // 60 secunde (Monument)
        }
    }

    public boolean isReady() {
        return System.currentTimeMillis() - lastUsedTime >= cooldownDuration;
    }

    public void use() {
        lastUsedTime = System.currentTimeMillis();
    }

    public int getSecondsLeft() {
        long diff = System.currentTimeMillis() - lastUsedTime;
        if (diff >= cooldownDuration) return 0;
        return (int)((cooldownDuration - diff) / 1000);
    }
}