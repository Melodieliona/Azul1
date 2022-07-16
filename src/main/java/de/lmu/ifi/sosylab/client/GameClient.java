package de.lmu.ifi.sosylab.client;

import de.lmu.ifi.sosylab.client.controller.GameController;
import de.lmu.ifi.sosylab.client.model.GameModel;
import de.lmu.ifi.sosylab.client.view.GameFrame;
import java.io.IOException;

/**
 * Starts the chat-client.
 */
public class GameClient {

  /**
   * Creates model, controller and starts the GUI.
   * */
  public static void main(String[] args) throws IOException {

    GameModel model = new GameModel();
    GameController controller = new GameController(model);
    GameFrame gameFrame = new GameFrame(controller, model);

    model.addPropertyChangeListener(gameFrame);


    //Frame sichtbar machen
    gameFrame.setVisible(true);
  }

}
