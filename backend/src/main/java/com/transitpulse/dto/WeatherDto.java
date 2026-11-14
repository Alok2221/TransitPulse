package com.transitpulse.dto;

public class WeatherDto {

    private String city;
    private String condition;
    private String labelPl;
    private int temperatureC;
    private boolean bikeFriendly;

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getCondition() {
        return condition;
    }

    public void setCondition(String condition) {
        this.condition = condition;
    }

    public String getLabelPl() {
        return labelPl;
    }

    public void setLabelPl(String labelPl) {
        this.labelPl = labelPl;
    }

    public int getTemperatureC() {
        return temperatureC;
    }

    public void setTemperatureC(int temperatureC) {
        this.temperatureC = temperatureC;
    }

    public boolean isBikeFriendly() {
        return bikeFriendly;
    }

    public void setBikeFriendly(boolean bikeFriendly) {
        this.bikeFriendly = bikeFriendly;
    }
}
