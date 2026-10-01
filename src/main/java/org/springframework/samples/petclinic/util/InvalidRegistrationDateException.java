package org.springframework.samples.petclinic.util;

import java.time.LocalDate;

/**
 * Thrown when a supplied registration date is later than the server's current date, i.e. it lies
 * in the future. A registration date cannot be in the future, so such a value is rejected by the
 * REST layer as a 400 Bad Request.
 */
public class InvalidRegistrationDateException extends RuntimeException {

    public InvalidRegistrationDateException(LocalDate registrationDate, LocalDate serverDate) {
        super("Registration date '" + registrationDate + "' is later than the server date '" + serverDate + "'");
    }
}
