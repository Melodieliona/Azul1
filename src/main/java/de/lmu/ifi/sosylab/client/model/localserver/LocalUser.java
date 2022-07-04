package de.lmu.ifi.sosylab.client.model.localserver;

import java.io.OutputStreamWriter;

/**
 * Stores the data corresponding to a single local user.
 * */

public class LocalUser {
    private final String name;


    /**
     * Represents a single local Player.
     * */
    public LocalUser(String name, OutputStreamWriter writer, int gameNumber) {
        this.name = name;
    }

    public String getName() {
        return name;
    }


}
