package org.springframework.samples.petclinic.rest.escalation;

/**
 * Raised when a create-owner request names a city that already contains the maximum number of
 * owners (50). Checked so it appears in a {@code throws} clause and can be routed to its escalation
 * handler, which responds 409 Conflict.
 */
public class CityAtCapacityException extends Exception {

    private final String city;

    public CityAtCapacityException(String city) {
        super("City is at capacity: " + city);
        this.city = city;
    }

    /** The city that already contains the maximum number of owners. */
    public String getCity() {
        return this.city;
    }
}
