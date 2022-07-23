package de.lmu.ifi.sosylab.client.model.events;

/**
 * Event to be fired when another player has placed tiles.
 */
public class OtherPlayerPlacedTilesEvent extends GameEvents {
  private final String color;
  private final int line;

  /**
   * Constructor.
   *
   * @param color         color of tiles
   * @param line          line
   */
  public OtherPlayerPlacedTilesEvent(String color, int line) {
    this.color = color;
    this.line = line;
  }

  @Override
  public String getName() {
    return "otherPlayerPlacedTilesEvent";
  }

  public int getLine() {
    return line;
  }

  public String getColor() {
    return color;
  }
}
