package de.lars.openrgbwrapper.models;

public class Mode {

    private String name;
    private int value;
    private int flags;
    private int speedMin;
    private int speedMax;
    private int colorMin;
    private int colorMax;
    private int speed;
    private int direction;
    private int colorMode;
    private Color[] colors;

    public Mode(String name, int value, int flags, int speedMin, int speedMax, int colorMin, int colorMax, int speed, int direction, int colorMode, Color[] colors) {
        this.name = name;
        this.value = value;
        this.flags = flags;
        this.speedMin = speedMin;
        this.speedMax = speedMax;
        this.colorMin = colorMin;
        this.colorMax = colorMax;
        this.speed = speed;
        this.direction = direction;
        this.colorMode = colorMode;
        this.colors = colors;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    public int getFlags() {
        return flags;
    }

    public void setFlags(int flags) {
        this.flags = flags;
    }

    public int getSpeedMin() {
        return speedMin;
    }

    public void setSpeedMin(int speedMin) {
        this.speedMin = speedMin;
    }

    public int getSpeedMax() {
        return speedMax;
    }

    public void setSpeedMax(int speedMax) {
        this.speedMax = speedMax;
    }

    public int getColorMin() {
        return colorMin;
    }

    public void setColorMin(int colorMin) {
        this.colorMin = colorMin;
    }

    public int getColorMax() {
        return colorMax;
    }

    public void setColorMax(int colorMax) {
        this.colorMax = colorMax;
    }

    public int getSpeed() {
        return speed;
    }

    public void setSpeed(int speed) {
        this.speed = speed;
    }

    public int getDirection() {
        return direction;
    }

    public void setDirection(int direction) {
        this.direction = direction;
    }

    public int getColorMode() {
        return colorMode;
    }

    public void setColorMode(int colorMode) {
        this.colorMode = colorMode;
    }

    public Color[] getColors() {
        return colors;
    }

    public void setColors(Color[] colors) {
        this.colors = colors;
    }

}
