package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown by the create-owner pipeline when the request repeats an {@code Idempotency-Key} that has
 * already produced an owner. Carries that owner's id so {@link IdempotentReplayExceptionHandler}
 * can return the original owner with 200, short-circuiting a second create (and its duplicate 409).
 */
public class IdempotentReplayException extends Exception {

    private final int ownerId;

    public IdempotentReplayException(int ownerId) {
        super("Idempotent replay of owner: " + ownerId);
        this.ownerId = ownerId;
    }

    public int getOwnerId() {
        return ownerId;
    }
}
