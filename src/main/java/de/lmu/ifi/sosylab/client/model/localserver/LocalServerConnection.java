package de.lmu.ifi.sosylab.client.model.localserver;

import de.lmu.ifi.sosylab.server.User;
import de.lmu.ifi.sosylab.shared.GameBoard;
import de.lmu.ifi.sosylab.shared.JsonMessage;
import de.lmu.ifi.sosylab.shared.Tile;
import de.lmu.ifi.sosylab.shared.TileCollection;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Network Layer of the game server.
 * */
public class LocalServerConnection {
  private static final int port = 9090;

  private final LocalServerConnection connection;

  private final ServerSocket socket;

  private final ExecutorService executorService;

  List<LocalUser> users;

  LocalGame game = null;

  private BufferedWriter writer;

  private BufferedReader reader;

  /**
   * Initializes the User list, which stores all clients that are currently connected.
   */
  public LocalServerConnection() throws IOException {
    users = new ArrayList<>();
    connection = this;
    executorService = Executors.newCachedThreadPool();
    socket = new ServerSocket(port);
  }

  /**
   * Start the network-connection such that clients can establish a connection to this server.
   * Starts a Thread, which listens for the client's connection request.
   */
  public void start() {
    // Waiting for a client in another Thread
    Thread waitForClientConnection = new Thread(() -> {
      try {
        Socket clientSocket = socket.accept();
        // Start a new thread for the hotseat client
        startHandler(clientSocket);
      } catch (IOException e) {
        System.out.println(e.getMessage());
      }
    });
    waitForClientConnection.start();
  }

  /**
   * Starts a new thread to handle one client.
   * Listens to incoming messages from the client.
   *
   * @param socket Provides the connection to a new client
   * */
  private void startHandler(Socket socket) {

    Thread newConnectionThread = new Thread() {

      private boolean keepReading = true;

      //TODO: Get rid of sout's
      @Override
      public void run() {

        try {
          BufferedReader reader = new BufferedReader(
              new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
          OutputStreamWriter writer =
              new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8);


          while (keepReading) {
            String clientNick = "Not initialized.";

            // Wait for a single message from the client.
            // If readLine is null, means that socket is closed / client disconnected.
            JSONObject jsonObject;
            try {
              String readLine = reader.readLine();
              jsonObject = new JSONObject(readLine);
            } catch (Exception e) {
              keepReading = false;
              break;
            }
            System.out.println("message received");
            // Get Info out of the message
            switch (JsonMessage.typeOf(jsonObject)) {
              case LOGIN:
                System.out.println("login request");
                try {
                  clientNick = (String) jsonObject.get("nick");

                  boolean nickAlreadyUsed = false;
                  for (LocalUser user : users) {
                    if (clientNick.equals(user.getName())) {
                      nickAlreadyUsed = true;
                      break;
                    }
                  }

                  if (nickAlreadyUsed) {
                    sendLoginFailed(writer);
                    break;
                  } else {
                    // Acknowledge successful login
                    sendLoginSuccess(writer);
                    sendUserJoined(clientNick);
                  }

                  // Add user to User list
                  users.add(new LocalUser(clientNick));

                  // TODO Determine when to start the game
                  int numberOfUsersInGame = 0;
                  for (int i = 0; i < users.size(); i++) {
                    numberOfUsersInGame++;
                  }

                  // When 4 players are logged in
                  // Add new game with these players to the game list
                  // Update the number of the next game
                  if (numberOfUsersInGame > 3) {
                    List<LocalUser> usersInGame = new ArrayList<>(users);

                    game = new LocalGame(usersInGame, connection);
                  }
                } catch (JSONException e) {
                  System.out.println(e.getMessage());
                }
                break;
              case TILE_SELECTION:
                // 0 = middle, 1-9 = plates
                int plateOrMiddle = (int) jsonObject.get("source");
                Tile tileColor = Tile.getTile((String) jsonObject.get("color"));
                int tileAmount = (int) jsonObject.get("amount");

                game.handleTileSelection(clientNick, plateOrMiddle, tileColor, tileAmount);

                break;
              case TILE_PLACEMENT:
                // '0' is row 1, '1' is row 2, '2' is row 3, etc...
                int targetRow = (int) jsonObject.get("target");
                tileColor = Tile.getTile((String) jsonObject.get("color"));
                tileAmount = (int) jsonObject.get("amount");

                game.handleTilePlacement(clientNick, targetRow, tileColor, tileAmount);

                break;
              default: break;
            }
          }
        } catch (IOException | JSONException e) {
          e.printStackTrace();
        } finally {
          // Remove user
          users.clear();

          // Close the socket
          try {
            socket.close();
          } catch (IOException e) {
            e.printStackTrace();
          }

        }
      }
    };

    newConnectionThread.start();
  }

  /**
   * Sends a login confirmation.
   */
  private void sendLoginSuccess(OutputStreamWriter writer) {
    try {
      System.out.println("log in"); // for debugging
      JSONObject sendLoginSuccessJson = new JSONObject();
      sendLoginSuccessJson.put("type", "login success");

      writer.write(sendLoginSuccessJson + System.lineSeparator());
      writer.flush();
    } catch (IOException | JSONException e) {
      System.out.println(e.getMessage());
    }
  }

  /**
   * Sends a login denial.
   */
  private void sendLoginFailed(OutputStreamWriter writer) {
    try {
      JSONObject sendLoginFailedJson = new JSONObject();
      sendLoginFailedJson.put("type", "login failed");

      writer.write(sendLoginFailedJson + System.lineSeparator());
      writer.flush();
    } catch (IOException | JSONException e) {
      System.out.println(e.getMessage());
    }
  }

  /**
   * Acknowledges that a player has successfully joined the game.
   * */
  private void sendUserJoined(String nickname) {
    try {
      JSONObject sendUserJoined = new JSONObject();
      sendUserJoined.put("type", "user joined");
      sendUserJoined.put("nick", nickname);

      writer.write(sendUserJoined + System.lineSeparator());
      writer.flush();
    } catch (IOException | JSONException e) {
      System.out.println(e.getMessage());
    }
  }

  /**
   * Denies a tile selection request.
   */
  protected void sendInvalidSelectionMessage(LocalUser user) {
    try {
      JSONObject sendNextPlayerJson = new JSONObject();
      sendNextPlayerJson.put("type", "tiles not allowed");

      writer.write(sendNextPlayerJson + System.lineSeparator());
      writer.flush();
    } catch (IOException | JSONException e) {
      System.out.println(e.getMessage());
    }
  }

  /**
   * Denies a tile placement request.
   */
  protected void sendInvalidPlacementMessage(LocalUser user) {
    try {
      JSONObject sendNextPlayerJson = new JSONObject();
      sendNextPlayerJson.put("type", "move not allowed");

      writer.write(sendNextPlayerJson + System.lineSeparator());
      writer.flush();
    } catch (IOException | JSONException e) {
      System.out.println(e.getMessage());
    }

  }

  /**
   * Sends a successful tile selection to all users (including the sender as confirmation).
   */
  public void sendTileSelection(String currentPlayer, int sourceTilePlate, Tile color, int amount) {
    try {
      JSONObject sendMoveJson = new JSONObject();
      sendMoveJson.put("type", "tile selection");
      sendMoveJson.put("nick", currentPlayer);
      // sourceTilePlate = 0 means the middle
      sendMoveJson.put("plate", sourceTilePlate);
      sendMoveJson.put("color", color.name());
      sendMoveJson.put("amount", amount);

      writer.write(sendMoveJson + System.lineSeparator());
      writer.flush();

    } catch (IOException | JSONException e) {
      System.out.println(e.getMessage());
    }
  }

  /**
   * Sends a successful tile placement to all users (including the sender as confirmation).
   */
  public void sendTilePlacement(String currentPlayer, Tile color, int amount, int layingRow) {
    try {
      JSONObject sendMoveJson = new JSONObject();
      sendMoveJson.put("type", "tile placement");
      sendMoveJson.put("nick", currentPlayer);
      sendMoveJson.put("color", color.name());
      sendMoveJson.put("amount", amount);
      sendMoveJson.put("row", layingRow);

      writer.write(sendMoveJson + System.lineSeparator());
      writer.flush();

    } catch (IOException | JSONException e) {
      System.out.println(e.getMessage());
    }
  }

  // TODO Integrate Floorline update into board update
  /**
   * Sends a message with all tiles that have been added to the floor line
   * to all other players of that game.
   * */
  public void sendFloorLineUpdate(
      List<LocalUser> userlist, LocalUser currentUser, TileCollection newFloorLineTiles) {
    try {
      for (LocalUser user : userlist) {
        JSONObject sendNewFloorLineTiles = new JSONObject();
        sendNewFloorLineTiles.put("type", "floorline_placement");
        sendNewFloorLineTiles.put("nick", currentUser.getName());
        sendNewFloorLineTiles.put("amount", newFloorLineTiles.size());

        // Formats tiles like this: {.."floortile0": "BLUE", "floortile1": "RED"..}
        // Index is relative to the newly added tiles ~ floortile0 is not the first tile on the
        // floor line, but the first tile to be added to it now.
        // If the starting marker was added, it will be "floortile0"
        int tileIndex = 0;

        for (Tile tile : newFloorLineTiles) {
          String tileNumber = "floortile" + tileIndex;
          sendNewFloorLineTiles.put(tileNumber, tile.name());
          tileIndex++;
        }

        writer.write(sendNewFloorLineTiles + System.lineSeparator());
        writer.flush();
      }
    } catch (IOException | JSONException e) {
      System.out.println(e.getMessage());
    }
  }

  /**
   * Sends their updated score to all players of a game before the next round starts.
   * */
  public void sendScoreUpdate(int score) {

    // TODO
  }

  /**
   * Send board state to all players at the very beginning and after a round ended.
   * Contains:
   * - the current score of each player
   * - whose turn it is next (..?..)
   * */
  public void sendBoardUpdate(GameBoard[] gameBoards) {
    try {
      JSONObject sendBoardUpdate = new JSONObject();
      sendBoardUpdate.put("type", "board update");

      writer.write(sendBoardUpdate + System.lineSeparator());
      writer.flush();
    } catch (IOException | JSONException e) {
      System.out.println(e.getMessage());
    }
  }

  /**
   * Send filled tile plates to all players at the very beginning of a round.
   * Contains all tiles on plates and the middle
   * */
  public void sendFilledPlates(List<LocalUser> userList, TileCollection[] tilePlates) {

    // { "type" : "fill plates", "color" : "red yellow,black green blue”, “tiles” : “ 3 1,1 1 2“ }
    // TODO Testing

    String tileColors = "";
    String tileAmounts = "";

    for (TileCollection plate : tilePlates) {
      ArrayList<Tile> containedColors = plate.getContainedColors();
      for (Tile tile : containedColors) {
        tileColors += (tile.name() + " ");
        tileAmounts += (plate.getAmountTilesOfColor(tile) + " ");
      }
      tileColors += ",";
      tileAmounts += ",";
    }

    // Send filled plates to client once
    // Client network layer needs to distribute the information to every player
    try {
      JSONObject sendFillPlates = new JSONObject();
      sendFillPlates.put("type", "fill plates");
      sendFillPlates.put("color", tileColors);
      sendFillPlates.put("tiles", tileAmounts);

      writer.write(sendFillPlates + System.lineSeparator());
      writer.flush();

    } catch (IOException | JSONException e) {
      System.out.println(e.getMessage());
    }
  }


  /**
   * Gets send after a tile selection was made successfully.
   * Tells the player whose turn it is, which rows he can place selected tile(s) on.
   * */
  public void sendClickableRows(int[] rows) {
    try {
      String clickableRows = "";
      for (int i = 0; i < rows.length; i++) {
        clickableRows += rows[i];
        if (i != rows.length - 1) {
          clickableRows += " ";
        }
      }

      JSONObject sendClickableRowsJson = new JSONObject();
      sendClickableRowsJson.put("type", "allowed fields");
      sendClickableRowsJson.put("row", clickableRows);

      writer.write(sendClickableRowsJson + System.lineSeparator());
      writer.flush();

    } catch (IOException | JSONException e) {
      System.out.println(e.getMessage());
    }
  }

  /**
   * Tells players whose turn it is now.
   */
  public void sendNextPlayer(LocalUser currentUser) {
    try {
      JSONObject sendNextPlayerJson = new JSONObject();
      sendNextPlayerJson.put("type", "next turn");
      sendNextPlayerJson.put("nick", currentUser.getName());

      writer.write(sendNextPlayerJson + System.lineSeparator());
      writer.flush();
    } catch (IOException | JSONException e) {
      System.out.println(e.getMessage());
    }
  }

  /**
   * TODO Template, maybe unused.
   */
  public void sendNextRound() {

  }

  /**
   * Announces the winner(s) of the game to all players and sends the final scores.
   * Format winnerJson: {"type": "winner", "amount": int amountOfWinners,
   *                     "winner0": ... [, winner1": ... [, "winner2": ... [, "winner3": ...]]]}
   * Format finalScoresJson: {"type": "points", "points0": ..., "points1": ..., "points2": ...,
   *                                            "points3": ...}
   * */
  public void announceWinner(int[] endScores, ArrayList<String> winners) {
    try {
      JSONObject winnerJson = new JSONObject();
      winnerJson.put("type", "winner");
      winnerJson.put("amount", winners.size());
      // winners are 0-indexed
      for (int i = 0; i < winners.size(); i++) {
        String winnerIndex = "winner" + i;
        winnerJson.put(winnerIndex, winners.get(i));
      }

      JSONObject finalScoresJson = new JSONObject();
      finalScoresJson.put("type", "points");
      for (int i = 0; i < endScores.length; i++) {
        String scoreOfPlayerIndex = "points" + i;
        finalScoresJson.put(scoreOfPlayerIndex, endScores[i]);
      }

      writer.write(winnerJson + System.lineSeparator());
      writer.flush();
      writer.write(finalScoresJson + System.lineSeparator());
      writer.flush();

    } catch (IOException | JSONException e) {
      System.out.println(e.getMessage());
    }
  }

  /**
   * Broadcasts that a user has disconnected from the server to all still connected clients.
   *
   * @param clientNick Name of the disconnected user.
   */
  private void sendUserLeft(String clientNick) {
    try {
      JSONObject postMessageJson = new JSONObject();
      postMessageJson.put("type", "user left");
      postMessageJson.put("nick", clientNick);

      writer.write(postMessageJson + System.lineSeparator());
      writer.flush();
    } catch (IOException | JSONException e) {
      System.out.println(e.getMessage());
    }
  }

  /**
   * Stop the network-connection.
   * Unused in this implementation.
   */
  public void stop() {
    executorService.shutdownNow();
    try {
      socket.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
