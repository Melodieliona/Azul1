package de.lmu.ifi.sosylab.client.model;

import de.lmu.ifi.sosylab.client.model.events.GameEvents;
import org.json.JSONObject;

import static java.util.Objects.requireNonNull;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.BufferedReader;
import java.io.InputStreamReader;


public class GameModel {

  private static final int MAX_LENGTH = 100;

  private final PropertyChangeSupport support;

  private GameClientNetworkConnection connection;
  private String nickname;
  private boolean loggedIn;


  public GameModel() {
    support = new PropertyChangeSupport(this);

  }




  public void addPropertyChangeListener(PropertyChangeListener listener) {
    requireNonNull(listener);
    support.addPropertyChangeListener(listener);
  }

  /**
   * Remove a listener from the model. It will then no longer get notified about any events
   * fired by the model.
   *
   * @param listener the view that is to be unsubscribed from the model.
   */
  public void removePropertyChangeListener(PropertyChangeListener listener) {
    requireNonNull(listener);
    support.removePropertyChangeListener(listener);
  }

  private void notifyListeners(GameEvents event) {
    support.firePropertyChange(event.getName(), null, event);
  }

  private synchronized GameClientNetworkConnection getConnection() {
    return connection;
  }

  /**
   * Add a network connector to this model.
   *
   * @param connection The network connection to be added.
   */
  public synchronized void setConnection(GameClientNetworkConnection connection) {
    this.connection = connection;
  }

  public synchronized boolean isLoggedIn() {
    return loggedIn;
  }

  public synchronized void setLoggedIn(boolean loggedIn) {
    this.loggedIn = loggedIn;
  }

  /**
   * Send a login request to the server.
   *
   * @param nickname the chosen nickname of the chat participant.
   */
  public void logInWithName(final String nickname) {
    this.nickname = nickname;
    getConnection().sendLogin(nickname);
  }

  /**
   * Update the model accordingly when a login attempt is successful. This is afterwards published
   * to the subscribed listeners.
   */
  public void loggedIn() {
    setLoggedIn(true);
  }

  /**
   * Notify the subscribed observers that a login attempt has failed.
   */
  public void loginFailed() {
    setLoggedIn(false);
    notifyListeners(new LoginFailedEvent());
  }

  /**
   * Send a chat-message to the server that is to be broadcasted to the other chat participants.
   *
   * @param message The message to be broadcasted.
   */
  public void sendMove(String message) {

  }



  /**
   * Add a status-update entry "User joined" to the list of chat entries.
   * Used by the network layer to update the model accordingly.
   *
   * @param nickname The name of the newly joined user.
   */
  public void userJoined(String nickname) {

  }

  /**
   * Add a status-update entry "User has left the chat" to the list of chat entries.
   * Used by the network layer to update the model accordingly.
   *
   * @param nickname
   */
  public void userLeft(String nickname) {

  }

  /**
   * Cleanup the resources.
   */
  public void dispose() {
    getConnection().stop();
  }

  //Brainstorming Sara and Petra
  private void notifyListeners(GameEvents event) {
    support.firePropertyChange(event.getName(), null, event);
  }



}
