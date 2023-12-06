/**
 * The PartyParticipantDetails class represents the details of a participant within a party session
 * in the OTA Lah app. It stores information about the participant's identity, location, estimated time
 * of arrival, and their role as a leader or member in the group.
 */
package com.project.sc2006.entities;

import com.google.android.gms.maps.model.LatLng;

import java.time.LocalDateTime;

public class PartyParticipantDetails {
    private LatLng location;
    private String userName;
    private int userID;

    private boolean isDisplayed;
    private boolean isLeader;
    private LocalDateTime eta;

    /**
     * Constructs a new PartyParticipantDetails object with the specified participant details.
     *
     * @param userID     The unique identifier for the participant.
     * @param userName   The name of the participant.
     * @param location   The current location of the participant as LatLng coordinates.
     * @param eta        The estimated time of arrival for the participant.
     * @param isLeader   A boolean flag indicating whether the participant is the leader of the party.
     */
    public PartyParticipantDetails(int userID, String userName, LatLng location, LocalDateTime eta, boolean isLeader) {
        this.userID = userID;
        this.userName = userName;
        this.location = location;
        this.eta = eta;
        this.isLeader = isLeader;
        this.isDisplayed = false;
    }

    public String getUserName() {
        return userName;
    }

    public void setIsDisplayed(boolean isDisplayed) {
        this.isDisplayed = isDisplayed;
    }

    public boolean getIsDisplayed() {
        return isDisplayed;
    }

    public int getUserID() {
        return userID;
    }

    public LatLng getLocation() {
        return location;
    }

    public LocalDateTime getEta() {
        return eta;
    }

    public void setEta(LocalDateTime eta) {
        this.eta = eta;
    }

    public void setLocation(LatLng location) {
        this.location = location;
    }

    public boolean getIsLeader(){
        return isLeader;
    }
}
