package de.lmu.ifi.sosylab.client.model;

import static java.util.Objects.requireNonNull;

import de.lmu.ifi.sosylab.client.model.events.GameEndedEvent;
import de.lmu.ifi.sosylab.client.model.events.GameEvents;
import de.lmu.ifi.sosylab.client.model.events.LoggedInEvent;
import de.lmu.ifi.sosylab.client.model.events.LoginFailedEvent;
import de.lmu.ifi.sosylab.client.model.events.MiddleTilesUpdateEvent;
import de.lmu.ifi.sosylab.client.model.events.OtherPlayerPlacedTilesEvent;
import de.lmu.ifi.sosylab.client.model.events.OtherPlayerSelectedTilesEvent;
import de.lmu.ifi.sosylab.client.model.events.PointsUpdatedEvent;
import de.lmu.ifi.sosylab.client.model.events.TilePlacementFailedEvent;
import de.lmu.ifi.sosylab.client.model.events.TileSelectionFailedEvent;
import de.lmu.ifi.sosylab.client.model.events.TilesAddedEvent;
import de.lmu.ifi.sosylab.client.model.events.TilesSelectedEvent;
import de.lmu.ifi.sosylab.client.model.events.UserJoinedEvent;
import de.lmu.ifi.sosylab.client.model.events.UserLeftEvent;
import de.lmu.ifi.sosylab.client.model.localserver.LocalGameServer;
import de.lmu.ifi.sosylab.client.model.localserver.LocalServerConnection;
import de.lmu.ifi.sosylab.shared.Tile;
import de.lmu.ifi.sosylab.shared.TileCollection;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.IOException;

/**
 * TODO Javadoc
 */
public class GameModel {

    private String gameMode;

    private final PropertyChangeSupport support;

    private GameClientNetworkConnection connection;

    private int numberOfPlayers = 0;

    private Player[] players;

    private TileCollection[] tilePlates;

    private boolean isLoggedin = false;

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
        } else {
            gameMode = "Hot seat";

            // Start local server
            LocalGameServer.startUpLocalServer();

            connection = new GameClientNetworkConnection(this);
            setConnection(connection);
            connection.start(9090);
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

    /**
     * TODO Javadoc
     */
    public void logInMultiplayer(String name) {
        //TODO: testing
        //loggedIn();

        players = new Player[1];
        players[0] = new Player(name);
        connection.sendLogin(name);

    }

    /**
     * TODO Javadoc
     */
    public void logInHotSeat(String[] playersName) {
        //TODO: testing
        //loggedIn();

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
     * Notifies the subscribed view that another player placed specific tiles.
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
     * Notifies the subscribed view that another player placed specific tiles.
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

    /**
     * Notifies the subscribed view that the tiles in the middle and on the factory plates were
     * updated by the server because the game has just started.
     */
    public void fillTiles(String[] colors, String[] amounts) {//array of tilecollection as parameters
        tilePlates = new TileCollection[colors.length];
        for (int n = 0; n < tilePlates.length; n++) {
            String hcolors = colors[n];
            String hamounts = amounts[n];
            String[] colorsCurrentPlate = hcolors.trim().split("\\s+");
            String[] amountsCurrentPlate = hamounts.trim().split("\\s+");
            int i = amountsCurrentPlate.length;
            for (int j = 0; j < i; j++) {
                int amountFirstColor = Integer.parseInt(amountsCurrentPlate[i]);
                for (int m = 0; m < amountFirstColor; m++) {
                    tilePlates[n].add(Tile.valueOf(colorsCurrentPlate[j]));
                }
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
        notifyListeners(new UserJoinedEvent(name));
    }

    /**
     * Notifies the subscribed view that a player left the game.
     */
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

    public TileCollection[] getTilePlates() {
        TileCollection[] copyofTilePlates = tilePlates.clone();
        return copyofTilePlates;
    }
}
