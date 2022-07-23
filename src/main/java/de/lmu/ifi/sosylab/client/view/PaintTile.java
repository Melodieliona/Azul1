package de.lmu.ifi.sosylab.client.view;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.io.Serial;
import javax.swing.JPanel;

/**
 * Creates the different tiles.
 */
public class PaintTile extends JPanel {

  @Serial
  private static final long serialVersionUID = 1L;
  private final String color;
  private final transient Images img;

  /**
   * Draws a tile.
   *
   * @param color - color to set this color.
   * @param img - import images.
   */
  public PaintTile(String color, Images img) {
    this.img = img;
    this.color = color;
    this.setPreferredSize(new Dimension(25, 25));
    this.setBackground(new Color(1.0f, 1.0f, 1.0f, 0.0f));
  }

  /**
   * Draws tiles.
   *
   * @param g - Graphics.
   */
  @Override
  public void paint(Graphics g) {
    Graphics2D g2D = (Graphics2D) g;
    switch (color) {
      case "BLUE" -> g2D.drawImage(img.getTileBlue(), 0, 0, null);
      case "YELLOW" -> g2D.drawImage(img.getTileYellow(), 0, 0, null);
      case "RED" -> g2D.drawImage(img.getTileRed(), 0, 0, null);
      case "BLACK" -> g2D.drawImage(img.getTileBlack(), 0, 0, null);
      case "WHITE" -> g2D.drawImage(img.getTileWhite(), 0, 0, null);
      case "STARTING_MARKER" -> g2D.drawImage(img.getTileStarter(), 0, 0, null);
      default -> throw new IllegalArgumentException("Invalid color.");
    }
  }
}
