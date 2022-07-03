package de.lmu.ifi.sosylab.client.view;

import static java.util.Objects.requireNonNull;

import de.lmu.ifi.sosylab.client.controller.GameController;
import de.lmu.ifi.sosylab.client.model.GameModel;
import de.lmu.ifi.sosylab.client.model.events.LoggedInEvent;
import de.lmu.ifi.sosylab.client.model.events.LoginFailedEvent;
import de.lmu.ifi.sosylab.server.Game;
import de.lmu.ifi.sosylab.server.User;
import de.lmu.ifi.sosylab.shared.TileCollection;
import org.w3c.dom.ls.LSOutput;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.Serial;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;

/**
 * The main view of the chat user interface. It provides and connects all graphical elements
 * that are necessary for a chat application. It provides a user a screen for logging in, and
 * in case of success shows afterwards the necessary elements for playing the game.
 */
public class GameFrame extends JFrame implements PropertyChangeListener {

    @Serial
    private static final long serialVersionUID = 1L;
    private static final String LOGIN_M_CARD = "loginMultiplayer";
    private static final String LOGIN_H_CARD = "loginHotseat";

    private static final String GAME_CARD = "game";
    private static final String GAMEMODE_CARD = "gameMode";
    private final int tileSize = 27;
    private transient GameModel model;
    private transient GameController controller;
    private CardLayout layout;
    private JTextField nickName;
    private JTextField nicknameHS;
    Integer[] numberOfPlayerOptions = {2, 3, 4};

    private final JComboBox<Integer> playerNumberSelection = new JComboBox<>(numberOfPlayerOptions);
    private List<String> playerNames;
    private JButton hotSeat;
    private JButton multiPlayer;
    private JPanel game;
    private JPanel middle = new JPanel();
    private transient List<User> playerList;
    private transient List<PlayerBoard> boardList;

    private transient Game gamesettings = null;


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
        // playerList = controller.getUserList(); // TODO get correct usercount.
        playerNames = new ArrayList<>();

        boardList = new ArrayList<>(4);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        this.setPreferredSize(new Dimension(400, 300));

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
        nicknameHS = new JTextField(50);
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
    public void createGameView() {
        game = (JPanel) wholeGame();
        add(game, GAME_CARD);
    }

    public void createMultiplayerLoginView() {

        JPanel login = new JPanel();
        login.setBackground(Color.CYAN);
        login.setPreferredSize(new Dimension(400, 100));
        login.add(new JLabel("Login with your nick name:"));
        login.add(nickName);
        add(login, LOGIN_M_CARD);
    }

    public void createHotSeatLoginView() {

        JPanel login = new JPanel();
        login.setBackground(Color.CYAN);
        login.setPreferredSize(new Dimension(400, 100));
        add(login, LOGIN_H_CARD);

        login.add(new JLabel("How many Players would you like to play with?"));
        login.add(playerNumberSelection);

        playerNumberSelection.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setPlayerNicknamesHS((int) playerNumberSelection.getSelectedItem());
            }
        });

    }

    public void setPlayerNicknamesHS(int numberOfPlayers) {

        JPanel loginNames = new JPanel();
        loginNames.setBackground(Color.CYAN);
        loginNames.setPreferredSize(new Dimension(400, 100));
        add(loginNames, "loginNames");
        layout.show(getContentPane(), "loginNames");

        for (int i = 0; i < numberOfPlayers; i++) {

            loginNames.add(new JLabel("Player " + (i + 1) + ": Login with your nick name:"));
            nicknameHS = new JTextField(20);
            loginNames.add(nicknameHS);

        }

    }

    /**
     * Set up the view in a way that is finally shown to the user.
     */
    private void createView() {
        JPanel panel = new JPanel(layout);
        setContentPane(panel);
        createSetGameModeView();
        createMultiplayerLoginView();
        createHotSeatLoginView();
        createGameView();
    }

    /**
     * Add event listeners to all widgets wherever needed and let them execute the respective action.
     */
    private void addEventListeners() {
        multiPlayer.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showCard(LOGIN_M_CARD);
                controller.setGameMode("Multiplayer");
            }
        });

        nickName.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                model.logInMultiplayer(nickName.getText());
                controller.logInMultiplayer(nickName.getText());
                System.out.println("sending log in");
            }
        });

        hotSeat.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                //TODO: pass in string array with players names
                showCard(LOGIN_H_CARD);
                controller.setGameMode("Hot Seat");
            }
        });

        nicknameHS.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                playerNames.add(nicknameHS.getText());
                controller.logInHotSeat(playerNames);
            }
        });
    }

    /**
     * @return Layout from game with PlayerBoard.
     */
    private Component wholeGame() {
        int playerBoard = 0;
        JPanel game = new JPanel(new BorderLayout());

        JPanel north = (JPanel) playerBoard(playerBoard);
        north.setBackground(Color.yellow);
        north.setPreferredSize(new Dimension(340, 250));
        game.add(north, BorderLayout.NORTH);
        playerBoard++;

        JPanel south = (JPanel) playerBoard(playerBoard);
        south.setBackground(Color.GREEN);
        game.add(south, BorderLayout.SOUTH);
        playerBoard++;


        try {
            if (playerNames.size() == 2) {
                JPanel west = (JPanel) playerBoard(playerBoard);
                west.setBackground(Color.BLUE);
                playerBoard++;
                west.setPreferredSize(new Dimension(340, 340));
                game.add(west, BorderLayout.WEST);
            }

            if (playerNames.size() == 3) {
                JPanel west = (JPanel) playerBoard(playerBoard);
                west.setPreferredSize(new Dimension(340, 340));
                west.setBackground(Color.cyan);
                game.add(west, BorderLayout.WEST);
                playerBoard++;

                JPanel east = (JPanel) playerBoard(playerBoard);
                east.setBackground(Color.magenta);
                east.setPreferredSize(new Dimension(340, 340));
                game.add(east, BorderLayout.EAST);
            }


            createPlates(5);
            createPile();
        } catch (NullPointerException e) {
            System.out.println("User Liste ist noch leer!");
        }

        JPanel center = middle;
        center.setPreferredSize(new Dimension(600, 340));
        center.setBackground(Color.PINK);
        game.add(center, BorderLayout.CENTER);

        return game;
    }

    /**
     * @return PlayerBoard with PointCounter, Name, PatternRows left and right and MouseListener.
     */
    private Component playerBoard(int playerBoard) {
        PlayerBoard pb = new PlayerBoard(playerBoard, tileSize, controller);
        boardList.add(pb);
        JPanel board = new JPanel(new BorderLayout());

        JPanel north = new JPanel();
        north.add(nameAndPoints(playerBoard));


        try {
            JPanel west = new JPanel();
            if ((playerBoard == 0 || playerBoard == 1) && playerNames.size() > 2) {
                west.setPreferredSize(new Dimension(390, 200));
            }

            board.setPreferredSize(new Dimension(340, 250));
            board.add(north, BorderLayout.NORTH);
            board.add(west, BorderLayout.WEST);
            board.add(pb, BorderLayout.CENTER);

        } catch (NullPointerException e) {
            System.out.println("User List is empty!");
        }

        pb.addMouseListener(new MouseAdapter() {
            /**
             * {@inheritDoc}
             *
             * @param e
             */
            @Override
            public void mouseClicked(MouseEvent e) {
                Point checkMouseTip = e.getPoint();
                int mousePointX = checkMouseTip.x / tileSize;
                int mousePointY = checkMouseTip.y / tileSize;


                if (mousePointX == 4 && mousePointY == 0) {
                    System.out.println("Clicked First Row");
                }
                if (mousePointX > 2 && mousePointX < 5 && mousePointY == 1) {
                    System.out.println("Clicked Second Row");
                }
                if (mousePointX > 1 && mousePointX < 5 && mousePointY == 2) {
                    System.out.println("Clicked Third Row");
                }
                if (mousePointX > 0 && mousePointX < 5 && mousePointY == 3) {
                    System.out.println("Clicked Forth Row");
                }
                if (mousePointX < 5 && mousePointY == 4) {
                    System.out.println("Clicked Fifth Row");
                }
                if (mousePointX < 7 && mousePointY == 6) {
                    System.out.println("Minus Points");
                }

            }
        });

        return board;
    }

    /**
     * @return Name and User Points.
     */
    private Component nameAndPoints(int userNumber) {
        JLabel counter;
        if (playerList != null) {
            counter = new JLabel("Player: " + playerList.get(userNumber).getName() + " Points: " +
                    controller.getCurrentScore(userNumber));
            return counter;
        }

        counter = new JLabel("There are no active Players");
        return counter;

    }

    /**
     * Creates all the Plates for the middle.
     *
     * @param plateNumber - Number of plates.
     */
    private void createPlates(int plateNumber) {
        for (int i = 0; i < plateNumber; i++) {
            middle.add(createPlate(i));
        }
    }

    /**
     * Creates a single Plate with Tiles.
     * Adds Mouselistener so that Tiles can be clicked.
     *
     * @param plateNumber - Platenumber so that the model knows wich plate was clicked.
     * @return - plate.
     */
    private Component createPlate(int plateNumber) {
        JPanel plate = new Plate(controller);
        plate.setPreferredSize(new Dimension(100, 120));

        plate.addMouseListener(new MouseAdapter() {
            /**
             * {@inheritDoc}
             *
             * @param e
             */
            @Override
            public void mouseClicked(MouseEvent e) {
                Point checkMouseTip = e.getPoint();
                int mousePointX = checkMouseTip.x / 20;
                int mousePointY = checkMouseTip.y / 20;


                if (mousePointX == 1 && mousePointY == 1) {
                    System.out.println("Plate: " + plateNumber + " Tile 1");
                    controller.selectAllTilesWithColor(
                            controller.getTile(plateNumber, 0)); //TODO Farbe auswählen
                }
                if (mousePointX == 2 && mousePointY == 1) {
                    System.out.println("Plate: " + plateNumber + " Tile 2");
                    controller.selectAllTilesWithColor(controller.getTile(plateNumber, 1));
                }
                if (mousePointX == 1 && mousePointY == 3) {
                    System.out.println("Plate: " + plateNumber + " Tile 3");
                    controller.selectAllTilesWithColor(controller.getTile(plateNumber, 2));
                }
                if (mousePointX == 2 && mousePointY == 3) {
                    System.out.println("Plate: " + plateNumber + " Tile 4");
                    controller.selectAllTilesWithColor(controller.getTile(plateNumber, 3));
                }
                if (mousePointX == 3 && mousePointY == 3) {
                    System.out.println("Plate: " + plateNumber + " Tile 5");
                    controller.selectAllTilesWithColor(controller.getTile(plateNumber, 4));
                }
            }
        });

        return plate;
    }

    private void createPile() {
        Pile pile = new Pile(controller);
        middle.add(pile);

        // Für Mouselistener.
        // JFrame Tile mit Listener und Farbe machen.
        // Checken ob dort Tile liegt. Falls Nein nichts, falls Ja Tile auswählen.
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
            showCard(LOGIN_M_CARD);
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
    private void showLoginMultiplayer() {
        showCard(LOGIN_M_CARD);
    }

    private void showLoginHotseat() {
        showCard(LOGIN_H_CARD);
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

