package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown by {@code EnsureOwnerHouseholdUnique} when a create request's last name and
 * address already belong to another owner. Handled by
 * {@link DuplicateHouseholdExceptionHandler}, which responds 409.
 */
public class DuplicateHouseholdException extends Exception {

    public DuplicateHouseholdException(String message) {
        super(message);
    }
}
