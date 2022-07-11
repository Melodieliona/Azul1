package de.lmu.ifi.sosylab.shared;

/**
 * Represents a single tile.
 */
public enum Tile {
  BLUE("blue"),
  YELLOW("yellow"),
  RED("yellow"),
  BLACK("black"),
  WHITE("white"),
  STARTING_MARKER("");

  final String color;

  Tile(String color) {
    this.color = color;
  }

  /**
   * Returns the corresponding tile for a given color.
   */
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

  /**
   * Returns the color of a given tile.
   */
  public String getColor() {
    return color;
  }
}
