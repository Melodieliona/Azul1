package de.lmu.ifi.sosylab.client.view;

import de.lmu.ifi.sosylab.client.controller.GameController;
import de.lmu.ifi.sosylab.client.model.Player;
import de.lmu.ifi.sosylab.shared.LayingRow;
import de.lmu.ifi.sosylab.shared.Tile;
import de.lmu.ifi.sosylab.shared.TileCollection;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * TODO Add JavaDoc
 */
public class Board extends JPanel {
  private int tileSize;
  private GameController controller;
  private Images img;
  private String name;
  private BufferedImage board;
  private int score;
  private int boardNumber;

  /**
   * Creates Board.
   *
   * @param tileSize - this.tilesize.
   * @param name     - username.
   * @param img      - imports images.
   */
  public Board(GameController controller, int tileSize, String name, Images img, int[] score, int boardNumber) {
    this.controller = controller;
    this.tileSize = tileSize;
    this.name = name;
    board = img.getBoard();
    this.img = img;
    this.boardNumber = boardNumber;
    if (score == null) {
      this.score = 0;
    } else {
      this.score = score[boardNumber];
    }
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
    g2D.drawString(String.valueOf(score), 250, 35);
    // createTiles(g2D);
  }

  /**
   * Creates the different tiles for the left board.
   *
   * @param g2D -graphics.
   */
  private void createTilesLeft(Graphics2D g2D) {
    try {
      Player player = controller.getPlayer(boardNumber);
      LayingRow[] rows = player.getBoard().getLayingRows();
      for (int i = 0; i < rows.length; i++) {
        for (int b = 0; b < rows[i].getRow().size(); b++) {
          int x = 5 - b;
          int y = i + 2;

          String color = String.valueOf(rows[i].getColor());
          x = x * tileSize;
          y = y * tileSize;

          switch (color) {
            case "BLUE" -> g2D.drawImage(img.getTileBlue(), x, y, null);
            case "YELLOW" -> g2D.drawImage(img.getTileYellow(), x, y, null);
            case "RED" -> g2D.drawImage(img.getTileRed(), x, y, null);
            case "BLACK" -> g2D.drawImage(img.getTileBlack(), x, y, null);
            case "WHITE" -> g2D.drawImage(img.getTileWhite(), x, y, null);
            default -> throw new IllegalArgumentException("Invalid color.");
          }

        }
      }

    } catch (NullPointerException e) {
      System.out.println("Collection ist noch leer! (Board)");
    }
  }

  /**
   * Creates the different tiles for the right board.
   *
   * @param g2D -graphics.
   */
  private void createTilesRight(Graphics2D g2D) {
    try {
      Player player = controller.getPlayer(boardNumber);
      Tile[][] collection = player.getBoard().getTileWall();

      for (int i = 0; i < 5; i++) {
        for (int b = 0; b < 5; b++) {
          String color = collection[i][b].getColor();
          int x = 7 + b;
          int y = i + 2;

          x = x * tileSize;
          y = y * tileSize;

          switch (color) {
            case "BLUE" -> g2D.drawImage(img.getTileBlue(), x, y, null);
            case "YELLOW" -> g2D.drawImage(img.getTileYellow(), x, y, null);
            case "RED" -> g2D.drawImage(img.getTileRed(), x, y, null);
            case "BLACK" -> g2D.drawImage(img.getTileBlack(), x, y, null);
            case "WHITE" -> g2D.drawImage(img.getTileWhite(), x, y, null);
            default -> throw new IllegalArgumentException("Invalid color.");
          }

        }
      }
    } catch (NullPointerException e) {
      System.out.println("Collection ist noch leer! (Board)");
    }
  }

  /**
   * Creates the different tiles for the left board.
   *
   * @param g2D -graphics.
   */
 /* private void createMinusPoints(Graphics2D g2D) {
    try {
      Player player = controller.getPlayer(boardNumber);

      for (int i = 0; i < collection.length; i++) {
        for (int b = 0; b < collection[i].size(); b++) {
          int x = 1 + b;
          int y = 8;

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
    } catch (NullPointerException e) {
      System.out.println("Collection ist noch leer! (Board)");
    }
  }*/

}
