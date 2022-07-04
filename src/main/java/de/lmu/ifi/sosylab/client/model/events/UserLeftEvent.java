package de.lmu.ifi.sosylab.client.model.events;

/**
 * TODO Javadoc
 * */
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
