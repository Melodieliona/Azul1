package de.lmu.ifi.sosylab.client.model.events;

/**
 * Event to be fired when timer has started.
 */
public class TimerEvent extends GameEvents {
  @Override
  public String getName() {
    return "TimerEvent";
  }
}
