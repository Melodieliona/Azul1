package de.lmu.ifi.sosylab.client.model.localserver;


import java.io.IOException;

/**
 * TODO Javadoc
 * */
public class LocalGameServer {

  /**
   * Launch the game server.
   */
  public static void startUpLocalServer() throws IOException {
    final LocalServerConnection connection = new LocalServerConnection();
    connection.start();

    Runtime.getRuntime().addShutdownHook(new Thread(connection::stop));
  }
}
