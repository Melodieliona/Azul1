package de.lmu.ifi.sosylab.client.model.events;

/**
 * To be fired when the logIn failed.
 */
public class LoginFailedEvent extends GameEvents {
  @Override
  public String getName() {
    return "LoginFailedEvent";
  }
}
