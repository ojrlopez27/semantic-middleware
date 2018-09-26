package edu.cmu.experiments.services;

import java.util.Date;

import edu.cmu.experiments.apis.BookFlightService;
import edu.cmu.inmind.multiuser.controller.log.Log4J;

/**
 * Created by oscarr on 8/8/18.
 */
public class GoogleFlightsService implements BookFlightService {

    @Override
    public String searchFlight(String from, String destination,
                                   Date departureDate, Date returnDate, Double maxPrice){
        Log4J.warn(this, String.format("Executing GoogleFlightsService.searchFlight for: [from: %s, destination: %s, " +
                "departure date: %s, return date: %s, maxPrice: %s]", from, destination,
                departureDate, returnDate, maxPrice));
        return null;
    }

    @Override
    public String bookFlight(String from, String destination,
                                 Date departureDate, Date returnDate){
        Log4J.warn(this, String.format("Executing GoogleFlightsService.bookFlight for: [from: %s, destination: %s, " +
                "departure date: %s, return date: %s]", from, destination, departureDate, returnDate));
        return null;
    }
}