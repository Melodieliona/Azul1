package de.lmu.ifi.sosylab.client.controller;

import de.lmu.ifi.sosylab.client.model.GameModel;
import de.lmu.ifi.sosylab.client.model.Player;
import de.lmu.ifi.sosylab.server.User;
import de.lmu.ifi.sosylab.shared.TileCollection;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.SwingWorker;

/**
 * The controller of the Azul-UI.
 */
public class GameController {



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

  public List<User> getUserList() {
    //TODO get User List through Connection, is there a method getUsers in Server?
    return null;
  }

  public void selectAllTiles(int source, String color) {
    model.selectTilesRequest(source, color);
  }

  public void restartGame() {
    System.out.println("restart request controller");
    model.requestGameRestart();
  }

  public void cancelGameRequest() {
    System.out.println("cancel request controller");
    model.requestGameCancel();
  }


  public String getCurrentPlayer() {
    return model.getCurrentPlayer();
  }

  public TileCollection[] getTilePlates() {
    return model.getTilePlates();
  }

  public void dispose() {
    model.dispose();
  }

  public void placeTiles(int numberOfSelectedTiles, int line) {
    model.placeTilesRequest(numberOfSelectedTiles, line);
  }

  public Player getPlayer(int player) {

    return model.getPlayers().get(player);
  }
  public ArrayList<Player> getPlayers(){return model.getPlayers(); }
  public int[] getValidRow() {
    return model.getValidRows();
  }

  public int[] getValidTiles() {
    return model.getValidPlates();
  }

  public ArrayList<String> getWinners() {
    return model.getNicksFromWinners();
  }

  public String getGameMode() {
    return model.getGameMode();
  }

  public String getNickname() {
    return model.getNickname();
  }

  public void restartGameRequest() {
    model.requestGameCancel();
  }
}




