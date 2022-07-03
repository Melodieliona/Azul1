package de.lmu.ifi.sosylab.client.model.localserver;

import java.io.OutputStreamWriter;

/**
 * Stores the data corresponding to a single local user.
 * */

public class LocalUser {
    private final String name;

    private final int gameNumber;
    private final OutputStreamWriter writer;

    /**
     * Represents a single local Player.
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
