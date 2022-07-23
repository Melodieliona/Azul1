package de.lmu.ifi.sosylab.client.view;

import de.lmu.ifi.sosylab.client.controller.GameController;
import de.lmu.ifi.sosylab.client.model.GameModel;
import de.lmu.ifi.sosylab.client.model.Player;
import de.lmu.ifi.sosylab.client.model.events.*;
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
  private static final String MULTIPLAYER = "Multiplayer";

  private static final String GAME_CARD = "game";
  private static final String GAMEMODE_CARD = "gameMode";
  private static final String TIMER_CARD = "timer";
  private static final String TIMERUPDATE_CARD = "resartedTimer";
  private static final String LOGINNAMES_CARD = "loginNames";
  private static final String WAIT_CARD = "wait";
  private static final String[] songList = {"MUSIC", "CHILL BEAT", "RETRO CITY", "MELODIC RHYTHM"};
  private static TileCollection[] collection;
  private int tileSize = 25;
  JLabel loginLabel;
  int counterSecond;
  String formatedCounterSecond;
  int counterMinute;
  String formatedCounterMinute;
  Integer[] numberOfPlayerOptions = {2, 3, 4};
  private final JComboBox<Integer> playerNumberSelection = new JComboBox<>(numberOfPlayerOptions);
  private transient final GameModel model;
  private transient final GameController controller;
  private CardLayout layout;
  private JTextField nickName;
  private JTextField firstNicknameHotSeat;
  private JTextField secondNicknameHotSeat;
  private JTextField thirdNicknameHotSeat;
  private JTextField fourthNicknameHotSeat;
  private JButton back;
  private JButton play;
  private JFrame settingWindow;
  private transient List<String> playerNames;
  private JButton hotSeat;
  private JButton multiPlayer;
  private JComboBox<String> songs;
  private int amountOfSelectedTiles;
  private String tile_color;
  private transient Images images;
  private JPanel gameField;
  private String currentPlayer;

  private final int frameWidth = 400;
  private final int frameHeight = 500;
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

  private JButton cancel;

  private JButton restart;

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


    //Creates Game icon.
    images = new Images("STANDARD");
    BufferedImage icon = images.getIcon();
    this.setIconImage(icon);

    setPreferredSize(new Dimension(frameWidth, frameHeight));
    setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

    initializeWidgets();
    addEventListeners();
    createView();
    this.setVisible(true);

    pack();
  }

  /**
   * Enables Player to choose music in home screen.
   */
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
   * Sets what cars is visible at the moment.
   * @param cardName of card.
   */
  public void setCurrentCard(String cardName) {
    this.currentCard = cardName;

  }
  /**
   * Sets what cars is visible at the moment.
   * @return currentCard
   */
  public String getCurrentCard() {
    return this.currentCard;
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
    firstNicknameHotSeat = new JTextField(20);
    secondNicknameHotSeat = new JTextField(20);
    thirdNicknameHotSeat = new JTextField(20);
    fourthNicknameHotSeat = new JTextField(20);
    cancel = new JButton("Cancel");
    cancel.setFont(standardFont);
    restart = new JButton("Restart");
    restart.setFont(standardFont);
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
   * Creates Card where the player chooses Game Mode. (Hot Seat or Multiplayer)
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

    BufferedImage img = images.getBackground();
    JLabel background = new JLabel(new ImageIcon(img));


    game.setPreferredSize(createGameField().getPreferredSize());
    background.setLayout(new FlowLayout());

    gameField = new JPanel(new BorderLayout());
    gameField.setBackground(new Color(1.0f, 1.0f, 1.0f, 0.0f));


    background.add(gameField);
    game.add(background);
    cardDeck.add(game, GAME_CARD);

  }

  /**
   * Creates Card for Multiplayer Login.
   */
  public void createMultiplayerLoginView() {

    JPanel login = new JPanel();
    login.setBackground(Color.CYAN);
    login.add(loginLabel);
    login.add(nickName);
    login.add(back);
    login.add(play);
    cardDeck.add(login, LOGIN_M_CARD);
  }

  /**
   * Creates waiting card first Multiplayer.
   */
  private void firstPlayerWait() {
    JPanel waitFirstPlayer = new JPanel();
    JLabel wait = new JLabel("Waiting for other players to join.");
    wait.setFont(standardFont);
    waitFirstPlayer.add(wait);
    waitFirstPlayer.setBackground(Color.CYAN);
    cardDeck.add(waitFirstPlayer, WAIT_CARD);
  }

  /**
   * Creates Timer for first Multiplayer.
   */
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

    timer = new Timer(1000, e -> {

      counterMinute = 0;
      counterSecond--;
      formatedCounterSecond = dFormat.format(counterSecond);
      formatedCounterMinute = dFormat.format(counterMinute);
      counter.setText(formatedCounterMinute + ":" + formatedCounterSecond);

      counter.setText(formatedCounterMinute + " : " + formatedCounterSecond);

    });

    timer.start();
    cardDeck.add(waitMultiPlayer, TIMER_CARD);


  }

  /**
   * Creates Timer for third Multiplayer.
   */
  private void timerRestart() {
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

    timer.stop();
    timer = new Timer(1000, e -> {

      counterMinute = 0;
      counterSecond--;
      formatedCounterSecond = dFormat.format(counterSecond);
      formatedCounterMinute = dFormat.format(counterMinute);
      counter.setText(formatedCounterMinute + ":" + formatedCounterSecond);

      counter.setText(formatedCounterMinute + " : " + formatedCounterSecond);

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
    south.add(back);
    south.add(play);
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
      case 2 -> {
        center.add(playerOne);
        center.add(firstNicknameHotSeat);
        center.add(playerTwo);
        center.add(secondNicknameHotSeat);
      }
      case 3 -> {
        center.add(playerOne);
        center.add(firstNicknameHotSeat);
        center.add(playerTwo);
        center.add(secondNicknameHotSeat);
        center.add(playerThree);
        center.add(thirdNicknameHotSeat);
      }
      case 4 -> {
        center.add(playerOne);
        center.add(firstNicknameHotSeat);
        center.add(playerTwo);
        center.add(secondNicknameHotSeat);
        center.add(playerThree);
        center.add(thirdNicknameHotSeat);
        center.add(playerFour);
        center.add(fourthNicknameHotSeat);
      }
      default -> {
      }
    }

    loginNames.add(center, BorderLayout.CENTER);
    loginNames.add(south, BorderLayout.SOUTH);


  }

  /**
   * Set up the view in a way that is finally shown to the user.
   */
  private void createView() {
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
    multiPlayer.addActionListener(e -> {
      showCard(LOGIN_M_CARD);
      try {
        controller.setGameMode("Multiplayer");
      } catch (IOException ex) {
        throw new RuntimeException(ex);
      }
    });


    hotSeat.addActionListener(e -> {
      showCard(LOGIN_H_CARD);
      try {
        controller.setGameMode("Hot Seat");
      } catch (IOException ex) {
        throw new RuntimeException(ex);
      }
    });

    songs.addActionListener(e -> {
      String selectedSong = songs.getSelectedItem().toString();
      switch (selectedSong) {
        case "CHILL BEAT" -> playMusic("src/main/java/de/lmu/ifi/sosylab/client/view/songs/chillbeat.wav");
        case "MELODIC RHYTHM" -> playMusic("src/main/java/de/lmu/ifi/sosylab/client/view/songs/melodicrhythm.wav");
        case "RETRO CITY" -> playMusic("src/main/java/de/lmu/ifi/sosylab/client/view/songs/retrocity.wav");
        default -> {
        }
      }
      songs.setEnabled(false); //TODO: entfernen wenn songWechseln(...) inplementiert wurde
    });

    playerNumberSelection.addActionListener(e -> {
      try {
        numberOfPayersHS = (int) playerNumberSelection.getSelectedItem();
        setPlayerNicknamesHS(numberOfPayersHS);
      } catch (NullPointerException n) {
        n.printStackTrace();
      }
    });


    play.addActionListener(e -> {

      if (controller.getGameMode().equals(MULTIPLAYER)) {
        if (controller.getPlayers().size() == 0) {
          firstPlayerWait();
          showCard(WAIT_CARD);
        }
        controller.logInMultiplayer(nickName.getText());
      } else if (controller.getGameMode().equals("Hot seat")) {

        if (numberOfPayersHS == 2) {
          playerNames.add(firstNicknameHotSeat.getText());
          playerNames.add(secondNicknameHotSeat.getText());

        } else if (numberOfPayersHS == 3) {
          playerNames.add(firstNicknameHotSeat.getText());
          playerNames.add(secondNicknameHotSeat.getText());
          playerNames.add(thirdNicknameHotSeat.getText());

        } else if (numberOfPayersHS == 4) {
          playerNames.add(firstNicknameHotSeat.getText());
          playerNames.add(secondNicknameHotSeat.getText());
          playerNames.add(thirdNicknameHotSeat.getText());
          playerNames.add(fourthNicknameHotSeat.getText());
        }
        controller.logInHotSeat(playerNames);
      }
    });

    back.addActionListener(e -> goBackToFirstCard());

    cancel.addActionListener(e -> controller.cancelGameRequest());

    restart.addActionListener(e -> controller.restartGameRequest());
  }

  /**
   * Creates the amount of boards according to the size of playernames and adds middle.
   *
   * @return - The Game Field with boards, pile and plates.
   */
  private Component createGameField() {
    JPanel boards = new JPanel(new BorderLayout());
    boards.setBackground(new Color(1.0f, 1.0f, 1.0f, 0.0f));
    switch (playerNames.size() - 1) {
      case 1 -> {
        boards.add(createBoard(0), BorderLayout.NORTH);
        boards.add(createBoard(1), BorderLayout.SOUTH);
        this.setSize((int) (350 * prozent), (int) (prozent * 950));
      }
      case 2 -> {
        boards.add(createBoard(0), BorderLayout.NORTH);
        boards.add(createBoard(1), BorderLayout.WEST);
        boards.add(createBoard(2), BorderLayout.SOUTH);
        this.setSize((int) (700 * prozent), (int) (970 * prozent));
      }
      case 3 -> {
        boards.add(createBoard(0), BorderLayout.NORTH);
        boards.add(createBoard(1), BorderLayout.EAST);
        boards.add(createBoard(2), BorderLayout.SOUTH);
        boards.add(createBoard(3), BorderLayout.WEST);
        this.setSize((int) (1020 * prozent), (int) (930 * prozent));
      }
      default -> {
      }
    }
    boards.add(createMiddle(), BorderLayout.CENTER);

    JPanel middle2 = new JPanel(new BorderLayout());
    middle2.setBackground(new Color(1.0f, 1.0f, 1.0f, 0.0f));
    middle2.setPreferredSize(new Dimension((int) (325 * prozent), (int) (50 * prozent)));
    middle2.add(createControl(), BorderLayout.NORTH);

    gameField.add(middle2, BorderLayout.NORTH);
    gameField.add(boards, BorderLayout.CENTER);


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

    createPlates();
    middle.add(createPile());
    return middle;
  }

  /**
   * Creates control buttons to cancel or restart the game.
   *
   * @return - control.
   */
  private Component createControl() {
    JPanel controller = new JPanel(new FlowLayout());
    controller.setSize((int) (325 * prozent), (int) (100 * prozent));
    controller.setBackground(new Color(1.0f, 1.0f, 1.0f, 0.0f));
    controller.add(createSettingButton());
    controller.add(cancel);
    controller.add(restart);
    return controller;
  }

  /**
   * Creates setting button image.
   */
  private Component createSettingButton() {
    BufferedImage setting = images.getSettings();
    JLabel settings = new JLabel(new ImageIcon(setting));

    settings.addMouseListener(new MouseAdapter() {
      @Override
      public void mouseClicked(MouseEvent e) {
        setWindow();

      }
    });
    return settings;
  }

  /**
   * Sets the version the Player has chosen to play the Game in (Standard or Winter).
   */
  private class itemListener implements ItemListener {

    @Override
    public void itemStateChanged(ItemEvent e) {
      String item = ((JRadioButton) e.getSource()).getName();
      if (item.equals("STANDARD") || item.equals("WINTER")) {
        images = new Images(item);
        createGameView();
      } else {
        prozent = Double.parseDouble(item);
      }
      images.setProzent(prozent);
      tileSize = (int) (prozent * 25);
      images.resize();
      showGame();
      gameField.removeAll();
      createGameView();
      repaint();
    }
  }

  private Component setWindow() {
    settingWindow = new JFrame();
    settingWindow.setLayout(new GridLayout(0, 1));
    settingWindow.setVisible(true);
    settingWindow.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    settingWindow.setSize(new Dimension(400, 150));

    JPanel radioPanel = new JPanel();

    JLabel winSize = new JLabel("Set Game Size: ");
    radioPanel.add(winSize);
    JRadioButton small = new JRadioButton("small");
    small.setBounds(0, 0, 100, 25);
    small.setName("0.8");
    small.addItemListener(new itemListener());
    radioPanel.add(small);

    JRadioButton medium = new JRadioButton("medium");
    medium.setBounds(50, 0, 100, 25);
    medium.setName("1");
    medium.addItemListener(new itemListener());
    radioPanel.add(medium);

    JRadioButton big = new JRadioButton("big");
    big.setBounds(100, 0, 100, 25);
    big.setName("1.2");
    big.addItemListener(new itemListener());
    radioPanel.add(big);

    ButtonGroup windowSize = new ButtonGroup();
    windowSize.add(small);
    windowSize.add(medium);
    windowSize.add(big);

    settingWindow.add(radioPanel, 0);
    setSkin();

    return settingWindow;
  }

  /**
   * Sets the Skin Version that the player wants to use.
   */
  private void setSkin() {
    JPanel radioPanel = new JPanel();

    JLabel skin = new JLabel("Set Game Skin: ");
    radioPanel.add(skin);
    JRadioButton standard = new JRadioButton("Standard");
    standard.setBounds(0, 0, 100, 25);
    standard.setName("STANDARD");
    standard.addItemListener(new itemListener());
    radioPanel.add(standard);

    JRadioButton winter = new JRadioButton("Winter");
    winter.setBounds(50, 0, 100, 25);
    winter.setName("WINTER");
    winter.addItemListener(new itemListener());
    radioPanel.add(winter);

    ButtonGroup skins = new ButtonGroup();
    skins.add(standard);
    skins.add(winter);

    settingWindow.add(radioPanel, 1);
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
      //do nothing
    }

  }

  /**
   * If it is this players turn, makes player confirm
   * selection after they have clicked on the tiles in the Middle.
   *
   * @return - confirmation.
   */
  private boolean confirmTileSelection(int plateNumber, String tile, int amount, String playerName) {

    if (controller.getGameMode().equals(MULTIPLAYER)) {
      if (!controller.getCurrentPlayer().equals(controller.getNickname())) {

        JOptionPane.showMessageDialog(this, "Selection Failed. " +
                "It is not your turn, wait for your turn!", "Error!", JOptionPane.ERROR_MESSAGE);
        return false;
      }
    }
    int selection = JOptionPane.showConfirmDialog(null,
            playerName + ": Are you sure you want to select the " + amount + " "
                    + tile + " tile(s) from plate " + plateNumber,
            "Tile Selection", JOptionPane.YES_NO_OPTION);

    return selection == 0;

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
            amountOfSelectedTiles = collection[0].getAmountTilesOfColor(Tile.getTile(tile_color));
            boolean confirmation = confirmTileSelection
                    (0, tile_color, amountOfSelectedTiles, currentPlayer);
            if (confirmation) {
              controller.selectAllTiles(0, tile_color);
            }
          }
        }
      });
    } catch (NullPointerException e) {
      //do nothing
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
      west.setPreferredSize(new Dimension((int) (20 * prozent), (int) (300 * prozent)));
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
            controller.placeTiles(0);
          }
          if (mousePointX > 3 && mousePointX < 6 && mousePointY == 3) {
            controller.placeTiles(1);
          }
          if (mousePointX > 2 && mousePointX < 6 && mousePointY == 4) {
            controller.placeTiles(2);
          }
          if (mousePointX > 1 && mousePointX < 6 && mousePointY == 5) {
            controller.placeTiles(3);
          }
          if (mousePointX > 0 && mousePointX < 6 && mousePointY == 6) {
            controller.placeTiles(4);
          }
          if (mousePointX > 0 && mousePointX < 8 && mousePointY == 8) {
            controller.placeTiles(5);
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
    SwingUtilities.invokeLater(() -> handleModelUpdate(event));
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

      if (controller.getGameMode().equals("Multiplayer")) {
        for (Player player : controller.getPlayers()) {
          playersInLobby.setText(playersInLobby.getText() + "\n " + player.getPlayerName());
        }

        int numberOfPlayers = controller.getPlayers().size() - 1;
        System.out.println("LogeddInEvent, numer of current players are: " + numberOfPlayers);

        String player = controller.getNickname();
        playerNames.add(player);
        this.setTitle(player);
        System.out.println(player + "has been added Login Event (this is the main playor of this client instance)");

        if (numberOfPlayers > 0) {
          ArrayList<Player> players = controller.getPlayers();
          for (int i = 0; i < controller.getPlayers().size(); i++) {
            String otherPlayer = players.get(i).getPlayerName();

            if (!player.equals(otherPlayer) && !player.equals("  ")) {
              playerNames.add(otherPlayer);
              System.out.println("LoginEvent adding player: " + otherPlayer);
            }
          }

        }


      } else if (controller.getGameMode().equals("Hot Seat")) {
        showGame();
      }
    } else if (newValue instanceof UserJoinedEvent) {
      if (controller.getGameMode().equals("Multiplayer")) {
        playersInLobby.setText(playersInLobby.getText() + "\n " + ((UserJoinedEvent) newValue).getUsername());
        int numberOfPlayers = controller.getPlayers().size() - 1;
        ArrayList<Player> players = controller.getPlayers();
        String playerName = players.get(numberOfPlayers).getPlayerName();
        playerNames.add(playerName);
        System.out.println("UserJoinedEvent in Frame adding " + playerName);

      }

    } else if (newValue instanceof UserLeftEvent) {
      String userLeftName = ((UserLeftEvent) newValue).getUsername();
      JOptionPane.showMessageDialog(this,
              String.format("Player, name \"%s\" has left.", userLeftName));

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

      JOptionPane.showMessageDialog(this, "You can't place your tiles here!",
              "Error!", JOptionPane.ERROR_MESSAGE);

    } else if (newValue instanceof TileSelectionFailedEvent) {

      JOptionPane.showMessageDialog(this, "You can't select these tiles!",
              "Error!", JOptionPane.ERROR_MESSAGE);

    } else if (newValue instanceof BoardUpdatedEvent) {
      gameField.removeAll();
      createGameView();
      repaint();
      currentPlayer = controller.getCurrentPlayer();

    } else if (newValue instanceof GameCanceledEvent) {

      JOptionPane.showMessageDialog(this, "The game has been canceled",
              "Game Canceled", JOptionPane.INFORMATION_MESSAGE);
      model.clear();
      playerNames.clear();
      model.dispose();
      goBackToFirstCard();

    } else if (newValue instanceof GameEndedEvent) {

      String message = handleGameEndedEvent();
      JOptionPane.showMessageDialog(this, message,
              "Game Ended", JOptionPane.INFORMATION_MESSAGE);
      model.clear();
      playerNames.clear();
      model.dispose();
      goBackToFirstCard();

    } else if (newValue instanceof GameRestartedEvent) {

      JOptionPane.showMessageDialog(this, "The game has been restarted",
              "Game Canceled", JOptionPane.INFORMATION_MESSAGE);

    } else if (newValue instanceof GameRestartRequestEvent) {

      JOptionPane.showMessageDialog(this, "Someone has requested to restart the game",
              "Game Request", JOptionPane.INFORMATION_MESSAGE);

    } else if (newValue instanceof GameCancelRequestEvent) {

      JOptionPane.showMessageDialog(this, "Someone has requested to cancel the game",
              "Game Request", JOptionPane.INFORMATION_MESSAGE);

    } else if (newValue instanceof NextPlayerEvent) {

      currentPlayer = controller.getCurrentPlayer();

    } else if (newValue instanceof TimerEvent) {
      System.out.println("Frame TimerEvent");
      if (timerEventCounter > 0) {
        timerEventCounter++;
        System.out.println("Timers active (else if): " + timerEventCounter);

        timerRestart();
        showCard(TIMERUPDATE_CARD);
      } else if (timerEventCounter == 0) {
        timerEventCounter++;
        System.out.println("Timers active: " + timerEventCounter);
        timerStart();
        showCard(TIMER_CARD);
      }

    } else if (newValue instanceof TimerEndedEvent) {
      showGame();

    }
  }

  /**
   * Show the game view to the user.
   */
  private void showGame() {
    showCard(GAME_CARD);
    setCurrentCard(LOGIN_M_CARD);
  }

  /**
   * Show the card to the user.
   *
   * @param card to be shown.
   */
  private void showCard(String card) {
    CardLayout localLayout = (CardLayout) cardDeck.getLayout();
    localLayout.show(cardDeck, card);
    setCurrentCard(card);
    setPreferredSize(localLayout.minimumLayoutSize(cardDeck.getParent()));
  }

  /**
   * Show the home screen card to the user.
   */
  private void goBackToFirstCard() {
    showCard(GAMEMODE_CARD);

  }

  /**
   * Shows game results once a game has ended.
   */
  private String handleGameEndedEvent() {
    ArrayList<String> winners = controller.getWinners();
    StringBuilder str = new StringBuilder();
    String message;
    String winner;
    if (winners.size() > 1) {
      str.append("Draw between: ");
      for (int i = 0; i < winners.size(); i++) {
        if (i == winners.size() - 1) {
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
      if (controller.getGameMode().equalsIgnoreCase("hot seat")) {
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

