package de.lmu.ifi.sosylab.client.model;

import de.lmu.ifi.sosylab.client.model.events.GameEvents;
import de.lmu.ifi.sosylab.client.model.events.LoggedInEvent;
import de.lmu.ifi.sosylab.client.model.events.LoginFailedEvent;
import org.json.JSONObject;

import static java.util.Objects.requireNonNull;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.BufferedReader;
import java.io.InputStreamReader;


public class GameModel {

  private final PropertyChangeSupport support;

  //ClientNetworkConnection Needed
  // private ClientNetworkConnection connection;
  private String nickname;
  private boolean loggedIn;
  private GameClientNetworkConnection connection;




  public GameModel() {
    support = new PropertyChangeSupport(this);
    loggedIn = false;

  }

  public synchronized boolean isLoggedIn() {
    return loggedIn;
  }
  public synchronized void setLoggedIn(boolean loggedIn) {
    this.loggedIn = loggedIn;
  }
  public synchronized void setConnection(GameClientNetworkConnection connection) {
    this.connection = connection;
  }
  private synchronized GameClientNetworkConnection getConnection() {
    return connection;
  }

  public void addPropertyChangeListener(PropertyChangeListener listener) {
    requireNonNull(listener);
    support.addPropertyChangeListener(listener);
  }

  public void removePropertyChangeListener(PropertyChangeListener listener) {
    requireNonNull(listener);
    support.removePropertyChangeListener(listener);
  }

  public void dispose() {
    //...
  }

  public void logInWithName(final String nickname) {
    this.nickname = nickname;
    getConnection().sendLogin(nickname);

    //testing purposes -> until loggedIn() Method works
    notifyListeners(new LoggedInEvent());
  }

  //ClientNetworkConnection Needed
  public void loggedIn() {
    setLoggedIn(true);
    notifyListeners(new LoggedInEvent());
  }

  //ClientNetworkConnection Needed
  public void loginFailed() {
    setLoggedIn(false);
    notifyListeners(new LoginFailedEvent());
  }

  private void notifyListeners(GameEvents event) {
    support.firePropertyChange(event.getName(), null, event);
  }



}
