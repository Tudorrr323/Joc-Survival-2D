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

public class Tree extends ResourceEntity implements Serializable{
    public Tree(Enums.Quality quality) {
        super(quality, 2); // Base amount 2
    }
    @Override public String getResourceType() { return "WOOD"; }
}
