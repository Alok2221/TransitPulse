package com.transitpulse.dto;

public class CitySuggestionDto {

    private String name;
    private String label;
    private String country;
    private String countryCode;
    private double lat;
    private double lon;

    public CitySuggestionDto() {
    }

    public CitySuggestionDto(String name, String label, String country, String countryCode, double lat, double lon) {
        this.name = name;
        this.label = label;
        this.country = country;
        this.countryCode = countryCode;
        this.lat = lat;
        this.lon = lon;
    }

    public String getName() {
        return name;
    }

    public String getLabel() {
        return label;
    }

    public String getCountry() {
        return country;
    }

    public String getCountryCode() {
        return countryCode;
    }

    public double getLat() {
        return lat;
    }

    public double getLon() {
        return lon;
    }
}
