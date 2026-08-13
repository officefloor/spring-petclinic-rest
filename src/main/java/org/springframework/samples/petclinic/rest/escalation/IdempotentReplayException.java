package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown at the start of a create when the request's {@code Idempotency-Key} has already produced an
 * owner. Carries that owner's id; handled by {@link IdempotentReplayExceptionHandler}, which responds
 * 200 with the originally created owner instead of creating a duplicate.
 */
public class IdempotentReplayException extends Exception {

    private final int ownerId;

    public IdempotentReplayException(int ownerId) {
        super("Idempotent replay of previously created owner: " + ownerId);
        this.ownerId = ownerId;
    }

    public int getOwnerId() {
        return this.ownerId;
    }
}
