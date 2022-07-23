package de.lmu.ifi.sosylab.client.model.events;

/**
 * Fired when the game is restarted.
 */
public class GameRestartedEvent extends GameEvents {
  @Override
  public String getName() {
    return "GameRestartedEvent";
  }
}
