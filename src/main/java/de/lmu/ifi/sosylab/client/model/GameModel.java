package de.lmu.ifi.sosylab.client.model;

import static java.util.Objects.requireNonNull;

import de.lmu.ifi.sosylab.client.model.events.GameEvents;
import de.lmu.ifi.sosylab.client.model.events.LoggedInEvent;
import de.lmu.ifi.sosylab.client.model.events.LoginFailedEvent;
import de.lmu.ifi.sosylab.client.model.events.TilesAddedEvent;
import de.lmu.ifi.sosylab.client.model.events.UserJoinedEvent;
import de.lmu.ifi.sosylab.client.model.events.UserLeftEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;


public class GameModel {

  private static final int MAX_LENGTH = 100;

  private String gameMode;

  private final PropertyChangeSupport support;

  private GameClientNetworkConnection connection;

  private final int maxNumberOfPlayers=4;
  private int numberOfPlayers=0;

  private Player[] players;

  public GameModel() {
    support = new PropertyChangeSupport(this);
  }

  public void setGameMode(String gMode){
    if(gMode.equals("Multiplayer")){
      gameMode = "Multiplayer";
      GameClientNetworkConnection connection = new GameClientNetworkConnection(this);
      setConnection(connection);
      connection.start();
    }else {
      gameMode = "Hot seat";
      //Hot seat
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

  public void logInMultiplayer(String name){
    players = new Player[1];
    players[0]=new Player(name);
    connection.sendLogin(name);
  }
  public void logInHotSeat(String[] playersName){
   numberOfPlayers = playersName.length;
   gameMode = "Hot Seat";
   players = new Player[numberOfPlayers];
   for(int i=0;i<numberOfPlayers;i++){
     players[i]=new Player(playersName[i]);
     notifyListeners(new UserJoinedEvent(playersName[i]));
   }
  }

  /**
   * Places the selected tiles into the selected pattern line.
   *
   * @param color                 of tile
   * @param numberOfSelectedTiles that were selected
   * @param line                  selected to place tiles
   */
  public void placeTiles(String color, int numberOfSelectedTiles, int line) {
    int minuspoints=0;
    //Hotseat mode missing
    if(gameMode.equals("Multiplayer")){
       minuspoints = players[0].placeTiles(line,color,numberOfSelectedTiles);
       connection.sendMove();
       //param: playername, tile color, number of tiles, which line, minuspoints
    }
       notifyListeners(new TilesAddedEvent(color,line,numberOfSelectedTiles,minuspoints));
  }

  /**
   * Notifies the subscribed view that another player placed specific tiles
   *
   * @param color type of tile
   * @param numberOfSelectedTiles number of tiles
   */
  public void otherPlayerSelectedTiles(String color, int numberOfSelectedTiles){
    //notifyListeners(new otherPlayerSelectedTilesEvent(color,numberOfSelectedTiles))
  }

  /**
   * Notifies the subscribed view that another player placed specific tiles
   *
   * @param color type of tile
   * @param line which line the tiles were placed
   * @param numberOfTiles number of tiles
   * @param minusPoints number of minus-points
   */
  public void otherPlayerPlacedTiles(String color,int line, int numberOfTiles, int minusPoints){
      //notifyListeners(new otherPlayerPlacedTilesEvent(color,numberOfSelectedTiles,line,minusPoints))
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


  public void loggedIn(){
   notifyListeners(new LoggedInEvent());
  }
  public void loginFailed(){
    notifyListeners(new LoginFailedEvent());
  }
  public void userJoined(String name){
    notifyListeners(new UserJoinedEvent(name));
  }
  public void userLeft(String name){
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
   * Remove a listener from the model. It will then no longer get notified about any events
   * fired by the model.
   *
   * @param listener the view that is to be unsubscribed from the model.
   */
  public void removePropertyChangeListener(PropertyChangeListener listener) {
    requireNonNull(listener);
    support.removePropertyChangeListener(listener);
  }

  public void dispose() {
  }
}
