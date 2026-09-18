package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown to short-circuit create-owner when the request's {@code Idempotency-Key} was already used
 * by an earlier create. Carries that original owner's id so {@link IdempotentReplayExceptionHandler}
 * can respond 200 with the existing owner instead of creating a duplicate.
 */
public class IdempotentReplayException extends Exception {

    private final int ownerId;

    public IdempotentReplayException(int ownerId) {
        super("Idempotent replay of owner " + ownerId);
        this.ownerId = ownerId;
    }

    public int getOwnerId() {
        return ownerId;
    }
}
