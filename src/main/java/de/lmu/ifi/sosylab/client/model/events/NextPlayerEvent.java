package de.lmu.ifi.sosylab.client.model.events;

/**
 * To be fired when a next player has been declared.
 */
public class NextPlayerEvent extends GameEvents {

  private String nextPlayer;

  public NextPlayerEvent(String nextPlayer) {
    this.nextPlayer = nextPlayer;
  }

  @Override
  public String getName() {
    return null;
  }

  public String getNextPlayer() {
    return nextPlayer;
  }
}
