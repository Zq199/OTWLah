/**
 * The SC2006Location class represents location information within the context of the SC2006 project.
 * It includes details about the location and the country associated with it.
 */
package com.project.sc2006.entities;

public class SC2006Location {
    private String location;
    private String country;

    /**
     * Constructs a new SC2006Location object with the specified location and country details.
     *
     * @param location The specific location or place.
     * @param country  The country in which the location is situated.
     */
    public SC2006Location(String location, String country) {
        this.location = location;
        this.country = country;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }
}
