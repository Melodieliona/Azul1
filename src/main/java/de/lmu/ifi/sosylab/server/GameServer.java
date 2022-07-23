package de.lmu.ifi.sosylab.server;

import java.io.IOException;

/**
 * Starts the remote Azul server.
 */
public class GameServer {

  /**
   * Launch the game server.
   */
  public static void main(String[] args) throws IOException {
    final ServerNetworkConnection connection = new ServerNetworkConnection();
    connection.start();

    Runtime.getRuntime().addShutdownHook(new Thread(connection::stop));

    //Start the server view
    final ServerFrame serverFrame = new ServerFrame(connection);
    serverFrame.setVisible(true);
  }
}
