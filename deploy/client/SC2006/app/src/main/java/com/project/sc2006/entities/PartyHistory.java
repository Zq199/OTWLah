package com.project.sc2006.entities;

public class PartyHistory {
    private String destinationName;
    private String arrivalTime;
    private int partyID;
    private int imageResId;
    public PartyHistory(String destinationName, String arrivalTime, int imageResId, int partyID){
        this.imageResId = imageResId;
        this.destinationName = destinationName;
        this.arrivalTime = arrivalTime;
        this.partyID = partyID;
    }

    public int getPartyID() {
        return partyID;
    }

    public int getImageResId() {
        return imageResId;
    }

    public String getDestinationName() {
        return destinationName;
    }

    public String getArrivalTime() {
        return arrivalTime;
    }
}
