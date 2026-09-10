package org.springframework.samples.petclinic.rest.function.owner;

/**
 * Thrown by {@link ValidateOwnerFields} and {@link ValidateOwner} when an owner request supplies
 * an {@code email} whose domain is on the disposable-domain blocklist (see {@link OwnerEmail}).
 * Handled by {@code DisposableOwnerEmailExceptionHandler}, which responds 400.
 */
public class DisposableOwnerEmailException extends Exception {

    public DisposableOwnerEmailException(String email) {
        super("Email domain is on the disposable-domain blocklist, but was: '" + email + "'");
    }
}
