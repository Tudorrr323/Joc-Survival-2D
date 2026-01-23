/*
 * Project: Joc Survival 2D
 * Author:  Tudor Baranga(Tudorrr323)
 * Date:    Jan 23, 2026
 *
 * Copyright (c) 2026 Tudor Baranga(Tudorrr323). All rights reserved.
 * This code is proprietary software. Use is strictly prohibited without permission.
 */
package entities;

import java.io.Serializable;
import utils.Enums;

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
