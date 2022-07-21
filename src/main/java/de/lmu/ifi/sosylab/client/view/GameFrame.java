package de.lmu.ifi.sosylab.client.view;

import de.lmu.ifi.sosylab.client.controller.GameController;
import de.lmu.ifi.sosylab.client.model.GameModel;
import de.lmu.ifi.sosylab.client.model.Player;
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
import java.awt.event.*;
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
 * The main view of the game user interface. It provides and connects all graphical elements
 * that are necessary for a game application. It provides a user a screen for logging in, and
 * in case of success shows afterwards the necessary elements for playing the game.
 */
public class GameFrame extends JFrame implements PropertyChangeListener {

  @Serial
  private static final long serialVersionUID = 1L;
  private static final String LOGIN_M_CARD = "loginMultiplayer";
  private static final String LOGIN_H_CARD = "loginHotseat";

  private static final String GAME_CARD = "game";
  private static final String GAMEMODE_CARD = "gameMode";
  private static final String TIMER_CARD = "timer";
  private static final String TIMERUPDATE_CARD = "resartedTimer";
  private static final String LOGINNAMES_CARD = "loginNames";
  private static final String WAIT_CARD = "wait";
  private static final String[] songList = {"CHILL BEAT", "RETRO CITY", "MELODIC RHYTHM"};
  private static TileCollection[] collection;
  private int tileSize = 25;
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
  private JComboBox<String> songs;
  private transient List<User> playerList;
  private transient Game gamesettings = null;
  private int amountOfSelectedTiles;
  private String tile_color;
  private transient Images images;
  private JPanel gameField;
  private String currentPlayer;
  private int frameWidth;
  private int frameHeight;
  private String currentCard;
  private Font standardFont;
  private DecimalFormat dFormat;
  private int numberOfPayersHS;
  private Timer timer;
  private JPanel middle;
  private boolean sizeset = false;
  private double prozent = 1;

  private JLabel playersInLobby;
  private JPanel cardDeck;
  private int timerEventCounter = 0;


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


    images = new Images();
    //Creates Game icon.
    BufferedImage icon = images.getIcon();
    this.setIconImage(icon);

    setPreferredSize(new Dimension(310, 450));
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
    standardFont = new Font("Arial", Font.PLAIN, 15);
    dFormat = new DecimalFormat("00");
    layout = new CardLayout();
    cardDeck = new JPanel(layout);
    this.add(cardDeck);
    nickName = new JTextField(20);
    firstNicknameHS = new JTextField(20);
    secondNicknameHS = new JTextField(20);
    thirdNicknameHS = new JTextField(20);
    fourthNicknameHS = new JTextField(20);
    play = new JButton("Play");
    play.setFont(standardFont);
    hotSeat = new JButton("HOTSEAT");
    hotSeat.setFont(standardFont);
    multiPlayer = new JButton("ONLINE");
    multiPlayer.setFont(standardFont);
    songs = new JComboBox<>(songList);
    back = new JButton("Back");
    back.setFont(standardFont);
    loginLabel = new JLabel("Login with your nick name:");
    loginLabel.setFont(standardFont);
    playersInLobby = new JLabel("Players waiting for game: ");
    playersInLobby.setFont(standardFont);
    numberOfPayersHS = 0;


    //Game
    playerNames = new ArrayList<>();

    collection = new TileCollection[10];
    gameField = new JPanel(new BorderLayout());
    gameField.setBackground(new Color(1.0f, 1.0f, 1.0f, 0.0f));
    gameField.setPreferredSize(createGameField().getPreferredSize());
  }

  /**
   * Creates Card to set Game Mode. (Hot Seat or Multiplayer)
   */
  public void createSetGameModeView() {

    JPanel setGameMode = new JPanel(new BorderLayout());
    BufferedImage img = images.getBackgroundSetGameMode();
    JLabel background = new JLabel(new ImageIcon(img));
    background.setLayout(new FlowLayout());

    JPanel south = new JPanel();
    south.add(hotSeat);
    south.add(multiPlayer);
    south.add(songs);
    setGameMode.add(south, BorderLayout.SOUTH);
    cardDeck.add(setGameMode, GAMEMODE_CARD);

    setGameMode.add(background);
    showCard(GAMEMODE_CARD);

  }

  /**
   * Creates Card for Game View.
   */
  public void createGameView() {
    JPanel game = new JPanel();
    cardDeck.add(game, GAME_CARD);

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
    login.setPreferredSize(new Dimension(100, 100));
    JPanel south = new JPanel(new GridLayout(1, 2));
    south.setBackground(Color.CYAN);
    south.add(back);
    south.add(play);
    login.add(south, BorderLayout.SOUTH);
    JPanel center = new JPanel();
    center.setBackground(Color.CYAN);
    center.add(loginLabel);
    center.add(nickName);
    login.add(center);
    cardDeck.add(login, LOGIN_M_CARD);
  }

  private void firstPlayerWait() {
    JPanel waitFirstPlayer = new JPanel();
    JLabel wait = new JLabel("Waiting for other players to join.");
    wait.setFont(standardFont);
    waitFirstPlayer.add(wait);
    waitFirstPlayer.setBackground(Color.CYAN);
    cardDeck.add(waitFirstPlayer, WAIT_CARD);
  }

  private void timerStart() {

    JPanel waitMultiPlayer = new JPanel();

    JLabel counter = new JLabel();
    counter.setFont(standardFont);
    JLabel waitingLabel = new JLabel("Waiting for other Players to join.");
    waitingLabel.setFont(standardFont);
    waitMultiPlayer.add(waitingLabel);
    waitMultiPlayer.add(counter);
    waitMultiPlayer.setBackground(Color.CYAN);
    waitMultiPlayer.add(playersInLobby);

    counterMinute = 1;
    counterSecond = 60;

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
    cardDeck.add(waitMultiPlayer, TIMER_CARD);


  }

  private void timerRestart(){JPanel waitMultiPlayer = new JPanel();
    JLabel counter = new JLabel();
    counter.setFont(standardFont);
    JLabel waitingLabel = new JLabel("Waiting for other Players to join.");
    waitingLabel.setFont(standardFont);
    waitMultiPlayer.add(waitingLabel);
    waitMultiPlayer.add(counter);
    waitMultiPlayer.setBackground(Color.CYAN);
    waitMultiPlayer.add(playersInLobby);

    counterMinute = 1;
    counterSecond = 60;

    timer.stop();
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
    cardDeck.add(waitMultiPlayer, TIMERUPDATE_CARD);


  }

  /**
   * Creates Card for Hot Seat Login.
   */
  public void createHotSeatLoginView() {

    JPanel login = new JPanel();
    login.setBackground(Color.CYAN);
    //login.setPreferredSize(new Dimension(100, 100));
    cardDeck.add(login, LOGIN_H_CARD);

    JLabel howMany = new JLabel("How many Players would you like to play with?");
    howMany.setFont(standardFont);
    login.add(howMany);
    playerNumberSelection.setFont(standardFont);
    login.add(playerNumberSelection);
    login.add(back);
    login.setBackground(Color.CYAN);


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
    south.setBackground(Color.CYAN);
    south.setLayout(new GridLayout(1, 2));
    JPanel center = new JPanel();
    center.setBackground(Color.CYAN);
    center.setBackground(Color.CYAN);
    south.setBackground(Color.CYAN);
    south.add(play);
    south.add(back);
    loginNames.setPreferredSize(new Dimension(400, 100));
    cardDeck.add(loginNames, LOGINNAMES_CARD);
    showCard(LOGINNAMES_CARD);

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
    setContentPane(cardDeck);
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
          case "CHILL BEAT" -> playMusic("src/main/java/de/lmu/ifi/sosylab/client/view/songs/chillbeat.wav");
          case "MELODIC RHYTHM" -> playMusic("src/main/java/de/lmu/ifi/sosylab/client/view/songs/melodicrhythm.wav");
          case "RETRO CITY" -> playMusic("src/main/java/de/lmu/ifi/sosylab/client/view/songs/retrocity.wav");
          default -> {
          }
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
          if (model.getPlayers().size() == 0) {
            firstPlayerWait();
            showCard(WAIT_CARD);
          }
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
        goBackToFirstCard();
      }
    });
  }

  /**
   * Creates the amount of boards according to the size of playernames and adds middle.
   *
   * @return - The Game Field with boards, pile and plates.
   */
  private Component createGameField() {
    switch (playerNames.size() - 1) {
      case 1 -> {
        gameField.add(createBoard(0), BorderLayout.NORTH);
        gameField.add(createBoard(1), BorderLayout.SOUTH);
        if (!sizeset) {
          this.setSize((int) (350 * prozent), (int) (prozent * 900));
        }
      }
      case 2 -> {
        gameField.add(createBoard(0), BorderLayout.NORTH);
        gameField.add(createBoard(1), BorderLayout.WEST);
        gameField.add(createBoard(2), BorderLayout.SOUTH);
        this.setSize((int) (700 * prozent), (int) (920 * prozent));
      }
      case 3 -> {
        gameField.add(createBoard(0), BorderLayout.NORTH);
        gameField.add(createBoard(1), BorderLayout.EAST);
        gameField.add(createBoard(2), BorderLayout.SOUTH);
        gameField.add(createBoard(3), BorderLayout.WEST);
        this.setSize(1020, 880);
      }
      default -> {
      }
    }
    gameField.add(createMiddle(), BorderLayout.CENTER);
    return gameField;
  }

  /**
   * Creates Middle with plates and pile.
   *
   * @return - middle.
   */
  private Component createMiddle() {
    middle = new JPanel(new FlowLayout());
    middle.setBackground(new Color(1.0f, 1.0f, 1.0f, 0.0f));
    middle.setPreferredSize(new Dimension((int) (325 * prozent), (int) (300 * prozent)));

    middle.add(createSettingButton());
    createPlates();
    middle.add(createPile());
    return middle;
  }

  private Component createSettingButton(){
    BufferedImage setting = images.getSettings();
    JLabel settings =new JLabel(new ImageIcon(setting));

    settings.addMouseListener(new MouseAdapter() {
      @Override
      public void mouseClicked(MouseEvent e) {
        settingWindow();

      }
    });
    return settings;
  }

  private Component settingWindow(){
    JFrame settingWindow = new JFrame();
    settingWindow.setVisible(true);
    settingWindow.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    settingWindow.setSize(new Dimension(400,300));

    JPanel radioPanel = new JPanel();

    JLabel winSize = new JLabel("Set Game Size: ");
    radioPanel.add(winSize);
    JRadioButton small = new JRadioButton("small");
    small.setBounds(0,0,100,25);
    small.addItemListener(new ItemListener() {
      @Override
      public void itemStateChanged(ItemEvent e) {
        prozent = 0.8;

        images.setProzent(prozent);
        tileSize = (int) (prozent * 25);
        images.resize();

        showGame();
        gameField.removeAll();
        createGameView();
        repaint();
      }
    });
    radioPanel.add(small);

    JRadioButton medium = new JRadioButton("medium");
    medium.setBounds(50,0,100,25);
    medium.addItemListener(new ItemListener() {
      @Override
      public void itemStateChanged(ItemEvent e) {
        prozent = 1;

        images.setProzent(prozent);
        tileSize = (int) (prozent * 25);
        images.resize();

        showGame();
        gameField.removeAll();
        createGameView();
        repaint();

      }
    });
    radioPanel.add(medium);

    JRadioButton big = new JRadioButton("big");
    big.setBounds(100,0,100,25);
    big.addItemListener(new ItemListener() {
      @Override
      public void itemStateChanged(ItemEvent e) {
        prozent = 1.2;

        images.setProzent(prozent);
        tileSize = (int) (prozent * 25);
        images.resize();

        showGame();
        gameField.removeAll();
        createGameView();
        repaint();
      }
    });
    radioPanel.add(big);

    ButtonGroup windowSize = new ButtonGroup();
    windowSize.add(small);
    windowSize.add(medium);
    windowSize.add(big);

    settingWindow.add(radioPanel);

    return settingWindow;
  }
  /**
   * Creates the Plates in the middle with the tiles and adds a MouseListener.
   * The MouseListener gets the name of the clicked Component like tile_color and plate.
   * Afterwards the information is sent to controller.selectAllTiles.
   * <p>
   * If collection ist empty a NullPointerException is thrown.
   */
  private void createPlates() {
    try {
      collection = controller.getTilePlates();
      for (int plateNumber = 1; plateNumber < collection.length; plateNumber++) {
        BufferedImage img = images.getPlate();

        JLabel plate = new JLabel();
        plate.removeAll();
        plate.setIcon(new ImageIcon(img));
        plate.setName(String.valueOf(plateNumber));
        plate.repaint();

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
          tile.setBounds((int) (12 * (x * prozent)), (int) (12 * (y * prozent)), tileSize, tileSize);
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
              int plateNumber = Integer.parseInt(plates);
              amountOfSelectedTiles = collection[plateNumber].getAmountTilesOfColor(Tile.getTile(tile_color));
              boolean confirmation = confirmTileSelection(plateNumber, tile_color, amountOfSelectedTiles, currentPlayer);
              if (confirmation) {
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
   * Creates tile pile in the Middle and adds a MouseListener to each Tile.
   * The MouseListener gets the name of the clicked Component.
   * Afterwards the information is sent to controller.selectAllTiles.
   * <p>
   * If the pile is null a Exception is thrown.
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
            boolean confirmation = confirmTileSelection(0, tile_color, amountOfSelectedTiles, currentPlayer);
            if (confirmation) {
              controller.selectAllTiles(0, tile_color);
            }
          }
        }
      });
    } catch (NullPointerException e) {
      System.out.println("Collection ist noch leer! (createPile)");
    }
    return pile;
  }

  /**
   * Creates a board with MouseListener for the rows.
   * When clicked the selected tiles are placed there.
   *
   * @param boardNumber - Number so that the playername can be fetched.
   * @return -board with name, points and placed tiles.
   */
  private Component createBoard(int boardNumber) {
    Board b = new Board(controller, tileSize, playerNames.get(boardNumber), images, boardNumber, prozent);

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
          }
          if (mousePointX > 3 && mousePointX < 6 && mousePointY == 3) {
            controller.placeTiles(amountOfSelectedTiles, 1);
          }
          if (mousePointX > 2 && mousePointX < 6 && mousePointY == 4) {
            controller.placeTiles(amountOfSelectedTiles, 2);
          }
          if (mousePointX > 1 && mousePointX < 6 && mousePointY == 5) {
            controller.placeTiles(amountOfSelectedTiles, 3);
          }
          if (mousePointX > 0 && mousePointX < 6 && mousePointY == 6) {
            controller.placeTiles(amountOfSelectedTiles, 4);
          }
          if (mousePointX > 0 && mousePointX < 8 && mousePointY == 8) {
            controller.placeTiles(amountOfSelectedTiles, 5);
          }
        }
      }
    });

    return board;
  }

  @Override
  public void dispose() {
    model.removePropertyChangeListener(this);
    controller.dispose();
    model.dispose();
    super.dispose();
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

      if (model.getGameMode().equals("Multiplayer")) {
        for (Player player : model.getPlayers()) {
          playersInLobby.setText(playersInLobby.getText() + "\n " + player.getPlayerName());
        }

        int numberOfPlayers = model.getPlayers().size() - 1;
        System.out.println("LogeddInEvent, numer of current players are: " + numberOfPlayers);

        String player = model.getNickname();
        playerNames.add(player);
        this.setTitle(player);
        System.out.println(player + "has been added Login Event (this is the main playor of this client instance)");

        if (numberOfPlayers > 0) {
          ArrayList<Player> players = model.getPlayers();
          for (int i = 0; i < model.getPlayers().size(); i++) {
            String otherPlayer = players.get(i).getPlayerName();

            if (!player.equals(otherPlayer) && !player.equals("  ")) {
              playerNames.add(otherPlayer);
              System.out.println("LoginEvent adding player: " + otherPlayer);
            }
          }

        }


      } else if (model.getGameMode().equals("Hot Seat")) {
        showGame();
      }
    } else if (newValue instanceof UserJoinedEvent) {
      if (model.getGameMode().equals("Hot seat")) {
        //TODO: was soll hier genau passieren?

      } else if (model.getGameMode().equals("Multiplayer")) {
        playersInLobby.setText(playersInLobby.getText() + "\n " + ((UserJoinedEvent) newValue).getUsername());
        int numberOfPlayers = model.getPlayers().size() - 1;
        ArrayList<Player> players = model.getPlayers();
        String playerName = players.get(numberOfPlayers).getPlayerName();
        playerNames.add(playerName);
        System.out.println("UserJoinedEvent in Frame adding " + playerName);

      }

    } else if (newValue instanceof UserLeftEvent) {
      //TODO: was soll hier genau passieren?

    } else if (newValue instanceof LoginFailedEvent) {
      JOptionPane.showMessageDialog(this,
              String.format("Login failed, name \"%s\" is already in use.", nickName.getText()));
      showCard(LOGIN_M_CARD);

    } else if (newValue instanceof MiddleTilesUpdateEvent) {
        showGame();
        gameField.removeAll();
        collection = controller.getTilePlates();
        createGameView();
        repaint();
        currentPlayer = controller.getCurrentPlayer();

    } else if (newValue instanceof OtherPlayerPlacedTilesEvent) {
      showGame();
      gameField.removeAll();
      collection = controller.getTilePlates();
      createGameView();
      repaint();
      currentPlayer = controller.getCurrentPlayer();

    } else if (newValue instanceof OtherPlayerSelectedTilesEvent) {
      showGame();
      gameField.removeAll();
      collection = controller.getTilePlates();

      createGameView();
      repaint();
      currentPlayer = controller.getCurrentPlayer();

    } else if (newValue instanceof TilesAddedEvent) {
      showGame();
      gameField.removeAll();
      collection = controller.getTilePlates();
      createGameView();
      repaint();
      currentPlayer = controller.getCurrentPlayer();

    } else if (newValue instanceof FloorLineEvent) {
      showGame();
      gameField.removeAll();
      collection = controller.getTilePlates();
      createGameView();
      repaint();
      currentPlayer = controller.getCurrentPlayer();

    } else if (newValue instanceof TilesSelectedEvent) {
      gameField.removeAll();
      collection = controller.getTilePlates();
      createGameView();
      repaint();
      currentPlayer = controller.getCurrentPlayer();

    } else if (newValue instanceof TilePlacementFailedEvent) {


    } else if (newValue instanceof UserLeftEvent) {
      //TODO: was soll hier genau passieren?

    } else if (newValue instanceof BoardUpdatedEvent) {
      gameField.removeAll();
      createGameView();
      repaint();
      currentPlayer = controller.getCurrentPlayer();

    } else if (newValue instanceof GameCanceledEvent) {

    } else if (newValue instanceof GameEndedEvent) {
      String message = handleGameEndedEvent();
      JOptionPane.showMessageDialog(this, message, "Game Ended", JOptionPane.INFORMATION_MESSAGE);
      goBackToFirstCard();
    } else if (newValue instanceof GameRestartedEvent) {

    } else if (newValue instanceof GameRestartRequestEvent) {

    } else if (newValue instanceof NextPlayerEvent) {
      currentPlayer = controller.getCurrentPlayer();

    } else if (newValue instanceof TimerEvent) {
      System.out.println("Frame TimerEvent");
      if (timerEventCounter > 0){
        timerEventCounter++;
        System.out.println("Timers active (else if): " + timerEventCounter);

        timerRestart();
        showCard(TIMERUPDATE_CARD);
      }
      else if(timerEventCounter == 0){
        timerEventCounter++;
        System.out.println("Timers active: " + timerEventCounter);
        timerStart();
        showCard(TIMER_CARD);
      }

    } else if (newValue instanceof TimerEndedEvent) {
      showGame();

    } else if (newValue instanceof FloorLineEvent) {
      System.out.println("Floor line event");

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
   * Show the game view to the user.
   */
  private void showGame() {
    showCard(GAME_CARD);
    setCurrentCard(LOGIN_M_CARD);
  }

  private void showCard(String card) {
    CardLayout localLayout = (CardLayout) cardDeck.getLayout();
    localLayout.show(cardDeck, card);
    setCurrentCard(card);
    setPreferredSize(localLayout.minimumLayoutSize(cardDeck.getParent()));
  }

  private void goBackToFirstCard() {
    showCard(GAMEMODE_CARD);

}

  private String handleGameEndedEvent(){
    ArrayList<String> winners = controller.getWinners();
    StringBuilder str = new StringBuilder();
    String message;
    String winner;
    if (winners.size()>1){
      str.append("Draw between: ");
      for (int i = 0; i < winners.size(); i++) {
        if (i == winners.size()-1){
          str.append("and ");
          str.append(winners.get(i));
        } else {
          str.append(winners.get(i));
          str.append(", ");
        }
      }
      message = str.toString();
    } else {
      winner = winners.get(0);
      if (controller.getGameMode().equalsIgnoreCase("hot seat")){
        message = String.format("Congratulations! \"%s\" won!", winner);
      } else if (controller.getNickname().equals(winner)) {
        message = "Congratulations! YOU won!";
      } else {
        message = String.format("\"%s\" won!", winner);
      }
    }
    return message;
  }
}

