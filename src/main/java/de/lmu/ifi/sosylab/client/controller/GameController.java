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

    public void logInMultiplayer(String nickname){

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
    public void logInHotSeat(String nickname){

        new SwingWorker<Boolean, Void>() {

            @Override
            protected Boolean doInBackground() {
               // model.logInHotSeat(nicknames);
                return true;
            }

        }.execute();

    }
    public void setGameMode(String gameMode){
        model.setGameMode(gameMode);
    }


    public void dispose() {
        model.dispose();
    }
}




