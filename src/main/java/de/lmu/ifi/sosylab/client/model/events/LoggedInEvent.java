package de.lmu.ifi.sosylab.client.model.events;

/**
 * To be fired when the logIn was successful.
 */
public class LoggedInEvent extends GameEvents {
  @Override
  public String getName() {
    return "LoggedInEvent";
  }
}

