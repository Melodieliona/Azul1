package de.lmu.ifi.sosylab.client.view;

import de.lmu.ifi.sosylab.client.controller.GameController;
import de.lmu.ifi.sosylab.client.model.Player;
import de.lmu.ifi.sosylab.shared.LayingRow;
import de.lmu.ifi.sosylab.shared.Tile;
import de.lmu.ifi.sosylab.shared.TileCollection;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.Serial;
import javax.swing.*;

/**
 * TODO Add JavaDoc
 */
public class Board extends JPanel {

  @Serial
  private static final long serialVersionUID = 1L;
  private int tileSize;
  private transient GameController controller;
  private transient Images img;
  private String name;
  private transient BufferedImage board;
  private int boardNumber;
  private transient Graphics2D g2D;

  /**
   * Creates Board with name, Points and Tiles.
   *
   * @param tileSize - this.tilesize.
   * @param name     - username.
   * @param img      - imports images.
   */
  public Board(GameController controller, int tileSize, String name, Images img, int boardNumber) {
    this.controller = controller;
    this.tileSize = tileSize;
    this.name = name;
    board = img.getBoard();
    this.img = img;
    this.boardNumber = boardNumber;
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
   * Paints board, playername and points.
   *
   * @param g -graphics.
   */
  public void paint(Graphics g) {
    g2D = (Graphics2D) g;
    g2D.drawImage(board, 0, 0, board.getWidth(), board.getHeight(), null);
    g2D.setFont(new Font("Arial", Font.PLAIN, 15));
    g2D.setColor(Color.white);
    g2D.drawString(name, 83, 35);
    int score;
    try {
      score = controller.getPlayer(boardNumber).getBoard().getCurrentScore();
    } catch (NullPointerException e) {
      score = 0;
    }

    g2D.drawString(String.valueOf(score), 250, 35);
    createTilesLeft();
    createTilesRight();
    createMinusPoints();
    createFrame();
    createValidRows();
  }

  /**
   * Creates the different tiles for the left board.
   * If rows are empty a NullPointerException is thrown.
   *
   */
  private void createTilesLeft() {
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
      System.out.println("LayingRows ist noch leer! (Board)");
    }
  }

  /**
   * Creates the different tiles for the right board.
   * If rows are empty a NullPointerException is thrown.
   */
  private void createTilesRight() {
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
      //System.out.println("Tile[][] ist noch leer! (Board)");
    }
  }

  /**
   * Creates the minusPoints.
   * If rows are empty a NullPointerException is thrown.
   *
   */
  private void createMinusPoints() {
    try {
      Player player = controller.getPlayer(boardNumber);
      TileCollection minusPoints = new TileCollection();
      minusPoints.addAll(player.getBoard().getFloorLine());

      for (int i = 0; i < minusPoints.size(); i++) {
        int x = 1 + i;
        int y = 8;
        String color = String.valueOf(minusPoints.get(i));

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
    } catch (NullPointerException e) {
      System.out.println("MinusPoints ist noch leer! (Board)");
    }
  }

  /**
   * Creates a green frame when it's the players turn.
   */
  private void createFrame() {
    if (name.equals(controller.getCurrentPlayer())) {
      g2D.setColor(Color.GREEN);
      g2D.drawRect(0, 0, board.getWidth() - 1, board.getHeight() - 1);
    }
  }

  /**
   * Creates a green frame when the row is clickable.
   */
  private void createValidRows() {
    try {
      if (controller.getCurrentPlayer().equals(controller.getPlayer(boardNumber).getPlayerName())) {
        int[] valid = controller.getValidRow();
        for (int i = 0; i < controller.getValidRow().length; i++) {
          int validNumber = valid[i];
          switch (validNumber) {
            case 0 -> g2D.drawRect(5 * 25, 2 * 25, 25, 25);
            case 1 -> g2D.drawRect(4 * 25, 3 * 25, 50, 25);
            case 2 -> g2D.drawRect(3 * 25, 4 * 25, 75, 25);
            case 3 -> g2D.drawRect(2 * 25, 5 * 25, 100, 25);
            case 4 -> g2D.drawRect(1 * 25, 6 * 25, 125, 25);
            case 5 -> g2D.drawRect(1 * 25, 8 * 25, 175, 25);
            default -> throw new IllegalArgumentException("Invalid Row.");
          }
        }
      }
    } catch (NullPointerException e) {
      System.out.println("ValidRows ist noch leer! (Board)");
    }
  }
}
