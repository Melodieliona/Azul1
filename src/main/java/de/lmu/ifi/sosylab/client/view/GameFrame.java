package de.lmu.ifi.sosylab.client.view;

import static java.util.Objects.requireNonNull;

import de.lmu.ifi.sosylab.client.controller.GameController;
import de.lmu.ifi.sosylab.client.model.GameModel;
import java.awt.CardLayout;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

/**
 * The main view of the chat user interface. It provides and connects all graphical elements
 * that are necessary for a chat application. It provides a user a screen for logging in, and
 * in case of success shows afterwards the necessary elements for writing and reading chat messages.
 */
public class GameFrame extends JFrame implements PropertyChangeListener {

  private final GameModel model;
  private final GameController controller;

  private CardLayout layout;


  /**
   * Create a new graphical view that contains all necessary elements for chatting with .
   *
   * @param model      The {@link GameModel} that handles the logic of the game.
   * @param controller The {@link GameController} that validates and forwards any user input.
   */
  public GameFrame(GameController controller, GameModel model) {
    super("~ Azul ~");

    this.controller = requireNonNull(controller);
    this.model = requireNonNull(model);

    setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

    //initializeWidgets();
    //addEventListeners();
    //createView();

    pack();
  }

  /**
   * Instantiate all Swing widgets and specify config options where appropriate.
   */
  private void initializeWidgets() {
    // CardLayout ist nur Platzhalter
    layout = new CardLayout();

  }

  /**
   * Set up the view in a way that is finally shown to the user.
   */
  private void createView() {
    JPanel panel = new JPanel(layout);
    setContentPane(panel);



  }

  /**
   * Add event listeners to all widgets wherever needed and let them execute the respective action.
   */
  private void addEventListeners() {

  }

  @Override
  public void dispose() {
    super.dispose();
    model.removePropertyChangeListener(this);
    controller.dispose();
  }

  @Override
  public void propertyChange(PropertyChangeEvent event) {
    SwingUtilities.invokeLater(new Runnable() {
      @Override
      public void run() {
        handleModelUpdate(event);
      }
    });
  }

  /**
   * The observable (= model) just published that it has changed its state.
   * The GUI is updated here accordingly.
   *
   * @param event The {@link PropertyChangeEvent} that was fired by the model.
   */
  private void handleModelUpdate(PropertyChangeEvent event) {
    Object newValue = event.getNewValue();

    /**
     *
     *  Das ist vom Chat, um zwischen den Ansichten (Login / Chat) zu wechseln. Könnten wir ja so
     *  irgendwie auch benutzten (?)
     *
     * if (newValue instanceof LoggedInEvent) {
     *       this.setTitle("Chat Client - " + nickName.getText());
     *       showChat();
     *     } else if (newValue instanceof LoginFailedEvent) {
     *       JOptionPane.showMessageDialog(new JFrame(), "This username is already taken!",
     *         "Error!", JOptionPane.ERROR_MESSAGE);
     *       showLogin();
     *     } else if (newValue instanceof MessageAddedEvent) {
     *       listModel.removeAllElements();
     *       listModel.addAll(model.getMessages());
     *     } else if (newValue instanceof MessageRemovedEvent) {
     *       listModel.remove(0);
     *     }
     *
     * */

  }

  /**
   * Show the login view to the user.
   */
  private void showLogin() {
    //showCard(LOGIN_CARD);
  }

  /**
   * Show the chat view to the user.
   */
  private void showChat() {
    //showCard(CHAT_CARD);
  }

  private void showCard(String card) {
    layout.show(getContentPane(), card);
  }
}

