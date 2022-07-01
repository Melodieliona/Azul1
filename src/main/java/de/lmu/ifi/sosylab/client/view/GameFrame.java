package de.lmu.ifi.sosylab.client.view;

import static java.util.Objects.requireNonNull;

import de.lmu.ifi.sosylab.client.controller.GameController;
import de.lmu.ifi.sosylab.client.model.GameModel;
import de.lmu.ifi.sosylab.client.model.events.LoggedInEvent;
import de.lmu.ifi.sosylab.client.model.events.LoginFailedEvent;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.Serial;
import javax.swing.*;

/**
 * The main view of the chat user interface. It provides and connects all graphical elements
 * that are necessary for a chat application. It provides a user a screen for logging in, and
 * in case of success shows afterwards the necessary elements for playing the game.
 */
public class GameFrame extends JFrame implements PropertyChangeListener {

    @Serial
    private static final long serialVersionUID = 1L;

    private transient GameModel model;
    private transient GameController controller;
    private CardLayout layout;
    private static final String LOGIN_CARD = "login";
    private static final String GAME_CARD = "game";
    private static final String GAMEMODE_CARD = "gameMode";
    private JTextField nickName;
    private JButton hotSeat;
    private JButton multiPlayer;
    private JPanel game;


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
        hotSeat = new JButton("HOTSEAT");
        multiPlayer = new JButton("MULTIPLAYER");

    }

    public void createSetGameModeView() {

        JPanel setGameMode = new JPanel();
        setGameMode.setBackground(Color.GRAY);
        setGameMode.setPreferredSize(new Dimension(400, 100));
        setGameMode.add(new JLabel("Chose mode"));
        setGameMode.add(hotSeat);
        setGameMode.add(multiPlayer);
        add(setGameMode, GAMEMODE_CARD);

    }

    //TODO: Petras Job (Next line is just for Testing purposes
    public void createGameView(){
        game = (JPanel) wholeGame();
        add(game, GAME_CARD);
    }

    public void createLoginView() {

        JPanel login = new JPanel();
        login.setBackground(Color.CYAN);
        login.setPreferredSize(new Dimension(400, 100));
        showCard(LOGIN_CARD);
        add(login, LOGIN_CARD);

        login.add(new JLabel("Login with your nick name:"));
        login.add(nickName);
    }

    /**
     * Set up the view in a way that is finally shown to the user.
     */
    private void createView() {
        JPanel panel = new JPanel(layout);
        setContentPane(panel);
        createSetGameModeView();
        createLoginView();
        createGameView();
    }

    /**
     * Add event listeners to all widgets wherever needed and let them execute the respective action.
     */
    private void addEventListeners() {
        multiPlayer.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showCard(LOGIN_CARD);

            }
        });

        nickName.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                controller.logInMultiplayer(nickName.getText());
            }
        });

        hotSeat.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                //TODO: pass in string array with players names
                //controller.logInHotSeat(nickNames);
            }
        });
    }

  /**
   * @return Layout from game with PlayerBoard.
   */
  private Component wholeGame(){
    JPanel game = new JPanel(new BorderLayout());

    JPanel north = new JPanel();
    JPanel east = new JPanel();
    JPanel south = (JPanel) playerBoard();
    JPanel west = (JPanel) playerBoard();
    JPanel center = new JPanel();

    north.setBackground(Color.RED);
    north.setPreferredSize(new Dimension(340, 340 ));

    east.setBackground(Color.GREEN);
    east.setPreferredSize(new Dimension(340, 340 ));

    west.setBackground(Color.orange);
    west.setPreferredSize(new Dimension(340, 340 ));

    center.setBackground(Color.yellow);
    center.setPreferredSize(new Dimension(340, 340 ));

    game.add(north, BorderLayout.NORTH);
    game.add(east, BorderLayout.EAST);
    game.add(south, BorderLayout.SOUTH);
    game.add(west, BorderLayout.WEST);
    game.add(center, BorderLayout.CENTER);



    return game;
  }

  /**
   * @return PlayerBoard with PointCounter, Name, PatternRows left and right and MouseListener.
   */
  private Component playerBoard(){
    PlayerBoard pb = new PlayerBoard();
    JPanel board = new JPanel(new BorderLayout());

    JPanel north2 = new JPanel();
    JPanel east2 = new JPanel();
    JPanel west2 = new JPanel();
    JPanel center2 = new JPanel();

    north2.setBackground(Color.RED);
    north2.add(nameAndPoints());
    east2.setBackground(Color.GREEN);
    west2.setBackground(Color.orange);
    //west2.setPreferredSize(new Dimension(340, 280 ));
    center2.setBackground(Color.yellow);

    board.setPreferredSize(new Dimension(340, 340 ));
    board.add(north2, BorderLayout.NORTH);
    board.add(east2, BorderLayout.EAST);
    board.add(west2, BorderLayout.WEST);
    board.add(pb, BorderLayout.CENTER);
    pb.addMouseListener(new MouseAdapter() {
      /**
       * {@inheritDoc}
       *
       * @param e
       */
      @Override
      public void mouseClicked(MouseEvent e) {
        super.mouseClicked(e);
        Point checkMouseTip = e.getPoint();
        int mousePointX = checkMouseTip.x / 30;
        int mousePointY = checkMouseTip.y / 30;


        if(mousePointX == 4 && mousePointY == 0){
          System.out.println("Clicked First Row");
        }
        if(mousePointX > 2 && mousePointY == 1){
          System.out.println("Clicked Second Row");
        }
        if(mousePointX > 1 && mousePointY == 2){
          System.out.println("Clicked Third Row");
        }
        if(mousePointX > 0 && mousePointY == 3){
          System.out.println("Clicked Forth Row");
        }
        if(mousePointX < 5 && mousePointY == 4){
          System.out.println("Clicked Fifth Row");
        }
        if(mousePointX<7 && mousePointY ==6){
          System.out.println("Minus Points");
        }

      }
    });

    return board;
  }

  /**
   * @return Name and User Points.
   */
  private Component nameAndPoints() {
    JLabel counter = new JLabel("Player: " + "Points: ");
    return counter;
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

