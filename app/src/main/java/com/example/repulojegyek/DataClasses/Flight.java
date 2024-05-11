package com.example.repulojegyek.DataClasses;

import com.google.firebase.Timestamp;

public class Flight {
    private String flightId;
    private String fromAirportCode;
    private String toAirportCode;
    private Timestamp departure;
    private Timestamp arrival;
    private String planeTypeName;

    public Flight() {}

    public Flight(String flightId, String fromAirportCode, String toAirportCode, Timestamp departure, Timestamp arrival, String planeTypeName) {
        this.flightId = flightId;
        this.fromAirportCode = fromAirportCode;
        this.toAirportCode = toAirportCode;
        this.departure = departure;
        this.arrival = arrival;
        this.planeTypeName = planeTypeName;
    }

    public String getFlightId() {
        return flightId;
    }

    public void setFlightId(String flightId) {
        this.flightId = flightId;
    }

    public String getFromAirportCode() {
        return fromAirportCode;
    }

    public void setFromAirportCode(String fromAirportCode) {
        this.fromAirportCode = fromAirportCode;
    }

    public String getToAirportCode() {
        return toAirportCode;
    }

    public void setToAirportCode(String toAirportCode) {
        this.toAirportCode = toAirportCode;
    }

    public Timestamp getDeparture() {
        return departure;
    }

    public void setDeparture(Timestamp departure) {
        this.departure = departure;
    }

    public Timestamp getArrival() {
        return arrival;
    }

    public void setArrival(Timestamp arrival) {
        this.arrival = arrival;
    }

    public String getPlaneTypeName() {
        return planeTypeName;
    }

    public void setPlaneTypeName(String planeTypeName) {
        this.planeTypeName = planeTypeName;
    }
}
