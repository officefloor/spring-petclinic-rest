package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown on create when the owner's city already contains the maximum number of owners
 * ({@code 50}). Handled by {@link CityAtCapacityExceptionHandler}, which responds 409.
 */
public class CityAtCapacityException extends Exception {

    private final String city;

    public CityAtCapacityException(String city) {
        super("City is at capacity (50 owners): " + city);
        this.city = city;
    }

    public String getCity() {
        return city;
    }
}
