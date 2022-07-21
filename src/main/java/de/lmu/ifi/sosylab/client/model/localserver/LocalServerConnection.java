package de.lmu.ifi.sosylab.client.model.localserver;

import de.lmu.ifi.sosylab.shared.GameBoard;
import de.lmu.ifi.sosylab.shared.JsonMessage;
import de.lmu.ifi.sosylab.shared.LayingRow;
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

  LocalGame game = null;

  private BufferedWriter writer;

  private BufferedReader reader;

  List<LocalUser> users;

  private int amountOfExpectedUsers = 0;

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
          reader = new BufferedReader(
              new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
          writer = new BufferedWriter(
              new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8));

          while (keepReading) {
            String clientNick;

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
              case PLAYERS:
                if (amountOfExpectedUsers == 0) {
                  amountOfExpectedUsers = Integer.parseInt(jsonObject.getString("amount"));
                }
                break;
              case LOGIN:
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
                    sendLoginFailed();
                    break;
                  } else {
                    // Add user to User list
                    users.add(new LocalUser(clientNick));

                    // Acknowledge successful login
                    sendLoginSuccess();
                    sendUserJoined(clientNick);
                  }

                  // Start the game immediately if 4 players are logged in.
                  if (users.size() == amountOfExpectedUsers) {
                    startGame();
                  }
                } catch (JSONException e) {
                  System.out.println(e.getMessage());
                }
                break;
              case TILE_SELECTION:
                // 0 = middle, 1-9 = plates
                int plateOrMiddle = jsonObject.getInt("plate");
                Tile selectionColor = Tile.getTile((String) jsonObject.get("color"));

                game.handleTileSelection(plateOrMiddle, selectionColor);

                break;
              case TILE_PLACEMENT:
                // '0' is row 1, '1' is row 2, '2' is row 3, etc...
                int targetRow = jsonObject.getInt("row");
                Tile placementColor = Tile.getTile((String) jsonObject.get("color"));

                game.handleTilePlacement(targetRow, placementColor);

                break;
              case GAME_CANCEL_REQUEST:
                String nick = JsonMessage.getNickname(jsonObject);
                game.handleGameCancelRequest(nick);

                break;
              default:
                sendInvalidJsonError();
                break;
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
   * Tells client that it sent an invalid json message.
   * */
  private void sendInvalidJsonError() {
    try {
      System.out.println("Invalid Json"); // for debugging
      JSONObject sendLoginSuccessJson = new JSONObject();
      sendLoginSuccessJson.put("type", "invalid json");

      writer.write(sendLoginSuccessJson + System.lineSeparator());
      writer.flush();
    } catch (IOException | JSONException e) {
      System.out.println(e.getMessage());
    }
  }

  /**
   * Sends a login confirmation.
   * Also tells the current number of players.
   */
  private void sendLoginSuccess() {
    try {
      JSONObject sendLoginSuccessJson = new JSONObject();
      sendLoginSuccessJson.put("type", "login success");

      //Add number of waiting players
      sendLoginSuccessJson.put("amount", users.size());

      //Add names of waiting players
      StringBuilder nicknames = new StringBuilder();
      int userCounter = 0;
      for (LocalUser user : users) {
        nicknames.append(user.getName());
        if (userCounter < users.size() - 1) {
          nicknames.append(",");
        }
        userCounter++;
      }

      sendLoginSuccessJson.put("nick", nicknames.toString());

      writer.write(sendLoginSuccessJson + System.lineSeparator());
      writer.flush();
    } catch (IOException | JSONException e) {
      System.out.println(e.getMessage());
    }
  }

  /**
   * Sends a login denial.
   */
  private void sendLoginFailed() {
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
      sendMoveJson.put("plate", String.valueOf(sourceTilePlate));
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
  public void sendFloorLineUpdate(LocalUser currentUser, TileCollection newFloorLineTiles) {
    try {
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

      writer.write(sendNewFloorLineTiles + System.lineSeparator());
      writer.flush();
    } catch (IOException | JSONException e) {
      System.out.println(e.getMessage());
    }
  }

  /**
   * Send board state to all players at the very beginning and after a round ended.
   * Contains:
   * - "nick": Name of the player, this play board belongs to
   * - "row": Empty laying rows
   * - "color": Colors of the laid wall tiles
   * - "score": The current score of the player
   * */
  public void sendBoardUpdate(GameBoard[] gameBoards) {
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

        writer.write(sendBoardUpdate + System.lineSeparator());
        writer.flush();
      } catch (IOException | JSONException e) {
        System.out.println(e.getMessage());
      }
    }
  }

  /**
   * Sends filled tile plates to all players at the very beginning of a round.
   * Contains all tiles on plates and the middle.
   * */
  public void sendFilledPlates(TileCollection[] tilePlates) {
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

    // Send filled plates to client once
    // Client network layer needs to distribute the information to every player
    try {
      JSONObject sendFillPlates = new JSONObject();
      sendFillPlates.put("type", "fill plates");
      sendFillPlates.put("color", tileColors.toString());
      sendFillPlates.put("tiles", tileAmounts.toString());

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
      StringBuilder clickableRows = new StringBuilder();
      for (int i = 0; i < rows.length; i++) {
        clickableRows.append(rows[i]);
        if (i != rows.length - 1) {
          clickableRows.append(" ");
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
   * Announces the winner(s) of the game to all players and sends the final scores.
   * */
  public void announceWinner(int[] endScores, ArrayList<String> winners,
                             ArrayList<String> usernames) {
    try {
      JSONObject message = JsonMessage.gameEndedMessage(endScores, winners, usernames);
      writer.write(message + System.lineSeparator());
      writer.flush();

    } catch (IOException e) {
      System.out.println(e.getMessage());
    }
  }

  /**
   * Starts the game.
   * */
  private void startGame() {
    game = new LocalGame(new ArrayList<>(users), connection);
  }

  /**
   * Stop the network-connection.
   * Unused in this implementation.
   */
  public void stop() {
    System.out.println("shuting down local");
    users.clear();
    System.out.println("names are free");
    executorService.shutdownNow();
    try {
      socket.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  /**
   * Sends a message to the client when the game was cancelled.
   * */
  public void sendGameCancel() {
    try {
      JSONObject message = JsonMessage.gameCancel();
      writer.write(message + System.lineSeparator());
      writer.flush();

    } catch (IOException e) {
      System.out.println(e.getMessage());
    }
  }


}
