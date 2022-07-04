package de.lmu.ifi.sosylab.client.model.localserver;


import java.io.IOException;

/**
 *
 */
public class LocalGameServer {

    /**
     * Launch the game server.
     */
    public static void main(String[] args) throws IOException {
        final LocalServerConnection connection = new LocalServerConnection();
        connection.start();

        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                connection.stop();
            }
        });
    }
}
