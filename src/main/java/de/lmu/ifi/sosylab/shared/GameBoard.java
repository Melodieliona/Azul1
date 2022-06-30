package de.lmu.ifi.sosylab.shared;

public class GameBoard {

  private boolean[][] tileWall;

  private LayingRow[] layingRows;

  private TileCollection floorLine;

  private int currentScore;

  private String playerName;





  public GameBoard(String playerName) {
    this.playerName = playerName;
    currentScore = 0;

    tileWall = new boolean[5][5];

    layingRows = new LayingRow[5];
    for (int i = 0; i < 5; i++) {
      layingRows[i] = new LayingRow(i, this);
    }



  }


  protected boolean[][] getTileWall() {
    return tileWall;
  }

  protected String getPlayerName() {
    return playerName;
  }

}
