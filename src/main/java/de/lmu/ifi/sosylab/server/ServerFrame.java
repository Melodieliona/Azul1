package de.lmu.ifi.sosylab.server;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.io.Serial;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;

/**
 * TODO Add JavaDoc
 * */
public class ServerFrame extends JFrame {
  @Serial
  private static final long serialVersionUID = 1L;

  private transient JPanel panel;
  private transient JButton stopServerButton;

  /**
   * TODO Add JavaDoc
   * */
  public ServerFrame(ServerNetworkConnection connection) {
    super("~ Azul Server ~");

    panel = new JPanel(new GridLayout(1, 1, 0, 0));

    stopServerButton =  new JButton("STOP");
    stopServerButton.setBackground(Color.RED);
    stopServerButton.setOpaque(true);
    stopServerButton.setBorderPainted(false);
    stopServerButton.setSize(50, 50);
    stopServerButton.setMaximumSize(getSize());

    stopServerButton.addActionListener(e -> connection.sendGameCancelledServerShutDown());

    panel.setBackground(Color.GRAY);
    panel.add(stopServerButton);

    this.add(panel);

    setPreferredSize(new Dimension(200, 200));
    setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

    pack();
  }
}
