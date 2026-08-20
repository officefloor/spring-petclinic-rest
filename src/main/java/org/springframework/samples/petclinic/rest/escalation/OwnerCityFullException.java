package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown by {@code CheckOwnerCityCapacity} when a create-owner request targets a city that
 * already contains 50 or more owners. Handled by {@link OwnerCityFullExceptionHandler}, which
 * responds 409.
 */
public class OwnerCityFullException extends Exception {

    public OwnerCityFullException(String city, int count) {
        super("City is at capacity (" + count + " owners): " + city);
    }
}
