package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown when a request to create an owner names a city that already contains the maximum number of
 * owners (50), so the request is answered with a 409 Conflict rather than exceeding the per-city
 * capacity. Carries the offending city and the capacity limit so {@link CityAtCapacityExceptionHandler}
 * can report them. Checked so it appears in the function's {@code throws} clause and is routed as an
 * escalation.
 */
public class CityAtCapacityException extends Exception {

    private final String city;

    private final int capacity;

    public CityAtCapacityException(String city, int capacity) {
        super("City '" + city + "' already contains the maximum of " + capacity + " owners");
        this.city = city;
        this.capacity = capacity;
    }

    public String getCity() {
        return this.city;
    }

    public int getCapacity() {
        return this.capacity;
    }
}
