package de.lmu.ifi.sosylab.client.model.events;

/**
 * TODO Javadoc
 * */
public class UserJoinedEvent extends GameEvents {
  String userName;

  public UserJoinedEvent(String name) {
    this.userName = name;
  }

  @Override
  public String getName() {
    return "UserJoinedEvent";
  }

  public String getUsername() {
    return userName;
  }
}
