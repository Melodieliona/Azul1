package de.lmu.ifi.sosylab.client.model.events;

/**
 * This event is triggered when tiles are placed on the floor line.
 */
public class FloorLineEvent extends GameEvents {

  @Override
  public String getName() {
    return "FloorLineUpdate";
  }
}
