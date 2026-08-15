package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown by {@link org.springframework.samples.petclinic.rest.function.owner.CheckIdempotencyKey}
 * when a create-owner request repeats an already-seen {@code Idempotency-Key}. Carries the id of the
 * owner the original request created; handled by {@link IdempotentReplayExceptionHandler}, which
 * responds 200 with that owner instead of creating a duplicate.
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
