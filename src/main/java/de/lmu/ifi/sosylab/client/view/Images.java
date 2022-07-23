package de.lmu.ifi.sosylab.client.view;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

/**
 * Imports the images and resizes them if needed.
 * */
public class Images {
  private static BufferedImage board;
  private static BufferedImage tileWhite;
  private static BufferedImage tileBlack;
  private static BufferedImage tileYellow;
  private static BufferedImage tileRed;
  private static BufferedImage tileBlue;
  private static BufferedImage plate;
  private static BufferedImage background;
  private static BufferedImage icon;
  private static BufferedImage tileStarter;
  private static BufferedImage backgroundHomeScreen;
  private static BufferedImage backgroundSetGameMode;
  private  static BufferedImage settings;

  private double prozent = 1;
  private final String skin;
  private final List<BufferedImage> pictures = new ArrayList<>();

  /**
   * Imports images and resizes them if needed.
   */
  Images(String skin) {
    this.skin = skin;

    importImages();
  }

  /**
   * Imports images.
   */
  private void importImages() {
    icon = new ImportImage("images/Icon.png").getImg();
    backgroundHomeScreen = new ImportImage("images/AzulHomeScreen.png").getImg();
    backgroundSetGameMode = new ImportImage("images/BackgroundSetGameMode.png").getImg();

    switch (skin) {
      case "STANDARD" -> {
        board = new ImportImage("images/Board.png").getImg();
        tileRed = new ImportImage("images/TileRed.png").getImg();
        tileBlue = new ImportImage("images/TileBlue.png").getImg();
        tileBlack = new ImportImage("images/TileBlack.png").getImg();
        tileYellow = new ImportImage("images/TileYellow.png").getImg();
        tileWhite = new ImportImage("images/TileWhite.png").getImg();
        tileStarter = new ImportImage("images/TileStarter.png").getImg();
        plate = new ImportImage("images/Plate.png").getImg();
        background = new ImportImage("images/Background.png").getImg();
        settings = new ImportImage("images/Settings.png").getImg();
      }
      case "WINTER" -> {
        board = new ImportImage("images/Board2.png").getImg();
        tileRed = new ImportImage("images/TileRed2.png").getImg();
        tileBlue = new ImportImage("images/TileBlue2.png").getImg();
        tileBlack = new ImportImage("images/TileGreen.png").getImg();
        tileYellow = new ImportImage("images/TileOrange.png").getImg();
        tileWhite = new ImportImage("images/TileWhite2.png").getImg();
        tileStarter = new ImportImage("images/TileStarter2.png").getImg();
        plate = new ImportImage("images/Plate2.png").getImg();
        background = new ImportImage("images/Background2.png").getImg();
        settings = new ImportImage("images/Settings2.png").getImg();
      }
      default -> System.out.println("Image modus not found!");
    }

    pictures.add(board);
    pictures.add(tileRed);
    pictures.add(tileBlue);
    pictures.add(tileBlack);
    pictures.add(tileYellow);
    pictures.add(tileWhite);
    pictures.add(tileStarter);
    pictures.add(plate);
    pictures.add(settings);
    pictures.add(background);
    resize();
  }

  /**
   * Resizes chosen images.
   */
  public void resize() {
    board = resizeImage(pictures.get(0), (int) (324 * prozent), (int) (250 * prozent));
    tileRed = resizeImage(pictures.get(1), (int) (25 * prozent), (int) (25 * prozent));
    tileBlue = resizeImage(pictures.get(2), (int) (25 * prozent), (int) (25 * prozent));
    tileBlack = resizeImage(pictures.get(3), (int) (25 * prozent), (int) (25 * prozent));
    tileYellow = resizeImage(pictures.get(4), (int) (25 * prozent), (int) (25 * prozent));
    tileWhite = resizeImage(pictures.get(5), (int) (25 * prozent), (int) (25 * prozent));
    tileStarter = resizeImage(pictures.get(6), (int) (25 * prozent), (int) (25 * prozent));
    plate = resizeImage(pictures.get(7), (int) (70 * prozent), (int) (70 * prozent));
    settings = resizeImage(pictures.get(8), (int) (25 * prozent), (int) (25 * prozent));
    background = resizeImage(pictures.get(9), 1300, pictures.get(9).getHeight());
    backgroundSetGameMode = resizeImage(backgroundSetGameMode, 400, 500);

  }

  /**
   * Method to resize images.
   *
   * @param originalImage - image that has to be resized.
   * @param targetWidth   - new width.
   * @param targetHeight  - new height.
   * @return - resized image.
   */
  private BufferedImage resizeImage(BufferedImage originalImage, int targetWidth,
                                    int targetHeight) {
    BufferedImage resizedImage =
        new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_ARGB);
    Graphics2D graphics2D = resizedImage.createGraphics();
    graphics2D.drawImage(originalImage, 0, 0, targetWidth, targetHeight, null);
    graphics2D.dispose();

    return resizedImage;
  }

  /**
   * TODO summary
   *
   * @param prozent - Sets prozent so that the images can be resized to small, medium and big.
   */
  public void setProzent(double prozent) {
    this.prozent = prozent;
  }

  /**
   * TODO summary
   *
   * @return - skin mode. Important for the Board so that
   *     the color can be adjusted (name, score ,...).
   */
  public String getSkin() {
    return skin;
  }

  /**
   * Getter for board.
   *
   * @return - board.
   */
  public BufferedImage getBoard() {
    return board;
  }

  /**
   * Getter for settings.
   *
   * @return - settingsIcon.
   */
  public BufferedImage getSettings() {
    return settings;
  }

  /**
   * Getter for white tile.
   *
   * @return - white tile.
   */
  public BufferedImage getTileWhite() {
    return tileWhite;
  }

  /**
   * Getter for black tile.
   *
   * @return - black tile.
   */
  public BufferedImage getTileBlack() {
    return tileBlack;
  }

  /**
   * Getter for yellow tile.
   *
   * @return - yellow tile.
   */
  public BufferedImage getTileYellow() {
    return tileYellow;
  }

  /**
   * Getter for red tile.
   *
   * @return - red tile.
   */
  public BufferedImage getTileRed() {
    return tileRed;
  }

  /**
   * Getter for blue tile.
   *
   * @return - blue tile.
   */
  public BufferedImage getTileBlue() {
    return tileBlue;
  }

  /**
   * Getter for plate.
   *
   * @return - plate.
   */
  public BufferedImage getPlate() {
    return plate;
  }

  /**
   * Getter for background.
   *
   * @return - background.
   */
  public BufferedImage getBackground() {
    return background;
  }

  /**
   * Getter for icon.
   *
   * @return - icon.
   */
  public BufferedImage getIcon() {
    return icon;
  }

  /**
   * Getter for tileStarter.
   *
   * @return  - tileStarter.
   */
  public BufferedImage getTileStarter() {
    return tileStarter;
  }

  public BufferedImage getBackgroundHomeScreen() {
    return backgroundHomeScreen;
  }

  /**
   * Getter for BackgroundSetGameMode.
   *
   * @return  - backgroundSetGameMode.
   */
  public BufferedImage getBackgroundSetGameMode() {
    return backgroundSetGameMode;
  }
}
