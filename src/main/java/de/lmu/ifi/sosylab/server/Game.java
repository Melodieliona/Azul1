package de.lmu.ifi.sosylab.server;

import de.lmu.ifi.sosylab.shared.GameBoard;
import de.lmu.ifi.sosylab.shared.Tile;
import de.lmu.ifi.sosylab.shared.TileCollection;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Represents a single game of Azul.
 * Handles the game logic and notifies other players of changes.
 * */
public class Game {

  private final ServerNetworkConnection connection;

  private final List<User> userList;

  private final int gameNumber;
  private int currentPlayer = -1;

  private TileCollection bag;

  // Index 0 is the centerArea
  private TileCollection[] tilePlates;

  // Tiles that were left after point counting at the end of a round.
  private TileCollection trash;

  private GameBoard[] gameBoards;

  private TileCollection currentSelection;

  // Maybe later used to undo a selection
  private int currentSelectionSource = -1;

  private String hasStartMarker;



  /**
   * Initializes all necessary data for a game with a given amount of users.
   * */
  public Game(int gameNumber, List<User> userList, ServerNetworkConnection connection) {
    this.userList = userList;
    this.gameNumber = gameNumber;
    this.connection = connection;


    bag = new TileCollection();
    bag.addTiles(Tile.BLUE, 20);
    bag.addTiles(Tile.YELLOW, 20);
    bag.addTiles(Tile.RED, 20);
    bag.addTiles(Tile.BLACK, 20);
    bag.addTiles(Tile.WHITE, 20);

    // + 1 for the centerArea
    tilePlates = new TileCollection[(userList.size() * 2) + 1 + 1];
    for (int i = 0; i < tilePlates.length - 1; i++) {
      tilePlates[i] = new TileCollection();
    }

    trash = new TileCollection();

    gameBoards = new GameBoard[userList.size()];
    int i = 0;
    for (User user : userList) {
      gameBoards[i] = new GameBoard(user.getName());
      i++;
    }

    currentSelection = new TileCollection();

    // Choose random player to begin with
    Random rand = new Random();
    currentPlayer = rand.nextInt(userList.size()) + 1;

    hasStartMarker = userList.get(currentPlayer).getName();

    fillPlates();

    connection.sendBoardState(tilePlates, gameBoards);

    connection.sendNextPlayer(userList, userList.get(currentPlayer));
    int[] clickableRows = {1, 2, 3, 4, 5};
    connection.sendClickableRows(userList.get(currentPlayer), clickableRows);
  }



  /**
   * Fill plates with tiles from the bag.
   * */
  private void fillPlates() {
    for (int i = 0; i < tilePlates.length; ++i) {
      tilePlates[i] = bag.drawTiles(4);
      // Check if all plates are full.
      // If not, refill bag with the trash and fill up plates with tiles from the bag.
      if (tilePlates[i].size() < 4) {
        if (!trash.isEmpty()) {
          bag.addAll(trash);
          trash.clear();
          tilePlates[i].addAll(bag.drawTiles(4 - tilePlates[i].size()));
        } else {
          break;
        }
      }
    }
    tilePlates[0].add(Tile.STARTING_MARKER);
  }

  /**
   * Checks a requested tile selection for validity and if valid changes model accordingly.
   * Remembers who picked the start marker.
   * */
  protected void handleTileSelection(String playerName, int source, Tile color, int amount) {
    if (!currentSelection.isEmpty()) {
      sendInvalidSelection();
      return;
    }

    if (Collections.frequency(tilePlates[source], color) == amount) {

      // Add starting marker to selection if it's the first pick out of the middle.
      if(source == 0 && tilePlates[0].contains(Tile.STARTING_MARKER)) {
        currentSelection.addAll(tilePlates[source].removeTilesOfColor(Tile.STARTING_MARKER));
        hasStartMarker = playerName;
      }

      currentSelection.addAllTiles(tilePlates[source].removeTilesOfColor(color));
      currentSelectionSource = source;

      sendSuccessfulSelection(source, color, amount);
    } else {
      sendInvalidSelection();
    }
  }

  /**
   * Checks a requested tile placement for validity and if valid changes model accordingly.
   * */
  protected void handleTilePlacement(String playerName, int targetRow, Tile color, int amount) {
    if (currentSelection.isEmpty() || !(Collections.frequency(currentSelection, color) == amount)) {
      sendInvalidPlacement();
      return;
    }

    GameBoard gameBoard = null;
    for (GameBoard board : gameBoards) {
      if (board.getPlayerName().equals(playerName)) {
        gameBoard = board;
        break;
      }
    }

    if (gameBoard.getLayingRow(targetRow).canAddTilesToLayingRow(color)) {
      TileCollection placedTiles =
        gameBoard.getLayingRow(targetRow).layTilesOnRow(currentSelection);

      // Puts tiles in the trash, that don't fit on the floor line
      TileCollection leftOverTiles = currentSelection;
      if (placedTiles.size() < amount) {
        leftOverTiles.removeAll(placedTiles);
        trash.addAll(gameBoard.addToFloorLine(leftOverTiles));
      }

      currentSelection.clear();
      currentSelectionSource = -1;

      //TODO Wenn currentSelectionSource != 0, nicht ausgewählte Fliesen der Platte in die Mitte schieben

      sendSuccessfulPlacement(currentSelection, targetRow);
      sendFloorLinePlacement(leftOverTiles);

      // If at lease one tile is left on plates or the middle, let the next player make a move.
      boolean everythingEmpty = true;
      for(int i = 0; i < tilePlates.length; i++) {
        if(!tilePlates[i].isEmpty()) {
          everythingEmpty = false;
          setAndSendNextPlayer();
          break;
        }
      }

      if(everythingEmpty) {
        endRound();
      }

    } else {
      sendInvalidPlacement();
    }
  }

  /**
   * Ends a round:
   * Calculates the score of every player,
   * starts a new round, fills all plates and the center,
   * notifies the next player (who had the start marker).
   * */
  private void endRound() {

    // Laying tiles on wall, clearing layingRows accordingly and put left over tiles in the trash
    for (GameBoard gameBoard : gameBoards) {
      for(int row = 1; row <= 5; row++) {
        if (gameBoard.getLayingRow(row).isRowFull()) {
          gameBoard.layWallTile(row , gameBoard.getLayingRow(row).getColor());
          trash.addAll(gameBoard.getLayingRow(row).clearRow());

          // TODO Zusätzliche Punkte nach jedem gelegten Stein berechnen und im Gameboard addieren
          // TODO Minuspunkte abziehen


        }
      }
    }

    // Neuen Spielstand an alle Spieler schicken
    connection.sendBoardState(tilePlates, gameBoards);

    //TODO Alle Floorlines clearen, Startmarker in die Mitte und hasStartmarker = "";


    if(!(bag.isEmpty() && trash.isEmpty())) {
      startNewRound();
    } else {

      // TODO Sonderpunkte berechnen und in Gameboards addieren
      // TODO Sieger verkünden

    }
  }

  private void startNewRound() {

    fillPlates();

    // Nächsten Spieler anhand Startmarker ermitteln, setzen und benachrichtigen.
    for(User user : userList) {
      if(user.getName().equals(hasStartMarker)) {
        currentPlayer = userList.indexOf(user) + 1;
        sendNextPlayer();
        break;
      }
    }

    connection.sendBoardState(tilePlates, gameBoards);
  }




  /**
   * Sends an Error message to a user if the made selection was invalid.
   * */
  private void sendInvalidSelection() {
    connection.sendInvalidSelectionMessage();
  }

  /**
   * Sends an Error message to a user if the made placement was invalid.
   * */
  private void sendInvalidPlacement() {
    connection.sendInvalidPlacementMessage();
  }

  /**
   * Informs all players of a successfully made tile selection.
   * */
  private void sendSuccessfulSelection(int tilePlate, Tile color, int amount) {
    String currentPlayer = userList.get(this.currentPlayer).getName();

    connection.sendTileSelection(userList, currentPlayer, tilePlate, color, amount);
  }

  /**
   * Informs all players of a successfully made tile placement.
   * */
  private void sendSuccessfulPlacement(TileCollection tileSelection, int layingRow) {
    String currentPlayer = userList.get(this.currentPlayer).getName();
    Tile color = tileSelection.get(0);
    int amount = tileSelection.size();

    connection.sendTilePlacement(userList, currentPlayer, color, amount, layingRow);
  }

  private void sendFloorLinePlacement(TileCollection leftOverTiles) {
    User currentPlayer = userList.get(this.currentPlayer);
    connection.sendFloorLineUpdate(userList, currentPlayer ,leftOverTiles);
  }


  /**
   * Sets the player whose turn it is to make a move next.
   * Tells all other players whose turn it is next.
   * Sends the next player the clickable rows.
   */
  private void setAndSendNextPlayer() {
    if (currentPlayer == userList.size()) {
      currentPlayer = 0;
    } else {
      currentPlayer++;
    }
    connection.sendNextPlayer(userList, userList.get(currentPlayer));

    // TODO Calculate clickable rows
    int[] clickableRows = {1, 2, 3, 4, 5};


    connection.sendClickableRows(userList.get(currentPlayer), clickableRows);
  }

  /**
   * Tells all other players whose turn it is next, but does not set the currentplayer value.
   * It needs to be set prior to this.
   * Sends the next player the clickable rows.
   */
  private void sendNextPlayer() {
    connection.sendNextPlayer(userList, userList.get(currentPlayer));

    // TODO Calculate clickable rows
    int[] clickableRows = {1, 2, 3, 4, 5};

    connection.sendClickableRows(userList.get(currentPlayer), clickableRows);
  }



  /**
   * Returns the index number of this game.
   * */
  public int getGameNumber() {
    return gameNumber;
  }

}
