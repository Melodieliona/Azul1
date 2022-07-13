package de.lmu.ifi.sosylab.client.model.events;

import java.util.Arrays;

/**
 * TODO Add JavaDoc
 * */
public class PointsUpdatedEvent extends GameEvents {
  private String[] playerNames;
  private int[] points;

  public PointsUpdatedEvent(String[] playerNames, int[] points) {
    this.playerNames = Arrays.copyOf(playerNames, playerNames.length);
    this.points = Arrays.copyOf(points, points.length);
  }

  @Override
  public String getName() {
    return "PointsUpdatedEvent";
  }

  public int[] getPoints() {
    return Arrays.copyOf(points, points.length);
  }

  public String[] getPlayerNames() {
    return Arrays.copyOf(playerNames, playerNames.length);
  }
}
