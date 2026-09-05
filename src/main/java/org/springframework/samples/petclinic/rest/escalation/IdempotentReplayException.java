package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown by the create-owner pipeline when the request's {@code Idempotency-Key} has already
 * produced an owner. Handled by {@link IdempotentReplayExceptionHandler}, which replays the
 * originally created owner with 200 instead of creating a duplicate.
 */
public class IdempotentReplayException extends Exception {

    private final int ownerId;

    public IdempotentReplayException(int ownerId) {
        this.ownerId = ownerId;
    }

    public int getOwnerId() {
        return ownerId;
    }
}
