package de.lmu.ifi.sosylab.client.view;

import static java.util.Objects.requireNonNull;

import de.lmu.ifi.sosylab.client.controller.GameController;
import de.lmu.ifi.sosylab.client.model.GameModel;
import de.lmu.ifi.sosylab.client.model.events.*;
import de.lmu.ifi.sosylab.server.Game;
import de.lmu.ifi.sosylab.server.User;
import de.lmu.ifi.sosylab.shared.Tile;
import de.lmu.ifi.sosylab.shared.TileCollection;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
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
    private JTextField firstNicknameHS;
    private JTextField secondNicknameHS;
    private JTextField thirdNicknameHS;
    private JTextField fourthNicknameHS;


    private JButton loginHS;
    Integer[] numberOfPlayerOptions = {2, 3, 4};

    private final JComboBox<Integer> playerNumberSelection = new JComboBox<>(numberOfPlayerOptions);
    private transient List<String> playerNames;
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
        //nicknameHS = new JTextField(50);
        firstNicknameHS = new JTextField(20);
        secondNicknameHS = new JTextField(20);
        thirdNicknameHS = new JTextField(20);
        fourthNicknameHS = new JTextField(20);
        loginHS = new JButton("Play");
        hotSeat = new JButton("HOTSEAT");
        multiPlayer = new JButton("MULTIPLAYER");

    }

    /**
     * Creates Card to set Game Mode. (Hot Seat or Multiplayer)
     */
    public void createSetGameModeView() {

        JPanel setGameMode = new JPanel();
        setGameMode.setBackground(Color.GRAY);
        setGameMode.setPreferredSize(new Dimension(400, 100));
        setGameMode.add(new JLabel("Choose mode"));
        setGameMode.add(hotSeat);
        setGameMode.add(multiPlayer);
        add(setGameMode, GAMEMODE_CARD);

    }

    /**
     * Creates Card for Game View.
     */
    public void createGameView() {
        game = (JPanel) wholeGame();
        add(game, GAME_CARD);
    }

    /**
     * Creates Card for Multiplayer Login.
     */
    public void createMultiplayerLoginView() {

        JPanel login = new JPanel();
        login.setBackground(Color.CYAN);
        login.setPreferredSize(new Dimension(400, 100));
        login.add(new JLabel("Login with your nick name:"));
        login.add(nickName);
        add(login, LOGIN_M_CARD);
    }

    /**
     * Creates Card for Hot Seat Login.
     */
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

        loginHS.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                controller.logInHotSeat(playerNames);
            }
        });

    }

    /**
     * Creates Card to enter nichnames in Hot Seat Mode.
     */

    public void setPlayerNicknamesHS(int numberOfPlayers) {

        //GridLayout gridLayout = new GridLayout(2,2);
        JPanel loginNames = new JPanel();
        loginNames.setBackground(Color.CYAN);
        loginNames.setPreferredSize(new Dimension(400, 100));
        add(loginNames, "loginNames");
        layout.show(getContentPane(), "loginNames");


        switch (numberOfPlayers) {
            case 2:
                loginNames.add(new JLabel("Player " + "1" + ": Login with your nick name:"));
                loginNames.add(firstNicknameHS);

                loginNames.add(new JLabel("Player " + "2" + ": Login with your nick name:"));
                loginNames.add(secondNicknameHS);

                break;

            case 3:
                loginNames.add(new JLabel("Player " + "1" + ": Login with your nick name:"));
                loginNames.add(firstNicknameHS);

                loginNames.add(new JLabel("Player " + "2" + ": Login with your nick name:"));
                loginNames.add(secondNicknameHS);

                loginNames.add(new JLabel("Player " + "3" + ": Login with your nick name:"));
                loginNames.add(thirdNicknameHS);

                break;

            case 4:
                loginNames.add(new JLabel("Player " + "1" + ": Login with your nick name:"));
                loginNames.add(firstNicknameHS);

                loginNames.add(new JLabel("Player " + "2" + ": Login with your nick name:"));
                loginNames.add(secondNicknameHS);

                loginNames.add(new JLabel("Player " + "3" + ": Login with your nick name:"));
                loginNames.add(thirdNicknameHS);

                loginNames.add(new JLabel("Player " + "4" + ": Login with your nick name:"));
                loginNames.add(fourthNicknameHS);

                break;
        }

        loginNames.add(loginHS);

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
                try {
                    controller.setGameMode("Multiplayer");
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            }
        });

        nickName.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                controller.logInMultiplayer(nickName.getText());
                System.out.println("sending log in");
            }
        });

        hotSeat.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                //TODO: pass in string array with players names
                showCard(LOGIN_H_CARD);
                try {
                    controller.setGameMode("Hot Seat");
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            }
        });

        firstNicknameHS.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                playerNames.add(firstNicknameHS.getText());
            }
        });

        secondNicknameHS.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                playerNames.add(secondNicknameHS.getText());
            }
        });

        thirdNicknameHS.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                playerNames.add(thirdNicknameHS.getText());
            }
        });

        fourthNicknameHS.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                playerNames.add(fourthNicknameHS.getText());
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
        center.setBackground(Color.getHSBColor(130, 189, 231));
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
        plate.setBackground(Color.getHSBColor(130, 189, 231));

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
        JPanel pile = new JPanel();
        pile.setBackground(Color.getHSBColor(130, 189, 231));
        pile.setPreferredSize(new Dimension(300, 300));
        //TODO Liste erstellen und updaten mit allen Tiles im Haufen.
        //Dann mit for alle abarbeiten. Farbe von Tile aufrufen.
        //Selber Ablauf dann mit den MouseListeners
        for (int i = 0; i < 1; i++) {
            JPanel tile = new Pile(0, "BLACK", tileSize);
            tile.setPreferredSize(new Dimension(tileSize, tileSize));
            tile.setBackground(Color.getHSBColor(130, 189, 231));
            pile.add(tile);
        }
        middle.add(pile);
    }

    /**
     * When MiddleTilesUpdateEvent is fired, gets Information from TileCollection[] (TileCollection[0] is the middle,
     * at the beginning it only Contains the Starting Marker, TileCollection[1] onwards are the Plates)
     * and gives information to Method ... that fills the Plates.
     *
     * @param tileCollection - The Array of TileCollections that where provided by the Server.
     *                      (Each collection is a plate) and need to be placed in the Middle.
     */
    private void setTilesInMiddle(TileCollection[] tileCollection){
        for (int i = 0; i < tileCollection.length; i++){

           int plateNumber = i;

           // Get colors that are contained in Plate number i.
           ArrayList<Tile> tileColors = tileCollection[i].getContainedColors();

           /* for(int j = 0; j < tileColors.size(); j++){
                Tile tileWithSpecificColour = tileColors.get(j);
                String colour = tileWithSpecificColour.toString();
                //test
                //System.out.println("Numer of colors" + tileColors.size());
                //System.out.println("Plate Number: " + i + " contains these colors: ");
                //System.out.println(colour);
                int amountOfTiles = tileCollection[i].getAmountTilesOfColor(tileWithSpecificColour);

                fillPlateWithTiles(plateNumber, colour, amountOfTiles);

            }*/
        }

    }
    //TODO: Petra mit dieser Mehtode kannst du die Plättchen in der Mitte füllen und updaten. Diese ethode wird ein mal pro Plättchen aufgerufen.
    public void fillPlateWithTiles(int plateNumber, String colour, int amountOfTiles){

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
        } else if (newValue instanceof MiddleTilesUpdateEvent) {
            TileCollection[] tileCollection = model.getTilePlates();
            setTilesInMiddle(tileCollection);


        } else if (newValue instanceof OtherPlayerPlacedTilesEvent) {

        } else if (newValue instanceof OtherPlayerSelectedTilesEvent) {

        } else if (newValue instanceof TilesAddedEvent) {

        } else if (newValue instanceof TilesSelectedEvent) {

        } else if (newValue instanceof UserJoinedEvent) {

        } else if (newValue instanceof UserLeftEvent) {

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

