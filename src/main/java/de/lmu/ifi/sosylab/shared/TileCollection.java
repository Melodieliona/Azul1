package de.lmu.ifi.sosylab.shared;

import java.util.ArrayList;

/**
 *
 */
public class TileCollection extends ArrayList<Tile> {

  /**
   *
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
   * Used for the bag.
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


}
