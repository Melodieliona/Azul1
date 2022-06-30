package de.lmu.ifi.sosylab.shared;

/**
 * A game board containing the score, tile wall and laying rows of one player.
 * */
public class GameBoard {

  private Tile[][] tileWall;

  private LayingRow[] layingRows;

  private TileCollection floorLine;

  private int currentScore;

  private String playerName;




  /**
   * Initializes one game board.
   * */
  public GameBoard(String playerName) {
    this.playerName = playerName;
    currentScore = 0;

    tileWall = new Tile[5][5];

    layingRows = new LayingRow[5];
    for (int i = 0; i < 5; i++) {
      layingRows[i] = new LayingRow(i, this);
    }
  }






  protected Tile[][] getTileWall() {
    return tileWall;
  }

  public LayingRow getLayingRow(int row) {
    return layingRows[row];
  }

  public String getPlayerName() {
    return playerName;
  }

}
