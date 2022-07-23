package de.lmu.ifi.sosylab.client.model.events;


//array of tilecollection . in the middle + on the numbered factory plates tilecollections[0]=middle

/**
 * To be fired when the middle of the game updates (plates and factory floor).
 */
public class MiddleTilesUpdateEvent extends GameEvents {

  @Override
  public String getName() {
    return "middleTilesUpdateEvent";
  }
}
