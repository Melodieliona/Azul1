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

  private int numberOfPlayers = 0;

  private ArrayList<Player> players = new ArrayList<>();

  private TileCollection[] tilePlates;

  private boolean isLoggedin = false;

  private String currentPlayer;

  private int[] validRows;

  private int[] validPlates;

  private TileCollection selectedTiles = new TileCollection();

  private String nickname;


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
    return numberOfPlayers;
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
    numberOfPlayers++;
    players.add(new Player(name));
    connection.sendLogin(name);
    setNickname(name);
  }

  /**
   * TODO Javadoc
   */
  public void logInHotSeat(String[] playersName) {
    //testing
    for (int i = 0; i < playersName.length; i++) {
      System.out.println(playersName[i]);
    }

    numberOfPlayers = playersName.length;
    connection.sendPlayers(numberOfPlayers);
    for (int i = 0; i < numberOfPlayers; i++) {
      players.add(new Player(playersName[i]));
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
    if (gameMode.equals("Hot Seat")) {
      setNickname(nickname);
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
    selectedTiles.removeAllTiles();
    int numberOfTiles = tilePlates[source].getAmountTilesOfColor(Tile.getTile(color));
    selectedTiles.addTiles(Tile.getTile(color), numberOfTiles);
    tilePlates[source].removeTilesOfColor(Tile.getTile(color));
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
   * @param players array of players in game
   * @param points  array of all players points
   * @param winner  name of the winner
   */
  public void gameEnded(String[] players, int[] points, String winner) {
    notifyListeners(new GameEndedEvent(players, points, winner));
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

  /**
   * Notifies the subscribed view that the points of each player were just updated.
   *
   * @param names  name of all players
   * @param points all players points
   */
  public void pointsUpdate(String[] names, int[] points) {
    notifyListeners(new PointsUpdatedEvent(names, points));
  }

  public void requestGameRestart() {
    connection.sendGameRestartRequest();
  }

  public void requestGameCancel() {
    connection.sendGameCancelRequest();
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
    for (String nickname :
        nicknames) {
      players.add(new Player(nickname));
    }
    numberOfPlayers += players.size();
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
    numberOfPlayers++;
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
  public void updateBoard(String[] rows, String[] columns, String nick, String points, String[] colors) {
    int numberOfTiles = rows.length;
    for (Player player :
        players) {
      if (player.getPlayerName().equals(nick)) {
        GameBoard board = player.getBoard();
        board.setCurrentScore(Integer.parseInt(points));
        for (int i = 0; i < numberOfTiles; i++) {
          int line = Integer.parseInt(rows[i]);
          Tile tile = Tile.getTile(colors[i]);
          board.layWallTile(line, tile);
          board.getLayingRow(line).clearRow();
        }
      }
    }
    notifyListeners(new BoardUpdatedEvent());
  }

  public void updateFloorLine(String nick, String[] colors) {

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
    return copyofTilePlates;
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
    return  copyOfPlayers;
  }

  public TileCollection getSelectedTiles() {
    TileCollection copyOfSelectedTiles = (TileCollection) selectedTiles.clone();
    return copyOfSelectedTiles;
  }

}
