package com.ryanair.content;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ryanair.model.Leg;
import com.ryanair.model.Schedule;
import com.ryanair.model.Voyage;
import com.ryanair.provider.ExternalProvider;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Arrays.asList;

public class VoyagesCalculatorImpl implements VoyagesCalculator {

    private Logger logger = LogManager.getLogger(VoyagesCalculatorImpl.class);

    @Autowired
    private ExternalProvider provider;

    @Autowired
    private ObjectMapper mapper;

    public String getCombinedVoyagesJSON(String departure, String arrival, LocalDateTime departureDateTime, LocalDateTime arrivalDateTime, Long hoursForInterconnect) {
        try {
            return mapper.writeValueAsString(getCombinedVoyages(departure, arrival, departureDateTime, arrivalDateTime, hoursForInterconnect));
        } catch (JsonProcessingException e) {
            logger.error(e.getMessage());
            return null;
        }
    }

    public List<Voyage> getCombinedVoyages(String departure, String arrival, LocalDateTime departureDateTime, LocalDateTime arrivalDateTime, Long hoursForInterconnect) {
        CompletableFuture<List<Voyage>> directFutore = CompletableFuture.supplyAsync(() -> getDirectVoyages(departure, arrival, departureDateTime, arrivalDateTime));
        CompletableFuture<List<Voyage>> interconnFuture = CompletableFuture.supplyAsync(() -> getInterconnVoyages(departure, arrival, departureDateTime, arrivalDateTime, hoursForInterconnect));

        try {
            CompletableFuture.allOf(directFutore, interconnFuture).get();
            return Stream.concat(directFutore.get().stream(), interconnFuture.get().stream()).collect(Collectors.toList());
        } catch (InterruptedException | ExecutionException e) {
            logger.error(e.getMessage());
            return null;
        }
    }

    public List<Voyage> getDirectVoyages(String departure, String arrival, LocalDateTime departureDateTime, LocalDateTime arrivalDateTime) {
        return provider.getSchedules(departure, arrival, departureDateTime, arrivalDateTime)
                .stream().flatMap(schedule -> schedule.getDays()
                        .stream().flatMap(day -> day.getFlights()
                                .stream().map(flight -> {
                                    Leg l = new Leg(departure, arrival,
                                            LocalDateTime.of(LocalDate.of(schedule.getYear(), schedule.getMonth(), day.getDay()), flight.getDepartureTime()),
                                            LocalDateTime.of(LocalDate.of(schedule.getYear(), schedule.getMonth(), day.getDay()), flight.getArrivalTime()));
                                    if (l.getDepartureDateTime().isAfter(departureDateTime) && l.getArrivalDateTime().isBefore(arrivalDateTime))
                                        return new Voyage(0, Collections.singletonList(l));
                                    else
                                        return null;
                                }).filter(Objects::nonNull))).collect(Collectors.toList());
    }

    public List<Voyage> getInterconnVoyages(String departure, String arrival, LocalDateTime departureDateTime, LocalDateTime arrivalDateTime, Long hoursForInterconnect) {
        return provider.getInterconnRoutes(departure, arrival)
                .stream().parallel().flatMap(route -> {
                    CompletableFuture<List<Schedule>> depaToIntrFut = CompletableFuture.supplyAsync(() ->
                            provider.getSchedules(departure, route.getAirportFrom(), departureDateTime, arrivalDateTime));
                    CompletableFuture<List<Schedule>> IntrToArivFut = CompletableFuture.supplyAsync(() ->
                            provider.getSchedules(route.getAirportFrom(), arrival, departureDateTime, arrivalDateTime));
                    List<Schedule> depaToIntr;
                    List<Schedule> IntrToAriv;
                    try {
                        CompletableFuture.allOf(depaToIntrFut, IntrToArivFut).get();
                        depaToIntr = depaToIntrFut.get();
                        IntrToAriv = IntrToArivFut.get();
                    } catch (InterruptedException | ExecutionException e) {
                        logger.error(e.getMessage());
                        return null;
                    }
                    return depaToIntr
                            .stream().flatMap(schedule -> schedule.getDays()
                                    .stream().flatMap(day -> day.getFlights()
                                            .stream().parallel().map(flight -> {
                                                Leg depaToIntrL = new Leg(departure, route.getAirportFrom(),
                                                        LocalDateTime.of(LocalDate.of(schedule.getYear(), schedule.getMonth(), day.getDay()), flight.getDepartureTime()),
                                                        LocalDateTime.of(LocalDate.of(schedule.getYear(), schedule.getMonth(), day.getDay()), flight.getArrivalTime()));
                                                Optional<Leg> IntrToArivL = IntrToAriv
                                                        .stream().flatMap(schedule1 -> schedule1.getDays()
                                                                .stream().flatMap(day1 -> day1.getFlights()
                                                                        .stream().map(flight1 -> new Leg(route.getAirportFrom(), arrival,
                                                                                LocalDateTime.of(LocalDate.of(schedule1.getYear(), schedule1.getMonth(), day1.getDay()), flight1.getDepartureTime()),
                                                                                LocalDateTime.of(LocalDate.of(schedule1.getYear(), schedule1.getMonth(), day1.getDay()), flight1.getArrivalTime())))))
                                                        .filter(leg -> leg.getDepartureDateTime().isAfter(depaToIntrL.getArrivalDateTime().plusHours(2)) &&
                                                                leg.getArrivalDateTime().isBefore(arrivalDateTime) &&
                                                                leg.getDepartureDateTime().isBefore(depaToIntrL.getArrivalDateTime().plusHours(hoursForInterconnect))).findFirst();
                                                if (IntrToArivL.isPresent() && depaToIntrL.getDepartureDateTime().isAfter(departureDateTime))
                                                    return new Voyage(1, asList(depaToIntrL, IntrToArivL.get()));
                                                else
                                                    return null;
                                            })));
                })
                .filter(Objects::nonNull).collect(Collectors.toList());
    }
}
