package de.lmu.ifi.sosylab.server;

/**
 * Starts the remote Azul server.
 */
public class GameServer {

  /**
   * Launch the game server.
   */
  public static void main(String[] args) {
    final ServerNetworkConnection connection = new ServerNetworkConnection();
    connection.start();

    Runtime.getRuntime().addShutdownHook(new Thread(connection::stop));
  }
}
