package de.lmu.ifi.sosylab.server;

import java.io.IOException;

/**
 *
 */
public class GameServer {

  /**
   * Launch the game server.
   */
  public static void main(String[] args) throws IOException {
    final ServerNetworkConnection connection = new ServerNetworkConnection();
    connection.start();

    Runtime.getRuntime().addShutdownHook(new Thread() {
      @Override
      public void run() {
        connection.stop();
      }
    });
  }
}
