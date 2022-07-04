package de.lmu.ifi.sosylab.client.view;

import de.lmu.ifi.sosylab.client.controller.GameController;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.io.Serial;
import javax.swing.JPanel;

/**
 * TODO Javadoc
 * */
public class Pile extends JPanel {

  @Serial
  private static final long serialVersionUID = 1L;
  private transient int place;
  private transient String color;
  private int tileSize;

  /**
   * TODO Javadoc
   * */
  public Pile(int place, String color, int tileSize) {
    this.place = place;
    this.color = color;
    this.tileSize = tileSize;
  }

  @Override
  protected void paintComponent(Graphics g) {
    Graphics2D g2D = (Graphics2D) g;
    drawTile(g2D);
  }

  private void drawTile(Graphics2D g2D) {
    switch (color) {
      case "BLACK":  g2D.setColor(Color.BLACK);
      break;
      case "RED":  g2D.setColor(Color.RED);
      break;
      default:
        System.out.println("Color not available.");
    }
    g2D.fillRect(0, 0, tileSize, tileSize);
    g2D.setColor(Color.WHITE);
    g2D.drawRect(0, 0, tileSize, tileSize);
  }


}
