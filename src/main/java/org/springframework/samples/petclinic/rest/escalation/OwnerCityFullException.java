package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown by the create-owner pipeline when the request's city already contains 50 or more owners,
 * i.e. the city is at capacity. Handled globally by {@link OwnerCityFullExceptionHandler}, which
 * responds 409.
 */
public class OwnerCityFullException extends Exception {

    public OwnerCityFullException(String city) {
        super("City " + city + " already contains the maximum number of owners");
    }
}
