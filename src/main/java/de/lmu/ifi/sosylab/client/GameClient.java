package de.lmu.ifi.sosylab.client;

import de.lmu.ifi.sosylab.client.model.GameModel;
import de.lmu.ifi.sosylab.client.controller.GameController;

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

    //View frame instanziieren
    //ChatFrame chatFrame = new ChatFrame(controller, model);

    //ChangeListener fürs Frame
    //model.addPropertyChangeListener(chatFrame);

    //ClientNetworkConnection connection = new ClientNetworkConnection(model);
    //model.setConnection(connection);
    //connection.start();

    //Frame sichtbar machen
    //chatFrame.setVisible(true);
  }

}
