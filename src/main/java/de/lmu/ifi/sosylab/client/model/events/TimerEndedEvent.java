package de.lmu.ifi.sosylab.client.model.events;

/**
 * Event to be fired when timer has ended.
 */
public class TimerEndedEvent extends GameEvents {
  @Override
  public String getName() {
    return "TimerEndedEvent";
  }
}
