package de.lmu.ifi.sosylab.client;

import de.lmu.ifi.sosylab.client.controller.GameController;
import de.lmu.ifi.sosylab.client.model.GameModel;
import de.lmu.ifi.sosylab.client.view.GameFrame;

/**
 * Starts the chat-client.
 */
public class GameClient {

  /**
   * Creates model, controller and starts the GUI.
   * */
  public static void main(String[] args) {

    GameModel model = new GameModel();
    GameController controller = new GameController(model);
    GameFrame gameFrame = new GameFrame(controller, model);

    model.addPropertyChangeListener(gameFrame);

    //ClientNetworkConnection connection = new ClientNetworkConnection(model);
    //model.setConnection(connection);
    //connection.start();

    //Frame sichtbar machen
    gameFrame.setVisible(true);
  }

}
