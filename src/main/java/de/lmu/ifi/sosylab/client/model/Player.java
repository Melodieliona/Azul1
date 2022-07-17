package de.lmu.ifi.sosylab.client.model;

import de.lmu.ifi.sosylab.server.Game;
import de.lmu.ifi.sosylab.shared.GameBoard;
import de.lmu.ifi.sosylab.shared.Tile;
import de.lmu.ifi.sosylab.shared.TileCollection;

/**
 * TODO Javadoc
 */
public class Player {

  private String playerName;

  private GameBoard board;

  /**
   * TODO Javadoc
   */
  public Player(String name) {
    playerName = name;
    board = new GameBoard(name);
    System.out.println("created player");
  }

  /**
   * TODO Javadoc
   */
  public void placeTiles(int line, String color, int numberOfTiles) {
    TileCollection tiles = new TileCollection();
    tiles.addTiles(Tile.getTile(color), numberOfTiles);
    System.out.println(tiles.size());
    board.getLayingRow(line).layTilesOnRow(tiles);
  }

  public String getPlayerName() {
    return playerName;
  }

  public GameBoard getBoard() {
    GameBoard copyOfGameBoard = new GameBoard(board);
    return copyOfGameBoard;
  }
}
