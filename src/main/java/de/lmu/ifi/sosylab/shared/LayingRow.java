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

  /**
   * Adds given tiles to the row.
   * Returns all tiles that were successfully placed.
   * */
  public TileCollection layTilesOnRow(TileCollection collection) {
    if (color == null) {
      if (collection.get(0) != Tile.STARTING_MARKER) {
        color = collection.get(0);
      } else if (collection.size() > 1) {
        color = collection.get(1);
      }
    }

    TileCollection placedTiles = new TileCollection();
    for (Tile tile : collection) {
      if (isRowFull() || tile.equals(Tile.STARTING_MARKER)) {
        continue;
      }
      placedTiles.add(tile);
      count++;
    }
    return placedTiles;
  }

  /**
   * Clears the row and returns all thrown away tiles (all except the one that stays on the wall).
   * */
  public TileCollection clearRow() {
    TileCollection trashedTiles = new TileCollection();
    trashedTiles.addTiles(color, count - 1);
    color = null;
    count = 0;

    return trashedTiles;
  }

  // TODO Maybe delete later if unused
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
  public boolean isRowFull() {
    return count == row;
  }

  /**
   * Returns the column, the given tile color lays in.
   * */
  public int columnOfColor(Tile color) {
    return ((row + color.ordinal()) % 5) + 1;
  }

  /**
   * Returns the color of which tiles can be placed on this row.
   * */
  public Tile getColor() {
    return color;
  }


  // TODO Maybe delete later if unused
  /**
   * Returns the number of this row.
   * */
  public int getRowNumber() {
    return row;
  }
}
