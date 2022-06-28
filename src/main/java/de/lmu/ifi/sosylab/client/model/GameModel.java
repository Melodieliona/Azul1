package de.lmu.ifi.sosylab.client.model;

import de.lmu.ifi.sosylab.client.model.events.GameEvents;
import org.json.JSONObject;

import static java.util.Objects.requireNonNull;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.BufferedReader;
import java.io.InputStreamReader;


public class GameModel {

  private final PropertyChangeSupport support;



  public GameModel() {
    support = new PropertyChangeSupport(this);

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

  //Brainstorming Sara and Petra
  private void notifyListeners(GameEvents event) {
    support.firePropertyChange(event.getName(), null, event);
  }



}
