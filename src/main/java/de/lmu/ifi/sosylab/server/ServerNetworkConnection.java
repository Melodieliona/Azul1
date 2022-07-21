package de.lmu.ifi.sosylab.server;

import de.lmu.ifi.sosylab.shared.GameBoard;
import de.lmu.ifi.sosylab.shared.JsonMessage;
import de.lmu.ifi.sosylab.shared.LayingRow;
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
 */
public class ServerNetworkConnection {
  private static final int port = 8080;

  private final ServerNetworkConnection connection;

  List<User> users;

  List<Game> games;

  private int nextGameNumber = 1;

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
      System.out.println("Cannot create socket with port " + port + ".\n"
          + "Likely the port is already in use.");

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
        e.printStackTrace();
      }
    });

    acceptThread.start();
  }

  /**
   * Starts a new thread to handle one client.
   * Listens to incoming messages from the client.
   *
   * @param socket Provides the connection to a new client
   */
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

            // Get Info out of the message
            switch (JsonMessage.typeOf(jsonObject)) {
              case LOGIN -> {
                clientNick = (String) jsonObject.get("nick");
                clientGameNumber = nextGameNumber;
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

                  //Immediately start the game without waiting for the timer if 4 players joined
                  if (numberOfUsersInNextGame == 4) {
                    sendTimerEnded();
                    startGame();
                    nextGameNumber++;
                  } else if ((numberOfUsersInNextGame == 2 | numberOfUsersInNextGame == 3)) {
                    startTimer();
                  }
                }
              }

              case TILE_SELECTION -> {
                // 0 = middle, 1-9 = plates
                int plateOrMiddle = jsonObject.getInt("plate");
                Tile selectionColor = Tile.getTile((String) jsonObject.get("color"));
                for (Game game : games) {
                  if (game.getGameNumber() == clientGameNumber) {
                    game.handleTileSelection(clientNick, plateOrMiddle, selectionColor);
                    break;
                  }
                }
              }

              case TILE_PLACEMENT -> {
                // '0' is row 1, '1' is row 2, '2' is row 3, etc...
                int targetRow = jsonObject.getInt("row");
                Tile placementColor = Tile.getTile((String) jsonObject.get("color"));
                for (Game game : games) {
                  if (game.getGameNumber() == clientGameNumber) {
                    game.handleTilePlacement(targetRow, placementColor);
                    break;
                  }
                }
              }
              case GAME_CANCEL_REQUEST -> {
                String nick = JsonMessage.getNickname(jsonObject);
                for (Game game : games) {
                  if (game.getGameNumber() == clientGameNumber) {
                    game.handleGameCancelRequest(nick);
                    break;
                  }
                }
              }

              default -> {
              }
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
      JSONObject sendLoginSuccessJson = new JSONObject();
      sendLoginSuccessJson.put("type", "login success");

      //Add number of waiting players
      int currentAmountOfWaitingPlayers = 0;
      for (User user : users) {
        if (user.getGameNumber() == nextGameNumber) {
          currentAmountOfWaitingPlayers++;
        }
      }
      sendLoginSuccessJson.put("amount", currentAmountOfWaitingPlayers);

      //Add names of waiting players
      StringBuilder nicknames = new StringBuilder();
      int userCounter = 0;
      for (User user : users) {
        if (user.getGameNumber() == nextGameNumber) {
          nicknames.append(user.getName());
          if (userCounter < users.size() - 1) {
            nicknames.append(",");
          }
          userCounter++;
        }
      }

      sendLoginSuccessJson.put("nick", nicknames.toString());

      writer.write(sendLoginSuccessJson + System.lineSeparator());
      writer.flush();
    } catch (IOException | JSONException e) {
      e.printStackTrace();
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
      e.printStackTrace();
    }
  }

  /**
   * Acknowledges that a player has successfully joined the game.
   */
  private void sendUserJoined(String nickname) {
    try {
      for (User user : users) {
        if (user.getGameNumber() == nextGameNumber && !(user.getName().equals(nickname))) {
          JSONObject userJoinedJson = new JSONObject();
          userJoinedJson.put("type", "user joined");
          userJoinedJson.put("nick", nickname);

          user.getWriter().write(userJoinedJson + System.lineSeparator());
          user.getWriter().flush();
        }
      }
    } catch (IOException | JSONException e) {
      e.printStackTrace();
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
      e.printStackTrace();
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
      e.printStackTrace();
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
        sendMoveJson.put("plate", String.valueOf(sourceTilePlate));
        sendMoveJson.put("color", color.name());
        sendMoveJson.put("amount", amount);

        user.getWriter().write(sendMoveJson + System.lineSeparator());
        user.getWriter().flush();
      }
    } catch (IOException | JSONException e) {
      e.printStackTrace();
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
      e.printStackTrace();
    }
  }

  // TODO Integrate Floorline update into board update

  /**
   * Sends a message with all tiles that have been added to the floor line
   * to all other players of that game.
   */
  public void sendFloorLineUpdate(
      List<User> userlist, User currentUser, TileCollection newFloorLineTiles) {
    try {
      for (User user : userlist) {
        JSONObject sendNewFloorLineTiles = new JSONObject();
        sendNewFloorLineTiles.put("type", "floor line update");
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
      e.printStackTrace();
    }
  }

  /**
   * Sends the updated score to the player who just made a move.
   */
  public void sendScoreUpdate(User user, int score) {
    try {
      JSONObject sendScoreUpdate = new JSONObject();

      sendScoreUpdate.put("score", 12345);

      user.getWriter().write(sendScoreUpdate + System.lineSeparator());
      user.getWriter().flush();
    } catch (IOException | JSONException e) {
      e.printStackTrace();
    }
  }

  /**
   * Send board state to all players at the very beginning and after a round ended.
   * Contains:
   * - "nick": Name of the player, this play board belongs to
   * - "row": Empty laying rows
   * - "color": Colors of the laid wall tiles
   * - "score": The current score of the player as Integer
   */
  public void sendBoardUpdate(List<User> userList, GameBoard[] gameBoards) {
    for (User user : userList) {
      for (GameBoard gameBoard : gameBoards) {
        try {
          JSONObject sendBoardUpdate = new JSONObject();
          sendBoardUpdate.put("type", "board update");
          sendBoardUpdate.put("nick", gameBoard.getPlayerName());

          //Format empty laying rows
          StringBuilder emptyLayingRows = new StringBuilder();
          for (LayingRow layingRow : gameBoard.getLayingRows()) {
            if (layingRow.isRowEmpty()) {
              emptyLayingRows.append(layingRow.getRowNumber());
              emptyLayingRows.append(',');
            }
          }
          if (emptyLayingRows.charAt(emptyLayingRows.length() - 1) == ',') {
            emptyLayingRows.deleteCharAt(emptyLayingRows.length() - 1);
          }

          //Format wall tiles
          Tile[][] wall = gameBoard.getTileWall();
          StringBuilder wallTileColors = new StringBuilder();
          for (int row = 0; row < 5; row++) {
            for (int col = 0; col < 5; col++) {
              if (wall[col][row] == null) {
                wallTileColors.append("0");
              } else {
                wallTileColors.append(wall[col][row].getColor());
              }
              wallTileColors.append(" ");
            }
            wallTileColors.deleteCharAt(wallTileColors.length() - 1);
            wallTileColors.append(',');
          }
          wallTileColors.deleteCharAt(wallTileColors.length() - 1);

          //Add data to JSON
          sendBoardUpdate.put("row", emptyLayingRows.toString());
          sendBoardUpdate.put("color", wallTileColors.toString());
          sendBoardUpdate.put("points", String.valueOf(gameBoard.getCurrentScore()));

          user.getWriter().write(sendBoardUpdate + System.lineSeparator());
          user.getWriter().flush();
        } catch (IOException | JSONException e) {
          System.out.println(e.getMessage());
        }
      }
    }
  }

  /**
   * Send filled tile plates to all players at the very beginning of a round.
   * Contains all tiles on plates and the middle
   */
  public void sendFilledPlates(List<User> userList, TileCollection[] tilePlates) {
    StringBuilder tileColors = new StringBuilder();
    StringBuilder tileAmounts = new StringBuilder();

    int platesIterator = 0;
    for (TileCollection plate : tilePlates) {
      ArrayList<Tile> containedColors = plate.getContainedColors();

      int colorsIterator = 0;
      for (Tile tile : containedColors) {
        tileColors.append(tile.name());
        tileAmounts.append(plate.getAmountTilesOfColor(tile));

        if (colorsIterator < containedColors.size() - 1) {
          tileColors.append(" ");
          tileAmounts.append(" ");
        }
        colorsIterator++;
      }

      if (platesIterator < tilePlates.length - 1) {
        tileColors.append(",");
        tileAmounts.append(",");
      }
      platesIterator++;
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
      e.printStackTrace();
    }
  }

  /**
   * Gets send after a tile selection was made successfully.
   * Tells the player whose turn it is, which rows he can place selected tile(s) on.
   */
  public void sendClickableRows(User user, int[] rows) {
    try {
      StringBuilder clickableRows = new StringBuilder();
      //String clickableRows = "";
      for (int i = 0; i < rows.length; i++) {
        clickableRows.append(rows[i]);
        if (i != rows.length - 1) {
          clickableRows.append(" ");
        }
      }

      JSONObject sendClickableRowsJson = new JSONObject();
      sendClickableRowsJson.put("type", "allowed fields");
      sendClickableRowsJson.put("row", clickableRows.toString());

      user.getWriter().write(sendClickableRowsJson + System.lineSeparator());
      user.getWriter().flush();

    } catch (IOException | JSONException e) {
      e.printStackTrace();
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
      e.printStackTrace();
    }
  }

  /**
   * Announces the winner(s) of the game to all players and sends the final scores.
   * Format winnerJson: {"type": "winner", "amount": int amountOfWinners,
   * "winner0": ... [, winner1": ... [, "winner2": ... [, "winner3": ...]]]}
   * Format finalScoresJson: {"type": "points", "points0": ..., "points1": ..., "points2": ...,
   * "points3": ...}
   */
  public void announceWinner(List<User> userList, int[] endScores, ArrayList<String> winners) {
    try {
      ArrayList<String> usernames = new ArrayList<>();
      for (User user : userList) {
        usernames.add(user.getName());
      }
      for (User user : userList) {
        JSONObject message = JsonMessage.gameEndedMessage(endScores, winners, usernames);
        user.getWriter().write(message + System.lineSeparator());
        user.getWriter().flush();
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public void sendGameCancel(List<User> userList) {
    try {
      for (User user : userList) {
        JSONObject message = JsonMessage.gameCancel();
        user.getWriter().write(message + System.lineSeparator());
        user.getWriter().flush();
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  /**
   * Sends each player that someone made a cancel request.
   *
   * @param list     the list of players in the game
   * @param nickname the nick of the player that has made the request
   */
  public void sendGameCancelRequest(List<User> list, String nickname) {
    try {
      for (User user : list) {
        JSONObject message = JsonMessage.gameCancelRequest(nickname);
        user.getWriter().write(message + System.lineSeparator());
        user.getWriter().flush();
      }
    } catch (IOException e) {
      e.printStackTrace();
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
      e.printStackTrace();
    }
  }

  /**
   * Tells all players waiting for the game to start, that the game will start soon.
   */
  private void sendStartTimer() {
    try {
      for (User user : users) {
        if (user.getGameNumber() == nextGameNumber) {
          JSONObject startTimerJson = new JSONObject();
          startTimerJson.put("type", "timer start");

          user.getWriter().write(startTimerJson + System.lineSeparator());
          user.getWriter().flush();
        }
      }
    } catch (IOException | JSONException e) {
      e.printStackTrace();
    }
  }

  /**
   * Tells all players waiting for the game to start, that the timer ended and game starts now.
   */
  private void sendTimerEnded() {
    try {
      for (User user : users) {
        if (user.getGameNumber() == nextGameNumber) {
          JSONObject timerEndedJson = new JSONObject();
          timerEndedJson.put("type", "timer end");

          user.getWriter().write(timerEndedJson + System.lineSeparator());
          user.getWriter().flush();
        }
      }
    } catch (IOException | JSONException e) {
      e.printStackTrace();
    }
  }

  /**
   * Starts the game.
   */
  private void startGame() {
    List<User> usersInGame = new ArrayList<>();
    for (User user : users) {
      if (user.getGameNumber() == nextGameNumber) {
        usersInGame.add(user);
      }
    }

    games.add(new Game(nextGameNumber, usersInGame, connection));
    nextGameNumber++;
  }

  /**
   * Starts a timer. When expired, starts the game with current amount of logged in players.
   * Timer length: 60 sec
   */
  private void startTimer() {
    sendStartTimer();
    //TODO: change timer to 1 minute
    Thread timerThread = new Thread(() -> {
      try {
        Thread.sleep(1000 * 30);
      } catch (InterruptedException e) {
        throw new RuntimeException(e);
      }

      List<User> usersInGame = new ArrayList<>();
      for (User user : users) {
        if (user.getGameNumber() == nextGameNumber) {
          usersInGame.add(user);
        }
      }

      // Check if game hasn't already been started (because a 4th user joined) and if there are
      // enough users for a game (at least 2)
      System.out.println("Timer elapsed! Game will start now with "
          + usersInGame.size() + " players.");
      if ((games.size() + 1 == nextGameNumber) && (usersInGame.size() > 1)) {
        sendTimerEnded();
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
