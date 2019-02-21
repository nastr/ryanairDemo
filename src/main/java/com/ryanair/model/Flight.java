package com.ryanair.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalTimeSerializer;

import java.time.LocalTime;

public class Flight {

    private final String carrierCode;
    private final Integer number;

    @JsonSerialize(using = LocalTimeSerializer.class)
    @JsonDeserialize(using = LocalTimeDeserializer.class)
    private final LocalTime departureTime;

    @JsonSerialize(using = LocalTimeSerializer.class)
    @JsonDeserialize(using = LocalTimeDeserializer.class)
    private final LocalTime arrivalTime;

    @JsonCreator
    public Flight(@JsonProperty("carrierCode") final String carrierCode,
                  @JsonProperty("number") final Integer number,
                  @JsonProperty("departureTime") final LocalTime departureTime,
                  @JsonProperty("arrivalTime") final LocalTime arrivalTime) {
        this.carrierCode = carrierCode;
        this.number = number;
        this.departureTime = departureTime;
        this.arrivalTime = arrivalTime;
    }

    public String getCarrierCode() {
        return carrierCode;
    }

    public Integer getNumber() {
        return number;
    }

    public LocalTime getDepartureTime() {
        return departureTime;
    }

    public LocalTime getArrivalTime() {
        return arrivalTime;
    }
}
