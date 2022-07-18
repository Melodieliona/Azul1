package de.lmu.ifi.sosylab.client.view;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import javax.imageio.ImageIO;

/**
 * Imports images.
 * */
public class ImportImage {
  private BufferedImage img;
  private final String path;

  /**
   * @param path - path for the picture.
   */
  public ImportImage(String path) {
    this.path = path;
    importImage();
  }

  /**
   * Imports images.
   */
  private void importImage() {
    InputStream is = getClass().getResourceAsStream(path);

    try {
      img = ImageIO.read(is);

    } catch (IOException e) {
      System.out.println("Image could not be imported.");
      e.printStackTrace();
    }
  }

  /**
   * Getter for image.
   *
   * @return image.
   */
  public BufferedImage getImg() {
    return img;
  }
}
