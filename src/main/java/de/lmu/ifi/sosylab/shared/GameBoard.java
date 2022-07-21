package de.lmu.ifi.sosylab.shared;

/**
 * A game board containing the score, tile wall and laying rows of one player.
 * */
public class GameBoard {

  // rows / columns start with 0
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

    floorLine = new TileCollection();
  }

  /**
   * Puts a tile of given color at the right spot on a wall row.
   * */
  public void layWallTile(int row, Tile color) {
    tileWall[getLayingRow(row).columnOfColor(color)][row] = color;
  }

  /**
   * Adds selected tiles to the floor line. Returns all tiles that didn't fit on the floor line.
   * Updates minus points after new tiles are placed on the floor line.
   * */
  public TileCollection addToFloorLine(TileCollection floorLineTiles) {
    TileCollection tilesDidntFit = new TileCollection();
    for (Tile tile : floorLineTiles) {
      if (floorLine.size() < 7) {
        floorLine.add(tile);
      } else {
        tilesDidntFit.add(tile);
      }
    }

    return tilesDidntFit;
  }

  /**
   * Calculates plus points after every round and adds them to 'pluspoints'.
   * Update the current score.
   * */
  public void updatePlusPoints(int row, int column) {

    // Check if placed tile is part of a row
    boolean isPartOfRow = (((column + 1 < 5) && (tileWall[column + 1][row] != null))
                          || ((column - 1 >= 0) && (tileWall[column - 1][row] != null)));

    //Check if placed tile is part of a column
    boolean isPartOfColumn = (((row + 1 < 5) && (tileWall[column][row + 1] != null))
                             || ((row - 1 >= 0) && (tileWall[column][row - 1] != null)));

    if (isPartOfRow) {
      // Add points for rows
      int lengthOfRow = 1;
      int colCounter = column;

      while (((++colCounter) < 5) && (tileWall[colCounter][row] != null)) {
        lengthOfRow++;
      }
      colCounter = column;
      while (((--colCounter) >= 0) && (tileWall[colCounter][row] != null)) {
        lengthOfRow++;
      }

      currentScore += lengthOfRow;

    } else if (isPartOfColumn) {
      // Add points for columns
      int lengthOfColumn = 1;

      int rowCounter = row;
      while (((++rowCounter) < 5) && (tileWall[column][rowCounter] != null)) {
        lengthOfColumn++;
      }
      rowCounter = row;
      while (((--rowCounter) >= 0) && (tileWall[column][rowCounter] != null)) {
        lengthOfColumn++;
      }

      currentScore += lengthOfColumn;

    } else {
      // Add one point for the added wall tile without any points for rows / columns
      currentScore++;
    }
  }

  /**
   * Sets the minus points for the current amount of tiles on the floor line.
   * Updates the current score.
   * */
  private void subtractMinusPoints() {
    int minusPoints;
    switch (floorLine.size()) {
      case 0 -> minusPoints = 0;
      case 1 -> minusPoints = 1;
      case 2 -> minusPoints = 2;
      case 3 -> minusPoints = 4;
      case 4 -> minusPoints = 6;
      case 5 -> minusPoints = 8;
      case 6 -> minusPoints = 11;
      case 7 -> minusPoints = 14;
      default -> throw new IllegalArgumentException("Tried to set minus points for more than 7 "
        + "tiles on the floor line. 7 tiles is the maximum amount.");
    }

    //Update current score
    currentScore -= minusPoints;
    if (currentScore < 0) {
      currentScore = 0;
    }
  }

  /**
   * Subtracts minus-points from plus-points and clears the floor line.
   *
   * @return The removed tiles
   * */
  public TileCollection clearFloorLine() {
    subtractMinusPoints();
    return floorLine.removeAllTiles();
  }

  /**
   * Returns this game board's tile wall.
   * */
  public Tile[][] getTileWall() {
    //TODO Test if copy is correct
    return tileWall;
  }

  /**
   * Returns the requested laying row.
   * */
  public LayingRow getLayingRow(int row) {
    return layingRows[row];
  }

  /**
   * Returns all laying rows.
   * */
  public LayingRow[] getLayingRows() {
    return layingRows;
  }

  /**
   * Calculates the extra points you get at the end of the game and adds them to the plus-points.
   * Updates the current score.
   */
  public void calculateAndAddExtraPoints() {

    int amountOfFullRows = 0;
    int amountOfFullColumns = 0;

    // Count full rows
    for (int row = 0; row < 5; row++) {

      int lengthOfRow = 0;

      for (int col = 0; col < 5; col++) {
        if (tileWall[col][row] != null) {
          lengthOfRow++;
        }
      }

      if (lengthOfRow == 5) {
        amountOfFullRows++;
      }
    }

    // Count full columns

    for (int col = 0; col < 5; col++) {

      int lengthOfColumn = 0;

      for (int row = 0; row < 5; row++) {
        if (tileWall[col][row] != null) {
          lengthOfColumn++;
        }
      }

      if (lengthOfColumn == 5) {
        amountOfFullColumns++;
      }
    }

    // Check if all wall tiles of one color are set

    TileCollection allColors = new TileCollection();
    allColors.addOneOfEachColor();

    int completedColors = 0;

    for (Tile color : allColors) {
      int tilesOfSameColor = 0;
      for (int row = 0; row < 5; row++) {
        for (int col = 0; col < 5; col++) {
          if ((tileWall[col][row] != null) && (tileWall[col][row].equals(color))) {
            tilesOfSameColor++;
          }
        }
      }

      if (tilesOfSameColor == 5) {
        completedColors++;
      }
    }

    // Add calculated points
    currentScore += (amountOfFullColumns * 7) + (amountOfFullRows * 5) + (completedColors * 10);
  }

  /**
   * Returns the current (the final at the time this method gets invoked) score of this player.
   * */
  public int getCurrentScore() {
    return currentScore;
  }

  /**
   * Returns the name of the player this game board belongs to.
   */
  public String getPlayerName() {
    return playerName;
  }

  public TileCollection getFloorLine() {
    return floorLine;
  }

  public void setCurrentScore(int points) {
    currentScore = points;
  }
}
