package de.lmu.ifi.sosylab.client.model.events;

/**
 * This event is triggered at the end of every round.
 */
public class BoardUpdatedEvent extends GameEvents {
  @Override
  public String getName() {
    return "BoardUpdatedEvent";
  }
}
