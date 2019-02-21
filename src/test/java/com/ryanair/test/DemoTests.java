package com.ryanair.test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ryanair.config.WebConfig;
import com.ryanair.content.VoyagesCalculator;
import com.ryanair.content.VoyagesCalculatorImpl;
import com.ryanair.model.Route;
import com.ryanair.model.Schedule;
import com.ryanair.model.Voyage;
import com.ryanair.provider.ExternalProvider;
import com.ryanair.provider.RyanairProvider;
import com.ryanair.provider.TimeZoneIataMaper;
import com.ryanair.provider.TimeZoneIataMaperAmazonaws;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.time.*;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@Execution(ExecutionMode.CONCURRENT)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class DemoTests {

    private ExternalProvider provider = new RyanairProvider();
    private String departure = "DUB";
    private String arrival = "WRO";
    private ZonedDateTime departureDateTime;
    private ZonedDateTime arrivalDateTime;
    private ObjectMapper mapper;

    @BeforeAll
    void setUp() throws ExecutionException, InterruptedException {
        Instant start = Instant.now();
        TimeZoneIataMaper iataMaper = new TimeZoneIataMaperAmazonaws();
        mapper = WebConfig.getObjectMapper();
        ReflectionTestUtils.setField(iataMaper, "mapper", mapper);

        CompletableFuture<ZonedDateTime> futureDepartureDateTime = CompletableFuture.supplyAsync(() ->
                ZonedDateTime.of(LocalDate.now().plusMonths(1L), LocalTime.now(), ZoneId.of(iataMaper.getTimeZoneFromIata(departure).getTimezone())));
        CompletableFuture<ZonedDateTime> futureArrivalDateTime = CompletableFuture.supplyAsync(() ->
                ZonedDateTime.of(LocalDate.now().plusMonths(1L).plusDays(3L), LocalTime.now(), ZoneId.of(iataMaper.getTimeZoneFromIata(arrival).getTimezone())));
        CompletableFuture.allOf(futureDepartureDateTime, futureArrivalDateTime).get();

        departureDateTime = futureDepartureDateTime.get();
        arrivalDateTime = futureArrivalDateTime.get();

        Instant finish = Instant.now();
        Duration duration = Duration.between(start, finish);
        System.out.printf("ZonedDateTime obtaining time:\t%02d:%02d:%02d:%02d\n", duration.toDays() * 24 + duration.toHours(),
                duration.toMinutes(), duration.getSeconds(), duration.getNano());

        ReflectionTestUtils.setField(provider, "mapper", mapper);
        ReflectionTestUtils.setField(provider, "iataMaper", iataMaper);

    }

    @Test
    public void interconnectionsTest() throws IOException {
        Instant start = Instant.now();

        VoyagesCalculator calculator = new VoyagesCalculatorImpl();
        ReflectionTestUtils.setField(calculator, "provider", provider);
        ReflectionTestUtils.setField(calculator, "mapper", mapper);

        List<Voyage> voyages = calculator.getCombinedVoyages(departure, arrival, departureDateTime.toLocalDateTime(), arrivalDateTime.toLocalDateTime(), 12L);

        Instant finish = Instant.now();
        Duration duration = Duration.between(start, finish);
        System.out.printf("Voyage obtaining time:\t%02d:%02d:%02d:%02d\tamount of available voyages: %02d\n", duration.toDays() * 24 +
                duration.toHours(), duration.toMinutes(), duration.getSeconds(), duration.getNano(), voyages.size());

        assertNotNull(voyages);
        assertFalse(voyages.isEmpty());

        String json = mapper.writeValueAsString(voyages);
        System.out.println(json);

//        json = calculator.getCombinedVoyagesJSON(departure, arrival, departureDateTime.toLocalDateTime(), arrivalDateTime.toLocalDateTime(), 12L);
//        System.out.println(json);
    }

    @Test
    public void externalProviderTest() throws ExecutionException, InterruptedException {
        Instant start = Instant.now();

        CompletableFuture<List<Route>> futureRoutes = CompletableFuture.supplyAsync(() -> provider.getRoutes());
        CompletableFuture<List<Schedule>> futureSchedules = CompletableFuture.supplyAsync(() ->
                provider.getSchedules(departure, arrival, departureDateTime.toLocalDateTime(), arrivalDateTime.toLocalDateTime()));
        CompletableFuture.allOf(futureRoutes, futureSchedules).get();

        List<Route> routes = futureRoutes.get();
        List<Schedule> schedules = futureSchedules.get();

        Instant finish = Instant.now();
        Duration duration = Duration.between(start, finish);
        System.out.printf("Route & Schedule obtaining time:\t%02d:%02d:%02d:%02d\n", duration.toDays() * 24 +
                duration.toHours(), duration.toMinutes(), duration.getSeconds(), duration.getNano());

        assertNotNull(routes);
        assertFalse(routes.isEmpty());

        assertNotNull(schedules);
        assertFalse(schedules.isEmpty());
    }


}
