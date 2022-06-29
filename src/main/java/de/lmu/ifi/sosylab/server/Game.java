package de.lmu.ifi.sosylab.server;

import de.lmu.ifi.sosylab.server.ServerNetworkConnection;
import de.lmu.ifi.sosylab.server.User;
import de.lmu.ifi.sosylab.shared.GameBoard;
import de.lmu.ifi.sosylab.shared.LayingRow;
import de.lmu.ifi.sosylab.shared.Tile;
import de.lmu.ifi.sosylab.shared.TileCollection;
import java.util.List;
import java.util.Random;

/**
 *
 * */
public class Game {

  private final ServerNetworkConnection connection;

  private List<User> userList;

  private int currentPlayer = -1;

  private int gameNumber;

  private TileCollection bag;

  private TileCollection[] tilePlates;

  private TileCollection centerArea;

  // Tiles that were left after point counting at the end of a round.
  private TileCollection trash;

  private GameBoard[] gameBoards;



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

    centerArea = new TileCollection();

    tilePlates = new TileCollection[(userList.size() * 2) + 1];
    for (int i = 0; i < tilePlates.length - 1; i++) {
      tilePlates[i] = new TileCollection();
    }

    trash = new TileCollection();

    gameBoards = new GameBoard[userList.size()];
    int i = 0;
    for(User user : userList) {
      gameBoards[i] = new GameBoard(user.getName());
      i++;
    }

    // Choose random player to begin with
    Random rand = new Random();
    currentPlayer = rand.nextInt(userList.size());


    fillPlates();
  }



  /**
   * Fill plates with tiles from the bag.
   * */
  private void fillPlates() {
    for (int i = 0; i < tilePlates.length; ++i) {
      tilePlates[i] = bag.drawTiles(4);
      // Check if all plates are full.
      // If not, refill bag with the trash and fill up plates with the bag.
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
    centerArea.add(Tile.WHITE);
  }












  private void pickTiles (TileCollection source, Tile color) {


  }


  private void sendTileSelection(int tilePlate, Tile color, int amount) {
    List<User> broadcastList = userList;
    String currentPlayer = userList.get(this.currentPlayer).getName();

    connection.sendTileSelection(broadcastList, currentPlayer, tilePlate, color, amount);
  }

  private void sendTilePlacement(TileCollection tileSelection, LayingRow layingRow) {
    List<User> broadcastList = userList;
    String currentPlayer = userList.get(this.currentPlayer).getName();
    Tile color = tileSelection.get(0);
    int amount = tileSelection.size();

    connection.sendTilePlacement(broadcastList, currentPlayer, color, amount, layingRow.getRow());
  }


  /**
   * Sets the player whose turn it is to make a move next.
   */
  private void nextPlayer() {
    if (currentPlayer == userList.size()) {
      currentPlayer = 0;
    } else {
      currentPlayer++;
    }
  }

  /**
   * Returns the index number of this game.
   * */
  public int getGameNumber() {
    return gameNumber;
  }

}
