package de.lmu.ifi.sosylab.client.model.events;

/**
 * Event to be fired when a user has left.
 */
public class UserLeftEvent extends GameEvents {
  String userName;

  public UserLeftEvent(String name) {
    this.userName = name;
  }

  @Override
  public String getName() {
    return "UserLeftEvent";
  }

  public String getUsername() {
    return userName;
  }
}
