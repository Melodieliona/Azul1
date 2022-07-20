package de.lmu.ifi.sosylab.shared;

import java.io.Serial;
import java.util.ArrayList;

/**
 * A set of tiles.
 * Is used for the bag, tile plates, the center area, tile selections and placements etc...
 */
public class TileCollection extends ArrayList<Tile> {

  @Serial
  private static final long serialVersionUID = 1L;

  /**
   * Creates a tile collection.
   * */
  public TileCollection() {
    super();
  }

  /**
   * Adds a given amount of Tiles of same color.
   */
  public void addTiles(Tile color, int amount) {
    for (int i = 0; i < amount; i++) {
      this.add(color);
    }
  }

  /**
   * Adds all tiles form a given collection to this one.
   * */
  public void addAllTiles(TileCollection collection) {
    this.addAll(collection);
  }

  /**
   * Adds one tile of each color to the collection.
   * Used for calculating thx extra points for having 5 wall tiles of the same color.
   * */
  public void addOneOfEachColor() {
    this.add(Tile.BLUE);
    this.add(Tile.YELLOW);
    this.add(Tile.RED);
    this.add(Tile.BLACK);
    this.add(Tile.WHITE);
  }

  /**
   * Removes all tiles of the collection.
   *
   * @return The removed tiles
   * */
  public TileCollection removeAllTiles() {
    TileCollection removedTiles = new TileCollection();
    int collectionSize = this.size();
    for (int i = 0; i < collectionSize; i++) {
      removedTiles.add(this.remove(0));
    }
    return removedTiles;
  }

  /**
   * Removes all tiles of a given color and returns removed Tiles as new collection.
   */
  public TileCollection removeTilesOfColor(Tile color) {
    TileCollection removedTiles = new TileCollection();
    for (int i = 0; i < this.size(); i++) {
      if (this.get(i) == color) {
        removedTiles.add(this.remove(i--));
      }
    }
    return removedTiles;
  }

  /**
   * Take a given amount of random colored tiles from the collection.
   * Can't return more tiles than there are in the collection.
   * Used to fill tile plates with tiles from the bag.
   */
  public TileCollection drawTiles(int amount) {
    TileCollection drawnTiles = new TileCollection();

    if (amount > this.size()) {
      amount = this.size();
    }

    for (int i = 0; i < amount; i++) {
      drawnTiles.add(this.remove((int) (Math.random() * this.size())));
    }

    return drawnTiles;
  }

  /**
   * Returns the amount of tiles of the same color.
   * */
  public int getAmountTilesOfColor(Tile color) {
    int amount = 0;

    for (Tile tile : this) {
      if (tile.equals(color)) {
        amount++;
      }
    }

    return amount;
  }

  /**
   * Returns all colors that there is at least one tile of in this collection.
   * */
  public ArrayList<Tile> getContainedColors() {
    ArrayList<Tile> containedColors = new ArrayList<>();

    if (this.contains(Tile.BLUE)) {
      containedColors.add(Tile.BLUE);
    }
    if (this.contains(Tile.YELLOW)) {
      containedColors.add(Tile.YELLOW);
    }
    if (this.contains(Tile.RED)) {
      containedColors.add(Tile.RED);
    }
    if (this.contains(Tile.BLACK)) {
      containedColors.add(Tile.BLACK);
    }
    if (this.contains(Tile.WHITE)) {
      containedColors.add(Tile.WHITE);
    }
    if (this.contains(Tile.STARTING_MARKER)) {
      containedColors.add(Tile.STARTING_MARKER);
    }

    return containedColors;
  }

}
