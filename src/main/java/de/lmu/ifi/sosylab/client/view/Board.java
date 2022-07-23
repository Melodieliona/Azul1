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
 * Creates playerBoard with name, score and placed tiles.
 * When it is the player's turn, it creates a border and shows the rows that can be clicked.
 */
public class Board extends JPanel {

  @Serial
  private static final long serialVersionUID = 1L;
  private final int tileSize;
  private final transient GameController controller;
  private final transient Images img;
  private final String name;
  private final transient BufferedImage board;
  private final int boardNumber;
  private transient Graphics2D g2D;
  private final transient double prozent;

  /**
   * Creates Board with name, Points and Tiles.
   *
   * @param tileSize - this.tilesize.
   * @param name     - username.
   * @param img      - imports images.
   */
  public Board(GameController controller, int tileSize, String name, Images img, int boardNumber,
               double prozent) {
    this.controller = controller;
    this.tileSize = tileSize;
    this.name = name;
    board = img.getBoard();
    this.img = img;
    this.boardNumber = boardNumber;
    this.prozent = prozent;
    setPanelSize();
  }

  /**
   * Sets panel size.
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

    createBoard();
    createTilesLeft();
    createTilesRight();
    createMinusPoints();
    createFrame();
    createValidRows();
  }

  /**
   * Creates Board with name and Score.
   */
  private void createBoard() {
    g2D.drawImage(board, 0, 0, tileSize * 13, tileSize * 10, null);
    g2D.setFont(new Font("Arial", Font.BOLD, (int) (15 * prozent)));
    if (img.getSkin().equals("STANDARD")) {
      g2D.setColor(Color.white);
    } else {
      g2D.setColor(new Color(0, 110, 222));
    }
    g2D.drawString(name, (int) (83 * prozent), (int) (prozent * 35));
    int score;
    try {
      score = controller.getPlayer(boardNumber).getScore();
    } catch (NullPointerException e) {
      score = 0;
    }

    g2D.drawString(String.valueOf(score), (int) (prozent * 250), (int) (prozent * 35));
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
      e.printStackTrace();
    }
  }

  /**
   * Creates the different tiles for the right board.
   * If rows are empty a NullPointerException is thrown
   */
  private void createTilesRight() {
    try {
      Player player = controller.getPlayer(boardNumber);
      Tile[][] collection = player.getBoard().getTileWall();

      for (int i = 0; i < 5; i++) {
        for (int b = 0; b < 5; b++) {
          if (collection[i][b] != null) {
            String color = collection[i][b].getColor().toUpperCase();
            int x = 7 + i;
            int y = b + 2;

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
      }
    } catch (NullPointerException e) {
      e.printStackTrace();
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
      e.printStackTrace();
    }
  }

  /**
   * Creates a green or pink frame when it's the players turn.
   */
  private void createFrame() {
    if (name.equals(controller.getCurrentPlayer())) {
      g2D.setStroke(new BasicStroke(3));
      if (img.getSkin().equals("STANDARD")) {
        g2D.setColor(Color.GREEN);
      } else {
        g2D.setColor(new Color(255, 0, 251));
      }
      g2D.drawRect(0, 0, board.getWidth() - 1, board.getHeight() - 1);
    }
  }

  /**
   * Creates a green or pink frame when the row is clickable.
   */
  private void createValidRows() {
    try {
      if (controller.getCurrentPlayer().equals(controller.getPlayer(boardNumber).getPlayerName())) {
        int[] valid = controller.getValidRow();
        for (int i = 0; i < controller.getValidRow().length; i++) {
          int validNumber = valid[i];
          switch (validNumber) {
            case 0 -> g2D.drawRect(5 * tileSize, 2 * tileSize, tileSize, tileSize);
            case 1 -> g2D.drawRect(4 * tileSize, 3 * tileSize, tileSize * 2, tileSize);
            case 2 -> g2D.drawRect(3 * tileSize, 4 * tileSize, tileSize * 3, tileSize);
            case 3 -> g2D.drawRect(2 * tileSize, 5 * tileSize, tileSize * 4, tileSize);
            case 4 -> g2D.drawRect(tileSize, 6 * tileSize, tileSize * 5, tileSize);
            case 5 -> g2D.drawRect(tileSize, 8 * tileSize, tileSize * 7, tileSize);
            default -> throw new IllegalArgumentException("Invalid Row.");
          }
        }
      }
    } catch (NullPointerException e) {
      e.printStackTrace();
    }
  }
}
