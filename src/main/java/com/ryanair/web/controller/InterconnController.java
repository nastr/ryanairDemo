package com.ryanair.web.controller;

import com.ryanair.content.VoyagesCalculator;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;

@Controller
@RequestMapping("/somevalidcontext/interconnections")
public class InterconnController {

    private Logger logger = LogManager.getLogger(InterconnController.class);


    @Autowired
    private VoyagesCalculator calculator;

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> showForm(@RequestParam("departure") String departure,
                                           @RequestParam("arrival") String arrival,
                                           @RequestParam("departureDateTime") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
                                                   LocalDateTime departureDateTime,
                                           @RequestParam("arrivalDateTime") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
                                                   LocalDateTime arrivalDateTime,
                                           @RequestParam(required = false, value = "hours", defaultValue = "12") Long hours) {
        Instant start = Instant.now();
        String json = calculator.getCombinedVoyagesJSON(departure, arrival, departureDateTime, arrivalDateTime, hours);
        Instant finish = Instant.now();
        Duration duration = Duration.between(start, finish);
        logger.printf(Level.DEBUG, "Voyages obtaining time:\t%02d:%02d:%02d:%02d", duration.toDays() * 24 +
                duration.toHours(), duration.toMinutes(), duration.getSeconds(), duration.getNano());
        return new ResponseEntity<>(json, HttpStatus.OK);
    }
    //http://localhost:8080/somevalidcontext/interconnections?departure=DUB&arrival=WRO&departureDateTime=2019-03-01T07:00&arrivalDateTime=2019-03-03T21:00
}
