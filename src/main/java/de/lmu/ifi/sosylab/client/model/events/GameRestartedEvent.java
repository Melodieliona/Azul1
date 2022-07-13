package de.lmu.ifi.sosylab.client.model.events;

public class GameRestartedEvent extends GameEvents{
  @Override
  public String getName() {
    return "GameRestartedEvent";
  }
}
