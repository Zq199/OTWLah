/**
 * The Party class represents a party session within the OTA Lah app. Party sessions
 * are used for group travel coordination and synchronization of arrivals at a shared destination.
 */
package com.project.sc2006.entities;

import android.location.Location;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.libraries.places.api.model.Place;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;

public class Party{
    private int partyID;
    private ArrayList<PartyParticipantDetails> membersDetails;
    private LatLng destination;
    private String destinationName;
    private String destinationAddress;
    private String partyEta;

    private int partyLeader;

    /**
     * Constructs a new party session with the specified details.
     *
     * @param partyID   The unique identifier for the party session.
     * @param location  The destination location of the party.
     * @param partyEta  The estimated time of arrival at the destination.
     */
    public Party(int partyID, Location location, String destinationName, String destinationAddress, String partyEta, int partyLeader) {
        this.destination = new LatLng(location.getLatitude(), location.getLongitude());
        this.partyEta = partyEta;
        this.destinationName = destinationName;
        this.destinationAddress = destinationAddress;
        this.partyID = partyID;
        this.partyLeader = partyLeader;
        membersDetails = new ArrayList<>();
    }

    public void updateParty(int partyID, Location location, String destinationName, String destinationAddress, String partyEta, int partyLeader) {
        this.destination = new LatLng(location.getLatitude(), location.getLongitude());
        this.partyEta = partyEta;
        this.destinationName = destinationName;
        this.destinationAddress = destinationAddress;
        this.partyID = partyID;
        this.partyLeader = partyLeader;
    }


    public Party() {
        membersDetails = new ArrayList<>();
    }

    /**
     * Updates the details of the party session, such as participant information, location,
     * estimated time of arrival, and the leader status.
     *
     * @param updateParticipantID   The unique identifier of the participant to update.
     * @param updateParticipantName The name of the participant to update.
     * @param location             The new location of the participant.
     * @param eta                  The new estimated time of arrival for the participant.
     * @param isLeader             A flag indicating whether the participant is the leader.
     */
    public void updateDetails(int updateParticipantID, String updateParticipantName, LatLng location, LocalDateTime eta, boolean isLeader) {
        for (int i = 0; i < membersDetails.size(); i++) {
            if (membersDetails.get(i).getUserID() == updateParticipantID) {
                membersDetails.get(i).setEta(eta);
                membersDetails.get(i).setLocation(location);
                return;
            }
        }
        PartyParticipantDetails partyParticipantDetails = new PartyParticipantDetails(updateParticipantID, updateParticipantName, location, eta, isLeader);
        membersDetails.add(partyParticipantDetails);
    }

    public void clearMemberList() {
        this.membersDetails.clear();
    }

    public int getPartyID() {
        return partyID;
    }

    public ArrayList<PartyParticipantDetails> getMembersDetails() {
        return membersDetails;
    }

    public String getDestinationName() {
        return destinationName;
    }

    public LatLng getDestination() {
        return destination;
    }

    public void removePartyMember(PartyParticipantDetails member){
        membersDetails.remove(member);
    }

    public void setPartyEta(String partyEta) {
        this.partyEta = partyEta;
    }

    public String getPartyEta() {
        return partyEta;
    }

    public void setDestination(LatLng destination) {
        this.destination = destination;
    }

    public void setDestinationName(String destinationName) {
        this.destinationName = destinationName;
    }

    public String getDestinationAddress() {
        return destinationAddress;
    }

    public int getPartyLeader() {
        return partyLeader;
    }
}
