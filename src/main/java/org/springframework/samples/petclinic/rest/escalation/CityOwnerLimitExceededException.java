package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown by {@code CheckOwnerCityCapacity} when a new owner would be created in a city that
 * already holds 50 or more owners (compared case-insensitively, matching how city membership is
 * counted elsewhere). Handled globally by {@link CityOwnerLimitExceededExceptionHandler}, which
 * responds 409 Conflict.
 */
public class CityOwnerLimitExceededException extends Exception {

    public CityOwnerLimitExceededException(String message) {
        super(message);
    }
}
