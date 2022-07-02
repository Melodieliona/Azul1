package de.lmu.ifi.sosylab.shared;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.Arrays;
import java.util.Date;
import java.util.Locale;
import java.util.Optional;
import org.json.JSONException;
import org.json.JSONObject;

public enum JsonMessage {

  LOGIN("login"), LOGIN_SUCCESS("login success"), LOGIN_FAILED("login failed"),
  USER_JOINED("user joined"), TILE_SELECTION("tile selection"), TILE_PLACEMENT("tile placement"),
  NEXT_TURN("next turn") , ALLOWED_TILES("allowed tiles"),
  MOVE_NOT_ALLOWED("move not allowed"), ALLOWED_FIELDS("board state"), BOARD_UPDATE("game state"),
  USER_LEFT("user left"), FILL_PLATES("fill plates"), POINTS("points"), GAME_ENDED("game ended"),
  GAME_RESTART("game restart");

  public static final String TYPE_FIELD = "type";

  public static final String NICK_FIELD = "nick";

  public static final String PLATE_FIELD = "plate";

  public static final String COLOR_FIELD = "color";

  public static final String TILES_FIELD = "tiles";

  public static final String ROWS_FIELD = "row";

  public static final String COLUMNS_FIELD = "columns";

  private final String jsonName;

  /**
   * Constructor for a JsonMessage.
   *
   * @param jsonName the type of JsonMessage
   */
  JsonMessage(String jsonName) {
    this.jsonName = jsonName;
  }

  /**
   * Gets the type of JsonMessage.
   *
   * @param message the JsonMessage
   * @return type of JsonMessage
   */
  public static JsonMessage typeOf(JSONObject message) {
    String typeName;
    try {
      typeName = message.getString(TYPE_FIELD);
    } catch (JSONException e) {
      throw new IllegalArgumentException(String.format("Unknown message type '%s'", message), e);
    }

    Optional<JsonMessage> opt =
        Arrays.stream(JsonMessage.values()).filter(x -> x.getJsonName().equals(typeName))
            .findFirst();
    return opt.orElseThrow(
        () -> new IllegalArgumentException(String.format("Unknown message type '%s'", typeName)));
  }

  /**
   * Creates JsonMessage to attempt Login.
   *
   * @param nickname nick to be logged in with
   * @return JsonMessage to be sent
   */
  public static JSONObject login(String nickname) {
    try {
      return createMessageOfType(LOGIN).put(NICK_FIELD, nickname);
    } catch (JSONException e) {
      throw new IllegalArgumentException("Failed to create a json object.", e);
    }
  }

  /**
   * Creates a message to be sent when a login was successful.
   *
   * @return JsonMessage to be sent
   */
  public static JSONObject loginSuccess() {
    try {
      return createMessageOfType(LOGIN_SUCCESS);
    } catch (JSONException e) {
      throw new IllegalArgumentException("Failed to create a json object.", e);
    }
  }


  /**
   * Creates a message to be sent when a login failed.
   *
   * @return JsonMessage to be sent
   */
  public static JSONObject loginFailed() {
    try {
      return createMessageOfType(LOGIN_FAILED);
    } catch (JSONException e) {
      throw new IllegalArgumentException("Failed to create a json object.", e);
    }
  }

  /**
   * Creates a message to be sent when a user joined.
   *
   * @param nickname nick of the user
   * @return JsonMessage to be sent
   */
  public static JSONObject userJoined(String nickname) {
    try {
      return createMessageOfType(USER_JOINED).put(NICK_FIELD, nickname);
    } catch (JSONException e) {
      throw new IllegalArgumentException("Failed to create a json object.", e);
    }
  }

  /**
   * Creates a message to be sent when a login left.
   *
   * @param nickname nick of the user
   * @return JsonMessage to be sent
   */
  public static JSONObject userLeft(String nickname) {
    try {
      return createMessageOfType(USER_LEFT).put(NICK_FIELD, nickname);
    } catch (JSONException e) {
      throw new IllegalArgumentException("Failed to create a json object.", e);
    }
  }

  /**
   * Creates a message to be sent when a tile selection has been made.
   *
   * @param color of the desired tiles
   * @return JsonMessage to be sent
   */
  public static JSONObject selectTile(String color) {
    try {
      JSONObject message = createMessageOfType(TILE_SELECTION);
      message.put(PLATE_FIELD, color.toUpperCase());

      return message;
    } catch (JSONException e) {
      throw new IllegalArgumentException("Failed to create a json object.", e);
    }
  }

  public static JSONObject boardState() {
   return null;
  }

  /**
   * Creates a message of a specified type.
   *
   * @param type of message
   * @return JsonMessage to be sent
   */
  private static JSONObject createMessageOfType(JsonMessage type) throws JSONException {
    return new JSONObject().put(TYPE_FIELD, type.getJsonName());
  }

  /**
   * Gets the nick of a user that sent a JsonMessage.
   *
   * @param object JsonMessage
   * @return nick
   */
  public static String getNickname(JSONObject object) {
    try {
      return object.getString(NICK_FIELD);
    } catch (JSONException e) {
      throw new IllegalArgumentException("Failed to read a json object.", e);
    }
  }

  /**
   * Gets the factory plate from a JsonMessage.
   *
   * @param object JsonMessage
   * @return factory plate number as a string
   */
  public static String getFactoryPlate(JSONObject object) {
    try {
      return object.getString(PLATE_FIELD);
    } catch (JSONException e) {
      throw new IllegalArgumentException("Failed to read a json object.", e);
    }
  }

  /**
   * Gets the color of a selection of (a) tile(s) from a JsonMessage.
   *
   * @param object JsonMessage
   * @return color as a string
   */
  public static String getTileColor(JSONObject object) {
    try {
      return object.getString(COLOR_FIELD);
    } catch (JSONException e) {
      throw new IllegalArgumentException("Failed to read a json object.", e);
    }
  }

  /**
   * Gets the tiles to be placed in a given set of fields from a JsonMessage.
   *
   * @param object JsonMessage
   * @return tiles as a string
   */
  public static String getTiles(JSONObject object) {
    try {
      return object.getString(TILES_FIELD);
    } catch (JSONException e) {
      throw new IllegalArgumentException("Failed to read a json object.", e);
    }
  }

  /**
   * Gets the rows of the fields where tiles should be placed from a JsonMessage.
   *
   * @param object JsonMessage
   * @return rows as a string
   */
  public static String getRows(JSONObject object) {
    try {
      return object.getString(ROWS_FIELD);
    } catch (JSONException e) {
      throw new IllegalArgumentException("Failed to read a json object.", e);
    }
  }

  /**
   * Gets the columns of the fields where tiles should be placed from a JsonMessage.
   *
   * @param object JsonMessage
   * @return columns as a string
   */
  public static String getColumns(JSONObject object) {
    try {
      return object.getString(COLUMNS_FIELD);
    } catch (JSONException e) {
      throw new IllegalArgumentException("Failed to read a json object.", e);
    }
  }

  public String getJsonName() {
    return jsonName;
  }
}