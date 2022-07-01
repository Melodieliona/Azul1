package de.lmu.ifi.sosylab.client.model.events;

public class otherPlayerSelectedTilesEvent extends GameEvents{
  private String color;
  private int numberOfTiles;

  public otherPlayerSelectedTilesEvent(String color, int numberOfTiles){
    this.color = color;
    this.numberOfTiles = numberOfTiles;
  }
  @Override
  public String getName() {
    return "otherPlayerSelectedTilesEvent";
  }

  public int getNumberOfTiles() {
    return numberOfTiles;
  }

  public String getColor() {
    return color;
  }
}
