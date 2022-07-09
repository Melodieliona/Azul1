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

/**
 * Todo JavaDoc
 * */
public class GameClientNetworkConnection {

  private static final String HOST = "localhost";
  private int port = 8080;

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
  public synchronized void start(int port) {
    this.port = port;
    thread = new Thread(this::doConnectLoop);
    thread.start();
  }

  private void doConnectLoop() {
    try {
      while (!Thread.interrupted()) {
        Socket socket;
        try {
          socket = new Socket(HOST, port);
        } catch (UnknownHostException e) {
          e.printStackTrace();
          break;
        } catch (IOException e) {
          e.printStackTrace();
          Thread.sleep(1000);
          continue;
        }
        try {
          System.out.println("setting up connection");
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

  //TODO: Get rid of sout's
  /**
   * Todo JavaDoc
   * */
  public void handleGameEvent(JSONObject object) {
    switch (JsonMessage.typeOf(object)) {
      case LOGIN_SUCCESS:
        System.out.println("logged in"); //for debugging
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
      case ALLOWED_FIELDS:
        handleAllowedFields(object);
        break;
      case ALLOWED_TILES:
        handleAllowedTiles(object);
        break;
      case NEXT_TURN:
        handleNextTurn(object);
        break;
      case MOVE_NOT_ALLOWED:
        handleMoveNotAllowed(object);
        break;
      case BOARD_UPDATE:
        handleBoardUpdate(object);
        break;
      case FILL_PLATES:
        handleFillPlates(object);
        break;
      case POINTS:
        handlePoints(object);
        break;
      case GAME_ENDED:
        handleGameEnded(object);
        break;
      case GAME_RESTART_REQUEST:
        handleGameRestartRequest(object);
        break;
      case GAME_RESTART:
        handleGameRestart(object);
        break;
      case TILES_NOT_ALLOWED:
        handleTilesNotAllowed(object);
        break;
      default:
        handleInvalidJson(object);
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
    String color = JsonMessage.getTileColor(object);
    String plate = JsonMessage.getFactoryPlate(object);
    //model.otherPlayerSelectedTiles();
  }

  private void handleTilePlacement(JSONObject object) {
    String[] rows = JsonMessage.getRows(object).trim().split("\\s+");
    String[] cols = JsonMessage.getColumns(object).trim().split("\\s+");
    //model.otherPlayerPlacedTiles();
  }

  private void handleAllowedFields(JSONObject object) {
    String[] rows = JsonMessage.getRows(object).trim().split("\\s+");
    //model.  the client has just received which fields can be clicked to place the selected tiles
  }

  private void handleAllowedTiles(JSONObject object) {
    String[] plates = JsonMessage.getFactoryPlate(object).trim().split("\\s+");
    //model. the client has just received which plates have allowed tiles to be selected
  }

  private void handleNextTurn(JSONObject object) {
    String nick = JsonMessage.getNickname(object);
    //model. the client has just received that is the turn of the player with nickname <nick>

  }

  private void handleMoveNotAllowed(JSONObject object) {
    model.tilePlacementFailed();
  }

  private void handleBoardUpdate(JSONObject object) {
    String nick = JsonMessage.getNickname(object);
    String rowsToBeCleared = JsonMessage.getRows(object);
    String[] patternRow = JsonMessage.getPatternRows(object).trim().split("\\s+");
    String[] patternColumns = JsonMessage.getPatternColumns(object).trim().split("\\s+");
    //model. the client has just received an update of the board because the round has ended,
    // meaning that some rows should be cleared and tiles may need to be placed in the pattern.
  }

  private void handleFillPlates(JSONObject object) {
    String[] colors = JsonMessage.getTileColor(object).split(",");
    String[] amounts = JsonMessage.getTiles(object).trim().split(",");
    model.fillTiles(colors, amounts);
  }

  private void handleGameEnded(JSONObject object) {
    //model.gameEnded();
  }

  private void handleGameRestartRequest(JSONObject object) {
    String nick = JsonMessage.getNickname(object);
    //model. the client has just received that the player <nick> wants to restart the game
  }

  private void handleGameRestart(JSONObject object) {
    //model. the client has just received that the game will be restarted
  }


  private void handlePoints(JSONObject object) {
    String[] nicks = JsonMessage.getNickname(object).trim().split("\\s+");
    String[] points = JsonMessage.getScores(object).trim().split("\\s+");
    //model. the client has just received the actual scores of each player
  }

  public void handleInvalidJson(JSONObject object) {
    throw new AssertionError("Invalid JSON Message sent");
  }

  public void handleTilesNotAllowed(JSONObject object) {
    model.tileSelectionFailed();
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
    System.out.println("login sent");
    JSONObject login = JsonMessage.login(nickname);
    send(login);
  }

  private synchronized void send(JSONObject message) {
    try {
      writer.write(message + System.lineSeparator());
      writer.flush();
      System.out.println("message sent **********");
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public void sendTileSelection(int source, String color, int numberOfTiles) {
    JSONObject tileSelection = JsonMessage.selectTile(color, source);
    send(tileSelection);
  }

  public void sendTilePlacement(int line, int color, int numberOfTiles) { //color should be string
    //JSONObject tilePlacement = JsonMessage.placeTiles(color, line); //it actually makes sense to
    //only send the line since the client already knows how many tiles and the color
    //send(tilePlacement);
  }
}
