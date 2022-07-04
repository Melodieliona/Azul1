package de.lmu.ifi.sosylab.client.model;

import static java.util.Objects.requireNonNull;

import de.lmu.ifi.sosylab.client.model.events.GameEndedEvent;
import de.lmu.ifi.sosylab.client.model.events.GameEvents;
import de.lmu.ifi.sosylab.client.model.events.LoggedInEvent;
import de.lmu.ifi.sosylab.client.model.events.LoginFailedEvent;
import de.lmu.ifi.sosylab.client.model.events.MiddleTilesUpdateEvent;
import de.lmu.ifi.sosylab.client.model.events.OtherPlayerPlacedTilesEvent;
import de.lmu.ifi.sosylab.client.model.events.OtherPlayerSelectedTilesEvent;
import de.lmu.ifi.sosylab.client.model.events.TilePlacementFailedEvent;
import de.lmu.ifi.sosylab.client.model.events.TileSelectionFailedEvent;
import de.lmu.ifi.sosylab.client.model.events.TilesAddedEvent;
import de.lmu.ifi.sosylab.client.model.events.TilesSelectedEvent;
import de.lmu.ifi.sosylab.client.model.events.UserJoinedEvent;
import de.lmu.ifi.sosylab.client.model.events.UserLeftEvent;
import de.lmu.ifi.sosylab.client.model.localserver.LocalServerConnection;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.IOException;


public class GameModel {

  private static final int MAX_LENGTH = 100;

  private String gameMode;

  private final PropertyChangeSupport support;

  private GameClientNetworkConnection connection;
  private LocalServerConnection localServer;

  private int numberOfPlayers = 0;

  private Player[] players;

  private boolean isLoggedin = false;

  public GameModel() {
    support = new PropertyChangeSupport(this);
  }

  public void setGameMode(String gMode) throws IOException {
    if (gMode.equals("Multiplayer")) {
      gameMode = "Multiplayer";
      connection = new GameClientNetworkConnection(this);
      setConnection(connection);
      connection.start(8080);
    } else {
     /* gameMode = "Hot seat";
      localServer = new LocalServerConnection();
      localServer.start();

      connection = new GameClientNetworkConnection(this);
      setConnection(connection);
      connection.start(9090);*/
    }
  }

  /**
   * Add a network connector to this model.
   *
   * @param connection The network connection to be added.
   */
  public void setConnection(GameClientNetworkConnection connection) {
    this.connection = connection;
  }

  public void logInMultiplayer(String name) {
    //TODO: testing
    loggedIn();

    players = new Player[1];
    players[0] = new Player(name);
    connection.sendLogin(name);

  }

  public void logInHotSeat(String[] playersName) {
    //TODO: testing
    loggedIn();

    numberOfPlayers = playersName.length;
    gameMode = "Hot Seat";
    players = new Player[numberOfPlayers];
    for (int i = 0; i < numberOfPlayers; i++) {
      players[i] = new Player(playersName[i]);
      connection.sendLogin(playersName[i]);
    }
  }


  /**
   * Sends a request to the server to select tiles.
   *
   * @param color         of tile
   * @param numberOfTiles that were selected
   * @param source        source of selected tiles (factory plates)
   * @param name          name of player
   */
  public void selectTilesRequest(int source, String color, int numberOfTiles, String name) {
    connection.sendTileSelection(source, color, numberOfTiles);
  }

  /**
   * Sends a request to the server to place tiles.
   *
   * @param color         of tile
   * @param numberOfTiles that were selected
   * @param line          desired row/line to place tiles
   */
  public void placeTilesRequest(int line, int color, int numberOfTiles) {
    connection.sendTilePlacement(line, color, numberOfTiles);
  }

  public void selectTiles(int source, String color, int numberOfTiles) {
    notifyListeners(new TilesSelectedEvent(source, color, numberOfTiles));
  }


  /**
   * Places the selected tiles into the selected pattern line.
   *
   * @param color                 of tile
   * @param numberOfSelectedTiles that were selected
   * @param line                  selected to place tiles
   */
  public void placeTiles(String color, int numberOfSelectedTiles, int line, String playersName) {
    int minuspoints = 0;
    if (gameMode.equals("Multiplayer")) {
      minuspoints = players[0].placeTiles(line, color, numberOfSelectedTiles);
    } else {
      for (Player player : players) {
        if (player.getPlayerName().equals(playersName)) {
          minuspoints = player.placeTiles(line, color, numberOfSelectedTiles);
        }
      }
    }
    notifyListeners(new TilesAddedEvent(color, line, numberOfSelectedTiles, minuspoints));
  }

  public void tileSelectionFailed(){
    notifyListeners(new TileSelectionFailedEvent());
  }
  public void tilePlacementFailed(){
    notifyListeners(new TilePlacementFailedEvent());
  }
  public void gameEnded(String[] players, int[] points, String winner){
    notifyListeners(new GameEndedEvent(players, points,winner));
  }

  /**
   * Notifies the subscribed view that another player placed specific tiles
   *
   * @param color                 type of tile
   * @param numberOfSelectedTiles number of tiles
   */
  public void otherPlayerSelectedTiles(String color, int numberOfSelectedTiles, String playerName,
      int source) {
    notifyListeners(
        new OtherPlayerSelectedTilesEvent(color, numberOfSelectedTiles, playerName, source));
  }


  /**
   * Notifies the subscribed view that another player placed specific tiles
   *
   * @param color         type of tile
   * @param line          which line the tiles were placed
   * @param numberOfTiles number of tiles
   * @param minusPoints   number of minus-points
   */
  public void otherPlayerPlacedTiles(String name, String color, int line, int numberOfTiles,
      int minusPoints) {
    notifyListeners(new OtherPlayerPlacedTilesEvent(name, color, numberOfTiles, line, minusPoints));
  }


  public void middleTilesUpdate() { //array of tilecollection as parameters
    notifyListeners(new MiddleTilesUpdateEvent());
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


  public void loggedIn() {
    notifyListeners(new LoggedInEvent());
    isLoggedin = true;
  }

  public void loginFailed() {
    notifyListeners(new LoginFailedEvent());
  }

  public void userJoined(String name) {
    notifyListeners(new UserJoinedEvent(name));
  }

  public void userLeft(String name) {
    notifyListeners(new UserLeftEvent(name));
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
}
