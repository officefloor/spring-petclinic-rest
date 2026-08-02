package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown when creating an owner would exceed the maximum number of owners allowed in
 * a single city. Handled globally by {@link OwnerCityLimitExceptionHandler}, which
 * responds 400.
 */
public class OwnerCityLimitException extends Exception {

    public OwnerCityLimitException(String message) {
        super(message);
    }
}
