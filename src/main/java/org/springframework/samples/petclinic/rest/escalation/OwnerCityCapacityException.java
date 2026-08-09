package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown when a create-owner request targets a city that has already reached capacity — it already
 * contains 50 or more owners. Handled globally by {@link OwnerCityCapacityExceptionHandler}, which
 * responds 409.
 */
public class OwnerCityCapacityException extends Exception {

    public OwnerCityCapacityException(String city, int count) {
        super("City is at capacity (" + count + " owners): " + city);
    }
}
