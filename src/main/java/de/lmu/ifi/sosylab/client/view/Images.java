package de.lmu.ifi.sosylab.client.view;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

/**
 * Imports the images and resizes them if needed.
 * */
public class Images {
  private BufferedImage board;
  private BufferedImage tileWhite;
  private BufferedImage tileBlack;
  private BufferedImage tileYellow;
  private BufferedImage tileRed;
  private BufferedImage tileBlue;
  private BufferedImage plate;
  private BufferedImage background;
  private BufferedImage icon;
  private BufferedImage tileStarter;
  private BufferedImage backgroundHomeScreen;
  private BufferedImage backgroundSetGameMode;
  private BufferedImage resizedplate;
  private BufferedImage settings;

  private double prozent = 1;
  private List<BufferedImage> pictures = new ArrayList<>();

  /**
   * Imports images and resizes them if needed.
   */
  Images() {

    importImages();
  }

  /**
   * Imports images.
   */
  private void importImages() {
    board = new ImportImage("images/Board.png").getImg();
    pictures.add(board);
    tileRed = new ImportImage("images/TileRed.png").getImg();
    pictures.add(tileRed);
    tileBlue = new ImportImage("images/TileBlue.png").getImg();
    pictures.add(tileBlue);
    tileBlack = new ImportImage("images/TileBlack.png").getImg();
    pictures.add(tileBlack);
    tileYellow = new ImportImage("images/TileYellow.png").getImg();
    pictures.add(tileYellow);
    tileWhite = new ImportImage("images/TileWhite.png").getImg();
    pictures.add(tileWhite);
    tileStarter = new ImportImage("images/TileStarter.png").getImg();
    pictures.add(tileStarter);
    plate = new ImportImage("images/Plate.png").getImg();
    pictures.add(plate);
    icon = new ImportImage("images/Icon.png").getImg();
    background = new ImportImage("images/Background.png").getImg();
    backgroundHomeScreen = new ImportImage("images/AzulHomeScreen.png").getImg();
    backgroundSetGameMode = new ImportImage("images/BackgroundSetGameMode.png").getImg();
    settings = new ImportImage("images/Settings.png").getImg();
    pictures.add(settings);
    resize();
  }

  /**
   * resizes chosen images.
   */
  public void resize() {

    plate = resizeImage(pictures.get(7), (int) (70 * prozent), (int) (70 * prozent));
    board = resizeImage(pictures.get(0), (int) (324 * prozent), (int) (250 * prozent));
    tileRed = resizeImage(pictures.get(1), (int) (25 * prozent), (int) (25 * prozent));
    tileBlue = resizeImage(pictures.get(2), (int) (25 * prozent), (int) (25 * prozent));
    tileBlack = resizeImage(pictures.get(3), (int) (25 * prozent), (int) (25 * prozent));
    tileYellow = resizeImage(pictures.get(4), (int) (25 * prozent), (int) (25 * prozent));
    tileWhite = resizeImage(pictures.get(5), (int) (25 * prozent), (int) (25 * prozent));
    tileStarter = resizeImage(pictures.get(6), (int) (25 * prozent), (int) (25 * prozent));
    backgroundSetGameMode = resizeImage(backgroundSetGameMode, 400, 500);
    settings = resizeImage(pictures.get(8), (int) (50 * prozent), (int) (50 * prozent));
    resizedplate = plate;
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

  public void setProzent(double prozent) {
    this.prozent = prozent;
  }


  /**
   * Getter for board.
   *
   * @return - board.
   */
  public BufferedImage getBoard() {
    return board;
  }

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
    return resizedplate;
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
