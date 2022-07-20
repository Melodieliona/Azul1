package de.lmu.ifi.sosylab.client.model;

import static java.util.Objects.requireNonNull;

import de.lmu.ifi.sosylab.client.model.events.*;
import de.lmu.ifi.sosylab.client.model.localserver.LocalGameServer;
import de.lmu.ifi.sosylab.shared.GameBoard;
import de.lmu.ifi.sosylab.shared.Tile;
import de.lmu.ifi.sosylab.shared.TileCollection;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.IOException;
import java.util.ArrayList;

/**
 * TODO Javadoc
 */
public class GameModel {

  private String gameMode;

  private final PropertyChangeSupport support;

  private GameClientNetworkConnection connection;

  private ArrayList<Player> players = new ArrayList<>();

  private TileCollection[] tilePlates;

  private boolean isLoggedin = false;

  private String currentPlayer;

  private int[] validRows;

  private int[] validPlates;

  private TileCollection selectedTiles = new TileCollection();

  private String nickname;

  private ArrayList<String> nicksFromWinners = new ArrayList<>();


  public GameModel() {
    support = new PropertyChangeSupport(this);
  }

  /**
   * Sets the game mode according to the value that is passed on by the view.
   *
   * @param gMode name of the player
   */
  public void setGameMode(String gMode) throws IOException {
    if (gMode.equals("Multiplayer")) {
      gameMode = "Multiplayer";
      connection = new GameClientNetworkConnection(this);
      setConnection(connection);
      connection.start(8080);
    } else if (gMode.equals("Hot Seat")) {
      gameMode = "Hot seat";

      // Start local server
      LocalGameServer.startUpLocalServer();

      connection = new GameClientNetworkConnection(this);
      setConnection(connection);
      connection.start(9090);
    }
  }

  public String getGameMode() {
    return gameMode;
  }

  public int getNumberOfPlayers() {
    return players.size() - 1;
  }

  /**
   * Add a network connector to this model.
   *
   * @param connection The network connection to be added.
   */
  public void setConnection(GameClientNetworkConnection connection) {
    this.connection = connection;
  }

  /**
   * TODO Javadoc
   */
  public void logInMultiplayer(String name) {
    connection.sendLogin(name);
    setNickname(name);
  }


  /**
   * TODO Javadoc
   */
  public void logInHotSeat(String[] playersName) {
    connection.sendPlayers(playersName.length);
    for (int i = 0; i < playersName.length; i++) {
      connection.sendLogin(playersName[i]);
    }
  }

  /**
   * Sends a NextPlayerEvent to the subscribed view with the name of the player whose turn it is.
   *
   * @param nickname Name of the player.
   */
  public void nextPlayer(String nickname) {
    currentPlayer = nickname;
    if (gameMode.equals("Hot seat")) {
      this.nickname = nickname;
    }
    notifyListeners(new NextPlayerEvent(nickname));
  }


  /**
   * Sends a request to the server to select tiles.
   *
   * @param color  of tile
   * @param source source of selected tiles (factory plates)
   */
  public void selectTilesRequest(int source, String color) {
    System.out.println("tile request model");
    int numberOfTiles = tilePlates[source].getAmountTilesOfColor(Tile.getTile(color));
    connection.sendTileSelection(source, color, numberOfTiles);
  }

  /**
   * Sends a request to the server to place tiles.
   *
   * @param numberOfTiles of tile
   * @param line          desired row/line to place tiles
   */
  public void placeTilesRequest(int numberOfTiles, int line) {
    connection.sendTilePlacement(numberOfTiles, line);
  }

  /**
   * Selects tiles.
   *
   * @param color  of tile
   * @param source source of selected tiles (factory plates)
   */
  public void selectTiles(int source, String color) {
    System.out.println("tiles selected model");
    selectedTiles.clear();
    int numberOfTiles = tilePlates[source].getAmountTilesOfColor(Tile.getTile(color));
    selectedTiles.addTiles(Tile.getTile(color), numberOfTiles);
    tilePlates[source].removeAll(tilePlates[source].removeTilesOfColor(Tile.getTile(color)));
    if (source != 0) {
      tilePlates[0].addAllTiles(tilePlates[source]);
      tilePlates[source].clear();
      System.out.println("all tiles from plate " + source + " removed");
      System.out.println("size from collection in plate " + source + ": " + tilePlates[source].size());
    } else {
      if (tilePlates[0].contains(Tile.STARTING_MARKER)) {
        selectedTiles.add(Tile.STARTING_MARKER);
        tilePlates[0].removeAll(tilePlates[0].removeTilesOfColor(Tile.STARTING_MARKER));
      }
    }
    notifyListeners(new TilesSelectedEvent(source, color, selectedTiles.size()));
  }


  /**
   * Places the selected tiles into the selected pattern line.
   *
   * @param numOfTiles that were selected
   * @param line       selected to place tiles
   */
  public void placeTiles(int line, int numOfTiles) {
    for (Player player :
        players) {
      if (player.getPlayerName().equals(currentPlayer)) {
        player.placeTiles(line, selectedTiles.getContainedColors().get(0).getColor(), numOfTiles);
      }
    }
    notifyListeners(new TilesAddedEvent(selectedTiles.getContainedColors().get(0).getColor(), line, numOfTiles, 0));
  }

  /**
   * Notifies the subscribed view that the tile selection was not accepted by the server.
   */
  public void tileSelectionFailed() {
    notifyListeners(new TileSelectionFailedEvent());
  }

  /**
   * Notifies the subscribed view that the tile placement was not accepted by the server.
   */
  public void tilePlacementFailed() {
    notifyListeners(new TilePlacementFailedEvent());
  }

  /**
   * Notifies the subscribed view that game has ended.
   *
   * @param usernames array of names players in game
   * @param points    array of all players points
   * @param winners   array of names of the winners
   */
  public void gameEnded(String[] winners, String[] points, String[] usernames) {
    selectedTiles.clear();
    validRows = new int[]{};
    validPlates = new int[]{};

    for (int i = 0; i < points.length; i++) {
      for (Player player :
          players) {
        if (usernames[i].equals(player.getPlayerName())) {
          player.getBoard().setCurrentScore(Integer.parseInt(points[i]));
        }
      }
    }

    for (String winner :
        winners) {
      nicksFromWinners.add(winner);
    }

    notifyListeners(new GameEndedEvent());
  }

  /**
   * Notifies the subscribed view that another player selected specific tiles.
   *
   * @param color type of tile
   */
  public void otherPlayerSelectedTiles(String color, int source) {
    selectedTiles.removeAllTiles();
    int numberOfSelectedTiles = tilePlates[source].getAmountTilesOfColor(Tile.getTile(color));
    selectedTiles.addTiles(Tile.getTile(color), numberOfSelectedTiles);
    tilePlates[source].removeTilesOfColor(Tile.getTile(color));
    if (source != 0) {
      tilePlates[0].addAll(tilePlates[source]);
    } else {
      if (tilePlates[0].contains(Tile.STARTING_MARKER)) selectedTiles.add(Tile.STARTING_MARKER);
    }
    notifyListeners(
        new OtherPlayerSelectedTilesEvent(color, numberOfSelectedTiles, currentPlayer, source));
  }


  /**
   * Notifies the subscribed view that another player placed specific tiles.
   *
   * @param row    which lines the tiles were placed
   * @param amount amount of tiles
   */
  public void otherPlayerPlacedTiles(int row, int amount) {
    String actualColor = selectedTiles.getContainedColors().get(0).getColor();
    for (Player player :
        players) {
      if (player.getPlayerName().equals(currentPlayer)) {
        player.placeTiles(row, selectedTiles.getContainedColors().get(0).getColor(), amount);
      }
    }
    notifyListeners(new OtherPlayerPlacedTilesEvent(currentPlayer, actualColor, amount, row, 0)); // I think the amount of points are always sent with the minus points calculated so I just put 0 in the parameter for minuspoints
  }

  /**
   * Notifies the subscribed view that the tiles in the middle and on the factory plates were
   * updated by the server because the game has just started.
   *
   * @param colors  array, a single index has all colors for a given plate
   * @param amounts array, a single index has all amounts for a given color
   */
  public void fillTiles(String[] colors, String[] amounts) { //array of tilecollection as parameters
    tilePlates = new TileCollection[colors.length];
    for (int n = 0; n < tilePlates.length; n++) {
      tilePlates[n] = new TileCollection();
      String hcolors = colors[n];
      String hamounts = amounts[n];
      String[] colorsCurrentPlate = hcolors.trim().split("\\s+");
      String[] amountsCurrentPlate = hamounts.trim().split("\\s+");
      int i = amountsCurrentPlate.length;
      for (int j = 0; j < i; j++) {
        String currentColor = colorsCurrentPlate[j];
        int amountCurrentColor = Integer.parseInt(amountsCurrentPlate[j]);
        tilePlates[n].addTiles(Tile.getTile(currentColor), amountCurrentColor);
      }
    }

    notifyListeners(new MiddleTilesUpdateEvent());
  }

  public void requestGameRestart() {
    connection.sendGameRestartRequest();
  }

  public void requestGameCancel() {
    String nickname;
    if (gameMode.equals("Hot seat")) {
      nickname = currentPlayer;
    } else {
      nickname = this.nickname;
    }
    connection.sendGameCancelRequest(nickname);
  }

  public void cancelGame() {
    notifyListeners(new GameCanceledEvent());
  }

  public void restartGame() {
    notifyListeners(new GameRestartedEvent());
  }

  /**
   * Notify subscribed listeners that the state of the model has changed. To this end, a specific
   * {@link GameEvents} gets fired such that the attached observers (i.e.,
   * {@link PropertyChangeListener}) can distinguish between what exactly has changed.
   *
   * @param event A concrete implementation of {@link GameEvents}
   */
  private void notifyListeners(GameEvents event) {
    support.firePropertyChange(event.getName(), null, event);
  }

  public void loggedIn(String[] nicknames) {
    if (gameMode.equals("Multiplayer")) {
      System.out.println("Model LoggedIn nickname array size: " + nicknames.length);
      players.add(new Player(this.nickname));
      System.out.println("Player array size is now: " + this.getPlayers().size());
      System.out.println("Model LogginSuccess adding: " + this.nickname);
      for (String nickname :
          nicknames) {
        if (players.contains(nickname) || nickname.trim().isEmpty()) {

        } else {
          players.add(new Player(nickname));
        }

        System.out.println("Model Logged in Event  Nickname of other player that was added to Player Array: " + nickname);
      }
    }
    notifyListeners(new LoggedInEvent());
    isLoggedin = true;
  }

  /**
   * Notifies the subscribed view that the login attempt was not successful.
   */
  public void loginFailed() {
    notifyListeners(new LoginFailedEvent());
  }

  /**
   * Notifies the subscribed view that a new player joined the game.
   */
  public void userJoined(String name) {
    players.add(new Player(name));
    notifyListeners(new UserJoinedEvent(name));
  }

  /**
   * Notifies the subscribed view that a player left the game.
   */
  public void userLeft(String name) {
    notifyListeners(new UserLeftEvent(name));
  }

  /**
   * Notifies the subscribed view that a timer for restart, cancellation or start of the game has been set by the server.
   */
  public void timer() {
    notifyListeners(new TimerEvent());
  }

  public void endTimer() {
    notifyListeners(new TimerEndedEvent());
  }

  /**
   * Notifies the subscribed view that a player has request to restart.
   */
  public void receivedRestartRequest(String nickname) {
    notifyListeners(new GameRestartRequestEvent(nickname));
  }

  /**
   * Notifies the subscribed view that a player has request to cancel.
   */
  public void receivedCancelRequest(String nickname) {
    notifyListeners(new GameCancelRequestEvent(nickname));
  }

  /**
   * Updates the information regarding the board of a given player as well as their score.
   *
   * @param rows   the laying lines that should be cleared as well as the line of the wall were the tile will be palced
   * @param nick   the nickname of the player
   * @param points the current score of the player
   * @param colors the colors of the tiles that will go in the wall
   */
  public void updateBoard(String[] rows, String nick, String points, String[] colors) {
    for (Player player :
        players) {
      if (player.getPlayerName().equals(nick)) {
        player.getBoard().setCurrentScore(Integer.parseInt(points));
        for (String row :
            rows) {
          int line = Integer.parseInt(row);
          for (String color :
              colors) {
            String[] tiles = color.trim().split("\\s+");
            TileCollection actualTiles = new TileCollection();

            for (String tile :
                tiles) {
              if (tile.equals("0") || actualTiles.contains(Tile.getTile(tile))) {
                //do nothing
              } else {
                actualTiles.add(Tile.getTile(tile));
              }

              for (Tile actualTile :
                  actualTiles) {
                player.getBoard().layWallTile(line, actualTile);
              }
            }
          }
          player.getBoard().getLayingRow(line).clearRow();
        }
        player.getBoard().clearFloorLine();
      }
    }
    notifyListeners(new BoardUpdatedEvent());
  }

  public void updateFloorLine(String nick, String[] colors) {
    System.out.println("updating floor line");
    int numberOfTiles = colors.length;
    System.out.println("number of tiles in floor line " + numberOfTiles);
    TileCollection tiles = new TileCollection();
    for (Player player :
        players) {
      System.out.println("looking for player");
      if (player.getPlayerName().equals(nick)) {
        System.out.println("player found");
        for (int i = 0; i < numberOfTiles; i++) {
          System.out.println("adding tiles");
          tiles.addTiles(Tile.getTile(colors[i]), 1);
          System.out.println("tile added to floor line");
        }
        player.getBoard().addToFloorLine(tiles);
        System.out.println("now all tiles actually added to floorline");
        System.out.println("Size of the floor line: " + player.getBoard().getFloorLine().size());
      }
    }
    notifyListeners(new FloorLineEvent());
  }

  /**
   * Add a {@link PropertyChangeListener} to the model for getting notified about any changes that
   * are published by this model.
   *
   * @param listener the view that subscribes itself to the model.
   */
  public void addPropertyChangeListener(PropertyChangeListener listener) {
    requireNonNull(listener);
    support.addPropertyChangeListener(listener);
  }

  /**
   * Remove a listener from the model. It will then no longer get notified about any events fired by
   * the model.
   *
   * @param listener the view that is to be unsubscribed from the model.
   */
  public void removePropertyChangeListener(PropertyChangeListener listener) {
    requireNonNull(listener);
    support.removePropertyChangeListener(listener);
  }

  public boolean isLoggedIn() {
    return isLoggedin;
  }

  private synchronized GameClientNetworkConnection getConnection() {
    return connection;
  }

  public void dispose() {
    getConnection().stop();
  }

  public TileCollection[] getTilePlates() {
    TileCollection[] copyofTilePlates = tilePlates.clone();
    return tilePlates;
  }

  public int[] getValidRows() {
    int[] copyOfValidRows = validRows.clone();
    return copyOfValidRows;
  }

  public void setValidRows(int numberOfValidRows, String[] rows) {
    validRows = new int[numberOfValidRows];
    for (int i = 0; i < numberOfValidRows; i++) {
      validRows[i] = Integer.parseInt(rows[i]);
    }
  }

  public int[] getValidPlates() {
    int[] copyOfValidPlates = validPlates.clone();
    return copyOfValidPlates;
  }

  public void setValidPlates(String[] plates) {
    validPlates = new int[plates.length];
    for (int i = 0; i < validPlates.length; i++) {
      validPlates[i] = Integer.parseInt(plates[i]);
    }
  }

  public String getNickname() {
    return nickname;
  }

  public void setNickname(String nickname) {
    this.nickname = nickname;
  }

  public String getCurrentPlayer() {
    return currentPlayer;
  }

  public int[] getScore() {
    int[] points;
    return null;
  }

  public ArrayList<Player> getPlayers() {
    ArrayList<Player> copyOfPlayers = new ArrayList<>(players);
    return copyOfPlayers;
  }

  public TileCollection getSelectedTiles() {
    TileCollection copyOfSelectedTiles = (TileCollection) selectedTiles.clone();
    return copyOfSelectedTiles;
  }

  public void clear() {
    players.clear();
    nicksFromWinners.clear();
  }

}
