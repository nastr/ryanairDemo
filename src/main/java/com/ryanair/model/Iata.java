package com.ryanair.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Iata {
    private final String airport_name;
    private final String city;
    private final String country;
    private final String iata;
    private final String icao;
    private final String latitude;
    private final String longitude;
    private final String elevation;
    private final String utc_offset;
    private final String _class;
    private final String timezone;

    @JsonCreator
    public Iata(@JsonProperty("airport_name") final String airport_name,
                @JsonProperty("city") final String city,
                @JsonProperty("country") final String country,
                @JsonProperty("iata") final String iata,
                @JsonProperty("icao") final String icao,
                @JsonProperty("latitude") final String latitude,
                @JsonProperty("longitude") final String longitude,
                @JsonProperty("elevation") final String elevation,
                @JsonProperty("utc_offset") final String utc_offset,
                @JsonProperty("_class") final String _class,
                @JsonProperty("timezone") final String timezone) {
        this.airport_name = airport_name;
        this.city = city;
        this.country = country;
        this.iata = iata;
        this.icao = icao;
        this.latitude = latitude;
        this.longitude = longitude;
        this.elevation = elevation;
        this.utc_offset = utc_offset;
        this._class = _class;
        this.timezone = timezone;
    }

    public String getAirport_name() {
        return airport_name;
    }

    public String getCity() {
        return city;
    }

    public String getCountry() {
        return country;
    }

    public String getIata() {
        return iata;
    }

    public String getIcao() {
        return icao;
    }

    public String getLatitude() {
        return latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public String getElevation() {
        return elevation;
    }

    public String getUtc_offset() {
        return utc_offset;
    }

    public String get_class() {
        return _class;
    }

    public String getTimezone() {
        return timezone;
    }
}
