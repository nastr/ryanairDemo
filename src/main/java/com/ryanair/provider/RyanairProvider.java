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
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class RyanairProvider implements ExternalProvider {

    protected int cacheValidSec = 60;

    private Logger logger = LogManager.getLogger(RyanairProvider.class);

    @Autowired
    private ObjectMapper mapper;

    private List<Route> routes = new ArrayList<>(5000);
    private Instant routesLastReq;

    private Map<String, Schedule> schedulesMap = new HashMap<>(200);
    private Map<String, Instant> schedulesLastReq = new HashMap<>(200);

    public List<Route> getRoutes() {
        try {
            String routesUrl = "https://services-api.ryanair.com/locate/3/routes";
            if (routes.isEmpty() || routesLastReq.plusSeconds(cacheValidSec).isBefore(Instant.now())) {
                routes = Arrays.stream(mapper.readValue(new URL(routesUrl), Route[].class))
                        .filter(r -> r.getConnectingAirport() == null && r.getOperator().equalsIgnoreCase("RYANAIR"))
                        .collect(Collectors.toList());
                routesLastReq = Instant.now();
            }
            return routes;
        } catch (IOException e) {
            logger.error(e.getMessage());
            return null;
        }
    }

    public Schedule getSchedule(String departure, String arrival, Integer year, Integer month) {
        try {
            String key = departure + "/" + arrival + "/" + "years/" + year + "/" + "months/" + month;
            if (!schedulesMap.containsKey(key) || schedulesLastReq.get(key).plusSeconds(cacheValidSec).isBefore(Instant.now())) {
                String link = "https://services-api.ryanair.com/timtbl/3/schedules/" + key;
                URL url = new URL(link);
                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                schedulesLastReq.put(key, Instant.now());
                if (httpURLConnection.getResponseCode() != 200) {
                    logger.warn(link + "\t" + httpURLConnection.getResponseMessage());
                    schedulesMap.put(key, null);
                    return null;
                } else {
                    Schedule s = mapper.readValue(url, Schedule.class);
                    s.setYear(year);
                    schedulesMap.put(key, s);
                    return s;
                }
            } else
                return schedulesMap.get(key);
        } catch (IOException e) {
            logger.error(e.getMessage());
            return null;
        }
    }

    public List<Schedule> getSchedules(String departure, String arrival, LocalDateTime departureDateTime, LocalDateTime arrivalDateTime) {
        return Stream.iterate(departureDateTime, d -> d.plusMonths(1))
                .limit(ChronoUnit.MONTHS.between(departureDateTime, arrivalDateTime) + 1).parallel()
                .map(d -> getSchedule(departure, arrival, d.getYear(), d.getMonthValue()))
                .filter(Objects::nonNull).collect(Collectors.toList());
    }

    public List<Route> getInterconnRoutes(String departure, String arrival) {
        List<String> interconn = getRoutes().stream().filter(d -> d.getAirportFrom().equals(departure))
                .map(Route::getAirportTo)
                .collect(Collectors.toList());

        return getRoutes().stream()
                .filter(r -> r.getAirportTo().equals(arrival) && interconn.contains(r.getAirportFrom()))
                .collect(Collectors.toList());
    }
}
