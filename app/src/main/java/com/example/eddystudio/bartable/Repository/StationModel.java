package com.example.eddystudio.bartable.Repository;


public class StationModel {
    private String location;
    private String weather;
    private String temp;

    public StationModel(String location, String weather, String temp) {
        this.location = location;
        this.weather = weather;
        this.temp = temp;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getWeather() {
        return weather;
    }

    public void setWeather(String weather) {
        this.weather = weather;
    }

    public String getTemp() {
        return temp;
    }

    public void setTemp(String temp) {
        this.temp = temp;
    }
}
