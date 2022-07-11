package de.lmu.ifi.sosylab.client.view;

import de.lmu.ifi.sosylab.shared.TileCollection;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import javax.swing.JPanel;

/**
 * TODO Add JavaDoc
 * */
public class Board extends JPanel {
  int tileSize;
  TileCollection[] collection;
  Images img;
  String name;
  BufferedImage board;

  /**
   * Creates Board.
   *
   * @param tileCollection - this.collection.
   * @param tileSize       - this.tilesize.
   * @param name           - username.
   * @param img            - imports images.
   */
  public Board(TileCollection[] tileCollection, int tileSize, String name, Images img) {
    this.collection = tileCollection;
    this.tileSize = tileSize;
    this.name = name;
    board = img.getBoard();
    this.img = img;
    setPanelSize();
  }

  /**
   * sets panel size.
   */
  private void setPanelSize() {
    Dimension size = new Dimension(board.getWidth(), board.getHeight());
    setPreferredSize(size);
  }

  /**
   * Paints board and playername and points.
   *
   * @param g -graphics.
   */
  public void paint(Graphics g) {
    Graphics2D g2D = (Graphics2D) g;
    g2D.drawImage(board, 0, 0, board.getWidth(), board.getHeight(), null);
    g2D.setFont(new Font("Arial", Font.PLAIN, 15));
    g2D.setColor(Color.white);
    g2D.drawString(name, 83, 35);
    // createTiles(g2D);
  }

  /**
   * Creates the different tiles for the left board.
   *
   * @param g2D -graphics.
   */
  private void createTiles(Graphics2D g2D) {
    try {
      for (int i = 0; i < collection.length; i++) {
        for (int b = 0; b < collection[i].size(); b++) {
          int x = 5 - b;
          int y = i + 2;

          String color = String.valueOf(collection[i].get(b));
          x = x * tileSize;
          y = y * tileSize;

          switch (color) {
            case "BLUE" -> g2D.drawImage(img.getTileBlue(), x, y, null);
            case "YELLOW" -> g2D.drawImage(img.getTileYellow(), x, y, null);
            case "RED" -> g2D.drawImage(img.getTileRed(), x, y, null);
            case "BLACK" -> g2D.drawImage(img.getTileBlack(), x, y, null);
            case "WHITE" -> g2D.drawImage(img.getTileWhite(), x, y, null);
            case "STARTING_MARKER" -> g2D.drawImage(img.getTileStarter(), x, y, null);
            default -> throw new IllegalArgumentException("Invalid color.");
          }

        }
      }
      //   repaint();

    } catch (NullPointerException e) {
      System.out.println("Collection ist noch leer! (Board)");
    }

  }

}
