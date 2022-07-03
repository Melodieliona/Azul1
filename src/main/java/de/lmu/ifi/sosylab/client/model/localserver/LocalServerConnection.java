package de.lmu.ifi.sosylab.client.model.localserver;

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
public class LocalServerConnection {
    private static final int port = 9090;

    private final LocalServerConnection connection;

    List<LocalUser> users;

    LocalGame game = null;

    private int nextGameNumber = 1;

    /**
     * Initializes the User list, which stores all clients that are currently connected.
     */
    public LocalServerConnection() {
        users = new ArrayList<>();
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

        try {
            Socket socket = serverSocket.accept();
            // Start a new thread for the hotseat client
            startHandler(socket);
        } catch (IOException e) {
            e.printStackTrace();
        }
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
                                clientNick = (String) jsonObject.get("nick");

                                boolean nickAlreadyUsed = false;
                                for (LocalUser user : users) {
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
                                    // TODO unnecessary to inform other users in hotseat ?
                                    // Inform other users
                                    for (LocalUser user : users) {
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
                                    System.out.println("log in"); // for debugging
                                    loginSuccessJson.put("type", "login success");

                                    //TODO unnecessary ?
                                    loginSuccessJson.put("gameNumber", nextGameNumber);

                                    writer.write(loginSuccessJson + System.lineSeparator());
                                    writer.flush();

                                    // Add user to User list
                                    users.add(new LocalUser(clientNick, writer, nextGameNumber));


                                    // TODO Determine when to start the game
                                    int numberOfUsersInNextGame = 0;
                                    for (LocalUser user : users) {
                                        if (user.getGameNumber() == nextGameNumber) {
                                            numberOfUsersInNextGame++;
                                        }
                                    }

                                    // When 4 players are logged in
                                    // Add new game with these players to the game list
                                    // Update the number of the next game
                                    if (numberOfUsersInNextGame > 3) {
                                        List<LocalUser> usersInGame = new ArrayList<>();
                                        for (LocalUser user : users) {
                                            if (user.getGameNumber() == nextGameNumber) {
                                                usersInGame.add(user);
                                            }
                                        }

                                        game = new LocalGame(usersInGame, connection);
                                        nextGameNumber++;
                                    }
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


    protected void sendInvalidSelectionMessage(LocalUser user) {
        try {
            JSONObject sendNextPlayerJson = new JSONObject();
            sendNextPlayerJson.put("type", "tiles not allowed");

            user.getWriter().write(sendNextPlayerJson + System.lineSeparator());
            user.getWriter().flush();
        } catch (IOException | JSONException e) {
            System.out.println(e.getMessage());
        }
    }

    protected void sendInvalidPlacementMessage(LocalUser user) {
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
      List<LocalUser> list, String currentPlayer, int sourceTilePlate, Tile color, int amount) {
        try {
            for (LocalUser user : list) {
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
      List<LocalUser> list, String currentPlayer, Tile color, int amount, int layingRow) {
        try {
            for (LocalUser user : list) {
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
     * - all tiles on plates, the middle or player boards
     * - the current score of each player
     * - whose turn it is next (..?..)
     * */
    public void sendBoardState(List<LocalUser> userList, TileCollection[] tilePlates, GameBoard[] gameBoards) {

        // TODO Pseudocode

        try {
            for(LocalUser user : userList) {
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
     * Gets send after a tile selection was made successfully.
     * Tells the player whose turn it is, which rows he can place selected tile(s) on.
     * */
    public void sendClickableRows(LocalUser user, int[] rows) {
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
    public void sendNextPlayer(List<LocalUser> userList, LocalUser currentUser) {
        try {
            for (LocalUser user : userList) {
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
    public void announceWinner(List<LocalUser> userList, int[] endScores, ArrayList<String> winners) {
        try {
            for (LocalUser user : userList) {
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
            for (LocalUser user : users) {
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
