package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown by the create-owner pipeline when the request's city already contains the maximum
 * number of owners (the per-city capacity), so no further owner may be registered there.
 * Carries the offending city so {@link CityAtCapacityExceptionHandler} can report it. Handled
 * as 409 Conflict.
 */
public class CityAtCapacityException extends Exception {

    private final String city;

    public CityAtCapacityException(String city) {
        super("City already at owner capacity: " + city);
        this.city = city;
    }

    public String getCity() {
        return this.city;
    }
}
