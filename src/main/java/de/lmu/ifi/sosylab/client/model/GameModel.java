package de.lmu.ifi.sosylab.client.model;

import static java.util.Objects.requireNonNull;

import de.lmu.ifi.sosylab.client.model.events.*;
import de.lmu.ifi.sosylab.client.model.localserver.LocalGameServer;
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

  private int[] validRows = new int[]{};

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
   * @param mode name of the player
   */
  public void setGameMode(String mode) throws IOException {
    if (mode.equals("Multiplayer")) {
      gameMode = "Multiplayer";
      connection = new GameClientNetworkConnection(this);
      setConnection(connection);
      connection.start(8080);
    } else if (mode.equals("Hot Seat")) {
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
    for (String s : playersName) {
      connection.sendLogin(s);
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
    notifyListeners(new TilesAddedEvent(selectedTiles.getContainedColors().get(0).getColor(), line,
        numOfTiles, 0));
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
      for (Player player : players) {
        if (usernames[i].equals(player.getPlayerName())) {
          player.setScore(Integer.parseInt(points[i]));
        }
      }
    }

    for (String winner : winners) {
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
      if (tilePlates[0].contains(Tile.STARTING_MARKER)) {
        selectedTiles.add(Tile.STARTING_MARKER);
      }
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
    notifyListeners(new OtherPlayerPlacedTilesEvent(currentPlayer, actualColor, amount, row, 0));
    // I think the amount of points are always sent with the minus points calculated so I just put
    // 0 in the parameter for minuspoints
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

  /**
   * TODO Add JavaDoc
   * */
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

  /**
   * TODO Add JavaDoc
   * */
  public void loggedIn(String[] nicknames) {
    if (gameMode.equals("Multiplayer")) {
      players.add(new Player(this.nickname));
      for (String nickname : nicknames) {
        if (players.contains(new Player(nickname)) || nickname.trim().isEmpty()) {
          //TODO is this right / something missing? *just asking*
          //TODO Also, players is an ArrayList<Player> and only contains Player objects, no Strings
        } else {
          players.add(new Player(nickname));
        }
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
   * Notifies the subscribed view that a timer for restart, cancellation or start of the game has
   * been set by the server.
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
   * @param rows   the laying lines that should be cleared as well as the line of the wall were the
   *               tile will be palced
   * @param nick   the nickname of the player
   * @param points the current score of the player
   * @param colors the colors of the tiles that will go in the wall
   */
  public void updateBoard(String[] rows, String nick, String points, String[] colors) {
    for (Player player : players) {
      if (player.getPlayerName().equals(nick)) {
        //Update Score
        player.setScore(Integer.parseInt(points));

        //Calculate new wall tiles
        String[][] tiles = new String[5][5];
        int rowIterator = 0;
        for (String color : colors) {
          String[] rowTiles = color.trim().split("\\s+");
          for (int j = 0; j < 5; j++) {
            tiles[j][rowIterator] = rowTiles[j];
          }
          rowIterator++;
        }

        //Set wall tiles
        for (int i = 0; i < 5; i++) {
          for (int j = 0; j < 5; j++) {
            if (!tiles[j][i].equals("0")) {
              player.getBoard().layWallTile(i, Tile.getTile(tiles[j][i]));
            }
          }
        }

        //Clear emptied rows
        for (String row : rows) {
          player.getBoard().getLayingRow(Integer.parseInt(row)).clearRow();
        }

        //Clear floorline
        player.getBoard().getFloorLine().clear();
      }
    }
    notifyListeners(new BoardUpdatedEvent());
  }

  /**
   * TODO Add JavaDoc
   * */
  public void updateFloorLine(String nick, String[] colors) {
    int numberOfTiles = colors.length;
    TileCollection tiles = new TileCollection();
    for (Player player : players) {
      if (player.getPlayerName().equals(nick)) {
        for (String color : colors) {
          tiles.addTiles(Tile.getTile(color), 1);
        }
        player.getBoard().addToFloorLine(tiles);
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

  /**
   * TODO Add JavaDoc
   * */
  public void dispose() {
    if (!(connection == null)) {
      connection.stop();
    }
  }

  public TileCollection[] getTilePlates() {
    TileCollection[] copyofTilePlates = tilePlates.clone();
    return tilePlates;
  }

  public int[] getValidRows() {
    return validRows.clone();
  }

  /**
   * TODO Add JavaDoc
   * */
  public void setValidRows(int numberOfValidRows, String[] rows) {
    validRows = new int[numberOfValidRows];
    for (int i = 0; i < numberOfValidRows; i++) {
      validRows[i] = Integer.parseInt(rows[i]);
    }
  }

  public int[] getValidPlates() {
    return validPlates.clone();
  }

  /**
   * TODO Add JavaDoc
   * */
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
    return new ArrayList<>(players);
  }

  public TileCollection getSelectedTiles() {
    return (TileCollection) selectedTiles.clone();
  }

  public ArrayList<String> getNicksFromWinners() {
    return nicksFromWinners;
  }

  public void clear() {
    players.clear();
    nicksFromWinners.clear();
  }

}
