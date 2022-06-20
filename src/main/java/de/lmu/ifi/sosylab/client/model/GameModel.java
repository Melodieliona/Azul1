package de.lmu.ifi.sosylab.client.model;

import static java.util.Objects.requireNonNull;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;


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
}
