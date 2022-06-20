package de.lmu.ifi.sosylab.client.controller;

import de.lmu.ifi.sosylab.client.model.GameModel;

public class GameController {

  /**
   * The controller of the chat-UI.
   */

    GameModel model;

    public GameController(GameModel model) {
      this.model = model;
    }


    public void dispose() {
      //model.dispose();
    }
  }




