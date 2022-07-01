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
  public TileCollection addToFloorLine (TileCollection minusPointTiles) {
    TileCollection tilesDidntFit = new TileCollection();
    for(Tile tile : minusPointTiles) {
      if(floorLine.size() == 7) {
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
      case 0: minusPoints = 0;
      break;
      case 1: minusPoints = 1;
        break;
      case 2: minusPoints = 2;
        break;
      case 3: minusPoints = 4;
        break;
      case 4: minusPoints = 6;
        break;
      case 5: minusPoints = 8;
        break;
      case 6: minusPoints = 11;
        break;
      case 7: minusPoints = 14;
        break;
      default: System.out.println("Error: Floor line size cannot be over 7!");
        break;
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
