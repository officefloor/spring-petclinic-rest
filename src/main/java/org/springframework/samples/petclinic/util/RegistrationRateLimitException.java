package org.springframework.samples.petclinic.util;

/**
 * Thrown when the per-day owner registration limit has been reached. Handled by the REST layer
 * as a 429 Too Many Requests.
 */
public class RegistrationRateLimitException extends RuntimeException {

    public RegistrationRateLimitException() {
        super("The daily owner registration limit has been reached; please retry later");
    }
}
