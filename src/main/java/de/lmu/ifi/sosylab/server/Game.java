package de.lmu.ifi.sosylab.server;

import de.lmu.ifi.sosylab.shared.GameBoard;
import de.lmu.ifi.sosylab.shared.Tile;
import de.lmu.ifi.sosylab.shared.TileCollection;
import java.util.Collections;
import java.util.List;
import java.util.Random;

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

  private int currentSelectionSource = -1;



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
    currentPlayer = rand.nextInt(userList.size());

    fillPlates();

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
   * */
  protected void handleTileSelection(String playerName, int source, Tile color, int amount) {
    if (!currentSelection.isEmpty()) {
      sendInvalidSelection();
      return;
    }

    int amountOfContainedTiles = Collections.frequency(tilePlates[source], color);
    if (amountOfContainedTiles == amount) {
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

      gameBoard.getLayingRow(targetRow).layTilesOnRow(currentSelection);

      currentSelection.clear();
      currentSelectionSource = -1;

      sendSuccessfulPlacement(currentSelection, targetRow);
    } else {
      sendInvalidPlacement();
    }
  }


  private void sendInvalidSelection() {
    connection.sendInvalidSelectionMessage();
  }

  private void sendInvalidPlacement() {
    connection.sendInvalidPlacementMessage();
  }


  private void sendSuccessfulSelection(int tilePlate, Tile color, int amount) {
    String currentPlayer = userList.get(this.currentPlayer).getName();

    connection.sendTileSelection(userList, currentPlayer, tilePlate, color, amount);
  }

  private void sendSuccessfulPlacement(TileCollection tileSelection, int layingRow) {
    String currentPlayer = userList.get(this.currentPlayer).getName();
    Tile color = tileSelection.get(0);
    int amount = tileSelection.size();

    connection.sendTilePlacement(userList, currentPlayer, color, amount, layingRow);
    nextPlayer();
  }



  /**
   * Sets the player whose turn it is to make a move next.
   * Tells all other players whose turn it is next.
   */
  private void nextPlayer() {
    if (currentPlayer == userList.size()) {
      currentPlayer = 0;
    } else {
      currentPlayer++;
    }
    connection.sendNextPlayer(userList, userList.get(currentPlayer));
  }

  /**
   * Returns the index number of this game.
   * */
  public int getGameNumber() {
    return gameNumber;
  }

}
