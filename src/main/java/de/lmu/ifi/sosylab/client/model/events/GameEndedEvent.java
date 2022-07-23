package de.lmu.ifi.sosylab.client.model.events;

/**
 * This event is fired when the game has ended.
 */
public class GameEndedEvent extends GameEvents {

  @Override
  public String getName() {
    return "GameEndedEvent";
  }

}
