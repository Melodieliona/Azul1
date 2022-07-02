package de.lmu.ifi.sosylab.shared;

/**
 * A game board containing the score, tile wall and laying rows of one player.
 * */
public class GameBoard {

  // rows / columns start with 1
  private Tile[][] tileWall;

  private LayingRow[] layingRows;

  private TileCollection floorLine;

  private int plusPoints;

  private int minusPoints;

  private int currentScore;

  private String playerName;




  /**
   * Initializes one game board.
   * */
  public GameBoard(String playerName) {
    this.playerName = playerName;
    currentScore = 0;
    plusPoints = 0;
    minusPoints = 0;

    tileWall = new Tile[5][5];

    layingRows = new LayingRow[5];
    for (int i = 0; i < 5; i++) {
      layingRows[i] = new LayingRow(i, this);
    }

    floorLine = new TileCollection();
  }




  public void layWallTile(int row, Tile color) {
    tileWall[getLayingRow(row).columnOfColor(color)][row] = color;
  }



  /**
   * Adds selected tiles to the floor line. Returns all tiles that didn't fit on the floor line.
   * Updates minus points after new tiles are placed on the floor line.
   * */
  public TileCollection addToFloorLine(TileCollection minusPointTiles) {
    TileCollection tilesDidntFit = new TileCollection();
    for (Tile tile : minusPointTiles) {
      if (floorLine.size() == 7) {
        tilesDidntFit.add(tile);
        continue;
      }
      floorLine.add(tile);
    }
    updateMinusPoints();

    return tilesDidntFit;
  }

  private void updateMinusPoints() {
    switch (floorLine.size()) {
      case 0 -> minusPoints = 0;
      case 1 -> minusPoints = 1;
      case 2 -> minusPoints = 2;
      case 3 -> minusPoints = 4;
      case 4 -> minusPoints = 6;
      case 5 -> minusPoints = 8;
      case 6 -> minusPoints = 11;
      case 7 -> minusPoints = 14;
      default -> System.out.println("Error: There cannot be more than 7 tiles on the floor line!");
    }
  }

  /**
   * Returns this game board's tile wall.
   * */
  protected Tile[][] getTileWall() {
    return tileWall;
  }

  /**
   * Returns the requested laying row.
   * */
  public LayingRow getLayingRow(int row) {
    return layingRows[row];
  }

  /**
   * Returns the name of the player this game board belongs to.
   */
  public String getPlayerName() {
    return playerName;
  }

}
