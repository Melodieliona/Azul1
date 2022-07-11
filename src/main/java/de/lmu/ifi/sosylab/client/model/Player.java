package de.lmu.ifi.sosylab.client.model;

/**
 * TODO Javadoc
 * */
public class Player {
  private Object[][] patternLines;
  private Object[][] tilesLines;
  private String playerName;

  /**
   * TODO Javadoc
   * */
  public Player(String name) {
    this.playerName = name;
    patternLines = new Object[5][2];
    tilesLines = new Object[5][5];
    createPatternLines();
    System.out.println("created player");
    //createTilesLines();
  }

  /**
   * Creates the left side of the player board (pattern lines) in form of a 2D array. Each line has
   * 2 entries: First entry contains the tile color. Second entry contains the number of empty
   * spaces
   */
  private void createPatternLines() {
    for (int i = 0; i < 5; i++) {
      patternLines[i][1] = i + 1;
    }
  }

  /**
   * Creates the right side of the player board (tiles lines) in form of a 2D array. Each line has
   * 5 entries for the tiles.
   */
  private void createTilesLines() {
    String[] colors = {"RED", "BLACK", "WHITE", "BLUE", "YELLOW"};
    for (int i = 0; i < 5; i++) {
      for (int j = 0; j < 5; j++) {
        tilesLines[i][j] = colors[i + j];
      }
    }
  }

  /**
   * TODO Javadoc
   * */
  public int placeTiles(int line, String color, int numberOfTiles) {
    int minusPointCounter = 0;
    if ((Integer) patternLines[line][1] == line) {
      patternLines[line][0] = color;
      int newNumberOfEmptySpaces = ((Integer) patternLines[line][1]) - numberOfTiles;
      patternLines[line][1] = newNumberOfEmptySpaces;
    } else if ((Integer) patternLines[line][1] >= numberOfTiles) {
      if (patternLines[line][0].toString().equals(color)) {
        int newNumberOfEmptySpaces = ((Integer) patternLines[line][1]) - numberOfTiles;
        patternLines[line][1] = newNumberOfEmptySpaces;
      } else {
        System.out.println("ERROR! You can only add tiles from the same color here");
      }
    } else if ((Integer) patternLines[line][1] < numberOfTiles) {
      if (patternLines[line][0].toString().equals(color)) {
        int minusPoints = numberOfTiles - ((Integer) patternLines[line][1]);
        patternLines[line][1] = 0;
        minusPointCounter = minusPoints;
      } else {
        System.out.println("ERROR! You can only add tiles from the same color here");
      }
    }
    return minusPointCounter;
  }

  public String getPlayerName() {
    return playerName;
  }
}
