package de.lmu.ifi.sosylab.client.model.events;

/**
 * This event is fired when the game was cancelled.
 * */
public class GameCanceledEvent extends GameEvents {

  @Override
  public String getName() {
    return "GameCanceledEvent";
  }
}
