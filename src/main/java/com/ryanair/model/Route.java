package com.ryanair.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Route {
    private final String airportFrom;
    private final String airportTo;
    private final String connectingAirport;
    private final String newRoute;
    private final String seasonalRoute;
    private final String operator;
    private final String group;
    private final String[] similarArrivalAirportCodes;
    private final String[] tags;

    @JsonCreator
    public Route(@JsonProperty("airportFrom") final String airportFrom,
                 @JsonProperty("airportTo") final String airportTo,
                 @JsonProperty("connectingAirport") final String connectingAirport,
                 @JsonProperty("newRoute") final String newRoute,
                 @JsonProperty("seasonalRoute") final String seasonalRoute,
                 @JsonProperty("operator") final String operator,
                 @JsonProperty("group") final String group,
                 @JsonProperty("similarArrivalAirportCodes") final String[] similarArrivalAirportCodes,
                 @JsonProperty("tags") final String[] tags) {
        this.airportFrom = airportFrom;
        this.airportTo = airportTo;
        this.connectingAirport = connectingAirport;
        this.newRoute = newRoute;
        this.seasonalRoute = seasonalRoute;
        this.operator = operator;
        this.group = group;
        this.similarArrivalAirportCodes = similarArrivalAirportCodes;
        this.tags = tags;
    }

    public String getAirportFrom() {
        return airportFrom;
    }

    public String getAirportTo() {
        return airportTo;
    }

    public String getConnectingAirport() {
        return connectingAirport;
    }

    public String getNewRoute() {
        return newRoute;
    }

    public String getSeasonalRoute() {
        return seasonalRoute;
    }

    public String getOperator() {
        return operator;
    }

    public String getGroup() {
        return group;
    }

    public String[] getSimilarArrivalAirportCodes() {
        return similarArrivalAirportCodes;
    }

    public String[] getTags() {
        return tags;
    }
}
