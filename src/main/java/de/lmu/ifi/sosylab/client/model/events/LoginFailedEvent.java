package de.lmu.ifi.sosylab.client.model.events;

public class LoginFailedEvent extends GameEvents {
    @Override
    public String getName() {
        return "LoginFailedEvent";
    }
}
