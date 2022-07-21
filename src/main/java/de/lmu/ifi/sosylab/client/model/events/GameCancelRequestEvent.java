package de.lmu.ifi.sosylab.client.model.events;

/**
 * This event is fired when
 * */
public class GameCancelRequestEvent extends GameEvents {
  private String nickname;

  public GameCancelRequestEvent(String nickname) {
    this.nickname = nickname;
  }

  @Override
  public String getName() {
    return "GameCancelRequest";
  }

  public String getNickname() {
    return nickname;
  }
}
