package de.lmu.ifi.sosylab.client.model;

import de.lmu.ifi.sosylab.shared.GameBoard;
import de.lmu.ifi.sosylab.shared.Tile;
import de.lmu.ifi.sosylab.shared.TileCollection;

import java.util.Objects;

/**
 * A given player. This class is to be used to manage all information relevant for the players.
 */
public class Player {

  private String playerName;

  private GameBoard board;

  private int score;

  /**
   * Constructor of the class.
   *
   * @param name nickname of the player.
   */
  public Player(String name) {
    playerName = name;
    board = new GameBoard(name);
    score = 0;
  }

  /**
   * Places tiles in the board of a player.
   *
   * @param line          the line in which the tiles should be placed
   * @param color         the color of the tiles
   * @param numberOfTiles amount of tiles
   */
  public void placeTiles(int line, String color, int numberOfTiles) {
    TileCollection tiles = new TileCollection();
    tiles.addTiles(Tile.getTile(color), numberOfTiles);
    board.getLayingRow(line).layTilesOnRow(tiles);
  }

  /**
   * Gets the nickname of the player.
   *
   * @return the nickname
   */
  public String getPlayerName() {
    return playerName;
  }

  /**
   * Gets the board of the player.
   *
   * @return the board
   */
  public GameBoard getBoard() {
    return board;
  }

  /**
   * Gets the score of the player.
   *
   * @return the score
   */
  public int getScore() {
    return score;
  }

  /**
   * Sets the score of the player.
   *
   * @param score score to be set
   */
  public void setScore(int score) {
    this.score = score;
  }

  /**
   * Clears the board of the player.
   */
  public void clearBoard() {
    this.board = new GameBoard(this.playerName);
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
