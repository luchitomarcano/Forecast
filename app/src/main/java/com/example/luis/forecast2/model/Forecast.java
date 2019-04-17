package com.example.luis.forecast2.model;

public class Forecast {

    private Main main;

    private Coord coord;

    private String description;
    private String icon;

    public Forecast(String description, String icon) {
        this.description = description;
        this.icon = icon;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public Main getMain() {
        return main;
    }

    public Coord getCoord() {
        return coord;
    }

}


