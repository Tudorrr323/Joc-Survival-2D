import java.io.Serializable;

public abstract class ResourceEntity implements Serializable {
    protected Enums.Quality quality;
    protected int quantity;
    
    public ResourceEntity(Enums.Quality quality, int baseAmount) {
        this.quality = quality;
        // Calcul multiplicator calitate
        int multiplier = 1;
        if (quality == Enums.Quality.RARE) multiplier = 2;
        if (quality == Enums.Quality.EPIC) multiplier = 4;
        
        this.quantity = baseAmount * multiplier;
    }
    
    public int getQuantity() { return quantity; }
    public Enums.Quality getQuality() { return quality; }
    
    // Metoda abstracta
    public abstract String getResourceType();
}