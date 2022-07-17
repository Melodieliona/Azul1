package de.lmu.ifi.sosylab.server;

import de.lmu.ifi.sosylab.shared.GameBoard;
import de.lmu.ifi.sosylab.shared.Tile;
import de.lmu.ifi.sosylab.shared.TileCollection;
import java.util.ArrayList;
import java.util.Arrays;
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
  private int currentPlayer;

  private TileCollection bag;

  // Index 0 is the centerArea
  private TileCollection[] tilePlates;

  // Tiles that were left after point counting at the end of a round.
  private TileCollection trash;

  private GameBoard[] gameBoards;

  private TileCollection currentSelection;

  // Maybe later used to undo a selection
  private int currentSelectionSource = -1;

  private String startsAtNextRound;

  private static final Random RANDOM = new Random();



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
    currentPlayer = RANDOM.nextInt(userList.size());

    startsAtNextRound = "";

    fillPlates();
    connection.sendFilledPlates(this.userList, tilePlates);

    connection.sendNextPlayer(userList, userList.get(currentPlayer));
  }



  /**
   * Fill plates with tiles from the bag.
   * Put start marker in the middle.
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
  protected void handleTileSelection(String playerName, int source, Tile color) {

    int amount = tilePlates[source].getAmountTilesOfColor(color);
    if (amount > 0 && !currentSelection.isEmpty()) {

      // Add starting marker to selection if it's the first pick out of the middle.
      if (source == 0 && tilePlates[0].contains(Tile.STARTING_MARKER)) {
        currentSelection.addAll(tilePlates[source].removeTilesOfColor(Tile.STARTING_MARKER));
        startsAtNextRound = playerName;
      }

      currentSelection.addAllTiles(tilePlates[source].removeTilesOfColor(color));
      currentSelectionSource = source;

      sendSuccessfulSelection(source, color, amount);
      connection.sendClickableRows(
          getUser(playerName), getClickableRows(getUser(playerName), color));
    } else {
      sendInvalidSelection(playerName);
    }
  }

  /**
   * Checks a requested tile placement for validity and if valid changes model accordingly.
   * */
  protected void handleTilePlacement(String playerName, int targetRow, Tile color) {
    if (currentSelection.isEmpty()) {
      sendInvalidPlacement(userList.get(currentPlayer).getName());
      return;
    }

    GameBoard gameBoard = getPlayersGameBoard(userList.get(currentPlayer).getName());

    // Check if at least one tile of the given color can be added to the row
    if (gameBoard.getLayingRow(targetRow).canAddTilesToLayingRow(color)) {
      //Attempt to place tiles
      TileCollection placedTiles =
          gameBoard.getLayingRow(targetRow).layTilesOnRow(currentSelection);

      // Try to add left over tiles to the floor line
      // Put tiles in the trash, that don't fit on the floor line
      TileCollection leftOverTiles = new TileCollection();
      leftOverTiles.addAll(currentSelection);
      leftOverTiles.removeAll(placedTiles);
      if (leftOverTiles.size() > 0) {
        TileCollection didNotFitOnFloorLine = gameBoard.addToFloorLine(leftOverTiles);
        trash.addAll(didNotFitOnFloorLine);

        leftOverTiles.removeAll(didNotFitOnFloorLine);
      }

      //Move unselected tiles to the middle
      moveTilesToMiddle();

      sendSuccessfulPlacement(placedTiles, targetRow);
      sendFloorLinePlacement(leftOverTiles);

      currentSelection.clear();
      currentSelectionSource = -1;

      //for debugging
      //connection.sendBoardUpdate(userList ,gameBoards);

      // If at lease one tile is left on plates or the middle, let the next player make a move.
      boolean everythingEmpty = true;
      for (TileCollection tilePlate : tilePlates) {
        if (!tilePlate.isEmpty()) {
          everythingEmpty = false;
          setAndSendNextPlayer();
          break;
        }
      }

      if (everythingEmpty) {
        endRound();
      }

    } else {
      sendInvalidPlacement(userList.get(currentPlayer).getName());
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
    //
    for (GameBoard gameBoard : gameBoards) {
      for (int row = 0; row < 5; row++) {
        if (gameBoard.getLayingRow(row).isRowFull()) {
          Tile color = gameBoard.getLayingRow(row).getColor();

          gameBoard.layWallTile(row, color);
          trash.addAll(gameBoard.getLayingRow(row).clearRow());

          // Add plus-points for the added tile
          gameBoard.updatePlusPoints(row, gameBoard.getLayingRow(row).columnOfColor(color));
        }
      }

      // Clear floor line - delete start marker if present - add cleared tiles to trash
      // clearFloorLine() also resets the floor line minus points
      TileCollection floorLineTiles = gameBoard.clearFloorLine();

      floorLineTiles.remove(Tile.STARTING_MARKER);

      trash.addAll(floorLineTiles);
    }

    // Neuen Spielstand an alle Spieler schicken
    connection.sendBoardUpdate(this.userList, gameBoards);

    if (!hasCompletedWallRow() && !(bag.isEmpty() && trash.isEmpty())) {
      startNewRound();
    } else {

      // Add extra points
      for (GameBoard gameBoard : gameBoards) {
        gameBoard.calculateAndAddExtraPoints();
      }

      // Calculate and gather final scores
      int[] endScores = new int[userList.size()];
      for (User user : userList) {
        endScores[userList.indexOf(user)] = getPlayersGameBoard(user.getName()).getCurrentScore();
      }

      // Calculate winner(s)
      int highestScore = Arrays.stream(endScores).max().getAsInt();
      ArrayList<String> winners = new ArrayList<>();
      for (int i = 0; i < endScores.length; i++) {
        if (endScores[i] == highestScore) {
          winners.add(userList.get(i).getName());
        }
      }

      connection.announceWinner(userList, endScores, winners);
    }
  }

  /**
   * Refills tile plates with tiles from the bag and lets the next player make a move.
   * */
  private void startNewRound() {

    fillPlates();
    connection.sendFilledPlates(userList, tilePlates);

    //Unnecessary ?
    //connection.sendBoardUpdate(this.userList, gameBoards);

    // Nächsten Spieler anhand Startmarker ermitteln, setzen und benachrichtigen.
    for (User user : userList) {
      if (user.getName().equals(startsAtNextRound)) {
        currentPlayer = userList.indexOf(user) + 1;
        startsAtNextRound = "";
        break;
      }
    }
    sendNextPlayer();
  }

  /**
   * Sends an Error message to a user if the made selection was invalid.
   * */
  private void sendInvalidSelection(String user) {
    connection.sendInvalidSelectionMessage(getUser(user));
  }

  /**
   * Sends an Error message to a user if the made placement was invalid.
   * */
  private void sendInvalidPlacement(String user) {
    connection.sendInvalidPlacementMessage(getUser(user));
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
    connection.sendFloorLineUpdate(userList, currentPlayer, leftOverTiles);
  }

  /**
   * Sets the player whose turn it is to make a move next.
   * Tells all other players whose turn it is next.
   */
  private void setAndSendNextPlayer() {
    if (currentPlayer == userList.size()) {
      currentPlayer = 0;
    } else {
      currentPlayer++;
    }
    connection.sendNextPlayer(userList, userList.get(currentPlayer));
  }

  /**
   * Tells all other players whose turn it is next, but does not set the currentplayer value.
   * It needs to be set prior to this.
   */
  private void sendNextPlayer() {
    connection.sendNextPlayer(userList, userList.get(currentPlayer));
  }

  /**
   * Moves tiles, that are left on a plate after a selection was made, to the middle.
   * */
  private void moveTilesToMiddle() {
    if (currentSelectionSource != 0) {
      int amountOfLeftTiles = tilePlates[currentSelectionSource].size();
      for (int i = 0; i < amountOfLeftTiles; i++) {
        tilePlates[0].add(tilePlates[currentSelectionSource].remove(i));
      }
    }
  }

  /**
   * Determines if at least one player has completed a wall row.
   * */
  private boolean hasCompletedWallRow() {
    boolean playerHasFullWallRow = false;
    for (GameBoard gameBoard : gameBoards) {
      Tile[][] tileWall = gameBoard.getTileWall();
      for (int j = 0; j < tileWall.length; j++) {
        int numberOfTilesOnWallRow = 0;
        for (int i = 0; i < tileWall[i].length; i++) {
          if (tileWall[i][j] != null) {
            numberOfTilesOnWallRow++;
          }
        }
        if (numberOfTilesOnWallRow == 5) {
          playerHasFullWallRow = true;
          break;
        }
      }
    }
    return playerHasFullWallRow;
  }

  /**
   * Returns all laying row indices that can be clicked on a player's board for a selected color.
   * */
  private int[] getClickableRows(User user, Tile color) {
    List<Integer> rowList = new ArrayList<>();
    GameBoard gameBoard = getPlayersGameBoard(user.getName());
    for (int i = 1; i < 5; i++) {
      if (gameBoard.getLayingRow(i).canAddTilesToLayingRow(color)) {
        rowList.add(i);
      }
    }

    int[] clickableRows = new int[rowList.size()];
    int i = 0;
    for (int row : rowList) {
      clickableRows[i] = row;
      i++;
    }
    return clickableRows;
  }

  /**
   * Returns the user for a given name.
   * */
  private User getUser(String userAsString) {
    User userAsUser = null;
    for (User user : userList) {
      if (user.getName().equals(userAsString)) {
        userAsUser = user;
      }
    }
    return userAsUser;
  }

  /**
   * Returns a players game board.
   */
  private GameBoard getPlayersGameBoard(String playerName) {
    GameBoard gameBoard = null;
    for (GameBoard board : gameBoards) {
      if (board.getPlayerName().equals(playerName)) {
        gameBoard = board;
        break;
      }
    }
    return gameBoard;
  }


  /**
   * Returns the index number of this game.
   * */
  public int getGameNumber() {
    return gameNumber;
  }

}
