package de.lmu.ifi.sosylab.client.model.events;

public class PointsUpdatedEvent extends GameEvents{
  private String[] playerName;
  private int[] points;
  public PointsUpdatedEvent(String[] playerName, int[] points) {
    this.playerName = playerName;
    this.points = points;
  }

  @Override
  public String getName() {
    return "PointsUpdatedEvent";
  }

  public int[] getPoints() {
    return points;
  }

  public String[] getPlayerName() {
    return playerName;
  }
}
