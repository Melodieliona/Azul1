package de.lmu.ifi.sosylab.client.view;

import static java.util.Objects.requireNonNull;

import de.lmu.ifi.sosylab.client.controller.GameController;
import de.lmu.ifi.sosylab.client.model.GameModel;
import de.lmu.ifi.sosylab.client.model.events.LoggedInEvent;
import de.lmu.ifi.sosylab.client.model.events.LoginFailedEvent;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.*;

/**
 * The main view of the chat user interface. It provides and connects all graphical elements
 * that are necessary for a chat application. It provides a user a screen for logging in, and
 * in case of success shows afterwards the necessary elements for playing the game.
 */
public class GameFrame extends JFrame implements PropertyChangeListener {

    private final GameModel model;
    private final GameController controller;
    private CardLayout layout;
    private static final String LOGIN_CARD = "login";
    private static final String GAME_CARD = "game";
    private JTextField nickName;


    /**
     * Create a new graphical view that contains all necessary elements for playing the game.
     *
     * @param model      The {@link GameModel} that handles the logic of the game.
     * @param controller The {@link GameController} that validates and forwards any user input.
     */
    public GameFrame(GameController controller, GameModel model) {
        super("~ Azul ~");

        this.controller = requireNonNull(controller);
        this.model = requireNonNull(model);

        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        //this.setPreferredSize(new Dimension(400, 300));

        initializeWidgets();
        addEventListeners();
        createView();

        pack();
    }

    /**
     * Instantiate all Swing widgets and specify config options where appropriate.
     */
    private void initializeWidgets() {
        // CardLayout ist nur Platzhalter
        layout = new CardLayout();
        nickName = new JTextField(20);

    }

    /**
     * Set up the view in a way that is finally shown to the user.
     */
    private void createView() {
        JPanel panel = new JPanel(layout);
        setContentPane(panel);

        // Panel for the login view
        JPanel login = new JPanel();
        login.setBackground(Color.CYAN);
        login.setPreferredSize(new Dimension(400, 100));
        showCard(LOGIN_CARD);
        add(login, LOGIN_CARD);

        login.add(new JLabel("Login with your nick name:"));
        login.add(nickName);

        JPanel game = new JPanel(new GridBagLayout());
        add(game, GAME_CARD);
        game.setBackground(Color.PINK);


    }

    /**
     * Add event listeners to all widgets wherever needed and let them execute the respective action.
     */
    private void addEventListeners() {

        nickName.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                controller.login(nickName.getText());
            }
        });
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

        if (newValue instanceof LoggedInEvent) {
            showGame();
        } else if (newValue instanceof LoginFailedEvent) {
            JOptionPane.showMessageDialog(this,
                    String.format("Login failed, name \"%s\" is already in use.", nickName.getText()));
            showLogin();
        }

        /*
         *
         *  Das ist vom Chat, um zwischen den Ansichten (Login / Chat) zu wechseln. Könnten wir ja so
         *  irgendwie auch benutzten (?)
         *
         * else if (newValue instanceof MessageAddedEvent) {
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
        showCard(LOGIN_CARD);
    }

    /**
     * Show the chat view to the user.
     */
    private void showGame() {
        showCard(GAME_CARD);
    }

    private void showCard(String card) {
        layout.show(getContentPane(), card);
    }
}

