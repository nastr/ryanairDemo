package com.ryanair.provider;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ryanair.model.Iata;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.net.URL;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.Map;

public class TimeZoneIataMaperAmazonaws implements TimeZoneIataMaper {

    private Logger logger = LogManager.getLogger(TimeZoneIataMaperAmazonaws.class);

    private String url = "https://airports-api.s3-us-west-2.amazonaws.com/iata/";

    private Map<String, Iata> cache = new HashMap<>();

    @Autowired
    private ObjectMapper mapper;

    public Iata getTimeZoneFromIata(String iata) {
        Iata iata1 = null;

        try {
            if (cache.containsKey(iata))
                iata1 = cache.get(iata);
            else {
                iata1 = mapper.readValue(new URL(url + iata.toLowerCase() + ".json"), Iata.class);
                cache.put(iata, iata1);
            }
        } catch (IOException e) {
            logger.error(e.getMessage());
        }

        return iata1;
    }

    public ZoneId getZoneId(String iata) {
        return ZoneId.of(getTimeZoneFromIata(iata).getTimezone());
    }
}
