package de.lmu.ifi.sosylab.client.model.events;

import java.util.Arrays;

/**
 * Todo JavaDoc
 * */
public class GameEndedEvent extends GameEvents {
  private final String[] players;
  private final int[] points;
  private final String winner;
  
  /**
   * Todo JavaDoc
   * */
  public GameEndedEvent(String[] players, int[] points, String winner) {
    this.players = Arrays.copyOf(players, players.length);
    this.points = Arrays.copyOf(points, points.length);
    this.winner = winner;
  }

  @Override
  public String getName() {
    return "GameEndedEvent";
  }

  public String[] getPlayers() {
    return Arrays.copyOf(players, players.length);
  }

  public String getWinner() {
    return winner;
  }

  public int[] getPoints() {
    return Arrays.copyOf(points, points.length);
  }
}
