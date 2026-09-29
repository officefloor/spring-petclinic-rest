package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown by {@code RequireCityCapacity} when a create-owner request targets a city that already
 * contains the maximum number of owners (50). {@link CityAtCapacityExceptionHandler} reports a 409.
 */
public class CityAtCapacityException extends Exception {

    private final String city;

    public CityAtCapacityException(String city) {
        super("The city has reached its owner capacity: '" + city + "'");
        this.city = city;
    }

    public String getCity() {
        return this.city;
    }
}
