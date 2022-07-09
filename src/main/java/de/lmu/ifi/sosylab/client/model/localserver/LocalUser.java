package de.lmu.ifi.sosylab.client.model.localserver;

/**
 * Stores the data corresponding to a single local user.
 * */

public class LocalUser {
  private final String name;

  /**
   * Represents a single local Player.
   * */
  public LocalUser(String name) {
    this.name = name;
  }

  public String getName() {
    return name;
  }

}
