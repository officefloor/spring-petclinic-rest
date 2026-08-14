package org.springframework.samples.petclinic.rest.escalation;

import org.springframework.samples.petclinic.model.Owner;

/**
 * Thrown by {@code CheckIdempotentCreate} at the start of the create-owner pipeline when the request
 * carries an {@code Idempotency-Key} that has already produced an owner. It is not an error but a
 * signal to short-circuit the pipeline: {@link IdempotentReplayExceptionHandler} responds 200 with the
 * originally created owner instead of creating a duplicate.
 */
public class IdempotentReplayException extends Exception {

    private final transient Owner owner;

    public IdempotentReplayException(Owner owner) {
        super("Idempotency-Key already used by owner: " + owner.getId());
        this.owner = owner;
    }

    public Owner getOwner() {
        return this.owner;
    }
}
