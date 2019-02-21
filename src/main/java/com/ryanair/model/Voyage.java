package com.ryanair.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class Voyage {

    private final Integer stops;
    private final List<Leg> legs;

    @JsonCreator
    public Voyage(@JsonProperty("stops") final Integer stops,
                  @JsonProperty("legs") final List<Leg> legs) {
        this.stops = stops;
        this.legs = legs;
    }

    public Integer getStops() {
        return stops;
    }

    public List<Leg> getLegs() {
        return legs;
    }
}
