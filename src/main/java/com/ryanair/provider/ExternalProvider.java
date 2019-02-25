package com.ryanair.provider;

import com.ryanair.model.Route;
import com.ryanair.model.Schedule;

import java.time.LocalDateTime;
import java.util.List;

public interface ExternalProvider {

    List<Route> getRoutes();

    Schedule getSchedule(String departure, String arrival, Integer year, Integer month);

    List<Schedule> getSchedules(String departure, String arrival, LocalDateTime departureDateTime, LocalDateTime arrivalDateTime);

    List<Route> getInterconnRoutes(String departure, String arrival);
}
