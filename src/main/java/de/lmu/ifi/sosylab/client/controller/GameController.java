package de.lmu.ifi.sosylab.client.controller;

import de.lmu.ifi.sosylab.client.model.GameModel;

import javax.swing.*;

public class GameController {

    /**
     * The controller of the chat-UI.
     */

    GameModel model;

    public GameController(GameModel model) {
        this.model = model;
    }

    public void login(String nickname){

        new SwingWorker<Boolean, Void>() {

            @Override
            protected Boolean doInBackground() {
                model.logInWithName(nickname);
                return true;
            }

        }.execute();

    }


    public void dispose() {
        model.dispose();
    }
}




