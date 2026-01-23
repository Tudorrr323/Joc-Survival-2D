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

public class Grain extends ResourceEntity implements Serializable{
    public Grain(Enums.Quality quality) {
        super(quality, 1); // Base amount 1
    }
    @Override public String getResourceType() { return "FOOD"; }
}
