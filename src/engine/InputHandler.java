/*
 * Project: Joc Survival 2D
 * Author:  Tudor Baranga(Tudorrr323)
 * Date:    Jan 23, 2026
 *
 * Copyright (c) 2026 Tudor Baranga(Tudorrr323). All rights reserved.
 * This code is proprietary software. Use is strictly prohibited without permission.
 */
package engine;

import java.awt.event.*;

public class InputHandler implements KeyListener, MouseListener, MouseMotionListener, MouseWheelListener {

    private GamePanel gamePanel;

    public boolean keyW, keyA, keyS, keyD, keyShift, keySpace;
    public int mouseX, mouseY;
    public boolean isMousePressed = false;

    public InputHandler(GamePanel gp) {
        this.gamePanel = gp;
    }

    @Override
    public void keyTyped(KeyEvent e) {
        gamePanel.handleKeyTyped(e.getKeyChar());
    }

    @Override
    public void keyPressed(KeyEvent e) {
        int k = e.getKeyCode();
        if (k == KeyEvent.VK_SHIFT) keyShift = true;
        if (k == KeyEvent.VK_SPACE) keySpace = true;
        gamePanel.handleKeyPressed(k);
    }

    @Override
    public void keyReleased(KeyEvent e) {
        int k = e.getKeyCode();
        if(k == KeyEvent.VK_W || k == KeyEvent.VK_UP) keyW = false;
        if(k == KeyEvent.VK_S || k == KeyEvent.VK_DOWN) keyS = false;
        if(k == KeyEvent.VK_A || k == KeyEvent.VK_LEFT) keyA = false;
        if(k == KeyEvent.VK_D || k == KeyEvent.VK_RIGHT) keyD = false;
        if(k == KeyEvent.VK_SHIFT) keyShift = false;
        if(k == KeyEvent.VK_SPACE) keySpace = false;
    }

    @Override
    public void mouseClicked(MouseEvent e) {}

    @Override
    public void mousePressed(MouseEvent e) {
        if (e.getButton() == MouseEvent.BUTTON1) {
            isMousePressed = true;
        }
        gamePanel.handleMousePressed(e.getX(), e.getY(), e.getButton());
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        if (e.getButton() == MouseEvent.BUTTON1) {
            isMousePressed = false;
            gamePanel.handleMouseReleased(e.getX(), e.getY());
        }
    }

    @Override
    public void mouseEntered(MouseEvent e) {}

    @Override
    public void mouseExited(MouseEvent e) {}

    @Override
    public void mouseDragged(MouseEvent e) {
        mouseX = e.getX();
        mouseY = e.getY();
        gamePanel.repaint();
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        mouseX = e.getX();
        mouseY = e.getY();
        // gamePanel.repaint(); // Repaint on move can be expensive, let the loop handle it or repaint only if needed
    }

    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {
        gamePanel.handleMouseWheel(e.getWheelRotation());
    }
}
