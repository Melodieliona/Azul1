package de.lmu.ifi.sosylab.server;

import de.lmu.ifi.sosylab.shared.JsonMessage;
import de.lmu.ifi.sosylab.shared.Tile;
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
              case LOGIN:
                clientNick = (String) jsonObject.get("nick");

                boolean nickAlreadyUsed = false;
                for (User user : users) {
                  if (clientNick.equals(user.getName())) {
                    nickAlreadyUsed = true;
                    break;
                  }
                }

                if (nickAlreadyUsed) {
                  JSONObject loginFailedJson = new JSONObject();
                  loginFailedJson.put("type", "login failed");

                  writer.write(loginFailedJson + System.lineSeparator());
                  writer.flush();
                } else {
                  // Inform other users
                  for (User user : users) {
                    if (user.getGameNumber() == nextGameNumber) {
                      JSONObject userJoinedJson = new JSONObject();
                      userJoinedJson.put("type", "user joined");
                      userJoinedJson.put("nick", clientNick);

                      user.getWriter().write(userJoinedJson + System.lineSeparator());
                      user.getWriter().flush();
                    }
                  }

                  // Inform newly logged in user
                  JSONObject loginSuccessJson = new JSONObject();
                  loginSuccessJson.put("type", "login success");
                  loginSuccessJson.put("gameNumber", nextGameNumber);

                  writer.write(loginSuccessJson + System.lineSeparator());
                  writer.flush();

                  // Add user to User list
                  users.add(new User(clientNick, writer, nextGameNumber));

                  //
                  int numberOfUsersInNextGame = 0;
                  for (User user : users) {
                    if (user.getGameNumber() == nextGameNumber) {
                      numberOfUsersInNextGame++;
                    }
                  }

                  // When 4 players are logged in
                  // Add new game with these players to the game list
                  // Update the number of the next game
                  if (numberOfUsersInNextGame > 3) {
                    List<User> usersInGame = new ArrayList<>();
                    for (User user : users) {
                      if (user.getGameNumber() == nextGameNumber) {
                        usersInGame.add(user);
                      }
                    }

                    games.add(new Game(nextGameNumber, usersInGame, connection));
                    clientGameNumber = nextGameNumber;
                    nextGameNumber++;
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



  protected void sendInvalidSelectionMessage() {

  }

  protected void sendInvalidPlacementMessage() {

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
  public void sendNextPlayer(List<User> userlist, User currentUser) {
    try {
      for (User user : userlist) {
        JSONObject sendClickableRowsJson = new JSONObject();
        sendClickableRowsJson.put("type", "next turn");
        sendClickableRowsJson.put("nick", currentUser.getName());

        user.getWriter().write(sendClickableRowsJson + System.lineSeparator());
        user.getWriter().flush();
      }
    } catch (IOException | JSONException e) {
      System.out.println(e.getMessage());
    }
  }

  public void sendNextRound() {




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
   * Stop the network-connection.
   * Unused in this implementation.
   */
  public void stop() {
    // stop connection
  }
}
