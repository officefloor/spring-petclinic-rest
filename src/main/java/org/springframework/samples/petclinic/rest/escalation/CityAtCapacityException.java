package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown when a create request names a city that has already reached its owner capacity — an existing
 * owner count at or above the per-city limit. Carries the offending city and that limit so
 * {@link CityAtCapacityExceptionHandler} can report them. Handled as a 409.
 */
public class CityAtCapacityException extends Exception {

    private final String city;

    private final int limit;

    public CityAtCapacityException(String city, int limit) {
        super("City is at capacity (" + limit + " owners): " + city);
        this.city = city;
        this.limit = limit;
    }

    public String getCity() {
        return city;
    }

    public int getLimit() {
        return limit;
    }
}
