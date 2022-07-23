package de.lmu.ifi.sosylab.client.model.events;

/**
 * Event to be fired when another player has selected tiles.
 */
public class OtherPlayerSelectedTilesEvent extends GameEvents {
  private String color;
  private int numberOfTiles;
  private String playerName;
  private int source;

  /**
   * Constructor.
   *
   * @param color         color of tiles
   * @param source        source plate
   * @param playerName    name of player
   * @param numberOfTiles amount of tiles
   */
  public OtherPlayerSelectedTilesEvent(
      String color, int numberOfTiles, String playerName, int source) {
    this.color = color;
    this.numberOfTiles = numberOfTiles;
    this.playerName = playerName;
    this.source = source;
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

  public int getSource() {
    return source;
  }

  public String getPlayerName() {
    return playerName;
  }
}
