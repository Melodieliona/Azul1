package de.lmu.ifi.sosylab.client.model.events;

/**
 * TODO Javadoc
 * */
public class OtherPlayerPlacedTilesEvent extends GameEvents {
  private String color;
  private int numberOfTiles;
  private int line;
  private int minuspoints;
  private String name;

  /**
   * TODO Javadoc
   * */
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
