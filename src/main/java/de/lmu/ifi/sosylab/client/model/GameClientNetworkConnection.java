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
 * The network-connection of the client. Establishes a connection to the server and takes
 * care of sending and receiving messages in JSON format.
 */
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
   * Decides methods to be executed based on type of message received.
   *
   * @param object JsonMessage received
   */
  public void handleGameEvent(JSONObject object) {
    switch (JsonMessage.typeOf(object)) {
      case LOGIN_SUCCESS -> {
        System.out.println("logged in"); //for debugging
        model.loggedIn();
      }
      case LOGIN_FAILED -> model.loginFailed();
      case USER_JOINED -> handleUserJoined(object);
      case USER_LEFT -> handleUserLeft(object);
      case TILE_SELECTION -> handleTileSelection(object);
      case TILE_PLACEMENT -> handleTilePlacement(object);
      case ALLOWED_FIELDS -> handleAllowedFields(object);
      case ALLOWED_TILES -> handleAllowedTiles(object);
      case NEXT_TURN -> handleNextTurn(object);
      case MOVE_NOT_ALLOWED -> handleMoveNotAllowed(object);
      case BOARD_UPDATE -> handleBoardUpdate(object);
      case FILL_PLATES -> handleFillPlates(object);
      case POINTS -> handlePoints(object);
      case GAME_ENDED -> handleGameEnded(object);
      case GAME_RESTART_REQUEST -> handleGameRestartRequest(object);
      case GAME_RESTART -> handleGameRestart(object);
      case TILES_NOT_ALLOWED -> handleTilesNotAllowed(object);
      case TIMER -> handleTimer(object);
      case GAME_CANCEL_REQUEST -> handleGameCancelRequest(object);
      case GAME_CANCEL-> handleGameCancel(object);
      default -> handleInvalidJson(object);
    }
  }

  /**
   * Handles receiving that a user has left.
   *
   * @param object JsonMessage received
   */
  private void handleUserLeft(JSONObject object) {
    String nick = JsonMessage.getNickname(object);
    model.userLeft(nick);
  }

  /**
   * Handles receiving that a user has joined.
   *
   * @param object JsonMessage received
   */
  private void handleUserJoined(JSONObject object) {
    String nick = JsonMessage.getNickname(object);
    model.userJoined(nick);
  }

  /**
   * Handles receiving that a user has made a tiles selection.
   *
   * @param object JsonMessage received
   */
  private void handleTileSelection(JSONObject object) {
    String color = JsonMessage.getTileColor(object);
    int plate = Integer.parseInt(JsonMessage.getFactoryPlate(object));
      if (model.getNickname().equals(model.getCurrentPlayer())) {
              model.selectTiles(plate, color);
      } else {
          model.otherPlayerSelectedTiles(color, plate);
      }

  }

  /**
   * Handles receiving that a user placed tiles.
   *
   * @param object JsonMessage received
   */
  private void handleTilePlacement(JSONObject object) {
    String[] rows = JsonMessage.getRows(object).trim().split("\\s+");
    if (model.getNickname().equals(model.getCurrentPlayer())) {
        for (int i = 0; i < rows.length; i++) {
            model.placeTiles(Integer.getInteger(rows[0]), 1);
        }
    } else {
      model.otherPlayerPlacedTiles(rows);
    }
  }

  /**
   * Handles receiving which fields are allowed to place tiles.
   *
   * @param object JsonMessage received
   */
  private void handleAllowedFields(JSONObject object) {
    String[] rows = JsonMessage.getRows(object).trim().split("\\s+");
    model.setValidRows(rows.length, rows);
  }

  /**
   * Handles receiving which tiles are allowed to be selected.
   *
   * @param object JsonMessage received
   */
  private void handleAllowedTiles(JSONObject object) {
    String[] plates = JsonMessage.getFactoryPlate(object).trim().split("\\s+");
    model.setValidPlates(plates);
  }

  /**
   * Handles receiving which player is next.
   *
   * @param object JsonMessage received
   */
  private void handleNextTurn(JSONObject object) {
    String nick = JsonMessage.getNickname(object);
    model.nextPlayer(nick);

  }

  /**
   * Handles receiving that a placement is not allowed.
   *
   * @param object JsonMessage received
   */
  private void handleMoveNotAllowed(JSONObject object) {
    model.tilePlacementFailed();
  }

  /**
   * Handles receiving that a board must be updated.
   *
   * @param object JsonMessage received
   */
  private void handleBoardUpdate(JSONObject object) {
    String nick = JsonMessage.getNickname(object);
    String rowsToBeCleared = JsonMessage.getRows(object);
    String[] patternRow = JsonMessage.getPatternRows(object).trim().split("\\s+");
    String[] patternColumns = JsonMessage.getPatternColumns(object).trim().split("\\s+");
    //model. the client has just received an update of the board because the round has ended,
    // meaning that some rows should be cleared and tiles may need to be placed in the pattern.
  }

  /**
   * Handles receiving that the plates should be filled in a given way.
   *
   * @param object JsonMessage received
   */
  private void handleFillPlates(JSONObject object) {
    String[] colors = JsonMessage.getTileColor(object).split(",");
    String[] amounts = JsonMessage.getTiles(object).trim().split(",");
    model.fillTiles(colors, amounts);
  }

  /**
   * Handles receiving that the game has ended.
   *
   * @param object JsonMessage received
   */
  private void handleGameEnded(JSONObject object) {
    //model.gameEnded();
  }

  /**
   * Handles receiving a game restart request.
   *
   * @param object JsonMessage received
   */
  private void handleGameRestartRequest(JSONObject object) {
    String nick = JsonMessage.getNickname(object);
    //model. the client has just received that the player <nick> wants to restart the game
  }

  /**
   * Handles receiving that the game has been restarted.
   *
   * @param object JsonMessage received
   */
  private void handleGameRestart(JSONObject object) {
    //model. the client has just received that the game will be restarted
  }

  /**
   * Handles receiving a game cancel request.
   *
   * @param object JsonMessage received
   */
  private void handleGameCancelRequest(JSONObject object) {
    String nick = JsonMessage.getNickname(object);
    //model. the client has just received that the player <nick> wants to restart the game
  }

  /**
   * Handles receiving that the game has been canceled.
   *
   * @param object JsonMessage received
   */
  private void handleGameCancel(JSONObject object) {
    //model. the client has just received that the game will be restarted
  }

  /**
   * Handles receiving the current scores.
   *
   * @param object JsonMessage received
   */
  private void handlePoints(JSONObject object) {
    String[] nicks = JsonMessage.getNickname(object).trim().split("\\s+");
    String[] points = JsonMessage.getScores(object).trim().split("\\s+");
    int[] intPoints = new int[points.length];
      for (int i = 0; i < points.length; i++) {
          intPoints[i] = Integer.parseInt(points[i]);
      }
    model.pointsUpdate(nicks,intPoints);
  }

  /**
   * Handles receiving that an invalid JSON has been sent.
   *
   * @param object JsonMessage received
   */
  private void handleInvalidJson(JSONObject object) {
    throw new AssertionError("Invalid JSON Message sent");
  }

  /**
   * Handles receiving that a tiles selection failed.
   *
   * @param object JsonMessage received
   */
  private void handleTilesNotAllowed(JSONObject object) {
    model.tileSelectionFailed();
  }

  /**
   * Handles receiving that a tiles selection failed.
   *
   * @param object JsonMessage received
   */
  private void handleTimer(JSONObject object) {
    model.timer();
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

  /**
   * Handles sending a JSON.
   *
   * @param message JsonMessage to be sent
   */
  private synchronized void send(JSONObject message) {
    try {
      writer.write(message + System.lineSeparator());
      writer.flush();
      System.out.println("message sent **********");
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  /**
   * Handles sending a tile selection.
   *
   * @param source        plate
   * @param color         color
   * @param numberOfTiles number of tiles
   */
  public void sendTileSelection(int source, String color, int numberOfTiles) {
    JSONObject tileSelection = JsonMessage.selectTile(color, source);
    send(tileSelection);
  }

  /**
   * Handles sending the amount of players that will play.
   *
   * @param players amount
   */
  public void sendPlayers(int players) {
    JSONObject amountOfPlayers = JsonMessage.players(players);
    send(amountOfPlayers);
  }

  public void sendTilePlacement(int line, String color) { //color should be string
    JSONObject tilePlacement = JsonMessage.placeTiles(color, line);
    send(tilePlacement);
  }

  public void sendGameRestartRequest() {
    JSONObject request = JsonMessage.gameRestartRequest();
    send(request);
  }

  public void sendGameCancelRequest() {
    JSONObject request = JsonMessage.gameCancelRequest();
    send(request);
  }
}
