package com.ryanair.provider;

import com.ryanair.model.Iata;

import java.time.ZoneId;

public interface TimeZoneIataMaper {

    Iata getTimeZoneFromIata(String iata);

    ZoneId getZoneId(String iata);
}
