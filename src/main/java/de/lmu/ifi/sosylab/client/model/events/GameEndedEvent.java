package de.lmu.ifi.sosylab.client.model.events;

/**
 * Todo JavaDoc
 * */
public class GameEndedEvent extends GameEvents {
  private String[] players;
  private int[] points;
  private String winner;
  
  /**
   * Todo JavaDoc
   * */
  public GameEndedEvent(String[] players, int[] points, String winner) {
    this.players = players;
    this.points = points;
    this.winner = winner;
  }

  @Override
  public String getName() {
    return "GameEndedEvent";
  }

  public String[] getPlayers() {
    return players;
  }

  public String getWinner() {
    return winner;
  }

  public int[] getPoints() {
    return points;
  }
}
