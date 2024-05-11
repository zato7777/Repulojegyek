package com.example.repulojegyek.DataClasses;

public class Ticket {
    private String id;
    private String ownerName;
    private String flightId;
    private String seat;

    public Ticket() {}

    public Ticket(String id, String ownerName, String flightId, String seat) {
        this.id = id;
        this.ownerName = ownerName;
        this.flightId = flightId;
        this.seat = seat;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public void setOwnerName(String ownerName) {
        this.ownerName = ownerName;
    }

    public String getFlightId() {
        return flightId;
    }

    public void setFlightId(String flightId) {
        this.flightId = flightId;
    }

    public String getSeat() {
        return seat;
    }

    public void setSeat(String seat) {
        this.seat = seat;
    }
}
