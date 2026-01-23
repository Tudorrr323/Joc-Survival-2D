/*
 * Project: Joc Survival 2D
 * Author:  Tudor Baranga(Tudorrr323)
 * Date:    Jan 23, 2026
 *
 * Copyright (c) 2026 Tudor Baranga(Tudorrr323). All rights reserved.
 * This code is proprietary software. Use is strictly prohibited without permission.
 */
package utils;

import java.awt.image.BufferedImage;

public class SaveInfo {
    public int slot;
    public String name;
    public String date;
    public BufferedImage thumbnail;
    public boolean exists;

    public SaveInfo(int slot, String name, String date, BufferedImage thumbnail, boolean exists) {
        this.slot = slot;
        this.name = name;
        this.date = date;
        this.thumbnail = thumbnail;
        this.exists = exists;
    }
}

