package de.lmu.ifi.sosylab.client.view;

import de.lmu.ifi.sosylab.client.controller.GameController;
import java.awt.Graphics;
import java.awt.Graphics2D;
import javax.swing.JPanel;

public class Pile extends JPanel {
  private final GameController controller;

  public Pile(GameController controller) {
    this.controller = controller;
  }

  @Override
  protected void paintComponent(Graphics g) {
    super.paintComponent(g);
    Graphics2D g2D = (Graphics2D) g;
    drawPile(g2D);
  }

  private void drawPile(Graphics2D g2D) {
  }

}
