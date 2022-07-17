package de.lmu.ifi.sosylab.client.view;

import de.lmu.ifi.sosylab.client.controller.GameController;
import de.lmu.ifi.sosylab.client.model.GameModel;
import de.lmu.ifi.sosylab.client.model.events.*;
import de.lmu.ifi.sosylab.server.Game;
import de.lmu.ifi.sosylab.server.User;
import de.lmu.ifi.sosylab.shared.Tile;
import de.lmu.ifi.sosylab.shared.TileCollection;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.io.Serial;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import static java.util.Objects.requireNonNull;

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
  private static final String COUNTER_CARD = "counter";
  private static final String[] songList = {"CHILL BEAT", "RETRO CITY", "MELODIC RHYTHM"};
  private static TileCollection[] collection;
  private final int tileSize = 25;
  JLabel loginLabel;
  int counterSecond;
  String formatedCounterSecond;
  int counterMinute;
  String formatedCounterMinute;
  Integer[] numberOfPlayerOptions = {2, 3, 4};
  private final JComboBox<Integer> playerNumberSelection = new JComboBox<>(numberOfPlayerOptions);
  private transient GameModel model;
  private transient GameController controller;
  private CardLayout layout;
  private JTextField nickName;
  private JTextField firstNicknameHS;
  private JTextField secondNicknameHS;
  private JTextField thirdNicknameHS;
  private JTextField fourthNicknameHS;
  private JButton back;
  private JButton play;
  private transient List<String> playerNames;
  private JButton hotSeat;
  private JButton multiPlayer;
  private JComboBox songs;
  private transient List<User> playerList;
  private transient Game gamesettings = null;
  private int amountOfSelectedTiles;
  private int[] score;
  private String tile_color;
  private Images images;
  private JPanel middle;
  private JPanel gameField;
  private String currentPlayer;
  private int frameWidth;
  private int frameHeight;
  private String currentCard;
  private Font standardFont;
  private DecimalFormat dFormat;
  private int numberOfPayersHS;
  private Timer timer;


  /**
   * Create a new graphical view that contains all necessary elements for playing the game.
   *
   * @param model      The {@link GameModel} that handles the logic of the game.
   * @param controller The {@link GameController} that validates and forwards any user input.
   */
  public GameFrame(GameController controller, GameModel model) throws IOException {
    super("~ Azul ~");

    this.controller = requireNonNull(controller);
    this.model = requireNonNull(model);

    this.frameWidth = 450;
    this.frameHeight = 450;
    this.setPreferredSize(new Dimension(this.frameWidth, this.frameHeight));

    images = new Images();
    //Creates Game icon.
    BufferedImage icon = images.getIcon();
    this.setIconImage(icon);

    setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

    initializeWidgets();
    addEventListeners();
    createView();

    pack();
  }

  public static void playMusic(String path) {
    Thread musicThread = new Thread(() ->
    {
      try {
        AudioInputStream music = AudioSystem.getAudioInputStream(new File(path).getAbsoluteFile());
        Clip clip = AudioSystem.getClip();
        clip.open(music);
        clip.loop(Clip.LOOP_CONTINUOUSLY);
        clip.start();

      } catch (Exception e) {
        e.printStackTrace();
        JOptionPane.showMessageDialog(null, "ERROR PLAYING MUSIC !");
      }

    });
    musicThread.start();
  }

  /**
   * Returns current Frame width.
   *
   * @return frameWidth
   */
  public int getFrameWidth() {
    return this.frameWidth;
  }

  /**
   * Returns current Frame height.
   *
   * @return frameHeight
   */
  public int getFrameHeight() {
    return this.frameWidth;
  }

  public void setCurrentCard(String cardName) {
    this.currentCard = cardName;

  }

  /**
   * Instantiate all Swing widgets and specify config options where appropriate.
   */
  private void initializeWidgets() {
    // CardLayout ist nur Platzhalter
    standardFont = new Font("Arial", Font.PLAIN, 15);
    dFormat = new DecimalFormat("00");
    layout = new CardLayout();
    nickName = new JTextField(20);
    firstNicknameHS = new JTextField(20);
    secondNicknameHS = new JTextField(20);
    thirdNicknameHS = new JTextField(20);
    fourthNicknameHS = new JTextField(20);
    play = new JButton("Play");
    play.setFont(standardFont);
    hotSeat = new JButton("HOTSEAT");
    hotSeat.setFont(standardFont);
    multiPlayer = new JButton("MULTIPLAYER");
    multiPlayer.setFont(standardFont);
    songs = new JComboBox(songList);
    back = new JButton("Back");
    back.setFont(standardFont);
    loginLabel = new JLabel("Login with your nick name:");
    loginLabel.setFont(standardFont);
    counterSecond = 60;
    counterMinute = 1;
    numberOfPayersHS = 0;

    //Game
    playerNames = new ArrayList<>();

    collection = new TileCollection[10];
    gameField = new JPanel(new BorderLayout());
    gameField.setBackground(new Color(1.0f, 1.0f, 1.0f, 0.0f));
    // gameField.setPreferredSize(createGameField().getPreferredSize());
  }

  /**
   * Creates Card to set Game Mode. (Hot Seat or Multiplayer)
   */
  public void createSetGameModeView() {

    JPanel setGameMode = new JPanel(new BorderLayout());
    BufferedImage img = images.getBackgroundSetGameMode();
    JLabel background = new JLabel(new ImageIcon(img));
    background.setLayout(new FlowLayout());

    //setGameMode.add(new JLabel("Choose mode"));
    JPanel south = new JPanel();
    south.add(hotSeat);
    south.add(multiPlayer);
    south.add(songs);
    setGameMode.add(south, BorderLayout.SOUTH);
    add(setGameMode, GAMEMODE_CARD);

    setGameMode.add(background);

  }

  /**
   * Creates Card for Game View.
   */
  public void createGameView() {
    JPanel game = new JPanel();
    add(game, GAME_CARD);

    BufferedImage img = images.getBackground();
    JLabel background = new JLabel(new ImageIcon(img));


    game.setPreferredSize(createGameField().getPreferredSize());
    background.setLayout(new FlowLayout());

    gameField = new JPanel(new BorderLayout());
    gameField.setBackground(new Color(1.0f, 1.0f, 1.0f, 0.0f));

    background.add(gameField);

    game.add(background);
  }

  /**
   * Creates Card for Multiplayer Login.
   */
  public void createMultiplayerLoginView() {

    JPanel login = new JPanel(new BorderLayout());
    login.setPreferredSize(new Dimension(200, 100));
    JPanel south = new JPanel(new GridLayout(1, 2));
    south.add(play);
    south.add(back);
    login.add(south, BorderLayout.SOUTH);
    JPanel center = new JPanel();
    center.add(loginLabel);
    center.add(nickName);
    login.add(center);
    add(login, LOGIN_M_CARD);
  }

  private void waitForEnoughPlayers() {
    JPanel waitMultiPlayer = new JPanel();

    JLabel counter = new JLabel();
    counter.setFont(standardFont);
    JLabel waitingLabel = new JLabel("Waiting for other Players to join.");
    waitingLabel.setFont(standardFont);
    waitMultiPlayer.add(waitingLabel);
    waitMultiPlayer.add(counter);
    timer = new Timer(1000, new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {

        counterMinute = 0;
        counterSecond--;
        formatedCounterSecond = dFormat.format(counterSecond);
        formatedCounterMinute = dFormat.format(counterMinute);
        counter.setText(formatedCounterMinute + ":" + formatedCounterSecond);

        counter.setText(formatedCounterMinute + " : " + formatedCounterSecond);
      }

    });

    timer.start();
    add(waitMultiPlayer, COUNTER_CARD);


  }

  /**
   * Creates Card for Hot Seat Login.
   */
  public void createHotSeatLoginView() {

    JPanel login = new JPanel();
    login.setBackground(Color.CYAN);
    login.setPreferredSize(new Dimension(400, 100));
    add(login, LOGIN_H_CARD);

    JLabel howMany = new JLabel("How many Players would you like to play with?");
    howMany.setFont(standardFont);
    login.add(howMany);
    playerNumberSelection.setFont(standardFont);
    login.add(playerNumberSelection);
    login.add(back);


  }

  /**
   * Creates Card to enter nichnames in Hot Seat Mode.
   */

  public void setPlayerNicknamesHS(int numberOfPlayers) {

    JPanel loginNames = new JPanel();
    loginNames.setLayout(new BorderLayout());
    JPanel east = new JPanel();
    east.setLayout(new GridLayout(numberOfPlayers, 1));
    JPanel south = new JPanel();
    south.setLayout(new GridLayout(1, 2));
    JPanel center = new JPanel();
    center.setBackground(Color.CYAN);
    south.setBackground(Color.CYAN);
    south.add(play);
    south.add(back);
    loginNames.setPreferredSize(new Dimension(400, 100));
    add(loginNames, "loginNames");
    layout.show(getContentPane(), "loginNames");

    JLabel playerOne = new JLabel("Player " + "1" + ": Login with your nick name:");
    playerOne.setFont(standardFont);
    JLabel playerTwo = new JLabel("Player " + "2" + ": Login with your nick name:");
    playerTwo.setFont(standardFont);
    JLabel playerThree = new JLabel("Player " + "3" + ": Login with your nick name:");
    playerThree.setFont(standardFont);
    JLabel playerFour = new JLabel("Player " + "4" + ": Login with your nick name:");
    playerFour.setFont(standardFont);


    switch (numberOfPlayers) {
      case 2:
        center.add(playerOne);
        center.add(firstNicknameHS);

        center.add(playerTwo);
        center.add(secondNicknameHS);

        break;

      case 3:
        center.add(playerOne);
        center.add(firstNicknameHS);

        center.add(playerTwo);
        center.add(secondNicknameHS);

        center.add(playerThree);
        center.add(thirdNicknameHS);

        break;

      case 4:
        center.add(playerOne);
        center.add(firstNicknameHS);

        center.add(playerTwo);
        center.add(secondNicknameHS);

        center.add(playerThree);
        center.add(thirdNicknameHS);

        center.add(playerFour);
        center.add(fourthNicknameHS);

        break;
      default:
        break;
    }

    loginNames.add(center, BorderLayout.CENTER);
    loginNames.add(south, BorderLayout.SOUTH);


  }

  /**
   * Set up the view in a way that is finally shown to the user.
   */
  private void createView() throws IOException {
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


    hotSeat.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        showCard(LOGIN_H_CARD);
        try {
          controller.setGameMode("Hot Seat");
        } catch (IOException ex) {
          throw new RuntimeException(ex);
        }
      }
    });

    songs.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        String selectedSong = songs.getSelectedItem().toString();
        switch (selectedSong) {
          case "CHILL BEAT":
            playMusic("src/main/java/de/lmu/ifi/sosylab/client/view/songs/chillbeat.wav");
            break;
          case "MELODIC RHYTHM":
            playMusic("src/main/java/de/lmu/ifi/sosylab/client/view/songs/melodicrhythm.wav");
            break;
          case "RETRO CITY":
            playMusic("src/main/java/de/lmu/ifi/sosylab/client/view/songs/retrocity.wav");
            break;
        }
        songs.setEnabled(false); //TODO: entfernen wenn songWechseln(...) inplementiert wurde
      }
    });

    playerNumberSelection.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        numberOfPayersHS = (int) playerNumberSelection.getSelectedItem();
        setPlayerNicknamesHS(numberOfPayersHS);
      }
    });


    play.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {

        if (model.getGameMode().equals("Multiplayer")) {
          controller.logInMultiplayer(nickName.getText());
        } else if (model.getGameMode().equals("Hot seat")) {

          if (numberOfPayersHS == 2) {
            playerNames.add(firstNicknameHS.getText());
            playerNames.add(secondNicknameHS.getText());

          } else if (numberOfPayersHS == 3) {
            playerNames.add(firstNicknameHS.getText());
            playerNames.add(secondNicknameHS.getText());
            playerNames.add(thirdNicknameHS.getText());

          } else if (numberOfPayersHS == 4) {
            playerNames.add(firstNicknameHS.getText());
            playerNames.add(secondNicknameHS.getText());
            playerNames.add(thirdNicknameHS.getText());
            playerNames.add(fourthNicknameHS.getText());
          }
          controller.logInHotSeat(playerNames);
        }
      }
    });

    back.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        goBackOneCard();
      }
    });
  }

  private Component createGameField() {
    switch (playerNames.size() - 1) {
      case 1 -> {
        gameField.add(createBoard(0), BorderLayout.NORTH);
        gameField.add(createBoard(1), BorderLayout.SOUTH);
        this.setSize(350, 900);
      }
      case 2 -> {
        gameField.add(createBoard(0), BorderLayout.NORTH);
        gameField.add(createBoard(1), BorderLayout.WEST);
        gameField.add(createBoard(2), BorderLayout.SOUTH);
        this.setSize(700, 920);
      }
      case 3 -> {
        gameField.add(createBoard(0), BorderLayout.NORTH);
        gameField.add(createBoard(1), BorderLayout.SOUTH);
        gameField.add(createBoard(2), BorderLayout.WEST);
        gameField.add(createBoard(3), BorderLayout.EAST);
        this.setSize(1020, 880);
      }
    }
    gameField.add(createMiddle(), BorderLayout.CENTER);
    return gameField;
  }

  /**
   * Creates Middle with plates and pile.
   *
   * @return -middle.
   */
  private Component createMiddle() {
    middle = new JPanel(new FlowLayout());
    middle.setBackground(new Color(1.0f, 1.0f, 1.0f, 0.0f));
    middle.setPreferredSize(new Dimension(325, 300));

    createPlates(middle);
    middle.add(createPile());
    return middle;
  }

  /**
   * Creates the Plates in the middle with the tiles and adds a MouseListener.
   *
   * @param middle - Plates can be directly added to the middle.
   */
  private void createPlates(JPanel middle) {
    try {
      for (int plateNumber = 1; plateNumber < collection.length; plateNumber++) {
        BufferedImage img = images.getPlate();

        JLabel plate = new JLabel(new ImageIcon(img));
        plate.setName(String.valueOf(plateNumber));

        int x = 1;
        int y = 1;

        for (int i = 0; i < collection[plateNumber].size(); i++) {
          String color = String.valueOf(collection[plateNumber].get(i));
          PaintTile tile = new PaintTile(color, images);
          tile.setName(color);
          if (i == 2) {
            x = 1;
            y = 3;
          }
          tile.setBounds(12 * x, 12 * y, 25, 25);
          x += 2;
          plate.add(tile);
        }

        plate.addMouseListener(new MouseAdapter() {
          /**
           * {@inheritDoc}
           *
           * @param e
           */
          @Override
          public void mouseClicked(MouseEvent e) {
            super.mouseClicked(e);
            Point checkMouseTip = e.getPoint();
            currentPlayer = controller.getCurrentPlayer();

            String plates = plate.getComponentAt(checkMouseTip).getParent().getName();
            tile_color = plate.getComponentAt(checkMouseTip).getName();
            if (plates != null && tile_color != null) {
              System.out.println("Plate: " + plates + " Tile: " + tile_color);
              int plateNumber = Integer.parseInt(plates);
              amountOfSelectedTiles = collection[plateNumber].getAmountTilesOfColor(Tile.getTile(tile_color));
              System.out.println("This amount is " + amountOfSelectedTiles);
              boolean confirmation = confirmTileSelection(plateNumber, tile_color, amountOfSelectedTiles, currentPlayer);
              if (confirmation) {
                //TODO: next line is thowing an exception
                controller.selectAllTiles(plateNumber, tile_color);
              }
            }
          }
        });
        middle.add(plate);
      }
    } catch (NullPointerException e) {
      System.out.println("collection ist noch leer! (createPlates)");
    }

  }

  private boolean confirmTileSelection(int plateNumber, String tile, int amount, String playerName) {

    int selection = JOptionPane.showConfirmDialog(null,
            playerName + ": Are you sure you want to select the " + amount + " " + tile + " tile(s) from plate " + plateNumber,
            "Tile Selection", JOptionPane.YES_NO_OPTION);

    if (selection == 0) {
      return true;
    }
    return false;


  }

  /**
   * Creates tile pile in the Middle.
   *
   * @return - pile.
   */
  private Component createPile() {
    JPanel pile = new JPanel();
    pile.setBackground(new Color(1.0f, 1.0f, 1.0f, 0.0f));
    pile.setPreferredSize(new Dimension(325, 100));
    try {
      for (int i = 0; i < collection[0].size(); i++) {
        String color = String.valueOf(collection[0].get(i));
        PaintTile tile = new PaintTile(color, images);
        tile.setName(color);
        tile.setBackground(new Color(1.0f, 1.0f, 1.0f, 0.0f));
        pile.add(tile);
      }
      pile.addMouseListener(new MouseAdapter() {
        /**
         * {@inheritDoc}
         *
         * @param e
         */
        @Override
        public void mouseClicked(MouseEvent e) {
          super.mouseClicked(e);
          Point checkMouseTip = e.getPoint();
          tile_color = pile.getComponentAt(checkMouseTip).getName();
          if (tile_color != null) {
            System.out.println("Tile " + tile_color + " was clicked on Plate 0");
            amountOfSelectedTiles = collection[0].getAmountTilesOfColor(Tile.getTile(tile_color));
            controller.selectAllTiles(0, tile_color);
          }

        }
      });
    } catch (NullPointerException e) {
      System.out.println("Collection ist noch leer! (createPile)");
    }
    return pile;
  }

  /**
   * Creates a Board with MouseListener for the Rows.
   *
   * @param boardNumber -
   * @return -board.
   */
  private Component createBoard(int boardNumber) {
    Board b = new Board(controller, tileSize, playerNames.get(boardNumber), images, score, boardNumber);

    JPanel board = new JPanel();
    board.setBackground(new Color(1.0f, 1.0f, 1.0f, 0.0f));

    if (playerNames.size() - 1 == 2) {
      board.setLayout(new BorderLayout());
      JPanel west = new JPanel();
      west.setPreferredSize(new Dimension(20, 300));
      west.setBackground(new Color(1.0f, 1.0f, 1.0f, 0.0f));
      board.add(west, BorderLayout.WEST);
    }

    board.add(b);
    b.addMouseListener(new MouseAdapter() {
      /**
       * {@inheritDoc}
       *
       * @param e
       */
      @Override
      public void mouseClicked(MouseEvent e) {
        super.mouseClicked(e);
        Point checkMouseTip = e.getPoint();
        int mousePointX = checkMouseTip.x / tileSize;
        int mousePointY = checkMouseTip.y / tileSize;
        currentPlayer = controller.getCurrentPlayer();

        if (playerNames.get(boardNumber).equals(controller.getCurrentPlayer())) {

          if (mousePointX == 5 && mousePointY == 2) {
            controller.placeTiles(amountOfSelectedTiles, 0);
            System.out.println("Clicked First Row");
          }
          if (mousePointX > 3 && mousePointX < 6 && mousePointY == 3) {
            System.out.println("Clicked Second Row");
            controller.placeTiles(amountOfSelectedTiles, 1);
          }
          if (mousePointX > 2 && mousePointX < 6 && mousePointY == 4) {
            System.out.println("Clicked Third Row");
            controller.placeTiles(amountOfSelectedTiles, 2);
          }
          if (mousePointX > 1 && mousePointX < 6 && mousePointY == 5) {
            System.out.println("Clicked Forth Row");
            controller.placeTiles(amountOfSelectedTiles, 3);
          }
          if (mousePointX > 0 && mousePointX < 6 && mousePointY == 6) {
            System.out.println("Clicked Fifth Row");
            controller.placeTiles(amountOfSelectedTiles, 4);
          }
          if (mousePointX > 0 && mousePointX < 8 && mousePointY == 8) {
            System.out.println("Minus Points");
            controller.placeTiles(amountOfSelectedTiles, 5);
          }
        }
      }
    });

    return board;
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
      int numberOfActivePlayers = model.getNumberOfPlayers();
      System.out.println("Number of active players provided from the model are: " + numberOfActivePlayers);
      System.out.println("Game mode is:" + model.getGameMode());
      if (model.getGameMode().equals("Multiplayer")) {

        //int numberOfActivePlayers = model.getNumberOfPlayers();
        System.out.println("Number of active players provided from the model are: " + numberOfActivePlayers);

        if (numberOfActivePlayers == 2 || numberOfActivePlayers == 3 || numberOfActivePlayers == 4) {
          timer.stop();
          showGame();

        } else {
          waitForEnoughPlayers();
          showCard(COUNTER_CARD);
        }

      } else if (model.getGameMode().equals("Hot Seat")) {
        showGame();
      }
    } else if (newValue instanceof LoginFailedEvent) {
      JOptionPane.showMessageDialog(this,
              String.format("Login failed, name \"%s\" is already in use.", nickName.getText()));
      showCard(LOGIN_M_CARD);

    } else if (newValue instanceof MiddleTilesUpdateEvent) {
      collection = controller.getTilePlates();
      gameField.removeAll();
      createGameView();
      repaint();
    } else if (newValue instanceof OtherPlayerPlacedTilesEvent) {
      gameField.removeAll();
      createGameView();
      repaint();

    } else if (newValue instanceof OtherPlayerSelectedTilesEvent) {
      gameField.removeAll();
      createGameView();
      repaint();

    } else if (newValue instanceof TilesAddedEvent) {
      gameField.removeAll();
      createGameView();
      repaint();

    } else if (newValue instanceof TilesSelectedEvent) {


    } else if (newValue instanceof TilePlacementFailedEvent) {

    } else if (newValue instanceof UserJoinedEvent) {
      int numberOfActivePlayers = model.getNumberOfPlayers();
      System.out.println("Number of active players provided from the model are: " + numberOfActivePlayers);

      if (numberOfActivePlayers > 1 && numberOfActivePlayers < 5) {
        timer.stop();
        showGame();

      } else {
        waitForEnoughPlayers();
        showCard(COUNTER_CARD);
      }


    } else if (newValue instanceof UserLeftEvent) {
      //TODO: was soll hier genau passieren?

    } else if (newValue instanceof PointsUpdatedEvent) {
      score = ((PointsUpdatedEvent) newValue).getPoints();

    } else if (newValue instanceof BoardUpdatedEvent) {

    } else if (newValue instanceof GameCanceledEvent) {

    } else if (newValue instanceof GameEndedEvent) {

    } else if (newValue instanceof GameRestartedEvent) {

    } else if (newValue instanceof GameRestartRequestEvent) {

    } else if (newValue instanceof NextPlayerEvent) {

    } else if (newValue instanceof TimerEvent) {

    }
  }

  /**
   * Show the login view to the user.
   */
  private void showLoginMultiplayer() {
    showCard(LOGIN_M_CARD);
    setCurrentCard(GAME_CARD);
  }

  private void showLoginHotseat() {
    showCard(LOGIN_H_CARD);
    setCurrentCard(LOGIN_M_CARD);
  }

  /**
   * Show the chat view to the user.
   */
  private void showGame() {
    showCard(GAME_CARD);
    setCurrentCard(LOGIN_M_CARD);
  }

  private void showCard(String card) {
    layout.show(getContentPane(), card);
    setCurrentCard(card);
  }

  private void goBackOneCard() {
    if (this.currentCard.equals(LOGIN_H_CARD) || this.currentCard.equals(LOGIN_M_CARD)) {
      showCard(GAMEMODE_CARD);
    }

  }
}

