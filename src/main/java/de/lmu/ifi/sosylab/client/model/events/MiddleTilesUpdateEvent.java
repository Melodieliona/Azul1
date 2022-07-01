package de.lmu.ifi.sosylab.client.model.events;


//array of tilecollection . in the middle + on the numbered factory plates tilecollections[0]=middle
public class MiddleTilesUpdateEvent extends GameEvents {

  @Override
  public String getName() {
    return "middleTilesUpdateEvent";
  }
}
