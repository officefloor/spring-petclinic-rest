package org.springframework.samples.petclinic.rest.function.owner;

/**
 * Thrown by {@link EnsureCityCapacity} when a create request's city already holds the maximum
 * number of owners. Carries the city for the handler's message. Checked so it appears in the
 * function's {@code throws} clause and routes to an escalation.
 */
public class CityAtCapacityException extends Exception {

    private final String city;

    public CityAtCapacityException(String city) {
        super("City is at capacity: " + city);
        this.city = city;
    }

    public String getCity() {
        return city;
    }
}
