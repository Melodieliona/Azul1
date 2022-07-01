package de.lmu.ifi.sosylab.shared;

/**
 * Represents a single tile.
 * */
public enum Tile {
  BLUE, YELLOW, RED, BLACK, WHITE, STARTING_MARKER;

  /**
   * Returns the corresponding tile for a given color.
   * */
  public static Tile getTile(String color) {
    return switch (color.toUpperCase()) {
      case "BLUE" -> BLUE;
      case "YELLOW" -> YELLOW;
      case "RED" -> RED;
      case "BLACK" -> BLACK;
      case "WHITE" -> WHITE;
      case "STARTING_MARKER" -> STARTING_MARKER;
      default -> throw new IllegalArgumentException("Invalid color.");
    };
  }
}
