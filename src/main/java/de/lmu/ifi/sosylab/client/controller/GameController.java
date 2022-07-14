package de.lmu.ifi.sosylab.client.controller;

import de.lmu.ifi.sosylab.client.model.GameModel;
import de.lmu.ifi.sosylab.client.model.Player;
import de.lmu.ifi.sosylab.server.User;
import de.lmu.ifi.sosylab.shared.LayingRow;
import de.lmu.ifi.sosylab.shared.TileCollection;
import java.io.IOException;
import java.util.List;
import javax.swing.SwingWorker;

/**
 * Todo JavaDoc
 */
public class GameController {

  /**
   * The controller of the chat-UI.
   */

  GameModel model;

  /**
   * Todo JavaDoc
   */
  public GameController(GameModel model) {
    this.model = model;
  }

  /**
   * Todo JavaDoc
   */
  public void logInMultiplayer(String nickname) {
    new SwingWorker<Boolean, Void>() {

      @Override
      protected Boolean doInBackground() {
        model.logInMultiplayer(nickname);
        return true;
      }

    }.execute();

  }

  //TODO: pass in string array with players names

  /**
   * Todo JavaDoc
   */
  public void logInHotSeat(List<String> playersName) {
    System.out.println("login HS controller");

    new SwingWorker<Boolean, Void>() {

      @Override
      protected Boolean doInBackground() {
        String[] namesArray = playersName.toArray(new String[0]);
        model.logInHotSeat(namesArray);
        return true;
      }

    }.execute();

  }

  public void setGameMode(String gameMode) throws IOException {
    model.setGameMode(gameMode);
  }

  //TODO
  public Player getPlayer(int player) {
    return /*model.getPlayer(player)*/ null;
  }

  public List<User> getUserList() {
    //TODO get User List through Connection, is there a method getUsers in Server?
    return null;
  }

  public void selectAllTiles(int source, String color) {
    System.out.println("tile request controller");
    model.selectTilesRequest(source, color);
  }

  public void setTilesToRow(int row) {
  }

  //left
  public LayingRow[] getLayingRow(int rowNumber) {
    return null;
  }

  //right
  public Object[][] getTileWall() {
    return null;
  }

  public int getCurrentScore(int board) {
    return 0;
  }

  public TileCollection[] getTilePlates() {
    return model.getTilePlates();
  }

  public void dispose() {
    model.dispose();
  }

  public void placeTiles(int numberOfSelectedTiles, int line) {
    model.placeTiles(numberOfSelectedTiles, line);
  }

}




