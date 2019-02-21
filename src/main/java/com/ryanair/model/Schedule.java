package com.ryanair.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class Schedule {

    private final List<Day> days;
    private final Integer month;
    @JsonIgnore
    private Integer year;

    @JsonCreator
    public Schedule(@JsonProperty("days") final List<Day> days,
                    @JsonProperty("month") final Integer month) {
        this.days = days;
        this.month = month;
    }

    @JsonProperty("days")
    public List<Day> getDays() {
        return days;
    }

    public Integer getMonth() {
        return month;
    }

    @JsonIgnore
    public Integer getYear() {
        return year;
    }

    @JsonIgnore
    public void setYear(Integer year) {
        this.year = year;
    }
}
