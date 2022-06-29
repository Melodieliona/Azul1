package de.lmu.ifi.sosylab.client.model.events;

public class TilesAddedEvent extends GameEvents{
  private String color;
  private int line;
  private int numberOfTiles;
  private int minuspoints;

  public TilesAddedEvent(String color,int line, int numberOfTiles, int minuspoints){
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
