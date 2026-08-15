package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown when a create-owner request names a city that already contains the maximum number of
 * owners. Handled globally by {@link CityAtCapacityExceptionHandler}, which responds 409.
 */
public class CityAtCapacityException extends Exception {

    private final String city;

    public CityAtCapacityException(String city) {
        super("City is at capacity and cannot accept more owners: " + city);
        this.city = city;
    }

    public String getCity() {
        return city;
    }
}
