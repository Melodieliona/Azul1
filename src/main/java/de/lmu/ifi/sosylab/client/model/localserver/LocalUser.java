package de.lmu.ifi.sosylab.client.model.localserver;

import java.io.OutputStreamWriter;

public class LocalUser {
    private final String name;

    private final int gameNumber;
    private final OutputStreamWriter writer;

    /**
     *
     * */
    public LocalUser(String name, OutputStreamWriter writer, int gameNumber) {
        this.name = name;
        this.gameNumber = gameNumber;
        this.writer = writer;
    }

    public String getName() {
        return name;
    }

    public int getGameNumber() { return gameNumber; }

    protected OutputStreamWriter getWriter() {
        return writer;
    }

}
