package de.lmu.ifi.sosylab.client.model.events;

public class GameCanceledEvent extends GameEvents {

  @Override
  public String getName() {
    return "GameCanceledEvent";
  }
}
