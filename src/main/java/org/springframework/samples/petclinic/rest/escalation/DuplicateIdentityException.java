package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown by the owner-create pipeline when a new owner would be a household duplicate: an
 * existing owner already carries the household id derived from the new owner's last name and
 * postcode. Bypassed when the request declares {@code sharesHousehold}.
 *
 * <p>Handled by {@link DuplicateIdentityExceptionHandler}, which responds 409.
 */
public class DuplicateIdentityException extends Exception {

    public DuplicateIdentityException(String message) {
        super(message);
    }
}
