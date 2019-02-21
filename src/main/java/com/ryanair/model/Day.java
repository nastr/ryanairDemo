package com.ryanair.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class Day {
    private final List<Flight> flights;
    private final Integer day;

    @JsonCreator
    public Day(@JsonProperty("flights") final List<Flight> flights,
               @JsonProperty("day") final Integer day) {
        this.flights = flights;
        this.day = day;
    }

    @JsonProperty("flights")
    public List<Flight> getFlights() {
        return flights;
    }

    public Integer getDay() {
        return day;
    }
}
