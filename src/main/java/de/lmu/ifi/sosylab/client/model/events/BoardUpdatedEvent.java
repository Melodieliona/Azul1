package de.lmu.ifi.sosylab.client.model.events;

public class BoardUpdatedEvent extends GameEvents{
  @Override
  public String getName() {
    return "BoardUpdatedEvent";
  }
}
