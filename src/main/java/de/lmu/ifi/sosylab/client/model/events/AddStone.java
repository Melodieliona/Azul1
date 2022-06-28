package de.lmu.ifi.sosylab.client.model.events;

public class AddStone extends GameEvents {

    private final String color;

    public String getName() {
        return "AddStoneEvent";
    }

    public AddStone(String color) {
        this.color = color;
    }

    public String getColor() {
        return color;
    }

}
