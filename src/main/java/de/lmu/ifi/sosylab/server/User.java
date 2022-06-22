package de.lmu.ifi.sosylab.server;


import java.io.OutputStreamWriter;

/**
 * Stores the data corresponding to a single user / connected client.
 * */

public class User {
  private final String name;

  private final int gameNumber;
  private final OutputStreamWriter writer;

  /**
   *
   * */
  public User(String name, OutputStreamWriter writer, int gameNumber) {
    this.name = name;
    this.gameNumber = gameNumber;
    this.writer = writer;
  }

  public String getName() {
    return name;
  }

  public int getGameNumber() { return gameNumber; }

  protected OutputStreamWriter getWriter() {
    return writer;
  }



}
