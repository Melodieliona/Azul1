package de.lmu.ifi.sosylab.shared;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Provides the structure for JSON Messages sent between server(s) and client(s).
 */
public enum JsonMessage {

  LOGIN("login"), LOGIN_SUCCESS("login success"),
  LOGIN_FAILED("login failed"), USER_JOINED("user joined"),
  TILE_SELECTION("tile selection"), TILE_PLACEMENT("tile placement"),
  NEXT_TURN("next turn"), ALLOWED_TILES("allowed tiles"),
  MOVE_NOT_ALLOWED("move not allowed"), ALLOWED_FIELDS("allowed fields"),
  BOARD_UPDATE("board update"), USER_LEFT("user left"),
  FILL_PLATES("fill plates"), POINTS("points"),
  GAME_ENDED("game ended"), GAME_RESTART("game restart"),
  GAME_RESTART_REQUEST("game restart request"), TIMER_END("timer end"),
  PLAYERS("players"), TILES_NOT_ALLOWED("tiles not allowed"),
  GAME_CANCEL_REQUEST("game cancel request"), GAME_CANCEL("game cancel"),
  TIMER_START("timer start"), FLOOR_LINE_UPDATE("floor line update");


  public static final String TYPE_FIELD = "type";

  public static final String NICK_FIELD = "nick";

  public static final String PLATE_FIELD = "plate";

  public static final String COLOR_FIELD = "color";

  public static final String TILES_FIELD = "tiles";

  public static final String ROWS_FIELD = "row";

  public static final String COLUMNS_FIELD = "column";

  public static final String PATTERN_ROWS_FIELD = "pattern rows";

  public static final String PATTERN_COLUMNS_FIELD = "pattern columns";

  public static final String SCORES_FIELD = "points";

  public static final String AMOUNTS = "amount";

  public static final String FLOORTILE = "floortile";

  private final String jsonName;

  private static final String WINNERS = "winners";

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
  public static JSONObject selectTile(String color, int plate) {
    try {
      JSONObject message = createMessageOfType(TILE_SELECTION);
      message.put(PLATE_FIELD, String.valueOf(plate));
      message.put(COLOR_FIELD, color);

      return message;
    } catch (JSONException e) {
      throw new IllegalArgumentException("Failed to create a json object.", e);
    }
  }

  /**
   * Creates a message to be sent when a tile placement has been done.
   *
   * @param color of the desired tiles
   * @param row   rows of the desired tiles
   * @return JsonMessage to be sent
   */
  public static JSONObject placeTiles(String color, int row) {
    try {
      JSONObject message = createMessageOfType(TILE_PLACEMENT);
      message.put(ROWS_FIELD, String.valueOf(row));
      message.put(COLOR_FIELD, color);

      return message;
    } catch (JSONException e) {
      throw new IllegalArgumentException("Failed to create a json object.", e);
    }
  }

  /**
   * Creates a message to be sent when it has been declared
   * how many players want to play in hotseat mode.
   *
   * @param players of the desired tiles
   * @return JsonMessage to be sent
   */
  public static JSONObject players(int players) {
    try {
      JSONObject message = createMessageOfType(PLAYERS);
      message.put(AMOUNTS, String.valueOf(players));
      return message;
    } catch (JSONException e) {
      throw new IllegalArgumentException("Failed to create a json object.", e);
    }
  }

  /**
   * Creates a message to be sent when a player wants to send a game restart request.
   *
   * @param nickname of the player
   * @return JsonMessage to be sent
   */
  public static JSONObject gameRestartRequest(String nickname) {
    try {
      JSONObject message = createMessageOfType(GAME_RESTART_REQUEST);
      message.put(NICK_FIELD, nickname);
      return message;
    } catch (JSONException e) {
      throw new IllegalArgumentException("Failed to create a json object.", e);
    }
  }

  /**
   * Creates a message to be sent when a player wants to send a game cancel request.
   *
   * @param nickname of the player
   * @return JsonMessage to be sent
   */
  public static JSONObject gameCancelRequest(String nickname) {
    try {
      JSONObject message = createMessageOfType(GAME_CANCEL_REQUEST);
      message.put(NICK_FIELD, nickname);
      return message;
    } catch (JSONException e) {
      throw new IllegalArgumentException("Failed to create a json object.", e);
    }
  }

  /**
   * Creates the message to be sent to all players when the game has ended.
   *
   * @param endScores end scores of the players
   * @param usernames usernames of the players
   * @param winners   winners
   */
  public static JSONObject gameEndedMessage(int[] endScores, ArrayList<String> winners,
                                            ArrayList<String> usernames) {
    try {
      String winnersField = winners.toString().replaceAll("\\[|]| ", "");
      String endScoresField = Arrays.toString(endScores).replaceAll("\\[|]| ", "");
      String usernamesField = usernames.toString().replaceAll("\\[|]| ", "");
      JSONObject message = createMessageOfType(GAME_ENDED);
      message.put(WINNERS, winnersField);
      message.put(SCORES_FIELD, endScoresField);
      message.put(NICK_FIELD, usernamesField);
      return message;
    } catch (JSONException e) {
      throw new IllegalArgumentException("Failed to create a json object.", e);
    }
  }

  /**
   * Creates the message to be sent to all players when the game has been canceled.
   *
   * @return the message
   */
  public static JSONObject gameCancel() {
    try {
      JSONObject message = createMessageOfType(GAME_CANCEL);
      return message;
    } catch (JSONException e) {
      throw new IllegalArgumentException("Failed to create a json object.", e);
    }
  }

  /**
   * Creates the message to be sent to all players when the game has been restarted.
   *
   * @return the message
   */
  public static JSONObject gameRestart() {
    try {
      JSONObject message = createMessageOfType(GAME_RESTART);
      return message;
    } catch (JSONException e) {
      throw new IllegalArgumentException("Failed to create a json object.", e);
    }
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
   * Gets the amount of tiles to be placed in a given place in the board from a JsonMessage.
   *
   * @param object JsonMessage
   * @return amount of tiles as a string
   */
  public static String getTiles(JSONObject object) {
    try {
      return String.valueOf(object.get(TILES_FIELD));
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
      return String.valueOf(object.get(ROWS_FIELD));
    } catch (JSONException e) {
      throw new IllegalArgumentException("Failed to read a json object.", e);
    }
  }


  /**
   * Gets the columns of the pattern fields where tiles should be placed from a JsonMessage.
   *
   * @param object JsonMessage
   * @return columns as a string
   */
  public static String getPatternColumns(JSONObject object) {
    try {
      return object.getString(PATTERN_COLUMNS_FIELD);
    } catch (JSONException e) {
      throw new IllegalArgumentException("Failed to read a json object.", e);
    }
  }

  /**
   * Gets the scores of the players from a JsonMessage.
   *
   * @param object JsonMessage
   * @return scores as a string
   */
  public static String getScores(JSONObject object) {
    try {
      return object.getString(SCORES_FIELD);
    } catch (JSONException e) {
      throw new IllegalArgumentException("Failed to read a json object.", e);
    }
  }

  /**
   * Gets the floortiles out of the JsonMessage.
   *
   * @param object the JSONObject
   * @return the floortiles
   */
  public static String getFloorTile(JSONObject object, int index) {
    try {
      return object.getString(FLOORTILE + index);
    } catch (JSONException e) {
      throw new IllegalArgumentException("Failed to read a json object.", e);
    }
  }

  /**
   * Gets the amounts of tiles out of the JsonMessage.
   *
   * @param object the JSONObject
   * @return the winners
   */
  public static String getAmounts(JSONObject object) {
    try {
      return String.valueOf(object.get(AMOUNTS));
    } catch (JSONException e) {
      throw new IllegalArgumentException("Failed to read a json object.", e);
    }
  }

  /**
   * Gets the winners of the game out of the JsonMessage.
   *
   * @param object the JSONObject
   * @return the winners
   */
  public static String getWinners(JSONObject object) {
    try {
      return String.valueOf(object.get(WINNERS));
    } catch (JSONException e) {
      throw new IllegalArgumentException("Failed to read a json object.", e);
    }
  }


  /**
   * Gets the type of JsonMessage.
   *
   * @return Json Type
   */
  public String getJsonName() {
    return jsonName;
  }
}