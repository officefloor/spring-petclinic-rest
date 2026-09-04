package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown when a create-owner request names a city that already contains 50 or more
 * owners. Handled by {@link CrowdedCityExceptionHandler}, which responds 409.
 */
public class CrowdedCityException extends Exception {

    public CrowdedCityException(String city) {
        super("City is at capacity: " + city);
    }
}
