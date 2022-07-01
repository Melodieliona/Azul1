package de.lmu.ifi.sosylab.client.model;

import de.lmu.ifi.sosylab.shared.JsonMessage;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import org.json.JSONException;
import org.json.JSONObject;



public class GameClientNetworkConnection {

    private static final String HOST = "localhost";
    private static final int PORT = 8080;

    private final GameModel model;
    private Socket socket;
    private BufferedWriter writer;
    private BufferedReader reader;
    private Thread thread;

    public GameClientNetworkConnection(GameModel model) {
        this.model = model;
    }

    /**
     * Start the network connection.
     */
    public synchronized void start() {
        thread = new Thread(this::doConnectLoop);
        thread.start();
    }

    private void doConnectLoop() {
        try {
            while (!Thread.interrupted()) {
                Socket socket;
                try {
                    socket = new Socket(HOST, PORT);
                } catch (UnknownHostException e) {
                    e.printStackTrace();
                    break;
                } catch (IOException e) {
                    e.printStackTrace();
                    Thread.sleep(1000);
                    continue;
                }
                try {
                    setupConnection(socket);
                } catch (IOException e) {
                    e.printStackTrace();
                    break;
                }
                doInputLoop();
            }
        } catch (InterruptedException ex) {
            // Acknowledged.
            // (Socket was requested to shut down, e.g. by the user pressing ctrl+d)
        }
    }

    private synchronized void setupConnection(Socket socket) throws IOException {
        this.socket = socket;
        writer = new BufferedWriter(
                new OutputStreamWriter(this.socket.getOutputStream(), StandardCharsets.UTF_8));
        reader =
                new BufferedReader(
                        new InputStreamReader(this.socket.getInputStream(), StandardCharsets.UTF_8));
    }



    private void doInputLoop() {
        while (!Thread.currentThread().isInterrupted()) {
            try {
                String line = reader.readLine();
                if (line == null || line.isEmpty()) {
                    break;
                }

                JSONObject object = new JSONObject(line);
                handleGameEvent(object);
            } catch (IOException e) {
                closeSocket();
                break;
            } catch (JSONException e) {
                e.printStackTrace();
                closeSocket();
                break;
            }
        }
        System.out.println("Input loop ended.");
    }

    public void handleGameEvent(JSONObject object) {
        switch (JsonMessage.typeOf(object)) {
            case LOGIN_SUCCESS:
                model.loggedIn();
                break;
            case LOGIN_FAILED:
                model.loginFailed();
                break;
            case USER_JOINED:
                handleUserJoined(object);
                break;
            case USER_LEFT:
                handleUserLeft(object);
                break;
            case TILE_SELECTION:
                handleTileSelection(object);
                break;
            case TILE_PLACEMENT:
                handleTilePlacement(object);
                break;
            default:
                throw new AssertionError("Unhandled message: " + object);
        }
    }

    private void handleUserLeft(JSONObject object) {
        String nick = JsonMessage.getNickname(object);
        model.userLeft(nick);
    }

    private void handleUserJoined(JSONObject object) {
        String nick = JsonMessage.getNickname(object);
        model.userJoined(nick);
    }

    private void handleTileSelection(JSONObject object) {

    }

    private void handleTilePlacement(JSONObject object) {

    }


    /**
     * Stop the network-connection.
     */
    public void stop() {
        synchronized (this) {
            thread.interrupt();
        }
        closeSocket();
    }

    private synchronized void closeSocket() {
        if (socket != null) {
            try {
                socket.close();
            } catch (IOException e) {
                // We want the socket to shut down anyway, so we do not care about an exception here
                e.printStackTrace();
            }
        }
        reader = null;
        writer = null;
        socket = null;
    }

    /**
     * Send a login-request to the server.
     *
     * @param nickname The name of the user with whom to log in.
     */
    public void sendLogin(String nickname) {
        JSONObject login = JsonMessage.login(nickname);
        send(login);
    }

    /**
     * Send a chat message to the server.
     *
     */
    public void sendMove() {

    }

    private synchronized void send(JSONObject message) {
        try {
            writer.write(message + System.lineSeparator());
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendTileSelection(int source, String color, int numberOfTiles) {
    }

    public void sendTilePlacement(int line, int color, int numberOfTiles) {
    }
}
