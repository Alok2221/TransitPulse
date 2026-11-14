package com.transitpulse.dto;

public class RouteMetaDto {

    private String distanceSource;
    private String weatherSource;
    private Double carDurationMinutesFromOsrm;

    public String getDistanceSource() {
        return distanceSource;
    }

    public void setDistanceSource(String distanceSource) {
        this.distanceSource = distanceSource;
    }

    public String getWeatherSource() {
        return weatherSource;
    }

    public void setWeatherSource(String weatherSource) {
        this.weatherSource = weatherSource;
    }

    public Double getCarDurationMinutesFromOsrm() {
        return carDurationMinutesFromOsrm;
    }

    public void setCarDurationMinutesFromOsrm(Double carDurationMinutesFromOsrm) {
        this.carDurationMinutesFromOsrm = carDurationMinutesFromOsrm;
    }
}
