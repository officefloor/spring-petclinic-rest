package org.springframework.samples.petclinic.rest.escalation;

import java.time.LocalDate;

/**
 * Thrown by {@link org.springframework.samples.petclinic.rest.function.owner.ValidateOwner} when the
 * request supplies a {@code registrationDate} that is later than the server's current date. Registration
 * cannot be dated in the future. Handled by {@link OwnerRegistrationDateFutureExceptionHandler}, which
 * responds 400.
 */
public class OwnerRegistrationDateFutureException extends Exception {

    private final LocalDate submitted;

    public OwnerRegistrationDateFutureException(LocalDate submitted) {
        super("Registration date is in the future: " + submitted);
        this.submitted = submitted;
    }

    public LocalDate getSubmitted() {
        return this.submitted;
    }
}
