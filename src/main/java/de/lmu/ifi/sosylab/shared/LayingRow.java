package de.lmu.ifi.sosylab.shared;

/**
 * One row, that needs to be laid out with tiles in order to place a tile on the wall.
 * */
public class LayingRow {

  GameBoard board;
  // The Index of this row (from top to bottom, starting with '1')
  private final int row;

  private Tile color;

  // Number of laid tiles in this row
  private int count;

  /**
   * Initializes the row with zero laid tiles.
   */
  public LayingRow(int number, GameBoard board) {
    this.board = board;
    row = number;
    color = null;
    count = 0;
  }

  // TODO Maybe remove return value later if unused
  /**
   * Adds given tiles to the row.
   * Returns the updated row.
   * */
  public TileCollection layTilesOnRow(TileCollection collection) {
    if (color == null) {
      color = collection.get(0);
    }

    for (Tile tile : collection) {
      count++;
    }
    return getRow();
  }


  public void clearRow() {
    color = null;
    count = 0;
  }


  /**
   * Returns all tiles that are currently laid on this row.
   */
  public TileCollection getRow() {
    TileCollection rowCollection = new TileCollection();
    for (int i = 0; i < count; i++) {
      rowCollection.add(color);
    }
    return rowCollection;
  }

  /**
   * Determines if it's allowed to lay tiles of a given color in this row.
   * */
  public boolean canAddTilesToLayingRow(Tile color) {
    Tile[][] tileWall = board.getTileWall();

    return (tileWall[columnOfColor(color)][row] == null)
      && (this.color == null || this.color == color) && !this.isRowFull();
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
    return ((row + color.ordinal()) % 5) + 1;
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

  public int getRowNumber() {
    return row;
  }
}
