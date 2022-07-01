package de.lmu.ifi.sosylab.client.model;

import de.lmu.ifi.sosylab.client.controller.GameController;
import de.lmu.ifi.sosylab.server.Game;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import javax.swing.JFrame;
import javax.swing.JPanel;

public class TableMiddle extends JPanel {
  private int tileSize = 20;
  private final GameController controller;
  private Game game = null;

  public TableMiddle(GameController controller) {
    this.controller = controller;
  }

  @Override
  protected void paintComponent(Graphics g) {
    super.paintComponent(g);
    Graphics2D g2D = (Graphics2D) g;
    drawPlate(g2D);
    drawTiles(g2D);
    drawPile(g2D);
  }

  private void drawPile(Graphics2D g2D) {
  }

  private void drawTiles(Graphics2D g2D) {
    int plateanzahl = 7;
    int b = 0;
    int row = 0;
    int erledigt =0;

    for(int plate = 0; plate < plateanzahl; plate++) {
      if(plate > 3){row = 1;}

      for (int i = 0; i < 5; i++) {
        if (i < 2) {
          g2D.setColor(Color.LIGHT_GRAY);
          g2D.fillRect((tileSize * (i + 1)) + (erledigt * 120), 20 + (row * 200), tileSize, tileSize);
          g2D.setColor(Color.WHITE);
          g2D.drawRect((tileSize * (i + 1)) + (erledigt * 120), 20 + (row * 200), tileSize, tileSize);
        }
        if (i > 1) {
          g2D.setColor(Color.LIGHT_GRAY);
          g2D.fillRect((tileSize * (b + 1)) + (erledigt * 60), 60 + (row * 200), tileSize, tileSize);
          g2D.setColor(Color.WHITE);
          g2D.drawRect((tileSize * (b + 1)) + (erledigt * 60), 60 + (row * 200), tileSize, tileSize);
          b++;
        }
      }
      erledigt++;
      if(plate == 3){
        erledigt = 0;
        b = 0;}
    }

  }

  private void drawPlate(Graphics2D g2D) {
    int anzahl = 9;
    int erledigt = 0;
    for(int i = 0; i < anzahl; i++){
      if(i < 4) {
        g2D.drawOval(120 * erledigt, 10, 100, 100);
        g2D.fillOval(120 * erledigt, 10, 100, 100);
      }
      if(i > 3) {
        g2D.drawOval(120 * erledigt, 210, 100, 100);
        g2D.fillOval(120 * erledigt, 210, 100, 100);
      }
      erledigt++;
      if(i == 4){erledigt = 0;}
    }
  }

}
