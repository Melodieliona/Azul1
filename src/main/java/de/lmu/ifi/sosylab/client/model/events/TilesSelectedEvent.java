package de.lmu.ifi.sosylab.client.model.events;

public class TilesSelectedEvent extends GameEvents {

  private int source;
  private String color;
  private int numberOfTiles;

  public TilesSelectedEvent(int source, String color, int numberOfTiles) {
    this.source = source;
    this.color = color;
    this.numberOfTiles = numberOfTiles;
  }

  @Override
  public String getName() {
    return "TilesSelectedEvent";
  }

  public int getNumberOfTiles() {
    return numberOfTiles;
  }

  public String getColor() {
    return color;
  }

  public int getSource() {
    return source;
  }
}
