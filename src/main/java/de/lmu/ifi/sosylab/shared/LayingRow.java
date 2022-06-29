package de.lmu.ifi.sosylab.shared;

public class LayingRow {

  GameBoard board;
  // The Index of this row (from top to bottom, starting with '1')
  private final int row;

  private Tile color;

  // Number of laid tiles in this row
  private int count;

  public LayingRow(int number, GameBoard board) {
    this.board = board;
    row = number;
    color = null;
    count = 0;
  }





  /**
   * Determines if it's allowed to lay tiles of a given color in this row.
   * */
  public boolean canAddTilesToLayingRow(Tile color) {
    boolean[][] tileWall = board.getTileWall();
    return !(tileWall[row][columnOfColor(color)]) && (this.color == null || this.color == color) && !this.isRowFull();
  }

  /**
   * Returns if this row is already laid out with tiles.
   * */
  private boolean isRowFull() {
    return count == row;
  }

  /**
   * Returns the column, the given tile color lays in.
   * */
  private int columnOfColor(Tile color) {
    return (row + color.ordinal()) % 5;
  }


  /**
   * Returns the tiles that are discarded at the end of a round.
   *
   * @return The discarded tiles
   */
  public TileCollection getDiscard() {
    TileCollection discardedTiles = new TileCollection();
    // Equal to the row number because one is kept for the wall
    discardedTiles.addTiles(color, row);
    this.count = 0;
    this.color = null;
    return discardedTiles;
  }

  public int getRow() {
    return row;
  }
}
