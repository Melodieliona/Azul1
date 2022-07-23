package de.lmu.ifi.sosylab.client.model.localserver;

import java.util.Objects;

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

  public int hashCode() {
    return Objects.hash(name);
  }

  @Override
  public boolean equals(Object other) {
    if (this == other) {
      return true;
    }
    if (other == null || getClass() != other.getClass()) {
      return false;
    }
    LocalUser user = (LocalUser) other;
    return name.equals(user.getName());
  }

}
