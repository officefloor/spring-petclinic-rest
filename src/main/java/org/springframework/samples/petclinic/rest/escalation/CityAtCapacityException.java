package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown on create when the owner's city already contains 50 or more owners, i.e. the
 * city is at capacity. Handled globally by {@link CityAtCapacityExceptionHandler}, which
 * responds 409 Conflict.
 */
public class CityAtCapacityException extends Exception {

    public CityAtCapacityException(String city, long count) {
        super("City '" + city + "' is at capacity (" + count + " owners already)");
    }
}
