package edu.cmu.inmind.demo.services;

import edu.cmu.inmind.demo.common.DemoConstants;
import edu.cmu.inmind.demo.markers.BatteryQoS;
import edu.cmu.inmind.demo.markers.ConnectivityQoS;
import edu.cmu.inmind.demo.apis.BookHotelService;

import edu.cmu.inmind.demo.pojos.HotelPOJO;
import edu.cmu.inmind.demo.pojos.LocationPOJO;
import edu.cmu.inmind.multiuser.controller.log.Log4J;

import java.util.Date;

/**
 * Created by oscarr on 8/8/18.
 */
public class BookingsService implements BookHotelService{

    @Override
    @BatteryQoS( minBatteryLevel = DemoConstants.WORKS_WITH_LOW_CHARGE)
    @ConnectivityQoS( wifiStatus = DemoConstants.NOT_REQUIRES_WIFI_CONNECTIVITY)
    public HotelPOJO searchHotel(LocationPOJO destination, Date checkin, Date checkout, Double maxPrice){
        Log4J.warn(this, String.format("Executing BookingsService.searchHotel for: [destination: %s, " +
                "checkin: %s, checkout: %s, maxPrice: %s]", destination.getPlace(), checkin, checkout, maxPrice));
        return null;
    }

    @Override
    @BatteryQoS( minBatteryLevel = DemoConstants.WORKS_WITH_LOW_CHARGE)
    @ConnectivityQoS( wifiStatus = DemoConstants.NOT_REQUIRES_WIFI_CONNECTIVITY)
    public HotelPOJO bookHotel(LocationPOJO destination, Date checkin, Date checkout){
        Log4J.warn(this, String.format("Executing BookingsService.bookHotel for: [destination: %s, " +
                "checkin: %s, checkout: %s]", destination.getPlace(), checkin, checkout));
        return null;
    }

    @Override
    public void execute() {
        Log4J.warn(this, "Executing BookingsService...");
    }

}