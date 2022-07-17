package de.lmu.ifi.sosylab.client.model.events;

/**
 * TODO JavaDoc
 * */
public class GameRestartRequestEvent extends GameEvents {
  private String nickname;

  public GameRestartRequestEvent(String nickname) {
    this.nickname = nickname;
  }

  @Override
  public String getName() {
    return "GameRestartRequestEvent";
  }

  public String getNickname() {
    return nickname;
  }
}
