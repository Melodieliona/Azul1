package de.lmu.ifi.sosylab.client.controller;

import de.lmu.ifi.sosylab.client.model.GameModel;
import de.lmu.ifi.sosylab.client.model.Player;
import de.lmu.ifi.sosylab.server.User;
import de.lmu.ifi.sosylab.shared.LayingRow;
import de.lmu.ifi.sosylab.shared.TileCollection;
import java.util.List;
import javax.swing.SwingWorker;

public class GameController {

  /**
   * The controller of the chat-UI.
   */

  GameModel model;

  public GameController(GameModel model) {
    this.model = model;
  }

  public void logInMultiplayer(String nickname) {

    //TODO: get rid of testing sout
    System.out.println("Login Multoplayer Controller");
    new SwingWorker<Boolean, Void>() {

      @Override
      protected Boolean doInBackground() {
        model.logInMultiplayer(nickname);
        return true;
      }

    }.execute();

  }

  //TODO: pass in string array with players names
  public void logInHotSeat(String nickname) {

    new SwingWorker<Boolean, Void>() {

      @Override
      protected Boolean doInBackground() {
        // model.logInHotSeat(nicknames);
        return true;
      }

    }.execute();

  }

  public void setGameMode(String gameMode) {
    model.setGameMode(gameMode);
  }

  //TODO
  public Player getPlayer(int player) {
    return /*model.getPlayer(player)*/ null;
  }

  public List<User> getUserList (){
    return null;
  }

  public TileCollection[] selectAllTilesWithColor(String color){
    return null;
  }

  public void setTilesToRow(int row){
  }
//left
  public LayingRow[] getLayingRow (int rowNumber){
    return null;
  }
//right
  public Object[][] getTileWall(){
    return null;
  }

  public int getCurrentScore(int board){
    int score = 0;
    return score;
  }

  public TileCollection[] getTilePlates(){
    //0 ist Haufen
    return null;
  }

  public String getTileColor(){
    return null;
  }

  public void dispose() {
    model.dispose();
  }

  public String getTile(int plateNumber, int i) {
    //dadurch dann auch Farbe holen
    return null;
  }
}




