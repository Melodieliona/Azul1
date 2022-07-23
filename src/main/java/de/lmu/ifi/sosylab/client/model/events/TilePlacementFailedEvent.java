package de.lmu.ifi.sosylab.client.model.events;

/**
 * Event to be fired when the tile placement failed.
 */
public class TilePlacementFailedEvent extends
    GameEvents {

  @Override
  public String getName() {
    return "TilePlacementFailedEvent";
  }
}
