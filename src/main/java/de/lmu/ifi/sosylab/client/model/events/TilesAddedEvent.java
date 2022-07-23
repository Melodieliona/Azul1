package de.lmu.ifi.sosylab.client.model.events;

/**
 * Event to be fired when tiles have been placed.
 */
public class TilesAddedEvent extends GameEvents {
  private String color;
  private int line;
  private int numberOfTiles;
  private int minuspoints;

  /**
   * Stores crucial information for the view.
   *
   * @param color  of the tile
   * @param line where the tiles were placed
   * @param numberOfTiles that were placed
   * @param minuspoints after tile placement
   */
  public TilesAddedEvent(String color, int line, int numberOfTiles, int minuspoints) {
    this.color = color;
    this.line = line;
    this.numberOfTiles = numberOfTiles;
    this.minuspoints = minuspoints;
  }

  @Override
  public String getName() {
    return "TilesAddedEvent";
  }

  public int getMinuspoints() {
    return minuspoints;
  }

  public int getLine() {
    return line;
  }

  public int getNumberOfTiles() {
    return numberOfTiles;
  }

  public String getColor() {
    return color;
  }
}
