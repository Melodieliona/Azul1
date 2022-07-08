package de.lmu.ifi.sosylab.server;

import de.lmu.ifi.sosylab.client.model.localserver.LocalGame;
import de.lmu.ifi.sosylab.shared.GameBoard;
import de.lmu.ifi.sosylab.shared.JsonMessage;
import de.lmu.ifi.sosylab.shared.Tile;
import de.lmu.ifi.sosylab.shared.TileCollection;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Network Layer of the game server.
 * */
public class ServerNetworkConnection {
  private static final int port = 8080;

  private final ServerNetworkConnection connection;

  List<User> users;

  List<Game> games;

  private int nextGameNumber = 1;

  private boolean gameStartTimerRunning = false;

  /**
   * Initializes the User list, which stores all clients that are currently connected.
   */
  public ServerNetworkConnection() {
    users = new ArrayList<>();
    games = new ArrayList<>();
    connection = this;
  }

  /**
   * Start the network-connection such that clients can establish a connection to this server.
   * Starts a Thread, which listens for new connection requests.
   */
  public void start() {

    ServerSocket serverSocket;
    try {
      serverSocket = new ServerSocket(port);
    } catch (IOException e) {
      System.out.println("Cannot create socket with port " + port + ".");
      return;
    }

    Thread acceptThread = new Thread(() -> {
      Socket socket;
      try {
        try {
          while (true) {
            socket = serverSocket.accept();
            // Start thread for every new client
            startHandler(socket);
          }
        } finally {
          serverSocket.close();
        }
      } catch (IOException e) {
        System.out.println(e.getMessage());
      }
    });

    acceptThread.start();
  }

  /**
   * Starts a new thread to handle one client.
   * Listens to incoming messages from the client.
   *
   * @param socket Provides the connection to a new client
   * */
  private void startHandler(Socket socket) {

    Thread newConnectionThread = new Thread() {
      String clientNick = "Not initialized.";
      int clientGameNumber = 0;

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
                clientNick = (String) jsonObject.get("nick");

                boolean nickAlreadyUsed = false;
                for (User user : users) {
                  if (user.getGameNumber() == nextGameNumber) {
                    if (clientNick.equals(user.getName())) {
                      nickAlreadyUsed = true;
                      break;
                    }
                  }
                }

                if (nickAlreadyUsed) {
                  sendLoginFailed(writer);
                  System.out.println("Login Failed Json sent!");
                  break;
                } else {
                  // Acknowledge successful login
                  sendLoginSuccess(writer);
                  sendUserJoined(clientNick);

                  // Add user to User list
                  users.add(new User(clientNick, writer, nextGameNumber));

                  // Start new game when 4 players are logged in
                  // Start a new timer when at least 2 players are logged in.
                  int numberOfUsersInNextGame = 0;
                  for (User user : users) {
                    if (user.getGameNumber() == nextGameNumber) {
                      numberOfUsersInNextGame++;
                    }
                  }

                  if (numberOfUsersInNextGame > 3) {
                    startGame();
                    clientGameNumber = nextGameNumber;
                    nextGameNumber++;
                  } else if (numberOfUsersInNextGame > 1) {
                    if(gameStartTimerRunning) {
                      startTimer();
                    }
                  }

                }
                break;
              case TILE_SELECTION:
                // 0 = middle, 1-9 = plates
                int plateOrMiddle = (int) jsonObject.get("source");
                Tile tileColor = Tile.getTile((String) jsonObject.get("color"));
                int tileAmount = (int) jsonObject.get("amount");

                for (Game game : games) {
                  if (game.getGameNumber() == clientGameNumber) {
                    game.handleTileSelection(clientNick, plateOrMiddle, tileColor, tileAmount);
                    break;
                  }
                }
                break;
              case TILE_PLACEMENT:
                // '0' is row 1, '1' is row 2, '2' is row 3, etc...
                int targetRow = (int) jsonObject.get("target");
                tileColor = Tile.getTile((String) jsonObject.get("color"));
                tileAmount = (int) jsonObject.get("amount");

                for (Game game : games) {
                  if (game.getGameNumber() == clientGameNumber) {
                    game.handleTilePlacement(clientNick, targetRow, tileColor, tileAmount);
                    break;
                  }
                }
                break;
              default: break;
            }
          }
        } catch (IOException | JSONException e) {
          e.printStackTrace();
        } finally {
          // Remove user
          User removeUser = null;
          for (User user : users) {
            if (clientNick.equals(user.getName())) {
              removeUser = user;
              break;
            }
          }
          users.remove(removeUser);

          // Close the socket
          try {
            socket.close();
          } catch (IOException e) {
            e.printStackTrace();
          }

          // Inform other users
          if (!(removeUser == null)) {
            sendUserLeft(removeUser.getName());
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
      for (User user : users) {
        if (user.getGameNumber() == nextGameNumber) {
          JSONObject userJoinedJson = new JSONObject();
          userJoinedJson.put("type", "user joined");
          userJoinedJson.put("nick", nickname);

          user.getWriter().write(userJoinedJson + System.lineSeparator());
          user.getWriter().flush();
        }
      }
    } catch (IOException | JSONException e) {
      System.out.println(e.getMessage());
    }
  }

  /**
   * Denies a tile selection request.
   */
  protected void sendInvalidSelectionMessage(User user) {
    try {
      JSONObject sendNextPlayerJson = new JSONObject();
      sendNextPlayerJson.put("type", "tiles not allowed");

      user.getWriter().write(sendNextPlayerJson + System.lineSeparator());
      user.getWriter().flush();
    } catch (IOException | JSONException e) {
      System.out.println(e.getMessage());
    }
  }

  /**
   * Denies a tile placement request.
   */
  protected void sendInvalidPlacementMessage(User user) {
    try {
      JSONObject sendNextPlayerJson = new JSONObject();
      sendNextPlayerJson.put("type", "move not allowed");

      user.getWriter().write(sendNextPlayerJson + System.lineSeparator());
      user.getWriter().flush();
    } catch (IOException | JSONException e) {
      System.out.println(e.getMessage());
    }

  }

  /**
   * Sends a successful tile selection to all users (including the sender as confirmation).
   */
  public void sendTileSelection(
      List<User> list, String currentPlayer, int sourceTilePlate, Tile color, int amount) {
    try {
      for (User user : list) {
        JSONObject sendMoveJson = new JSONObject();
        sendMoveJson.put("type", "tile selection");
        sendMoveJson.put("nick", currentPlayer);
        // sourceTilePlate = 0 means the middle
        sendMoveJson.put("plate", sourceTilePlate);
        sendMoveJson.put("color", color.name());
        sendMoveJson.put("amount", amount);

        user.getWriter().write(sendMoveJson + System.lineSeparator());
        user.getWriter().flush();
      }
    } catch (IOException | JSONException e) {
      System.out.println(e.getMessage());
    }
  }

  /**
   * Sends a successful tile placement to all users (including the sender as confirmation).
   */
  public void sendTilePlacement(
      List<User> list, String currentPlayer, Tile color, int amount, int layingRow) {
    try {
      for (User user : list) {
        JSONObject sendMoveJson = new JSONObject();
        sendMoveJson.put("type", "tile placement");
        sendMoveJson.put("nick", currentPlayer);
        sendMoveJson.put("color", color.name());
        sendMoveJson.put("amount", amount);
        sendMoveJson.put("row", layingRow);

        user.getWriter().write(sendMoveJson + System.lineSeparator());
        user.getWriter().flush();
      }
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
      List<User> userlist, User currentUser, TileCollection newFloorLineTiles) {
    try {
      for (User user : userlist) {
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

        user.getWriter().write(sendNewFloorLineTiles + System.lineSeparator());
        user.getWriter().flush();
      }
    } catch (IOException | JSONException e) {
      System.out.println(e.getMessage());
    }
  }

  /**
   * Sends their updated score to all players of a game before the next round starts.
   * */
  public void sendScoreUpdate(int minusPoints) {

    // TODO
  }

  /**
   * Send board state to all players at the very beginning and after a round ended.
   * Contains:
   * - the current score of each player
   * - whose turn it is next (..?..)
   * */
  public void sendBoardUpdate(List<User> userList, GameBoard[] gameBoards) {
    try {
      for (User user : userList) {
        JSONObject sendBoardUpdate = new JSONObject();
        sendBoardUpdate.put("type", "board update");

        user.getWriter().write(sendBoardUpdate + System.lineSeparator());
        user.getWriter().flush();
      }
    } catch (IOException | JSONException e) {
      System.out.println(e.getMessage());
    }
  }

  /**
   * Send filled tile plates to all players at the very beginning of a round.
   * Contains all tiles on plates and the middle
   * */
  public void sendFilledPlates(List<User> userList, TileCollection[] tilePlates) {

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

    try {
      for (User user : userList) {
        JSONObject sendFillPlates = new JSONObject();
        sendFillPlates.put("type", "fill plates");
        sendFillPlates.put("color", tileColors);
        sendFillPlates.put("tiles", tileAmounts);

        user.getWriter().write(sendFillPlates + System.lineSeparator());
        user.getWriter().flush();
      }
    } catch (IOException | JSONException e) {
      System.out.println(e.getMessage());
    }
  }

  /**
   * Gets send after a tile selection was made successfully.
   * Tells the player whose turn it is, which rows he can place selected tile(s) on.
   * */
  public void sendClickableRows(User user, int[] rows) {
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

      user.getWriter().write(sendClickableRowsJson + System.lineSeparator());
      user.getWriter().flush();

    } catch (IOException | JSONException e) {
      System.out.println(e.getMessage());
    }
  }

  /**
   * Tells players whose turn it is now.
   */
  public void sendNextPlayer(List<User> userList, User currentUser) {
    try {
      for (User user : userList) {
        JSONObject sendNextPlayerJson = new JSONObject();
        sendNextPlayerJson.put("type", "next turn");
        sendNextPlayerJson.put("nick", currentUser.getName());

        user.getWriter().write(sendNextPlayerJson + System.lineSeparator());
        user.getWriter().flush();
      }
    } catch (IOException | JSONException e) {
      System.out.println(e.getMessage());
    }
  }

  /**
   * Announces the winner(s) of the game to all players and sends the final scores.
   * Format winnerJson: {"type": "winner", "amount": int amountOfWinners,
   *                     "winner0": ... [, winner1": ... [, "winner2": ... [, "winner3": ...]]]}
   * Format finalScoresJson: {"type": "points", "points0": ..., "points1": ..., "points2": ...,
   *                                            "points3": ...}
   * */
  public void announceWinner(List<User> userList, int[] endScores, ArrayList<String> winners) {
    try {
      for (User user : userList) {
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

        user.getWriter().write(winnerJson + System.lineSeparator());
        user.getWriter().flush();
        user.getWriter().write(finalScoresJson + System.lineSeparator());
        user.getWriter().flush();
      }
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
      for (User user : users) {
        JSONObject postMessageJson = new JSONObject();
        postMessageJson.put("type", "user left");
        postMessageJson.put("nick", clientNick);

        user.getWriter().write(postMessageJson + System.lineSeparator());
        user.getWriter().flush();
      }
    } catch (IOException | JSONException e) {
      System.out.println(e.getMessage());
    }
  }

  /**
   * Tells all players waiting for the game to start, that the game will start soon.
   * */
  private void sendStartTimer() {
    try {
      for (User user : users) {
        if (user.getGameNumber() == nextGameNumber) {
          JSONObject postMessageJson = new JSONObject();
          postMessageJson.put("type", "timer");

          user.getWriter().write(postMessageJson + System.lineSeparator());
          user.getWriter().flush();
        }
      }
    } catch (IOException | JSONException e) {
      System.out.println(e.getMessage());
    }
  }

  /**
   * Starts the game.
   * */
  private void startGame() {
    List<User> usersInGame = new ArrayList<>();
    for (User user : users) {
      if (user.getGameNumber() == nextGameNumber) {
        usersInGame.add(user);
      }
    }

    games.add(new Game(nextGameNumber, usersInGame, connection));
  }

  /**
   * Starts a timer. When expired, starts the game with current amount of logged in players.
   * Timer length: 60 sec
   * */
  private void startTimer() {
    gameStartTimerRunning = true;
    sendStartTimer();

    Thread timerThread = new Thread(() -> {
      try {
        Thread.sleep(1000 * 60);
      } catch (InterruptedException e) {
        throw new RuntimeException(e);
      }
      // Check if game hasn't already been started (because a 4th user joined) and if there are
      // enough users for a game (at least 2)
      if((games.size() == nextGameNumber + 1) && (users.size() > 1)) {
        gameStartTimerRunning = false;
        startGame();
      }
    });

    timerThread.start();
  }

  /**
   * Stop the network-connection.
   * Unused in this implementation.
   */
  public void stop() {
    // stop connection
  }
}
