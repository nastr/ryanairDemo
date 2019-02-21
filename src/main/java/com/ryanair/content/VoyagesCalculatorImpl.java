package com.ryanair.content;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ryanair.model.Leg;
import com.ryanair.model.Route;
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

        List<Voyage> direct;
        List<Voyage> interconn;
        try {
            CompletableFuture.allOf(directFutore, interconnFuture).get();
            direct = directFutore.get();
            interconn = interconnFuture.get();
        } catch (InterruptedException | ExecutionException e) {
            logger.error(e.getMessage());
            return null;
        }

        return Stream.concat(direct.stream(), interconn.stream()).collect(Collectors.toList());
    }

    public List<Voyage> getDirectVoyages(String departure, String arrival, LocalDateTime departureDateTime, LocalDateTime arrivalDateTime) {
        return provider.getSchedules(departure, arrival, departureDateTime, arrivalDateTime).stream().flatMap(d -> d.getDays().stream()
                .flatMap(d1 -> d1.getFlights().stream().map(d2 -> {
                    Leg l = new Leg(departure, arrival,
                            LocalDateTime.of(LocalDate.of(d.getYear(), d.getMonth(), d1.getDay()), d2.getDepartureTime()),
                            LocalDateTime.of(LocalDate.of(d.getYear(), d.getMonth(), d1.getDay()), d2.getArrivalTime()));
                    if (l.getDepartureDateTime().isAfter(departureDateTime) && l.getArrivalDateTime().isBefore(arrivalDateTime))
                        return new Voyage(0, Collections.singletonList(l));
                    else
                        return null;
                }).filter(Objects::nonNull))).collect(Collectors.toList());
    }

    public List<Voyage> getInterconnVoyages(String departure, String arrival, LocalDateTime departureDateTime, LocalDateTime arrivalDateTime, Long hoursForInterconnect) {
        List<Route> routes = provider.getRoutes();
        return routes.stream().parallel()
                .filter(r -> routes.stream().anyMatch(t -> t.getAirportFrom().equals(departure) &&
                        r.getAirportFrom().equals(t.getAirportTo()) && r.getAirportTo().equals(arrival)))
                .flatMap(r -> {
                    CompletableFuture<List<Schedule>> depaToIntrFut = CompletableFuture.supplyAsync(() ->
                            provider.getSchedules(departure, r.getAirportFrom(), departureDateTime, arrivalDateTime));
                    CompletableFuture<List<Schedule>> IntrToArivFut = CompletableFuture.supplyAsync(() ->
                            provider.getSchedules(r.getAirportFrom(), arrival, departureDateTime, arrivalDateTime));
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
                    return depaToIntr.stream().flatMap(d -> d.getDays().stream()
                            .flatMap(d1 -> d1.getFlights().stream().map(d2 -> {
                                Leg depaToIntrL = new Leg(departure, r.getAirportFrom(),
                                        LocalDateTime.of(LocalDate.of(d.getYear(), d.getMonth(), d1.getDay()), d2.getDepartureTime()),
                                        LocalDateTime.of(LocalDate.of(d.getYear(), d.getMonth(), d1.getDay()), d2.getArrivalTime()));
                                Optional<Leg> IntrToArivL = IntrToAriv.stream().flatMap(z -> z.getDays().stream()
                                        .flatMap(x -> x.getFlights().stream().map(e -> new Leg(r.getAirportFrom(), arrival,
                                                LocalDateTime.of(LocalDate.of(z.getYear(), z.getMonth(), x.getDay()), e.getDepartureTime()),
                                                LocalDateTime.of(LocalDate.of(z.getYear(), z.getMonth(), x.getDay()), e.getArrivalTime())))))
                                        .filter(a -> a.getDepartureDateTime().isAfter(depaToIntrL.getArrivalDateTime().plusHours(2)) &&
                                                a.getArrivalDateTime().isBefore(arrivalDateTime) &&
                                                a.getDepartureDateTime().isBefore(depaToIntrL.getArrivalDateTime().plusHours(hoursForInterconnect))).findFirst();
                                if (IntrToArivL.isPresent() && depaToIntrL.getDepartureDateTime().isAfter(departureDateTime))
                                    return new Voyage(1, asList(depaToIntrL, IntrToArivL.get()));
                                else
                                    return null;
                            })));
                })
                .filter(Objects::nonNull).collect(Collectors.toList());
    }
}
