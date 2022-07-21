package de.lmu.ifi.sosylab.client.model;

import de.lmu.ifi.sosylab.shared.GameBoard;
import de.lmu.ifi.sosylab.shared.Tile;
import de.lmu.ifi.sosylab.shared.TileCollection;
import java.util.Objects;

/**
 * TODO Javadoc
 */
public class Player {

  private String playerName;

  private GameBoard board;

  private int score;

  /**
   * TODO Javadoc
   */
  public Player(String name) {
    playerName = name;
    board = new GameBoard(name);
    score = 0;
  }

  /**
   * TODO Javadoc
   */
  public void placeTiles(int line, String color, int numberOfTiles) {
    TileCollection tiles = new TileCollection();
    tiles.addTiles(Tile.getTile(color), numberOfTiles);
    board.getLayingRow(line).layTilesOnRow(tiles);
  }

  public String getPlayerName() {
    return playerName;
  }

  public GameBoard getBoard() {
    return board;
  }

  public int getScore() {
    return score;
  }

  public void setScore(int score) {
    this.score = score;
  }

  public void clearBoard(){
    this.board=new GameBoard(this.playerName);
  }

  @Override
  public int hashCode() {
    return Objects.hash(playerName, board, score);
  }

  @Override
  public boolean equals(Object other) {
    if (this == other) {
      return true;
    }
    if (other == null || getClass() != other.getClass()) {
      return false;
    }
    Player player = (Player) other;
    return playerName.equals(player.playerName);
  }
}
