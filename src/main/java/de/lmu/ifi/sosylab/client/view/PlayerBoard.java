package de.lmu.ifi.sosylab.client.view;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.io.Serial;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JPanel;

public class PlayerBoard extends JPanel {

  @Serial
  private static final long serialVersionUID = 1L;
  private int tileSize = 30;

  public PlayerBoard() {
  }

  @Override
  protected void paintComponent(Graphics g) {
    super.paintComponent(g);

    Graphics2D g2D = (Graphics2D) g;
    drawField(g2D);
    drawTiles(g2D);
  }

  private void drawField(Graphics2D g2D) {
    int chosenColor = 0;

    List<Color> color = new ArrayList<Color>(5);
    color.add(new Color(147, 145, 145));
    color.add(new Color(220, 114, 114));
    color.add(new Color(215, 151, 68));
    color.add(new Color(100, 147, 236));
    color.add(new Color(130, 231, 201));
//_______________________Left Pattern Row_________________________________________________
    for (int row = 0; row < 5; row++) {
      for (int col = 4 - row; col < 5; col++) {
        g2D.setColor(Color.LIGHT_GRAY);
        g2D.fillRect(row * tileSize, col * tileSize, tileSize, tileSize);
        g2D.setColor(Color.WHITE);
        g2D.drawRect(row * tileSize, col * tileSize, tileSize, tileSize);
      }
    }
//__________________________Wall__________________________________________________________
    for (int row = 0; row < 5; row++) {
      for (int col = 0; col < 5; col++) {

        if (col == 0) {
          if (chosenColor < 1) {
            chosenColor = 4;
          }
          chosenColor -= 1;
        }

        if (chosenColor >= 5) {
          chosenColor = 0;
        }

        g2D.setColor(color.get(chosenColor));
        g2D.fillRect(row * tileSize + (6 * tileSize), col * tileSize, tileSize, tileSize);
        g2D.setColor(Color.WHITE);
        g2D.drawRect(row * tileSize + (6 * tileSize), col * tileSize, tileSize, tileSize);

        chosenColor += 1;
      }
    }
    //________________Minus Points______________________________________________________________
    int minus = 0;
    for (int row = 0; row < 7; row++) {
      g2D.setColor(Color.LIGHT_GRAY);
      g2D.fillRect(row * tileSize, 6 * tileSize, tileSize, tileSize);
      g2D.setColor(Color.WHITE);
      g2D.drawRect(row * tileSize, 6 * tileSize, tileSize, tileSize);
      g2D.setColor(Color.RED);
      g2D.setFont(new Font("Arial", Font.BOLD, 15));
      if (minus < 2) {
        g2D.drawString("  - 1  ", row * tileSize, 6 * tileSize);
      }
      if (minus >= 2 && minus < 5) {
        g2D.drawString("  - 2  ", row * tileSize, 6 * tileSize);
      }
      if (minus >= 5) {
        g2D.drawString("  - 3  ", row * tileSize, 6 * tileSize);
      }
      minus++;
    }
  }

  private void drawTiles(Graphics2D g2D) {
    //player.getPlaced tales
    //von placeTiles Reihe, Farbe, Anzahl
    int line = 3;

    for (int row = line; row < line + 1; row++) {
      //tileRow[line].getColor;
      int number = 3; //tileRow[line].getNumber;
      for (int i = 0; i < number; i++) {
        int col = 4 - i;
        g2D.setColor(Color.ORANGE);
        g2D.fillRect(col * tileSize, row * tileSize, tileSize, tileSize);
        g2D.setColor(Color.WHITE);
        g2D.drawRect(col * tileSize, row * tileSize, tileSize, tileSize);
      }
    }
    repaint();
  }

}
