package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown by the create pipeline when a new owner's whole {@code identityKey}
 * ({@code normalizedTelephone + '|' + (email or empty) + '|' + householdId}) equals an
 * existing owner's. This single key consolidates the former separate telephone, email and
 * household duplicate checks. Handled globally by {@link DuplicateIdentityExceptionHandler},
 * which responds 409.
 */
public class DuplicateIdentityException extends Exception {

    public DuplicateIdentityException(String message) {
        super(message);
    }
}
