package de.lmu.ifi.sosylab.client.view;

import de.lmu.ifi.sosylab.client.controller.GameController;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.io.Serial;
import javax.swing.JPanel;

/**
 * TODO JavaDoc
 * */
public class Plate extends JPanel {

  @Serial
  private static final long serialVersionUID = 1L;
  private transient GameController controller;

  public Plate(GameController controller) {
    this.controller = controller;
  }

  @Override
  protected void paintComponent(Graphics g) {
    super.paintComponent(g);
    Graphics2D g2D = (Graphics2D) g;
    drawPlate(g2D);
    drawTiles(g2D);
  }

  private void drawTiles(Graphics2D g2D) {
    int b = 0;

    for (int i = 0; i < 4; i++) {
      int tileSize = 20;
      if (i < 2) {
        //TODO Farbe von Controller holen
        g2D.setColor(Color.LIGHT_GRAY);
        g2D.fillRect((tileSize * (i + 1)), 20, tileSize, tileSize);
        g2D.setColor(Color.WHITE);
        g2D.drawRect((tileSize * (i + 1)), 20, tileSize, tileSize);
      }
      if (i > 1) {
        g2D.setColor(Color.LIGHT_GRAY);
        g2D.fillRect((tileSize * (b + 1)), 60, tileSize, tileSize);
        g2D.setColor(Color.WHITE);
        g2D.drawRect((tileSize * (b + 1)), 60, tileSize, tileSize);
        b++;
      }
    }
  }

  private void drawPlate(Graphics2D g2D) {
    g2D.drawOval(0, 10, 100, 100);
    g2D.fillOval(0, 10, 100, 100);
  }
}

