package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown by the first step of {@code POST /api/owners} when the request carries an
 * {@code Idempotency-Key} that has already created an owner. Rather than creating a duplicate, the
 * pipeline short-circuits and {@link IdempotentReplayExceptionHandler} replays the originally created
 * owner with 200.
 */
public class IdempotentReplayException extends Exception {

    private final int ownerId;

    public IdempotentReplayException(int ownerId) {
        super("Idempotent replay of owner: " + ownerId);
        this.ownerId = ownerId;
    }

    public int getOwnerId() {
        return this.ownerId;
    }
}
