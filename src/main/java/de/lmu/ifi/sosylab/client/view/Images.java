package de.lmu.ifi.sosylab.client.view;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

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

    /**
     * Imports images and resizes them if needed.
     */
    Images() {
        System.out.println("Import");
        importImages();
    }

    /**
     * Imports images.
     */
    private void importImages() {
        board = new ImportImage("Board.png").getImg();
        tileRed = new ImportImage("TileRed.png").getImg();
        tileBlue = new ImportImage("TileBlue.png").getImg();
        tileBlack = new ImportImage("TileBlack.png").getImg();
        tileYellow = new ImportImage("TileYellow.png").getImg();
        tileWhite = new ImportImage("TileWhite.png").getImg();
        plate = new ImportImage("Plate.png").getImg();
        icon = new ImportImage("Icon.png").getImg();
        background = new ImportImage("Background.png").getImg();
        resize();
    }

    /**
     * resizes chosen images.
     */
    private void resize() {
        plate = resizeImage(plate, 70, 70);
        board = resizeImage(board, board.getWidth() / 2, board.getHeight() / 2);
        tileRed = resizeImage(tileRed, 25, 25);
        tileBlue = resizeImage(tileBlue, 25, 25);
        tileBlack = resizeImage(tileBlack, 25, 25);
        tileYellow = resizeImage(tileYellow, 25, 25);
        tileWhite = resizeImage(tileWhite, 25, 25);
    }

    /**
     * Method to resize images.
     *
     * @param originalImage - image that has to be resized.
     * @param targetWidth   - new width.
     * @param targetHeight  - new height.
     * @return - resized image.
     */
    public BufferedImage resizeImage(BufferedImage originalImage, int targetWidth, int targetHeight) {
        BufferedImage resizedImage =
                new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics2D = resizedImage.createGraphics();
        graphics2D.drawImage(originalImage, 0, 0, targetWidth, targetHeight, null);
        graphics2D.dispose();

        return resizedImage;
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
}
