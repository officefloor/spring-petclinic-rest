package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown by {@code CheckIdempotencyKey} when a create-owner request carries an {@code Idempotency-Key}
 * that has already produced an owner. Handled globally by {@link IdempotentReplayExceptionHandler},
 * which replays the originally created owner with 200 instead of creating a duplicate.
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
