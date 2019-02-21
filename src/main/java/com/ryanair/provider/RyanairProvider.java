package com.ryanair.provider;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ryanair.model.Route;
import com.ryanair.model.Schedule;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class RyanairProvider implements ExternalProvider {

    private Logger logger = LogManager.getLogger(RyanairProvider.class);

    @Autowired
    private ObjectMapper mapper;

    private String routesUrl = "https://services-api.ryanair.com/locate/3/routes";

    private List<Route> routes = new ArrayList<>(5000);
    private Instant timeStamp = Instant.now();

    private static String getSchedulesUrl(String departure, String arrival, Integer year, Integer month) {
        return "https://services-api.ryanair.com/timtbl/3/schedules/" +
                departure + "/" +
                arrival + "/" +
                "years/" +
                year + "/" +
                "months/" +
                month;
    }

    public List<Route> getRoutes() {
        try {
            if (routes.isEmpty() || timeStamp.plusSeconds(60).isBefore(Instant.now()))
                routes = Arrays.stream(mapper.readValue(new URL(routesUrl), Route[].class))
                        .filter(r -> r.getConnectingAirport() == null && r.getOperator().equalsIgnoreCase("RYANAIR"))
                        .collect(Collectors.toList());
            return routes;
        } catch (IOException e) {
            logger.error(e.getMessage());
            return null;
        }
    }

    public Schedule getSchedule(String departure, String arrival, Integer year, Integer month) {
        try {
            URL url = new URL(getSchedulesUrl(departure, arrival, year, month));

            if (((HttpURLConnection) url.openConnection()).getResponseCode() != 200)
                return null;

            Schedule s = mapper.readValue(url, Schedule.class);
            s.setYear(year);
            return s;
        } catch (IOException e) {
            logger.error(e.getMessage());
            return null;
        }
    }

    public List<Schedule> getSchedules(String departure, String arrival,
                                       LocalDateTime departureDateTime, LocalDateTime arrivalDateTime) {
        return Stream.iterate(departureDateTime, d -> d.plusMonths(1)).parallel()
                .limit(ChronoUnit.MONTHS.between(departureDateTime, arrivalDateTime) + 1)
                .map(d -> getSchedule(departure, arrival, d.getYear(), d.getMonthValue()))
                .filter(Objects::nonNull).collect(Collectors.toList());
    }
}
