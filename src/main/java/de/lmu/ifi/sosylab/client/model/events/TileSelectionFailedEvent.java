package de.lmu.ifi.sosylab.client.model.events;

/**
 * Event to be fired when tile selection has failed.
 */
public class TileSelectionFailedEvent extends
    GameEvents {

  @Override
  public String getName() {
    return "TileSelectionFailedEvent";
  }
}
