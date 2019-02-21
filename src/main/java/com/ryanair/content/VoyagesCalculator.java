package com.ryanair.content;

import com.ryanair.model.Voyage;

import java.time.LocalDateTime;
import java.util.List;

public interface VoyagesCalculator {

    String getCombinedVoyagesJSON(String departure, String arrival, LocalDateTime departureDateTime, LocalDateTime arrivalDateTime, Long hoursForInterconnect);

    List<Voyage> getCombinedVoyages(String departure, String arrival, LocalDateTime departureDateTime, LocalDateTime arrivalDateTime, Long hoursForInterconnect);

    List<Voyage> getDirectVoyages(String departure, String arrival, LocalDateTime departureDateTime, LocalDateTime arrivalDateTime);

    List<Voyage> getInterconnVoyages(String departure, String arrival, LocalDateTime departureDateTime, LocalDateTime arrivalDateTime, Long hoursForInterconnect);
}
