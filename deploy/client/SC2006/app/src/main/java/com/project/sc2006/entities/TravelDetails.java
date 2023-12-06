/**
 * The TravelDetails class represents details related to travel within the OTA Lah app.
 * It includes information about the destination and the scheduled time of travel.
 */
package com.project.sc2006.entities;

import java.time.LocalDateTime;

public class TravelDetails {
    private String destination;
    private LocalDateTime localDateTime;

    /**
     * Constructs a new TravelDetails object with the specified destination and scheduled time.
     *
     * @param destination   The destination or location to travel to.
     * @param localDateTime The scheduled date and time of the travel.
     */
    public TravelDetails(String destination, LocalDateTime localDateTime) {
        this.destination = destination;
        this.localDateTime = localDateTime;
    }

    public String getDestination() {
        return destination;
    }

    public LocalDateTime getLocalDateTime() {
        return localDateTime;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public void setLocalDateTime(LocalDateTime localDateTime) {
        this.localDateTime = localDateTime;
    }
}
