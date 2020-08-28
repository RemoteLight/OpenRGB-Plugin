package de.lars.openrgbwrapper.models;

public class Led {

    private String name;
    private Color color;

    public Led(String name, Color color) {
        this.name = name;
        this.color = color;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        this.color = color;
    }

    public static Led decode(byte[] data) {
        // TODO
        return null;
    }

}
