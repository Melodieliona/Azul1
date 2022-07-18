package de.lmu.ifi.sosylab.client.model.events;

public class TimerEndedEvent extends GameEvents{
  @Override
  public String getName() {
    return "TimerEndedEvent";
  }
}
