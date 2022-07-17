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
  }

  /**
   * TODO Javadoc
   */
  public void placeTiles(int line, String color, int numberOfTiles) {
    TileCollection tiles = new TileCollection();
    tiles.addTiles(Tile.getTile(color), numberOfTiles);
    board.getLayingRow(line).layTilesOnRow(tiles);
    System.out.println("Size of the collection in line "+line+": "+board.getLayingRow(line).getRow().size());
    System.out.println("Player: "+playerName+ " has placed "+numberOfTiles+" "+ color+" in line "+line);
  }

  public String getPlayerName() {
    return playerName;
  }

  public GameBoard getBoard() {
    GameBoard copyOfGameBoard = new GameBoard(board);
    return copyOfGameBoard;
  }

}
