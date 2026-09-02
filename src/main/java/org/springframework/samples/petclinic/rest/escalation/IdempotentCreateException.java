package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown when a create-owner request repeats an already-seen {@code Idempotency-Key}; carries the
 * id of the originally created owner. Handled by {@link IdempotentCreateExceptionHandler}, which
 * responds 200 with that owner instead of creating a duplicate.
 */
public class IdempotentCreateException extends Exception {

    private final int ownerId;

    public IdempotentCreateException(int ownerId) {
        super("Idempotent create; returning existing owner " + ownerId);
        this.ownerId = ownerId;
    }

    public int getOwnerId() {
        return this.ownerId;
    }
}
