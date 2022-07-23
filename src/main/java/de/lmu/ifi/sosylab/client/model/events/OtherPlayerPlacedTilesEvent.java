package de.lmu.ifi.sosylab.client.model.events;

/**
 * Event to be fired when another player has placed tiles.
 */
public class OtherPlayerPlacedTilesEvent extends GameEvents {
  private String color;
  private int numberOfTiles;
  private int line;
  private int minuspoints;
  private String name;

  /**
   * Constructor.
   *
   * @param color         color of tiles
   * @param line          line
   * @param minuspoints   minuspoints
   * @param name          name of player
   * @param numberOfTiles amount of tiles
   */
  public OtherPlayerPlacedTilesEvent(
      String name, String color, int numberOfTiles, int line, int minuspoints) {
    this.name = name;
    this.color = color;
    this.numberOfTiles = numberOfTiles;
    this.line = line;
    this.minuspoints = minuspoints;
  }

  @Override
  public String getName() {
    return "otherPlayerPlacedTilesEvent";
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
