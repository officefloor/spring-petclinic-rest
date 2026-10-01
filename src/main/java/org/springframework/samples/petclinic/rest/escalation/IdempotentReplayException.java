package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown by {@link org.springframework.samples.petclinic.rest.function.owner.CheckIdempotency} when a
 * {@code POST /api/owners} carries an {@code Idempotency-Key} header that has already produced an owner.
 * Carries the id of that originally created owner. Handled by {@link IdempotentReplayExceptionHandler},
 * which responds 200 with the original owner rather than creating a duplicate.
 */
public class IdempotentReplayException extends Exception {

    private final int ownerId;

    public IdempotentReplayException(int ownerId) {
        super("Idempotency-Key already used by owner: " + ownerId);
        this.ownerId = ownerId;
    }

    public int getOwnerId() {
        return this.ownerId;
    }
}
