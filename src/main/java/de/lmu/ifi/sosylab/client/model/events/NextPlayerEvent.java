package de.lmu.ifi.sosylab.client.model.events;

public class NextPlayerEvent extends GameEvents {

  private String nextPlayer;

  public NextPlayerEvent (String nextPlayer){
    this.nextPlayer = nextPlayer;
  }
  @Override
  public String getName() {
    return null;
  }
  public String getNextPlayer(){
    return nextPlayer;
  }
}
