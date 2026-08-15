package org.springframework.samples.petclinic.rest.escalation;

/**
 * Raised by {@code POST /api/owners} when the request carries an {@code Idempotency-Key} that has
 * already produced an owner — the create is a repeat. Carries the id of that originally created
 * owner so the escalation handler can return it with 200 instead of creating a duplicate. Checked so
 * it appears in a {@code throws} clause and is routed to its escalation handler.
 */
public class IdempotentReplayException extends Exception {

    private final int ownerId;

    public IdempotentReplayException(int ownerId) {
        super("Idempotent replay of a create that already produced owner: " + ownerId);
        this.ownerId = ownerId;
    }

    /** The id of the owner the original create produced. */
    public int getOwnerId() {
        return this.ownerId;
    }
}
